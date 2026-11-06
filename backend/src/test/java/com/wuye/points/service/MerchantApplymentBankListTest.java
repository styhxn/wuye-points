package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchant.BankBranchListResponse;
import com.wuye.points.dto.merchant.BankListResponse;
import com.wuye.points.dto.merchant.BankOptionResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.PartnerApplymentApiService;
import com.wuye.points.payment.PartnerApplymentProperties;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.MerchantApplymentRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantWechatAccountRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

/**
 * 进件银行列表：全量分页 + 缓存 + 关键字检索 + 支行 limit 回显。
 *
 * <p>回归目标：旧实现写死 offset=0&amp;limit=100，前端只能看到前 100 家银行且要解微信原始应答的
 * 双层 data；这里锁住「拉全量、只拉一次、能搜、层级只有一层」四件事。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
@DisplayName("MerchantApplymentService 银行列表测试")
class MerchantApplymentBankListTest {

    private static final String CORPORATE = "BANK_ACCOUNT_TYPE_CORPORATE";

    @Mock
    private MerchantApplymentRepository applymentRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private MerchantWechatAccountRepository wechatAccountRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private PartnerApplymentApiService applymentApiService;
    @Mock
    private PartnerApplymentProperties applymentProps;
    @Mock
    private WechatPayProperties wechatPayProperties;
    @Mock
    private WechatCertManager certManager;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private MerchantApplymentService service;

    @BeforeEach
    void setUp() {
        Resident user = new Resident();
        user.setId("res_mch001");
        user.setPhone("13900000001");
        user.setPropertyCompanyId("pc_demo001");
        user.setStatus("active");
        Merchant merchant = new Merchant();
        merchant.setId("mch_demo001");
        when(residentRepository.findById("res_mch001")).thenReturn(Optional.of(user));
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                "13900000001", "pc_demo001")).thenReturn(Optional.of(merchant));

        when(applymentProps.isEnabled()).thenReturn(true);
        when(wechatPayProperties.isEnabled()).thenReturn(true);
        when(wechatPayProperties.getMchId()).thenReturn("1752012787");
        when(wechatPayProperties.getPrivateKeyPath()).thenReturn("/opt/wxcert/apiclient_key.pem");
        when(wechatPayProperties.getMerchantSerialNumber()).thenReturn("3BDE4E98996D960A27D13EFCBD5368159D122C4F");
        when(wechatPayProperties.isPublicKeyMode()).thenReturn(true);
        when(wechatPayProperties.getPublicKeyId()).thenReturn("PUB_KEY_ID_0117520127872026102700192123001602");

        UserContext.set(UserContext.builder().userId("res_mch001").role("merchant").build());
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("全量分页拉取：第一页满 200 条时继续拉第二页")
    void listBanks_fetchesAllPages() {
        when(applymentApiService.listBanks(eq(CORPORATE), eq(0), eq(200)))
                .thenReturn(JSON(banksJson(200, 250, 0)));
        when(applymentApiService.listBanks(eq(CORPORATE), eq(200), eq(200)))
                .thenReturn(JSON(banksJson(50, 250, 200)));

        BankListResponse response = service.listBanks(CORPORATE, null, 0, 50);

        assertEquals(250, response.getTotal());
        assertEquals(250, response.getFilteredTotal());
        assertEquals(50, response.getCount());
        assertEquals(0, response.getOffset());
        verify(applymentApiService).listBanks(CORPORATE, 200, 200);
    }

    @Test
    @DisplayName("第二次请求命中缓存，不再打微信")
    void listBanks_secondCallHitsCache() {
        when(applymentApiService.listBanks(eq(CORPORATE), eq(0), eq(200)))
                .thenReturn(JSON(banksJson(10, 10, 0)));

        service.listBanks(CORPORATE, null, 0, 50);
        BankListResponse again = service.listBanks(CORPORATE, null, 0, 50);

        assertEquals(10, again.getTotal());
        verify(applymentApiService, times(1)).listBanks(eq(CORPORATE), anyInt(), anyInt());
    }

    @Test
    @DisplayName("关键字检索：命中名称/别名/编码，且返回项全部匹配")
    void listBanks_filtersByKeyword() {
        when(applymentApiService.listBanks(eq(CORPORATE), eq(0), eq(200)))
                .thenReturn(JSON(banksJson(30, 30, 0)));

        BankListResponse byName = service.listBanks(CORPORATE, "银行7", 0, 50);
        assertTrue(byName.getCount() >= 1);
        assertTrue(byName.getList().stream()
                .allMatch(b -> b.getAccountBank().contains("银行7") || b.getBankAlias().contains("银行7")));

        BankListResponse byCode = service.listBanks(CORPORATE, "12", 0, 50);
        assertTrue(byCode.getList().stream()
                .map(BankOptionResponse::getAccountBankCode)
                .anyMatch(code -> Integer.valueOf(12).equals(code)));
    }

    @Test
    @DisplayName("对私类型走 personal-banking 路径")
    void listBanks_personalPath() {
        when(applymentApiService.listBanks(anyString(), anyInt(), anyInt()))
                .thenReturn(JSON(banksJson(1, 1, 0)));

        service.listBanks("BANK_ACCOUNT_TYPE_PERSONAL", null, 0, 50);

        verify(applymentApiService).listBanks("BANK_ACCOUNT_TYPE_PERSONAL", 0, 200);
    }

    @Test
    @DisplayName("微信拉取失败且无缓存：返回 99002，不返回空列表骗过前端")
    void listBanks_wxFailureWithoutCache() {
        when(applymentApiService.listBanks(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("微信不可达"));

        BizException ex = assertThrows(BizException.class,
                () -> service.listBanks(CORPORATE, null, 0, 50));

        assertEquals(ErrorCode.WECHAT_API_ERROR.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("凭据未就绪时仍被 99016 挡住（不进微信调用）")
    void listBanks_requiresApplymentReady() {
        when(wechatPayProperties.isPublicKeyMode()).thenReturn(false);
        when(wechatPayProperties.getApiV3Key()).thenReturn("too-short-key");

        BizException ex = assertThrows(BizException.class,
                () -> service.listBanks(CORPORATE, null, 0, 50));

        assertEquals(ErrorCode.WECHAT_APPLYMENT_NOT_CONFIGURED.getCode(), ex.getCode());
        verify(applymentApiService, never()).listBanks(anyString(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("支行列表：limit 超 500 夹取并回显生效值")
    void listBranches_clampsAndReportsEffectiveLimit() {
        when(applymentApiService.listBranches(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(JSON("{\"total_count\":37,\"count\":2,\"data\":["
                        + "{\"bank_branch_name\":\"招行北京分行\",\"bank_branch_id\":\"308100005019\"},"
                        + "{\"bank_branch_name\":\"招行上海分行\",\"bank_branch_id\":\"308290005019\"}],"
                        + "\"account_bank\":\"招商银行\",\"account_bank_code\":1001,"
                        + "\"bank_alias\":\"招商银行\",\"bank_alias_code\":\"1000009561\"}"));

        BankBranchListResponse response = service.listBranches("1000009561", "211100", 0, 9999);

        assertEquals(500, response.getLimitEffective());
        assertEquals(37, response.getTotal());
        assertEquals(2, response.getCount());
        assertEquals("308100005019", response.getList().get(0).getBankBranchId());
        assertEquals("招商银行", response.getAccountBank());
        verify(applymentApiService).listBranches("1000009561", "211100", 0, 500);
    }

    @Test
    @DisplayName("支行列表：cityCode 缺失报 10001")
    void listBranches_requiresCityCode() {
        BizException ex = assertThrows(BizException.class,
                () -> service.listBranches("1000009561", " ", 0, 100));

        assertEquals(ErrorCode.PARAM_MISSING.getCode(), ex.getCode());
    }

    private static cn.hutool.json.JSONObject JSON(String raw) {
        return cn.hutool.json.JSONUtil.parseObj(raw);
    }

    private static String banksJson(int count, int totalCount, int startIdx) {
        StringBuilder sb = new StringBuilder("{\"total_count\":").append(totalCount)
                .append(",\"count\":").append(count).append(",\"data\":[");
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sb.append(',');
            }
            int code = startIdx + i;
            sb.append("{\"account_bank\":\"银行").append(code)
                    .append("\",\"account_bank_code\":").append(code)
                    .append(",\"bank_alias\":\"别名").append(code)
                    .append("\",\"bank_alias_code\":\"1000").append(code)
                    .append("\",\"need_bank_branch\":").append(code % 2 == 0).append('}');
        }
        return sb.append("]}").toString();
    }
}

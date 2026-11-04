package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.merchant.ApplymentDetailResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantApplyment;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.MerchantApplymentRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * 进件详情诊断字段可见性回归：微信原始报错 lastError 与 retryCount 只给运营后台。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MerchantApplymentService 进件诊断字段可见性")
class MerchantApplymentDiagnosticsVisibilityTest {

    private static final String MERCHANT_ID = "mch_001";

    @Mock
    private MerchantApplymentRepository applymentRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private RoleGuard roleGuard;

    @InjectMocks
    private MerchantApplymentService service;

    private MerchantApplyment rejectedApplyment;

    @BeforeEach
    void setUp() {
        rejectedApplyment = new MerchantApplyment();
        rejectedApplyment.setMerchantId(MERCHANT_ID);
        rejectedApplyment.setApplymentState("rejected");
        rejectedApplyment.setRejectReason("营业执照照片不清晰");
        rejectedApplyment.setLastError("微信受理失败：account_no 与签约主体不一致（sub_mchid=1900000xxx）");
        rejectedApplyment.setRetryCount(3);
        when(applymentRepository.findByMerchantId(MERCHANT_ID)).thenReturn(Optional.of(rejectedApplyment));
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("商家端查询进件：不回传 lastError/retryCount，驳回原因照常可见")
    void merchantViewHidesWechatRawError() {
        stubMerchantCaller();

        ApplymentDetailResponse response = service.getApplyment(MERCHANT_ID);

        assertNull(response.getLastError(), "商家端不得拿到微信原始报错");
        assertNull(response.getRetryCount(), "商家端不得拿到重试次数");
        assertEquals("营业执照照片不清晰", response.getRejectReason(), "驳回原因仍须可见，否则商家无从改资料");
        assertEquals("rejected", response.getApplymentState());
    }

    @Test
    @DisplayName("运营后台查询进件：保留 lastError/retryCount 供排障")
    void adminViewKeepsWechatRawError() {
        stubPlatformAdminCaller();

        ApplymentDetailResponse response = service.getApplymentForAdmin(MERCHANT_ID);

        assertEquals(rejectedApplyment.getLastError(), response.getLastError());
        assertEquals(Integer.valueOf(3), response.getRetryCount());
    }

    private void stubMerchantCaller() {
        UserContext.set(UserContext.builder()
                .userId("res_mch001")
                .role("merchant")
                .propertyCompanyId("pc_demo001")
                .build());
        Resident owner = new Resident();
        owner.setId("res_mch001");
        owner.setPhone("13800000001");
        owner.setPropertyCompanyId("pc_demo001");
        when(residentRepository.findById("res_mch001")).thenReturn(Optional.of(owner));
        when(merchantRepository.findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                "13800000001", "pc_demo001")).thenReturn(Optional.of(merchant()));
    }

    private void stubPlatformAdminCaller() {
        UserContext.set(UserContext.builder()
                .userId("res_admin001")
                .role("platform_admin")
                .propertyCompanyId("pc_demo001")
                .build());
        when(merchantRepository.findById(MERCHANT_ID)).thenReturn(Optional.of(merchant()));
    }

    private Merchant merchant() {
        Merchant merchant = new Merchant();
        merchant.setId(MERCHANT_ID);
        merchant.setName("万达花园便利店");
        merchant.setPropertyCompanyId("pc_demo001");
        return merchant;
    }
}

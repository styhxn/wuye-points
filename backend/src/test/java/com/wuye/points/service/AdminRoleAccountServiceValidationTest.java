package com.wuye.points.service;

import com.wuye.points.dto.admin.roleaccount.CreateRoleAccountRequest;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.MerchantPropertyConfigRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.PlatformMerchantRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理端直建业务角色账号的入参前置校验单元测试（Mockito，不加载 Spring 上下文）。
 *
 * <p>回归目标：前端 2026-11-01 报 {@code POST /admin/role-accounts} 409「数据冲突，请刷新后重试」。
 * 小区外键不存在、抽佣比例按百分数填（10 而非 0.10）撞 {@code decimal(5,4)} 上限，
 * 原先都以 DB 异常冒出来、运营看不到是哪个字段错，现应在写库前转成带字段名的 400。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminRoleAccountServiceValidationTest {

    private static final String PC = "pc_demo001";
    private static final String COM = "com_demo001";

    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private PropertyCompanyRepository propertyCompanyRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PlatformMerchantRepository platformMerchantRepository;
    @Mock
    private MerchantPropertyConfigRepository merchantPropertyConfigRepository;
    @Mock
    private MerchantBindingService merchantBindingService;
    @Mock
    private MerchantConverter merchantConverter;
    @Mock
    private MerchantCategoryService merchantCategoryService;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private AdminSupport adminSupport;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private ActivityLeaderIdentityRepository activityLeaderIdentityRepository;

    @InjectMocks
    private AdminRoleAccountService service;

    @BeforeEach
    void setUp() {
        when(roleGuard.isPlatformAdmin()).thenReturn(true);
        when(propertyCompanyRepository.findById(PC)).thenReturn(Optional.of(new PropertyCompany()));
        when(idGenerator.generateId(org.mockito.ArgumentMatchers.anyString())).thenReturn("gen_001");
        when(merchantCategoryService.activeCategoryNames())
                .thenReturn(java.util.Set.of("周围商家", "看病买药", "休闲玩乐"));
    }

    private CreateRoleAccountRequest merchantRequest() {
        CreateRoleAccountRequest request = new CreateRoleAccountRequest();
        request.setRole("merchant");
        request.setName("张老板");
        request.setPhone("13900000008");
        request.setPassword("A7kP2mQ9xR4tZ6");
        request.setPropertyCompanyId(PC);
        request.setCategory("周围商家");
        return request;
    }

    private void communityBelongsTo(String communityId, String propertyCompanyId) {
        Community community = new Community();
        community.setId(communityId);
        community.setPropertyCompanyId(propertyCompanyId);
        when(communityRepository.findById(communityId)).thenReturn(Optional.of(community));
    }

    @Test
    @DisplayName("抽佣比例按百分数填（10）应报 400 并点名字段，而不是撞 decimal(5,4) 变 409")
    void createAccount_commissionRateAsPercent_rejectedWithFieldName() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setCommissionRate(new BigDecimal("10"));

        BizException e = assertThrows(BizException.class, () -> service.createAccount(request));
        assertEquals(10002, e.getCode());
        assertTrue(e.getMessage().contains("commissionRate"), e.getMessage());
        assertTrue(e.getMessage().contains("0.10 表示 10%"), e.getMessage());
    }

    @Test
    @DisplayName("积分兑换比超出 decimal(10,4) 上限应报 400")
    void createAccount_pointExchangeRateOverflow_rejected() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setPointExchangeRate(new BigDecimal("1000000"));

        BizException e = assertThrows(BizException.class, () -> service.createAccount(request));
        assertEquals(10002, e.getCode());
        assertTrue(e.getMessage().contains("pointExchangeRate"), e.getMessage());
    }

    @Test
    @DisplayName("小区不存在应报 400，而不是撞 residents 外键变 409")
    void createAccount_unknownCommunity_rejected() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setCommunityId("com_not_exist");
        when(communityRepository.findById("com_not_exist")).thenReturn(Optional.empty());

        BizException e = assertThrows(BizException.class, () -> service.createAccount(request));
        assertEquals(10002, e.getCode());
        assertEquals("小区不存在", e.getMessage());
    }

    @Test
    @DisplayName("小区不属于该物业公司应报 400")
    void createAccount_communityOfAnotherPropertyCompany_rejected() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setCommunityId(COM);
        communityBelongsTo(COM, "pc_other");

        BizException e = assertThrows(BizException.class, () -> service.createAccount(request));
        assertEquals(10002, e.getCode());
        assertEquals("小区不属于该物业公司", e.getMessage());
    }

    @Test
    @DisplayName("合法参数（0.10 抽佣 + 本物业小区）不被新校验拦截")
    void createAccount_validParams_passesValidation() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setCommunityId(COM);
        request.setCommissionRate(new BigDecimal("0.1000"));
        request.setPointExchangeRate(new BigDecimal("100"));
        request.setDeliveryFee(BigDecimal.ZERO);
        communityBelongsTo(COM, PC);

        assertDoesNotThrow(() -> service.createAccount(request));
    }

    @Test
    @DisplayName("品类以字典表为准：字典里有的「休闲玩乐」可以建（硬编码清单曾漏它）")
    void createAccount_acceptsCategoryPresentOnlyInDict() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setCategory("休闲玩乐");

        assertDoesNotThrow(() -> service.createAccount(request));
    }

    @Test
    @DisplayName("未知品类报错要指路到品类接口，而不是只甩一句 category值无效")
    void createAccount_unknownCategory_pointsToCategoryApi() {
        CreateRoleAccountRequest request = merchantRequest();
        request.setCategory("不存在的品类");

        BizException e = assertThrows(BizException.class, () -> service.createAccount(request));
        assertEquals(10002, e.getCode());
        assertTrue(e.getMessage().contains("/merchant-categories"), e.getMessage());
    }

    @Test
    @DisplayName("新建账号必须带 tokenVersion=0：库里该列 NOT NULL，Hibernate 写显式 NULL 会直接建号失败")
    void createAccount_setsTokenVersionSoInsertSatisfiesNotNull() {
        CreateRoleAccountRequest request = merchantRequest();
        communityBelongsTo(COM, PC);
        request.setCommunityId(COM);

        service.createAccount(request);

        ArgumentCaptor<Resident> saved = ArgumentCaptor.forClass(Resident.class);
        verify(residentRepository).saveAndFlush(saved.capture());
        assertEquals(Integer.valueOf(0), saved.getValue().getTokenVersion(),
                "tokenVersion 为 null 时 MySQL 报「字段 token_version 不能为空」，直建商家/组长/技工/配送员全部失败");
    }

    @Test
    @DisplayName("唯一键冲突要点名是哪张表哪个约束，不再甩「数据冲突，请刷新后重试」")
    void createAccount_duplicateKey_namesTableAndConstraint() {
        when(residentRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                        "Could not execute statement [Duplicate entry '13900000008-pc_demo001' "
                                + "for key 'residents.uk_phone_property']"));

        BizException e = assertThrows(BizException.class,
                () -> service.createAccount(merchantRequest()));

        assertEquals(97074, e.getCode());
        assertTrue(e.getMessage().contains("residents（账号）"), e.getMessage());
        assertTrue(e.getMessage().contains("residents.uk_phone_property"), e.getMessage());
    }

    @Test
    @DisplayName("外键冲突报「指向的记录不存在」，运营能直接看懂是缺哪条数据")
    void createAccount_foreignKeyViolation_namesConstraint() {
        when(residentRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                        "Cannot add or update a child row: a foreign key constraint fails "
                                + "(`wuye`.`residents`, CONSTRAINT `residents_ibfk_2` "
                                + "FOREIGN KEY (`community_id`) REFERENCES `communities` (`id`))"));

        BizException e = assertThrows(BizException.class,
                () -> service.createAccount(merchantRequest()));

        assertEquals(97074, e.getCode());
        assertTrue(e.getMessage().contains("外键 residents_ibfk_2"), e.getMessage());
    }
}

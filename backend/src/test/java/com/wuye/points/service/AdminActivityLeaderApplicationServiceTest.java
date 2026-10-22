package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.activityleaderapplication.AuditActivityLeaderApplicationRequest;
import com.wuye.points.entity.ActivityLeaderApplication;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityLeaderApplicationRepository;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
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
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 活动组组长申请审核（管理端）单元测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AdminActivityLeaderApplicationService 组长申请审核测试")
class AdminActivityLeaderApplicationServiceTest {

    @Mock
    private ActivityLeaderApplicationRepository applicationRepository;
    @Mock
    private ActivityLeaderIdentityRepository identityRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private MerchantBindingService merchantBindingService;
    @Mock
    private AdminSupport adminSupport;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private AdminActivityLeaderApplicationService service;

    private Resident applicant;

    @BeforeEach
    void setUp() {
        applicant = new Resident();
        applicant.setId("res_dev001");
        applicant.setName("张阿姨");
        applicant.setRole("resident");
        applicant.setStatus("active");
        applicant.setPropertyCompanyId("pc_demo001");

        when(idGenerator.generateId("ali")).thenReturn("ali_new001");
        // save 原样返回入参，保证 orElseGet 链路拿到已持久化对象
        when(identityRepository.save(any(ActivityLeaderIdentity.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        UserContext.set(UserContext.builder()
                .userId("res_admin")
                .role("property_admin")
                .propertyCompanyId("pc_demo001")
                .build());
        when(residentRepository.findById("res_dev001")).thenReturn(Optional.of(applicant));
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    private ActivityLeaderApplication pendingApplication() {
        ActivityLeaderApplication app = new ActivityLeaderApplication();
        app.setId("ala_001");
        app.setResidentId("res_dev001");
        app.setPropertyCompanyId("pc_demo001");
        app.setAuditStatus("pending");
        return app;
    }

    @Test
    @DisplayName("审核通过：创建组长身份 + 开通组长小店 + 申请置 approved")
    void audit_approved_shouldCreateIdentityAndShop() {
        ActivityLeaderApplication app = pendingApplication();
        when(applicationRepository.findById("ala_001")).thenReturn(Optional.of(app));
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active")).thenReturn(Optional.empty());

        AuditActivityLeaderApplicationRequest request = new AuditActivityLeaderApplicationRequest();
        request.setAuditStatus("approved");

        var resp = service.auditApplication("ala_001", request);

        assertEquals("approved", resp.getAuditStatus());
        assertEquals("ali_new001", resp.getActivityLeaderIdentityId());
        verify(identityRepository).save(any(ActivityLeaderIdentity.class));
        verify(merchantBindingService).ensureGroupLeaderMerchant("res_dev001");
        verify(applicationRepository).save(app);
    }

    @Test
    @DisplayName("审核通过：已有 active 身份则复用（幂等）")
    void audit_approved_shouldReuseExistingIdentity() {
        ActivityLeaderApplication app = pendingApplication();
        when(applicationRepository.findById("ala_001")).thenReturn(Optional.of(app));
        ActivityLeaderIdentity existing = new ActivityLeaderIdentity();
        existing.setId("ali_existing");
        existing.setResidentId("res_dev001");
        existing.setPropertyCompanyId("pc_demo001");
        existing.setStatus("active");
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active")).thenReturn(Optional.of(existing));

        AuditActivityLeaderApplicationRequest request = new AuditActivityLeaderApplicationRequest();
        request.setAuditStatus("approved");

        var resp = service.auditApplication("ala_001", request);

        assertEquals("ali_existing", resp.getActivityLeaderIdentityId());
        verify(identityRepository, org.mockito.Mockito.never()).save(any());
        verify(merchantBindingService).ensureGroupLeaderMerchant("res_dev001");
    }

    @Test
    @DisplayName("审核驳回：必须填拒绝原因")
    void audit_rejected_shouldRequireReason() {
        ActivityLeaderApplication app = pendingApplication();
        when(applicationRepository.findById("ala_001")).thenReturn(Optional.of(app));

        AuditActivityLeaderApplicationRequest request = new AuditActivityLeaderApplicationRequest();
        request.setAuditStatus("rejected");

        BizException ex = assertThrows(BizException.class,
                () -> service.auditApplication("ala_001", request));
        assertEquals(ErrorCode.PARAM_MISSING.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("审核驳回：记录原因且不创建身份")
    void audit_rejected_shouldRecordReason() {
        ActivityLeaderApplication app = pendingApplication();
        when(applicationRepository.findById("ala_001")).thenReturn(Optional.of(app));

        AuditActivityLeaderApplicationRequest request = new AuditActivityLeaderApplicationRequest();
        request.setAuditStatus("rejected");
        request.setRejectReason("经验不足");

        var resp = service.auditApplication("ala_001", request);

        assertEquals("rejected", resp.getAuditStatus());
        assertEquals("经验不足", resp.getRejectReason());
        verify(identityRepository, org.mockito.Mockito.never()).save(any());
        verify(merchantBindingService, org.mockito.Mockito.never())
                .ensureGroupLeaderMerchant(any());
    }

    @Test
    @DisplayName("重复审核抛 97061")
    void audit_shouldRejectAlreadyAudited() {
        ActivityLeaderApplication app = pendingApplication();
        app.setAuditStatus("approved");
        when(applicationRepository.findById("ala_001")).thenReturn(Optional.of(app));

        AuditActivityLeaderApplicationRequest request = new AuditActivityLeaderApplicationRequest();
        request.setAuditStatus("approved");

        BizException ex = assertThrows(BizException.class,
                () -> service.auditApplication("ala_001", request));
        assertEquals(ErrorCode.ACTIVITY_LEADER_APPLICATION_ALREADY_AUDITED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("撤销身份：置 revoked 并记录操作人")
    void revokeIdentity_shouldRevoke() {
        ActivityLeaderIdentity identity = new ActivityLeaderIdentity();
        identity.setId("ali_001");
        identity.setResidentId("res_dev001");
        identity.setPropertyCompanyId("pc_demo001");
        identity.setStatus("active");
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active")).thenReturn(Optional.of(identity));

        var resp = service.revokeIdentity("res_dev001");

        assertEquals("revoked", resp.getAuditStatus());
        verify(identityRepository).save(identity);
        assertEquals("revoked", identity.getStatus());
        assertEquals("res_admin", identity.getRevokedBy());
    }

    @Test
    @DisplayName("撤销身份：无生效中身份抛 97063")
    void revokeIdentity_shouldThrowWhenNone() {
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active")).thenReturn(Optional.empty());

        BizException ex = assertThrows(BizException.class,
                () -> service.revokeIdentity("res_dev001"));
        assertEquals(ErrorCode.ACTIVITY_LEADER_IDENTITY_NOT_FOUND.getCode(), ex.getCode());
    }
}

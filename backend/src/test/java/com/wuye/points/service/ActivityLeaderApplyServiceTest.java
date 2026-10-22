package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.activityleader.ApplyActivityLeaderRequest;
import com.wuye.points.entity.ActivityLeaderApplication;
import com.wuye.points.entity.ActivityLeaderIdentity;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ActivityLeaderApplicationRepository;
import com.wuye.points.repository.ActivityLeaderIdentityRepository;
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
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 活动组组长申请（C 端）单元测试。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActivityLeaderApplyService 组长申请测试")
class ActivityLeaderApplyServiceTest {

    @Mock
    private ActivityLeaderApplicationRepository applicationRepository;
    @Mock
    private ActivityLeaderIdentityRepository identityRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private ActivityLeaderApplyService service;

    private Resident resident;

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setId("res_dev001");
        resident.setName("张阿姨");
        resident.setRole("resident");
        resident.setStatus("active");
        resident.setPropertyCompanyId("pc_demo001");
        resident.setCommunityId("com_demo001");

        when(idGenerator.generateId("ala")).thenReturn("ala_test001");
        UserContext.set(UserContext.builder()
                .userId("res_dev001")
                .role("resident")
                .propertyCompanyId("pc_demo001")
                .build());
        when(residentRepository.findById("res_dev001")).thenReturn(Optional.of(resident));
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    @Test
    @DisplayName("申请：正常提交并落库 pending")
    void apply_shouldPersistPending() {
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active")).thenReturn(Optional.empty());
        when(applicationRepository.findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "pending")).thenReturn(Optional.empty());

        ApplyActivityLeaderRequest request = new ApplyActivityLeaderRequest();
        request.setActivityField("文艺");
        request.setRemark("组织过广场舞活动");

        var resp = service.apply(request);

        assertEquals("ala_test001", resp.getApplicationId());
        assertEquals("pending", resp.getStatus());
        verify(applicationRepository).save(any(ActivityLeaderApplication.class));
    }

    @Test
    @DisplayName("申请：已是组长则拒绝")
    void apply_shouldRejectWhenAlreadyLeader() {
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active"))
                .thenReturn(Optional.of(mockIdentity()));

        BizException ex = assertThrows(BizException.class, () -> service.apply(new ApplyActivityLeaderRequest()));
        assertEquals(ErrorCode.ACTIVITY_LEADER_APPLICATION_PENDING_EXISTS.getCode(), ex.getCode());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("申请：已有 pending 申请则拒绝")
    void apply_shouldRejectWhenPendingExists() {
        when(identityRepository.findFirstByResidentIdAndPropertyCompanyIdAndStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "active")).thenReturn(Optional.empty());
        when(applicationRepository.findFirstByResidentIdAndPropertyCompanyIdAndAuditStatusOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001", "pending"))
                .thenReturn(Optional.of(new ActivityLeaderApplication()));

        BizException ex = assertThrows(BizException.class, () -> service.apply(new ApplyActivityLeaderRequest()));
        assertEquals(ErrorCode.ACTIVITY_LEADER_APPLICATION_PENDING_EXISTS.getCode(), ex.getCode());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("查询我的申请：无记录返回 none")
    void getMyLatestApplication_shouldReturnNone() {
        when(applicationRepository.findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001")).thenReturn(Optional.empty());

        var resp = service.getMyLatestApplication();
        assertEquals("none", resp.getStatus());
    }

    @Test
    @DisplayName("查询我的申请：pending/approved/rejected 状态回显")
    void getMyLatestApplication_shouldReflectStatus() {
        ActivityLeaderApplication app = new ActivityLeaderApplication();
        app.setId("ala_x");
        app.setAuditStatus("rejected");
        app.setRejectReason("不符合");
        when(applicationRepository.findFirstByResidentIdAndPropertyCompanyIdOrderByCreatedAtDesc(
                "res_dev001", "pc_demo001")).thenReturn(Optional.of(app));

        var resp = service.getMyLatestApplication();
        assertEquals("rejected", resp.getStatus());
        assertEquals("不符合", resp.getRejectReason());
    }

    private ActivityLeaderIdentity mockIdentity() {
        ActivityLeaderIdentity identity = new ActivityLeaderIdentity();
        identity.setId("ali_x");
        identity.setResidentId("res_dev001");
        identity.setPropertyCompanyId("pc_demo001");
        identity.setStatus("active");
        return identity;
    }
}

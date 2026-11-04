package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityCommentRepository;
import com.wuye.points.repository.CommunityPostRepository;
import com.wuye.points.repository.ContentReportRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 管理端举报列表/处理接口的权限回归测试（§4-1）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ContentReportService 管理端权限测试")
class ContentReportAdminSecurityTest {

    @Mock
    private ContentReportRepository reportRepository;
    @Mock
    private CommunityPostRepository postRepository;
    @Mock
    private CommunityCommentRepository commentRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private CommunityConverter converter;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private PermissionGuard permissionGuard;

    @InjectMocks
    private ContentReportService contentReportService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("无 community:read 的账号不能查看举报列表")
    void unauthorized_cannotListReports() {
        doThrow(new BizException(ErrorCode.PERMISSION_DENIED))
                .when(permissionGuard).requirePermission("community.read");

        BizException ex = assertThrows(BizException.class,
                () -> contentReportService.listReports(1, 20, null));

        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
        verify(reportRepository, never()).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("无 community:manage 的账号不能处理举报")
    void unauthorized_cannotHandleReport() {
        doThrow(new BizException(ErrorCode.PERMISSION_DENIED))
                .when(permissionGuard).requirePermission("community.manage");

        BizException ex = assertThrows(BizException.class,
                () -> contentReportService.handleReport("crp_001", null));

        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
        verify(reportRepository, never()).findById(org.mockito.ArgumentMatchers.anyString());
    }
}

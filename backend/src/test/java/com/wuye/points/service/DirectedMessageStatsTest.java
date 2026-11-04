package com.wuye.points.service;

import com.wuye.points.dto.message.DirectedMessageStatsResponse;
import com.wuye.points.repository.DirectedMessageTaskRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.PermissionGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("定向消息统计测试")
class DirectedMessageStatsTest {

    @Mock
    private DirectedMessageTaskRepository taskRepository;
    @Mock
    private AdminSupport adminSupport;
    @Mock
    private PermissionGuard permissionGuard;

    @InjectMocks
    private DirectedMessageService service;

    @Test
    @DisplayName("物业维度统计任务数、触达、已读与近7天发送量")
    void getStats_propertyScoped() {
        when(adminSupport.resolvePropertyCompanyId(null)).thenReturn("pc_001");
        DirectedMessageTaskRepository.TaskSummary summary =
                mock(DirectedMessageTaskRepository.TaskSummary.class);
        when(summary.getTaskCount()).thenReturn(4L);
        when(summary.getRecipientCount()).thenReturn(5L);
        when(summary.getReadCount()).thenReturn(3L);
        when(taskRepository.summarizeByPropertyCompanyId("pc_001")).thenReturn(summary);
        when(taskRepository.countByPropertyCompanyIdAndCreatedAtGreaterThanEqual(
                eq("pc_001"), any(LocalDateTime.class))).thenReturn(2L, 7L);

        DirectedMessageStatsResponse response = service.getStats(null);

        verify(permissionGuard).requirePermission("directed_message.stats");
        assertEquals("pc_001", response.getPropertyCompanyId());
        assertEquals(4L, response.getTaskCount());
        assertEquals(5L, response.getRecipientCount());
        assertEquals(3L, response.getReadCount());
        assertEquals(2L, response.getUnreadCount());
        assertEquals(0.6d, response.getReadRate());
        assertEquals(2L, response.getTodayTaskCount());
        assertEquals(7L, response.getLast7DaysTaskCount());
    }

    @Test
    @DisplayName("平台维度未指定物业时统计全平台")
    void getStats_platformAll() {
        when(adminSupport.resolvePropertyCompanyId(null)).thenReturn(null);
        DirectedMessageTaskRepository.TaskSummary summary =
                mock(DirectedMessageTaskRepository.TaskSummary.class);
        when(summary.getTaskCount()).thenReturn(10L);
        when(summary.getRecipientCount()).thenReturn(0L);
        when(summary.getReadCount()).thenReturn(0L);
        when(taskRepository.summarizeAll()).thenReturn(summary);
        when(taskRepository.countByCreatedAtGreaterThanEqual(any(LocalDateTime.class)))
                .thenReturn(1L, 3L);

        DirectedMessageStatsResponse response = service.getStats(null);

        assertEquals(null, response.getPropertyCompanyId());
        assertEquals(10L, response.getTaskCount());
        assertEquals(0d, response.getReadRate());
        assertEquals(1L, response.getTodayTaskCount());
        assertEquals(3L, response.getLast7DaysTaskCount());
    }
}

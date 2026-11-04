package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.community.ForumBanRequest;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentForumBan;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityCommentRepository;
import com.wuye.points.repository.CommunityPostRepository;
import com.wuye.points.repository.ResidentForumBanRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.PermissionGuard;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 论坛禁言（影子封禁）单元测试。
 * 覆盖：禁言/解禁的状态流转与计数调整、到期判定、数据范围、参数校验。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ForumBanService 论坛禁言测试")
class ForumBanServiceTest {

    @Mock
    private ResidentForumBanRepository banRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private CommunityPostRepository postRepository;
    @Mock
    private CommunityCommentRepository commentRepository;
    @Mock
    private PermissionGuard permissionGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @InjectMocks
    private ForumBanService forumBanService;

    private Resident admin;
    private Resident target;

    @BeforeEach
    void setUp() {
        admin = new Resident();
        admin.setId("res_admin");
        admin.setName("物业管理员");
        admin.setRole("property_admin");
        admin.setPropertyCompanyId("pc_001");

        target = new Resident();
        target.setId("res_target");
        target.setName("住户甲");
        target.setRole("resident");
        target.setPropertyCompanyId("pc_001");
        target.setPhone("13900000001");

        when(idGenerator.generateId("rfb")).thenReturn("rfb_test001");
        UserContext.set(UserContext.builder()
                .userId("res_admin")
                .role("property_admin")
                .propertyCompanyId("pc_001")
                .build());
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(admin));
        when(residentRepository.findById("res_target")).thenReturn(Optional.of(target));
    }

    @AfterEach
    void cleanup() {
        UserContext.clear();
    }

    private ResidentForumBan activeBan(LocalDateTime expiresAt) {
        ResidentForumBan ban = new ResidentForumBan();
        ban.setId("rfb_existing");
        ban.setResidentId("res_target");
        ban.setPropertyCompanyId("pc_001");
        ban.setStatus("active");
        ban.setExpiresAt(expiresAt);
        return ban;
    }

    @Test
    @DisplayName("禁言：保存记录 + 扣回评论计数 + 内容打标")
    void ban_shouldPersistAndApplyShadowFlags() {
        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_target", "active"))
                .thenReturn(Optional.empty());

        ForumBanRequest request = new ForumBanRequest();
        request.setReason("发布违规内容");
        var resp = forumBanService.ban("res_target", request);

        assertEquals("rfb_test001", resp.getId());
        assertEquals("active", resp.getStatus());
        assertEquals("res_target", resp.getResidentId());
        verify(banRepository).save(any(ResidentForumBan.class));
        // 顺序：先扣计数再打标
        verify(postRepository).decrementCommentCountForBannedAuthor("res_target");
        verify(postRepository).updateShadowBannedByAuthor("res_target", true);
        verify(commentRepository).updateShadowBannedByAuthor("res_target", true);
    }

    @Test
    @DisplayName("禁言：带到期时间时落库 expiresAt")
    void ban_shouldStoreExpiresAt() {
        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_target", "active"))
                .thenReturn(Optional.empty());
        String future = LocalDateTime.now().plusDays(7)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        ForumBanRequest request = new ForumBanRequest();
        request.setExpiresAt(future);
        var resp = forumBanService.ban("res_target", request);

        assertNotNull(resp.getExpiresAt());
        verify(banRepository).save(any(ResidentForumBan.class));
    }

    @Test
    @DisplayName("禁言：重复禁言抛 97050")
    void ban_shouldRejectDuplicate() {
        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_target", "active"))
                .thenReturn(Optional.of(activeBan(null)));

        BizException ex = assertThrows(BizException.class,
                () -> forumBanService.ban("res_target", new ForumBanRequest()));
        assertEquals(ErrorCode.FORUM_BAN_ALREADY_ACTIVE.getCode(), ex.getCode());
        verify(banRepository, never()).save(any());
    }

    @Test
    @DisplayName("禁言：跨物业用户抛权限不足")
    void ban_shouldRejectCrossProperty() {
        Resident other = new Resident();
        other.setId("res_other");
        other.setPropertyCompanyId("pc_other");
        when(residentRepository.findById("res_other")).thenReturn(Optional.of(other));

        BizException ex = assertThrows(BizException.class,
                () -> forumBanService.ban("res_other", new ForumBanRequest()));
        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("禁言：expiresAt 早于当前时间抛参数错误")
    void ban_shouldRejectPastExpiresAt() {
        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_target", "active"))
                .thenReturn(Optional.empty());
        String past = LocalDateTime.now().minusHours(1)
                .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        ForumBanRequest request = new ForumBanRequest();
        request.setExpiresAt(past);
        BizException ex = assertThrows(BizException.class,
                () -> forumBanService.ban("res_target", request));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("解禁：回补计数 + 取消打标 + 状态 lifted")
    void unban_shouldRestoreCountsAndFlags() {
        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_target", "active"))
                .thenReturn(Optional.of(activeBan(null)));

        var resp = forumBanService.unban("res_target");

        assertEquals("lifted", resp.getStatus());
        verify(postRepository).incrementCommentCountForUnbannedAuthor("res_target");
        verify(postRepository).updateShadowBannedByAuthor("res_target", false);
        verify(commentRepository).updateShadowBannedByAuthor("res_target", false);
    }

    @Test
    @DisplayName("解禁：无生效中禁言抛 97051")
    void unban_shouldThrowWhenNoActiveBan() {
        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_target", "active"))
                .thenReturn(Optional.empty());

        BizException ex = assertThrows(BizException.class,
                () -> forumBanService.unban("res_target"));
        assertEquals(ErrorCode.FORUM_BAN_NOT_FOUND.getCode(), ex.getCode());
    }

    @Test
    @DisplayName("isCurrentlyBanned：永久禁言=true，未到期=true，已到期=false，无记录=false")
    void isCurrentlyBanned_shouldHonourExpiry() {
        Resident banned = new Resident();
        banned.setId("res_banned");
        banned.setPropertyCompanyId("pc_001");

        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_banned", "active"))
                .thenReturn(Optional.of(activeBan(null)));
        assertTrue(forumBanService.isCurrentlyBanned(banned));

        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_banned", "active"))
                .thenReturn(Optional.of(activeBan(LocalDateTime.now().plusHours(1))));
        assertTrue(forumBanService.isCurrentlyBanned(banned));

        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_banned", "active"))
                .thenReturn(Optional.of(activeBan(LocalDateTime.now().minusHours(1))));
        assertFalse(forumBanService.isCurrentlyBanned(banned));

        when(banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc("res_banned", "active"))
                .thenReturn(Optional.empty());
        assertFalse(forumBanService.isCurrentlyBanned(banned));

        assertFalse(forumBanService.isCurrentlyBanned(null));
    }

    @Test
    @DisplayName("到期自动解除：逐条 lift 并置 expired")
    void liftExpiredBans_shouldLiftAll() {
        ResidentForumBan first = activeBan(LocalDateTime.now().minusMinutes(1));
        first.setId("rfb_first");
        ResidentForumBan second = activeBan(LocalDateTime.now().minusMinutes(2));
        second.setId("rfb_second");
        second.setResidentId("res_target2");

        when(banRepository.findByStatusAndExpiresAtNotNullAndExpiresAtLessThanEqual(
                eq("active"), any(LocalDateTime.class)))
                .thenReturn(List.of(first, second));

        int lifted = forumBanService.liftExpiredBans();

        assertEquals(2, lifted);
        verify(postRepository).incrementCommentCountForUnbannedAuthor("res_target");
        verify(postRepository).incrementCommentCountForUnbannedAuthor("res_target2");
        verify(banRepository).save(first);
        verify(banRepository).save(second);
        assertEquals("expired", first.getStatus());
        assertEquals("expired", second.getStatus());
    }

    @Test
    @DisplayName("到期自动解除：单条失败不影响其他条（异常被吞并继续）")
    void liftExpiredBans_shouldContinueOnFailure() {
        ResidentForumBan ok = activeBan(LocalDateTime.now().minusMinutes(1));
        ok.setId("rfb_ok");
        ok.setResidentId("res_ok");
        ResidentForumBan bad = activeBan(LocalDateTime.now().minusMinutes(2));
        bad.setId("rfb_bad");
        bad.setResidentId("res_bad");

        when(banRepository.findByStatusAndExpiresAtNotNullAndExpiresAtLessThanEqual(
                eq("active"), any(LocalDateTime.class)))
                .thenReturn(List.of(ok, bad));
        org.mockito.Mockito.doThrow(new RuntimeException("boom"))
                .when(postRepository).incrementCommentCountForUnbannedAuthor("res_bad");

        int lifted = forumBanService.liftExpiredBans();

        // 两条都尝试处理（失败的记录状态不落库）
        assertEquals(2, lifted);
        verify(banRepository).save(ok);
        assertEquals("expired", ok.getStatus());
    }
}

package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.CommunityPost;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityCommentRepository;
import com.wuye.points.repository.CommunityPostLikeRepository;
import com.wuye.points.repository.CommunityPostRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.PermissionGuard;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理端社区论坛接口的权限与数据范围回归测试（§4-1 / §4-2）。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("CommunityForumService 管理端权限测试")
class CommunityForumAdminSecurityTest {

    @Mock
    private CommunityPostRepository postRepository;
    @Mock
    private CommunityCommentRepository commentRepository;
    @Mock
    private CommunityPostLikeRepository likeRepository;
    @Mock
    private ResidentRepository residentRepository;
    @Mock
    private SnowflakeIdGenerator idGenerator;
    @Mock
    private CommunityConverter converter;
    @Mock
    private SensitiveWordService sensitiveWordService;
    @Mock
    private RoleGuard roleGuard;
    @Mock
    private PermissionGuard permissionGuard;
    @Mock
    private ForumBanService forumBanService;

    @InjectMocks
    private CommunityForumService communityForumService;

    private Resident admin;
    private CommunityPost post;

    @BeforeEach
    void setUp() {
        admin = new Resident();
        admin.setId("res_admin");
        admin.setRole("property_admin");
        admin.setPropertyCompanyId("pc_001");

        post = new CommunityPost();
        post.setId("post_001");
        post.setPropertyCompanyId("pc_001");
        post.setStatus("published");

        UserContext.set(UserContext.builder()
                .userId("res_admin")
                .role("property_admin")
                .propertyCompanyId("pc_001")
                .build());
        when(residentRepository.findById("res_admin")).thenReturn(Optional.of(admin));
        when(postRepository.findById("post_001")).thenReturn(Optional.of(post));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("无 community:manage 的账号调用管理端删帖被拒绝，且不触达帖子数据")
    void unauthorized_cannotDeletePostThroughAdminEndpoint() {
        doThrow(new BizException(ErrorCode.PERMISSION_DENIED))
                .when(permissionGuard).requirePermission("community.manage");

        BizException ex = assertThrows(BizException.class,
                () -> communityForumService.adminDeletePost("post_001"));

        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
        verify(postRepository, never()).findById(anyString());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("管理端帖子列表走 community:read 门")
    void adminListPosts_requiresCommunityRead() {
        doThrow(new BizException(ErrorCode.PERMISSION_DENIED))
                .when(permissionGuard).requirePermission("community.read");

        BizException ex = assertThrows(BizException.class,
                () -> communityForumService.adminListPosts(1, 10, null, null));

        assertEquals(ErrorCode.PERMISSION_DENIED.getCode(), ex.getCode());
        verify(postRepository, never()).findAll(
                any(org.springframework.data.jpa.domain.Specification.class),
                any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("物业管理员可删除本物业帖子")
    void propertyAdmin_canDeleteOwnPropertyPost() {
        communityForumService.adminDeletePost("post_001");

        assertEquals("deleted", post.getStatus());
        verify(postRepository).save(post);
    }

    @Test
    @DisplayName("family 可见范围指定跨物业住户时拒绝保存")
    void familyVisibility_rejectsCrossPropertyResident() {
        Resident outsider = new Resident();
        outsider.setId("res_other");
        outsider.setPropertyCompanyId("pc_002");
        when(residentRepository.findById("res_other")).thenReturn(Optional.of(outsider));

        BizException ex = assertThrows(BizException.class,
                () -> communityForumService.setPostVisibility("post_001", "family", "res_other"));

        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
        assertTrue(ex.getMessage().contains("不属于本物业公司"));
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("family 可见范围指定同物业住户时正常保存")
    void familyVisibility_acceptsSamePropertyResident() {
        Resident sameProperty = new Resident();
        sameProperty.setId("res_neighbor");
        sameProperty.setPropertyCompanyId("pc_001");
        when(residentRepository.findById("res_neighbor")).thenReturn(Optional.of(sameProperty));

        communityForumService.setPostVisibility("post_001", "family", "res_neighbor");

        assertEquals("family", post.getVisibilityType());
        assertEquals("res_neighbor", post.getVisibleResidentId());
        verify(postRepository).save(post);
    }
}

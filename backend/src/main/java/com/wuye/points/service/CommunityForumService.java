package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.community.CommunityCommentResponse;
import com.wuye.points.dto.community.CommunityPostResponse;
import com.wuye.points.dto.community.CreateCommunityCommentRequest;
import com.wuye.points.dto.community.CreateCommunityPostRequest;
import com.wuye.points.dto.community.ToggleLikeResponse;
import com.wuye.points.entity.CommunityComment;
import com.wuye.points.entity.CommunityPost;
import com.wuye.points.entity.CommunityPostLike;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityCommentRepository;
import com.wuye.points.repository.CommunityPostLikeRepository;
import com.wuye.points.repository.CommunityPostRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.CommunityPostSpecifications;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommunityForumService {

    private static final int POST_CONTENT_MAX = 2000;
    private static final int COMMENT_CONTENT_MAX = 1000;
    private static final int POST_IMAGE_MAX = 9;

    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityPostLikeRepository likeRepository;
    private final ResidentRepository residentRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final CommunityConverter converter;
    private final SensitiveWordService sensitiveWordService;
    private final RoleGuard roleGuard;

    public CommunityPostResponse createPost(CreateCommunityPostRequest req) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        if (resident.getCommunityId() == null || resident.getPropertyCompanyId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "请先完善小区/物业信息后再发帖");
        }
        if (req.getContent() != null && req.getContent().length() > POST_CONTENT_MAX) {
            throw new BizException(ErrorCode.COMMUNITY_POST_TOO_LONG);
        }
        if (req.getImageUrls() != null && req.getImageUrls().size() > POST_IMAGE_MAX) {
            throw new BizException(ErrorCode.PARAM_INVALID, "图片最多9张");
        }
        sensitiveWordService.assertNotSensitive(req.getContent());
        CommunityPost post = new CommunityPost();
        post.setId(idGenerator.generateId("cp_"));
        post.setCommunityId(resident.getCommunityId());
        post.setPropertyCompanyId(resident.getPropertyCompanyId());
        post.setAuthorId(resident.getId());
        post.setAuthorName(resident.getName());
        post.setAuthorRole(resident.getRole());
        post.setContent(req.getContent());
        post.setImageUrls(converter.toJson(req.getImageUrls()));
        post.setStatus("published");
        post.setLikeCount(0);
        post.setCommentCount(0);
        return converter.toPostResponse(postRepository.save(post), false);
    }

    public Page<CommunityPostResponse> listPosts(int page, int pageSize, String sort, String keyword) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        Specification<CommunityPost> spec = CommunityPostSpecifications.withFilters(
                resident.getCommunityId(), "published", keyword, resident.getId());
        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<CommunityPost> result = postRepository.findAll(spec, pageable);
        Set<String> likedPostIds = loadLikedPostIds(result.getContent(), resident.getId());
        return result.map(p -> converter.toPostResponse(p, likedPostIds.contains(p.getId())));
    }

    public CommunityPostResponse getPost(String id) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (!"published".equals(post.getStatus())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        assertSameCommunity(post, resident);
        return converter.toPostResponse(post, isLikedByMe(post));
    }

    public void deleteOwnPost(String id) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (!post.getAuthorId().equals(resident.getId())) {
            throw new BizException(ErrorCode.COMMUNITY_NOT_ALLOWED_DELETE);
        }
        post.setStatus("deleted");
        postRepository.save(post);
    }

    public ToggleLikeResponse toggleLike(String id) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (!"published".equals(post.getStatus())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        assertSameCommunity(post, resident);
        int current = post.getLikeCount() == null ? 0 : post.getLikeCount();
        boolean liked;
        if (likeRepository.existsByPostIdAndResidentId(id, resident.getId())) {
            // 并发取消点赞时 delete 可能返回 0（已被其他请求删除），仅在真实删除时减计数
            if (likeRepository.deleteByPostIdAndResidentId(id, resident.getId()) > 0) {
                postRepository.updateLikeCount(id, -1);
            }
            liked = false;
        } else {
            // INSERT IGNORE 原子防重复：返回 1=本次插入成功，0=并发下已被插入（不重复加计数）
            if (likeRepository.insertIgnore(idGenerator.generateId("like_"), id, resident.getId()) > 0) {
                postRepository.updateLikeCount(id, 1);
            }
            liked = true;
        }
        return ToggleLikeResponse.builder()
                .likeCount(liked ? current + 1 : Math.max(0, current - 1))
                .liked(liked)
                .build();
    }

    public CommunityCommentResponse commentOnPost(String postId, CreateCommunityCommentRequest req) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (!"published".equals(post.getStatus())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        assertSameCommunity(post, resident);
        if (req.getContent() != null && req.getContent().length() > COMMENT_CONTENT_MAX) {
            throw new BizException(ErrorCode.COMMUNITY_COMMENT_TOO_LONG);
        }
        sensitiveWordService.assertNotSensitive(req.getContent());
        if (req.getParentId() != null && !req.getParentId().isBlank()) {
            CommunityComment parent = commentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
            if (!parent.getPostId().equals(postId) || !"published".equals(parent.getStatus())) {
                throw new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
            }
        }
        CommunityComment comment = new CommunityComment();
        comment.setId(idGenerator.generateId("cc_"));
        comment.setPostId(postId);
        comment.setAuthorId(resident.getId());
        comment.setAuthorName(resident.getName());
        comment.setAuthorRole(resident.getRole());
        comment.setContent(req.getContent());
        comment.setParentId(req.getParentId());
        comment.setReplyToUserId(req.getReplyToUserId());
        comment.setReplyToUserName(req.getReplyToUserName());
        comment.setStatus("published");
        CommunityComment saved = commentRepository.save(comment);
        post.setCommentCount((post.getCommentCount() == null ? 0 : post.getCommentCount()) + 1);
        postRepository.save(post);
        return converter.toCommentResponse(saved);
    }

    public Page<CommunityCommentResponse> listComments(String postId, int page, int pageSize) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (!"published".equals(post.getStatus())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        assertSameCommunity(post, resident);
        Specification<CommunityComment> spec = (root, query, cb) -> cb.and(
                cb.equal(root.get("postId"), postId),
                cb.equal(root.get("status"), "published"));
        Pageable pageable = PageRequest.of(Math.max(0, page - 1),
                Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<CommunityComment> result = commentRepository.findAll(spec, pageable);
        return result.map(converter::toCommentResponse);
    }

    public void deleteOwnComment(String id) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getViewerResident();
        CommunityComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
        if (!comment.getAuthorId().equals(resident.getId())) {
            throw new BizException(ErrorCode.COMMUNITY_NOT_ALLOWED_DELETE);
        }
        comment.setStatus("deleted");
        commentRepository.save(comment);
        decrementPostComment(comment.getPostId());
    }

    public Page<CommunityPostResponse> adminListPosts(int page, int pageSize, String status, String keyword) {
        Resident admin = getViewerResident();
        Specification<CommunityPost> spec = CommunityPostSpecifications.withAdminFilters(
                admin.getPropertyCompanyId(), status, keyword);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1),
                Math.min(Math.max(pageSize, 1), 50), Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CommunityPost> result = postRepository.findAll(spec, pageable);
        return result.map(p -> converter.toPostResponse(p, false));
    }

    /**
     * 管理端查看某帖子下的评论列表（含已删除评论，用于管理员选择性删除）。
     * 管理员可查看本物业公司范围内的帖子评论。
     */
    public Page<CommunityCommentResponse> adminListComments(String postId, int page, int pageSize, String status) {
        Resident admin = getViewerResident();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (admin.getPropertyCompanyId() != null
                && !admin.getPropertyCompanyId().equals(post.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        Specification<CommunityComment> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("postId"), postId));
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Pageable pageable = PageRequest.of(Math.max(0, page - 1),
                Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<CommunityComment> result = commentRepository.findAll(spec, pageable);
        return result.map(converter::toCommentResponse);
    }

    public void adminDeletePost(String id) {
        Resident admin = getViewerResident();
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (admin.getPropertyCompanyId() != null
                && !admin.getPropertyCompanyId().equals(post.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        post.setStatus("deleted");
        postRepository.save(post);
    }

    public void adminDeleteComment(String id) {
        Resident admin = getViewerResident();
        CommunityComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND));
        CommunityPost post = postRepository.findById(comment.getPostId()).orElse(null);
        if (admin.getPropertyCompanyId() != null
                && (post == null || !admin.getPropertyCompanyId().equals(post.getPropertyCompanyId()))) {
            throw new BizException(ErrorCode.COMMUNITY_COMMENT_NOT_FOUND);
        }
        comment.setStatus("deleted");
        commentRepository.save(comment);
        decrementPostComment(comment.getPostId());
    }

    /**
     * 管理端设置帖子可见范围。
     * visibilityType=community 表示本小区可见；=family 表示仅 visibleResidentId 指定住户可见。
     */
    public void setPostVisibility(String postId, String visibilityType, String visibleResidentId) {
        Resident admin = getViewerResident();
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND));
        if (admin.getPropertyCompanyId() != null
                && !admin.getPropertyCompanyId().equals(post.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
        if (!"community".equals(visibilityType) && !"family".equals(visibilityType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "visibilityType 仅支持 community/family");
        }
        if ("family".equals(visibilityType) && (visibleResidentId == null || visibleResidentId.isBlank())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "family 可见范围必须指定 visibleResidentId");
        }
        post.setVisibilityType(visibilityType);
        post.setVisibleResidentId("family".equals(visibilityType) ? visibleResidentId : null);
        postRepository.save(post);
    }

    private void decrementPostComment(String postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setCommentCount(Math.max(0, (post.getCommentCount() == null ? 0 : post.getCommentCount()) - 1));
            postRepository.save(post);
        });
    }

    private Resident getViewerResident() {
        return residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
    }

    /** 仅本小区可见：跨小区访问按不存在处理 */
    private void assertSameCommunity(CommunityPost post, Resident resident) {
        if (post.getCommunityId() == null || !post.getCommunityId().equals(resident.getCommunityId())) {
            throw new BizException(ErrorCode.COMMUNITY_POST_NOT_FOUND);
        }
    }

    private boolean isLikedByMe(CommunityPost post) {
        String userId = UserContext.currentUserId();
        if (userId == null) {
            return false;
        }
        return likeRepository.existsByPostIdAndResidentId(post.getId(), userId);
    }

    /** 批量查询当前用户已点赞的帖子ID，避免列表 N+1 */
    private Set<String> loadLikedPostIds(List<CommunityPost> posts, String residentId) {
        if (posts.isEmpty()) {
            return Set.of();
        }
        List<String> postIds = posts.stream().map(CommunityPost::getId).toList();
        return likeRepository.findByPostIdInAndResidentId(postIds, residentId).stream()
                .map(CommunityPostLike::getPostId)
                .collect(Collectors.toSet());
    }

    private Pageable buildPageable(int page, int pageSize, String sort) {
        int p = Math.max(0, page - 1);
        int size = Math.min(Math.max(pageSize, 1), 50);
        Sort.Direction dir = Sort.Direction.DESC;
        String field = "createdAt";
        if ("hot".equals(sort)) {
            field = "likeCount";
        } else if ("oldest".equals(sort)) {
            dir = Sort.Direction.ASC;
            field = "createdAt";
        }
        return PageRequest.of(p, size, Sort.by(dir, field));
    }
}

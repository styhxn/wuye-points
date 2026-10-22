package com.wuye.points.repository.spec;

import com.wuye.points.entity.CommunityPost;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CommunityPostSpecifications {

    /**
     * 住户端列表过滤。
     * @param viewerResidentId 当前住户ID；非空的"仅一家可见"(family)帖子，仅作者本人或指定住户可见；
     *                        影子封禁（shadow_banned）内容仅作者本人可见，其他住户不可见。
     */
    public static Specification<CommunityPost> withFilters(
            String communityId, String status, String keyword, String viewerResidentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (communityId != null) {
                predicates.add(cb.equal(root.get("communityId"), communityId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("content"), "%" + keyword.trim() + "%"));
            }
            if (viewerResidentId != null) {
                predicates.add(cb.or(
                        cb.or(cb.isNull(root.get("visibilityType")), cb.equal(root.get("visibilityType"), "community")),
                        cb.equal(root.get("visibleResidentId"), viewerResidentId),
                        cb.equal(root.get("authorId"), viewerResidentId)));
                // 影子封禁：被禁作者的内容仅本人可见（禁言用户发帖无感知，别人看不到）
                predicates.add(cb.or(
                        cb.equal(root.get("shadowBanned"), false),
                        cb.equal(root.get("authorId"), viewerResidentId)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<CommunityPost> withAdminFilters(String propertyCompanyId, String status, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (propertyCompanyId != null) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("content"), "%" + keyword.trim() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

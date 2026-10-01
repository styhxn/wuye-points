package com.wuye.points.repository.spec;

import com.wuye.points.entity.ActivityGroup;
import com.wuye.points.entity.ActivityGroupMember;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ActivityGroupSpecifications {

    private ActivityGroupSpecifications() {
    }

    public static Specification<ActivityGroup> withFilters(
            String propertyCompanyId,
            String communityId,
            String status,
            String memberResidentId) {
        return withFilters(propertyCompanyId, communityId, status, memberResidentId, null);
    }

    public static Specification<ActivityGroup> withFilters(
            String propertyCompanyId,
            String communityId,
            String status,
            String memberResidentId,
            String reviewStatus) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(communityId)) {
                predicates.add(cb.equal(root.get("communityId"), communityId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(reviewStatus)) {
                // 兼容历史数据：reviewStatus 为空（V8 迁移前已 active）的旧组也视为"已通过"，对业主可见
                predicates.add(cb.or(
                        cb.equal(root.get("reviewStatus"), reviewStatus),
                        cb.isNull(root.get("reviewStatus"))));
            }
            if (StringUtils.hasText(memberResidentId)) {
                var subquery = query.subquery(String.class);
                var memberRoot = subquery.from(ActivityGroupMember.class);
                subquery.select(memberRoot.get("groupId"));
                subquery.where(cb.and(
                        cb.equal(memberRoot.get("residentId"), memberResidentId),
                        cb.equal(memberRoot.get("status"), "active"),
                        cb.equal(memberRoot.get("groupId"), root.get("id"))));
                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

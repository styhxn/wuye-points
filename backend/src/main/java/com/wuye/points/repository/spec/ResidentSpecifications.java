package com.wuye.points.repository.spec;

import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ResidentSpecifications {

    private ResidentSpecifications() {
    }

    public static Specification<Resident> withFilters(
            String propertyCompanyId,
            String keyword,
            String communityId,
            String building,
            String status,
            String role) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            predicates.add(cb.isNull(root.get("deletedAt")));

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(communityId)) {
                predicates.add(cb.equal(root.get("communityId"), communityId));
            }
            if (StringUtils.hasText(building)) {
                predicates.add(cb.equal(root.get("building"), building));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(role)) {
                predicates.add(cb.equal(root.get("role"), role));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("phone"), like)));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Resident> withFilters(
            String propertyCompanyId, String keyword, String communityId, String building,
            String status, String role, String merchantId, String courierId) {
        return withFilters(propertyCompanyId, keyword, communityId, building, status, role);
    }
}

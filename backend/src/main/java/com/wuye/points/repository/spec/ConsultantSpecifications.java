package com.wuye.points.repository.spec;

import com.wuye.points.entity.Consultant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class ConsultantSpecifications {

    private ConsultantSpecifications() {
    }

    public static Specification<Consultant> withFilters(
            String propertyCompanyId,
            String category,
            String keyword,
            String status) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(status)) {
                if ("pending".equals(status) || "pending_audit".equals(status)) {
                    predicates.add(cb.equal(root.get("auditStatus"), "pending_audit"));
                } else if ("approved".equals(status) || "rejected".equals(status)) {
                    predicates.add(cb.equal(root.get("auditStatus"), status));
                } else {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("organization"), like),
                        cb.like(root.get("specialty"), like)));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

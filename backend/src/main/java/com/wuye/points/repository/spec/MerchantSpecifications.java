package com.wuye.points.repository.spec;

import com.wuye.points.entity.Merchant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class MerchantSpecifications {

    private MerchantSpecifications() {
    }

    public static Specification<Merchant> withFilters(
            String propertyCompanyId,
            String keyword,
            String category,
            String merchantLevel,
            boolean publicOnly) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(merchantLevel)) {
                predicates.add(cb.equal(root.get("merchantLevel"), merchantLevel));
            }
            if (publicOnly) {
                predicates.add(cb.equal(root.get("auditStatus"), "approved"));
                predicates.add(cb.equal(root.get("status"), "active"));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.like(root.get("name"), like));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

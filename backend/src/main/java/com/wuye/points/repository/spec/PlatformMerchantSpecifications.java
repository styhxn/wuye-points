package com.wuye.points.repository.spec;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.PlatformMerchant;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PlatformMerchantSpecifications {

    private PlatformMerchantSpecifications() {
    }

    public static Specification<PlatformMerchant> withFilters(
            String keyword,
            String category,
            String status,
            String linkedPropertyCompanyId) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("name"), pattern),
                        cb.like(root.get("contactPhone"), pattern)));
            }
            if (StringUtils.hasText(linkedPropertyCompanyId)) {
                Subquery<Long> subquery = query.subquery(Long.class);
                var merchantRoot = subquery.from(Merchant.class);
                subquery.select(cb.literal(1L))
                        .where(
                                cb.equal(merchantRoot.get("platformMerchantId"), root.get("id")),
                                cb.equal(merchantRoot.get("propertyCompanyId"), linkedPropertyCompanyId));
                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

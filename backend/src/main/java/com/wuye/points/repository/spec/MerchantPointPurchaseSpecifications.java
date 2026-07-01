package com.wuye.points.repository.spec;

import com.wuye.points.entity.MerchantPointPurchase;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class MerchantPointPurchaseSpecifications {

    private MerchantPointPurchaseSpecifications() {
    }

    public static Specification<MerchantPointPurchase> withFilters(
            String propertyCompanyId,
            String merchantId,
            String auditStatus,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(auditStatus)) {
                predicates.add(cb.equal(root.get("auditStatus"), auditStatus));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

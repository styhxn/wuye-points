package com.wuye.points.repository.spec;

import com.wuye.points.entity.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> withFilters(
            String propertyCompanyId,
            String residentId,
            String merchantId,
            String orderStatus,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(orderStatus)) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
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

package com.wuye.points.repository.spec;

import com.wuye.points.entity.ServiceOrder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class ServiceOrderSpecifications {

    private ServiceOrderSpecifications() {
    }

    public static Specification<ServiceOrder> withFilters(
            String propertyCompanyId,
            String residentId,
            String providerId,
            String orderStatus) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(providerId)) {
                predicates.add(cb.equal(root.get("providerId"), providerId));
            }
            if (StringUtils.hasText(orderStatus)) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

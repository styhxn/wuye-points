package com.wuye.points.repository.spec;

import com.wuye.points.entity.PermissionChangeLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PermissionChangeLogSpecifications {

    private PermissionChangeLogSpecifications() {
    }

    public static Specification<PermissionChangeLog> withFilters(
            String propertyCompanyId,
            String residentId,
            String operatorId,
            String action,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(operatorId)) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action));
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

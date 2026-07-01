package com.wuye.points.repository.spec;

import com.wuye.points.entity.OperationLog;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OperationLogSpecifications {

    private OperationLogSpecifications() {
    }

    public static Specification<OperationLog> withFilters(
            String propertyCompanyId,
            String operatorId,
            String module,
            String action,
            String targetType,
            String targetId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(operatorId)) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
            }
            if (StringUtils.hasText(module)) {
                predicates.add(cb.equal(root.get("module"), module));
            }
            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (StringUtils.hasText(targetType)) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            if (StringUtils.hasText(targetId)) {
                predicates.add(cb.equal(root.get("targetId"), targetId));
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

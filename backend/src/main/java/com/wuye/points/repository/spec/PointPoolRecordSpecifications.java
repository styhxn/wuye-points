package com.wuye.points.repository.spec;

import com.wuye.points.entity.PointPoolRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class PointPoolRecordSpecifications {

    private PointPoolRecordSpecifications() {
    }

    public static Specification<PointPoolRecord> withFilters(
            String propertyCompanyId,
            String poolType,
            String source,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(poolType)) {
                predicates.add(cb.equal(root.get("poolType"), poolType));
            }
            if (StringUtils.hasText(source)) {
                predicates.add(cb.equal(root.get("source"), source));
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

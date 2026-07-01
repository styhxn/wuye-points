package com.wuye.points.repository.spec;

import com.wuye.points.entity.PropertyCoinRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class PropertyCoinRecordSpecifications {

    private PropertyCoinRecordSpecifications() {
    }

    public static Specification<PropertyCoinRecord> withFilters(
            String residentId,
            String familyId,
            String coinType,
            String source,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (StringUtils.hasText(residentId)) {
                predicates = cb.and(predicates, cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(familyId)) {
                predicates = cb.and(predicates, cb.equal(root.get("familyId"), familyId));
            }
            if (StringUtils.hasText(coinType)) {
                predicates = cb.and(predicates, cb.equal(root.get("coinType"), coinType));
            }
            if (StringUtils.hasText(source)) {
                predicates = cb.and(predicates, cb.equal(root.get("source"), source));
            }
            if (startDate != null) {
                predicates = cb.and(predicates, cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates = cb.and(predicates, cb.lessThan(root.get("createdAt"), endDate));
            }

            return predicates;
        };
    }
}

package com.wuye.points.repository.spec;

import com.wuye.points.entity.PointRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

public final class PointRecordSpecifications {

    private PointRecordSpecifications() {
    }

    public static Specification<PointRecord> withFilters(
            String residentId,
            String familyId,
            String pointType,
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
            if (StringUtils.hasText(pointType)) {
                predicates = cb.and(predicates, cb.equal(root.get("pointType"), pointType));
            }
            if (StringUtils.hasText(source)) {
                // v8.23：家庭内转赠历史流水 source=transfer；同时兼容早期 family_transfer 取值
                if ("transfer".equals(source)) {
                    predicates = cb.and(predicates,
                            root.get("source").in(List.of("transfer", "family_transfer")));
                } else {
                    predicates = cb.and(predicates, cb.equal(root.get("source"), source));
                }
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

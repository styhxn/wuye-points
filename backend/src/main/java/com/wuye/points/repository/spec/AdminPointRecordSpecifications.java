package com.wuye.points.repository.spec;

import com.wuye.points.entity.PointRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class AdminPointRecordSpecifications {

    private AdminPointRecordSpecifications() {
    }

    public static Specification<PointRecord> offlineGrantRecords(
            String propertyCompanyId,
            String residentId,
            String operatorId,
            String source,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("source"), "manual"));
            predicates.add(cb.isNotNull(root.get("operatorId")));

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(operatorId)) {
                predicates.add(cb.equal(root.get("operatorId"), operatorId));
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

    public static Specification<PointRecord> feeClearRecords(
            String propertyCompanyId,
            String residentId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("pointType"), "expire"));
            predicates.add(cb.equal(root.get("source"), "fee_clear"));

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
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

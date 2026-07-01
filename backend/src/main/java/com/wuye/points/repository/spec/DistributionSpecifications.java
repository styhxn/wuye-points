package com.wuye.points.repository.spec;

import com.wuye.points.entity.DistributionRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class DistributionSpecifications {

    private DistributionSpecifications() {
    }

    public static Specification<DistributionRecord> withFilters(
            String propertyCompanyId,
            String orderId,
            String merchantId,
            String coordinatorId,
            String sectorLeaderId,
            String individualLeaderId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(orderId)) {
                predicates.add(cb.equal(root.get("orderId"), orderId));
            }
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(coordinatorId)) {
                predicates.add(cb.equal(root.get("coordinatorId"), coordinatorId));
            }
            if (StringUtils.hasText(sectorLeaderId)) {
                predicates.add(cb.equal(root.get("sectorLeaderId"), sectorLeaderId));
            }
            if (StringUtils.hasText(individualLeaderId)) {
                predicates.add(cb.equal(root.get("individualLeaderId"), individualLeaderId));
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

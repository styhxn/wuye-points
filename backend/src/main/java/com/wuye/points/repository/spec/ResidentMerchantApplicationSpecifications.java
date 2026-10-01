package com.wuye.points.repository.spec;

import com.wuye.points.entity.ResidentMerchantApplication;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.List;

public final class ResidentMerchantApplicationSpecifications {

    private ResidentMerchantApplicationSpecifications() {
    }

    public static Specification<ResidentMerchantApplication> withFilters(
            String propertyCompanyId,
            String status,
            List<String> residentIdsByKeyword) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (residentIdsByKeyword != null) {
                if (residentIdsByKeyword.isEmpty()) {
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("residentId").in(residentIdsByKeyword));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

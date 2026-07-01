package com.wuye.points.repository.spec;

import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PropertyFeeSpecifications {

    private PropertyFeeSpecifications() {
    }

    public static Specification<PropertyFee> withFilters(
            String propertyCompanyId,
            String residentId,
            String familyId,
            String communityId,
            String feeType,
            String paymentStatus,
            String period,
            String keyword) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(familyId)) {
                predicates.add(cb.equal(root.get("familyId"), familyId));
            }
            if (StringUtils.hasText(communityId)) {
                predicates.add(cb.equal(root.get("communityId"), communityId));
            }
            if (StringUtils.hasText(feeType)) {
                predicates.add(cb.equal(root.get("feeType"), feeType));
            }
            if (StringUtils.hasText(paymentStatus)) {
                predicates.add(cb.equal(root.get("paymentStatus"), paymentStatus));
            }
            if (StringUtils.hasText(period)) {
                predicates.add(cb.equal(root.get("period"), period));
            }
            if (StringUtils.hasText(keyword)) {
                var subquery = query.subquery(String.class);
                var residentRoot = subquery.from(Resident.class);
                subquery.select(residentRoot.get("id"));
                String pattern = "%" + keyword.trim() + "%";
                List<jakarta.persistence.criteria.Predicate> subPredicates = new ArrayList<>();
                subPredicates.add(cb.or(
                        cb.like(residentRoot.get("name"), pattern),
                        cb.like(residentRoot.get("building"), pattern),
                        cb.like(residentRoot.get("room"), pattern)));
                if (StringUtils.hasText(propertyCompanyId)) {
                    subPredicates.add(cb.equal(residentRoot.get("propertyCompanyId"), propertyCompanyId));
                }
                subquery.where(cb.and(subPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
                predicates.add(root.get("residentId").in(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<PropertyFee> forResidentScope(String residentId, String familyId) {
        return (root, query, cb) -> {
            if (StringUtils.hasText(familyId)) {
                return cb.or(
                        cb.equal(root.get("residentId"), residentId),
                        cb.equal(root.get("familyId"), familyId));
            }
            return cb.equal(root.get("residentId"), residentId);
        };
    }
}

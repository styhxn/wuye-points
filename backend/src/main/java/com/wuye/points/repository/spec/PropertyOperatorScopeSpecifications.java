package com.wuye.points.repository.spec;

import com.wuye.points.entity.PropertyOperatorScope;
import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class PropertyOperatorScopeSpecifications {

    private PropertyOperatorScopeSpecifications() {
    }

    public static Specification<PropertyOperatorScope> withFilters(
            String propertyCompanyId,
            String keyword,
            String status) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(keyword) || StringUtils.hasText(status)) {
                var subquery = query.subquery(String.class);
                var residentRoot = subquery.from(Resident.class);
                subquery.select(residentRoot.get("id"));
                List<jakarta.persistence.criteria.Predicate> residentPredicates = new ArrayList<>();
                if (StringUtils.hasText(keyword)) {
                    String pattern = "%" + keyword.trim() + "%";
                    residentPredicates.add(cb.or(
                            cb.like(residentRoot.get("name"), pattern),
                            cb.like(residentRoot.get("phone"), pattern)));
                }
                if (StringUtils.hasText(status)) {
                    residentPredicates.add(cb.equal(residentRoot.get("status"), status));
                }
                subquery.where(cb.and(residentPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
                predicates.add(root.get("residentId").in(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

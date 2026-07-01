package com.wuye.points.repository.spec;

import com.wuye.points.entity.Coordinator;
import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class CoordinatorSpecifications {

    private CoordinatorSpecifications() {
    }

    public static Specification<Coordinator> withFilters(
            String propertyCompanyId,
            String status,
            String keyword) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(keyword)) {
                var subquery = query.subquery(String.class);
                var residentRoot = subquery.from(Resident.class);
                subquery.select(residentRoot.get("id"));
                String pattern = "%" + keyword.trim() + "%";
                subquery.where(cb.or(
                        cb.like(residentRoot.get("name"), pattern),
                        cb.like(residentRoot.get("phone"), pattern)));
                predicates.add(root.get("residentId").in(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

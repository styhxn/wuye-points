package com.wuye.points.repository.spec;

import com.wuye.points.entity.IndividualLeaderApplication;
import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class IndividualLeaderApplicationSpecifications {

    private IndividualLeaderApplicationSpecifications() {
    }

    public static Specification<IndividualLeaderApplication> withFilters(
            String propertyCompanyId,
            String sector,
            String auditStatus,
            String keyword) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(sector)) {
                predicates.add(cb.equal(root.get("sector"), sector));
            }
            if (StringUtils.hasText(auditStatus)) {
                predicates.add(cb.equal(root.get("auditStatus"), auditStatus));
            }
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim() + "%";
                var residentSubquery = query.subquery(String.class);
                var residentRoot = residentSubquery.from(Resident.class);
                residentSubquery.select(residentRoot.get("id"));
                residentSubquery.where(cb.or(
                        cb.like(residentRoot.get("name"), pattern),
                        cb.like(residentRoot.get("phone"), pattern)));
                predicates.add(root.get("residentId").in(residentSubquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

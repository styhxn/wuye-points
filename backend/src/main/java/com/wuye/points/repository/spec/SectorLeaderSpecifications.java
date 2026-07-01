package com.wuye.points.repository.spec;

import com.wuye.points.entity.SectorLeader;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class SectorLeaderSpecifications {

    private SectorLeaderSpecifications() {
    }

    public static Specification<SectorLeader> withFilters(
            String propertyCompanyId,
            String sector,
            String coordinatorId,
            String status) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(sector)) {
                predicates.add(cb.equal(root.get("sector"), sector));
            }
            if (StringUtils.hasText(coordinatorId)) {
                predicates.add(cb.equal(root.get("coordinatorId"), coordinatorId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

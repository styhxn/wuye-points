package com.wuye.points.repository.spec;

import com.wuye.points.entity.SectorLeader;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SectorLeaderSpecifications {

    private SectorLeaderSpecifications() {
    }

    public static Specification<SectorLeader> withFilters(
            String propertyCompanyId,
            String sector,
            String coordinatorId,
            String status) {
        return withFilters(propertyCompanyId, sector, coordinatorId, status, null);
    }

    /**
     * @param residentIds 非空时按这些住户ID收敛（2026-09-08：列表 keyword 过滤）
     */
    public static Specification<SectorLeader> withFilters(
            String propertyCompanyId,
            String sector,
            String coordinatorId,
            String status,
            Collection<String> residentIds) {
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
            if (residentIds != null) {
                if (residentIds.isEmpty()) {
                    // 关键字命中不到任何住户时返回空结果
                    predicates.add(cb.disjunction());
                } else {
                    predicates.add(root.get("residentId").in(residentIds));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

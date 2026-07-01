package com.wuye.points.repository.spec;

import com.wuye.points.entity.Announcement;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class AnnouncementSpecifications {

    private AnnouncementSpecifications() {
    }

    public static Specification<Announcement> withFilters(
            String propertyCompanyId,
            String announcementType,
            String communityId,
            String merchantId,
            String status,
            String residentRole,
            String residentBuilding) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.or(
                        cb.equal(root.get("propertyCompanyId"), propertyCompanyId),
                        cb.isNull(root.get("propertyCompanyId"))));
            }
            if (StringUtils.hasText(announcementType)) {
                predicates.add(cb.equal(root.get("announcementType"), announcementType));
            }
            if (StringUtils.hasText(communityId)) {
                predicates.add(cb.or(
                        cb.equal(root.get("communityId"), communityId),
                        cb.isNull(root.get("communityId"))));
            }
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(residentRole)) {
                predicates.add(cb.or(
                        cb.isNull(root.get("targetRoles")),
                        cb.like(root.get("targetRoles"), "%" + residentRole + "%")));
            }
            if (StringUtils.hasText(residentBuilding)) {
                predicates.add(cb.or(
                        cb.isNull(root.get("targetBuildings")),
                        cb.like(root.get("targetBuildings"), "%" + residentBuilding + "%")));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

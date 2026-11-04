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
            String residentBuilding,
            boolean residentView,
            String residentUnit) {
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
            // 单元必须按 JSON 数组元素整段匹配（带引号）：'5单元' 是 '15单元' 的子串，
            // 沿用楼栋那种裸 LIKE 会把通知漏给别的单元。
            // 住户视角一律参与判定：本人没有 unit 时只能看不限单元的公告，
            // 否则列表会列出单元通知、点进详情又被 getReadableAnnouncement 挡成 90001。
            if (residentView) {
                if (StringUtils.hasText(residentUnit)) {
                    predicates.add(cb.or(
                            cb.isNull(root.get("targetUnits")),
                            cb.like(root.get("targetUnits"), "%\"" + residentUnit + "\"%")));
                } else {
                    predicates.add(cb.isNull(root.get("targetUnits")));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

package com.wuye.points.repository.spec;

import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WithdrawalRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class WithdrawalRecordSpecifications {

    private WithdrawalRecordSpecifications() {
    }

    public static Specification<WithdrawalRecord> withFilters(
            String withdrawalType,
            String applicantId,
            String propertyCompanyId,
            String auditStatus,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(withdrawalType)) {
                predicates.add(cb.equal(root.get("withdrawalType"), withdrawalType));
            }
            if (StringUtils.hasText(applicantId)) {
                predicates.add(cb.equal(root.get("applicantId"), applicantId));
            }
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(auditStatus)) {
                predicates.add(cb.equal(root.get("auditStatus"), auditStatus));
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

    /**
     * 管理端提现审批列表过滤：
     * 按提现类型/物业/审核状态/时间段过滤，keyword 模糊匹配提现ID、申请人主体ID或关联住户姓名/手机号。
     */
    public static Specification<WithdrawalRecord> withAdminFilters(
            String withdrawalType,
            String propertyCompanyId,
            String auditStatus,
            String keyword,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(withdrawalType)) {
                predicates.add(cb.equal(root.get("withdrawalType"), withdrawalType));
            }
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(auditStatus)) {
                predicates.add(cb.equal(root.get("auditStatus"), auditStatus));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                jakarta.persistence.criteria.Subquery<String> sub = query.subquery(String.class);
                jakarta.persistence.criteria.Root<Resident> residentRoot = sub.from(Resident.class);
                sub.select(residentRoot.get("id"));
                sub.where(cb.or(
                        cb.like(residentRoot.get("name"), like),
                        cb.like(residentRoot.get("phone"), like)));
                predicates.add(cb.or(
                        cb.like(root.get("id"), like),
                        cb.like(root.get("applicantId"), like),
                        root.get("residentId").in(sub)));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

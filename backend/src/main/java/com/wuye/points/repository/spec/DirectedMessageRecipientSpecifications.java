package com.wuye.points.repository.spec;

import com.wuye.points.entity.DirectedMessageRecipient;
import com.wuye.points.entity.Resident;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class DirectedMessageRecipientSpecifications {

    private DirectedMessageRecipientSpecifications() {
    }

    public static Specification<DirectedMessageRecipient> withFilters(
            String taskId,
            String readStatus,
            String buildingNo,
            Integer gender,
            List<String> ageBracketResidentIds) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("taskId"), taskId));

            if (StringUtils.hasText(readStatus)) {
                predicates.add(cb.equal(root.get("readStatus"), readStatus));
            }
            if (StringUtils.hasText(buildingNo) || gender != null
                    || (ageBracketResidentIds != null && !ageBracketResidentIds.isEmpty())) {
                var subquery = query.subquery(String.class);
                var residentRoot = subquery.from(Resident.class);
                subquery.select(residentRoot.get("id"));
                List<jakarta.persistence.criteria.Predicate> residentPredicates = new ArrayList<>();
                if (StringUtils.hasText(buildingNo)) {
                    residentPredicates.add(cb.equal(residentRoot.get("building"), buildingNo));
                }
                if (gender != null) {
                    residentPredicates.add(cb.equal(residentRoot.get("gender"), gender));
                }
                if (ageBracketResidentIds != null && !ageBracketResidentIds.isEmpty()) {
                    residentPredicates.add(residentRoot.get("id").in(ageBracketResidentIds));
                }
                subquery.where(cb.and(residentPredicates.toArray(new jakarta.persistence.criteria.Predicate[0])));
                predicates.add(root.get("recipientId").in(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

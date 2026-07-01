package com.wuye.points.repository.spec;

import com.wuye.points.entity.OpinionBox;
import com.wuye.points.entity.VoteRecord;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class OpinionBoxSpecifications {

    private OpinionBoxSpecifications() {
    }

    public static Specification<OpinionBox> withFilters(
            String propertyCompanyId,
            String status,
            String publisherId,
            String votedFamilyId) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(publisherId)) {
                predicates.add(cb.equal(root.get("publisherId"), publisherId));
            }
            if (StringUtils.hasText(votedFamilyId)) {
                var subquery = query.subquery(String.class);
                var voteRoot = subquery.from(VoteRecord.class);
                subquery.select(voteRoot.get("opinionBoxId"));
                subquery.where(cb.and(
                        cb.equal(voteRoot.get("familyId"), votedFamilyId),
                        cb.equal(voteRoot.get("opinionBoxId"), root.get("id"))));
                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

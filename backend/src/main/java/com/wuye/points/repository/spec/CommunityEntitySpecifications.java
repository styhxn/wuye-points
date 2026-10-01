package com.wuye.points.repository.spec;

import com.wuye.points.entity.CommunityEntity;
import com.wuye.points.entity.CommunityPropertyBinding;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class CommunityEntitySpecifications {

    private CommunityEntitySpecifications() {
    }

    public static Specification<CommunityEntity> withFilters(String keyword, String propertyCompanyId) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim() + "%";
                predicates.add(cb.like(root.get("name"), pattern));
            }
            if (StringUtils.hasText(propertyCompanyId)) {
                var subquery = query.subquery(String.class);
                var bindingRoot = subquery.from(CommunityPropertyBinding.class);
                subquery.select(bindingRoot.get("communityEntityId"));
                subquery.where(cb.equal(bindingRoot.get("propertyCompanyId"), propertyCompanyId));
                predicates.add(root.get("id").in(subquery));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}

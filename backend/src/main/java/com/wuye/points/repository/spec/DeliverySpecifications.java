package com.wuye.points.repository.spec;

import com.wuye.points.entity.Delivery;
import com.wuye.points.entity.Order;
import com.wuye.points.entity.Resident;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class DeliverySpecifications {

    private DeliverySpecifications() {
    }

    public static Specification<Delivery> withFilters(
            String propertyCompanyId,
            String courierId,
            String status,
            String communityId,
            List<String> statuses) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(courierId)) {
                predicates.add(cb.equal(root.get("courierId"), courierId));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (StringUtils.hasText(communityId)) {
                predicates.add(root.get("orderId").in(orderIdsInCommunity(query, cb, communityId)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Delivery> pendingPaidOrders(String propertyCompanyId, String communityId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "pending"));
            predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            predicates.add(root.get("orderId").in(paidOrderIds(query, cb)));

            if (StringUtils.hasText(communityId)) {
                predicates.add(root.get("orderId").in(orderIdsInCommunity(query, cb, communityId)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Subquery<String> paidOrderIds(jakarta.persistence.criteria.CriteriaQuery<?> query,
                                               jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<Order> orderRoot = subquery.from(Order.class);
        subquery.select(orderRoot.get("id"));
        subquery.where(cb.equal(orderRoot.get("orderStatus"), "paid"));
        return subquery;
    }

    private static Subquery<String> orderIdsInCommunity(jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                        jakarta.persistence.criteria.CriteriaBuilder cb,
                                                        String communityId) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<Order> orderRoot = subquery.from(Order.class);
        Root<Resident> residentRoot = subquery.from(Resident.class);
        subquery.select(orderRoot.get("id"));
        subquery.where(
                cb.equal(orderRoot.get("residentId"), residentRoot.get("id")),
                cb.equal(residentRoot.get("communityId"), communityId));
        return subquery;
    }
}

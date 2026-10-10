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

    /**
     * 待抢单：仅匹配「商家已选择平台配送」的 {@code pending} 配送单。
     *
     * <p>条件：{@code order.orderStatus = delivering AND order.fulfillmentMode = courier_hall}，
     * 即商家已选择 {@code courier_hall} 或超时兜底后生成的配送单，等待骑手抢单。
     *
     * <p>支付后仍处于 {@code pending_choice}（{@code order_status = paid}）的订单<b>不</b>进入待抢大厅：
     * 该阶段尚未生成配送单，须等商家选择后再发布，避免「商家选择自配 / 发布抢单大厅」形同虚设。
     *
     * <p>同时排除商家自配单（{@code carrier_type = merchant}）。
     */
    public static Specification<Delivery> pendingPaidOrders(String propertyCompanyId, String communityId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "pending"));
            predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            predicates.add(cb.or(
                    cb.isNull(root.get("carrierType")),
                    cb.notEqual(root.get("carrierType"), "merchant")));
            predicates.add(root.get("orderId").in(grabbableOrderIds(query, cb)));

            if (StringUtils.hasText(communityId)) {
                predicates.add(root.get("orderId").in(orderIdsInCommunity(query, cb, communityId)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Subquery<String> grabbableOrderIds(jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                      jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<String> subquery = query.subquery(String.class);
        Root<Order> orderRoot = subquery.from(Order.class);
        subquery.select(orderRoot.get("id"));
        subquery.where(
                cb.equal(orderRoot.get("orderStatus"), "delivering"),
                cb.equal(orderRoot.get("fulfillmentMode"), "courier_hall"));
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

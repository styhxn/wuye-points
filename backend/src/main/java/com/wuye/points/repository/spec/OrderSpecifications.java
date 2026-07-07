package com.wuye.points.repository.spec;

import com.wuye.points.entity.Order;
import com.wuye.points.entity.OrderItem;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> withFilters(
            String propertyCompanyId,
            String residentId,
            String merchantId,
            String orderStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String keyword) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(orderStatus)) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), endDate));
            }
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + escapeLike(keyword.trim()) + "%";

                Predicate orderNoMatch = cb.like(root.get("orderNo"), pattern, '\\');

                // 商品名搜索：orders 与 order_items 无 JPA 关联映射，使用关联子查询
                Subquery<String> itemSubquery = query.subquery(String.class);
                Root<OrderItem> itemRoot = itemSubquery.from(OrderItem.class);
                itemSubquery.select(itemRoot.get("orderId"))
                        .where(
                                cb.equal(itemRoot.get("orderId"), root.get("id")),
                                cb.like(itemRoot.get("productName"), pattern, '\\'));
                Predicate productNameMatch = cb.exists(itemSubquery);

                predicates.add(cb.or(orderNoMatch, productNameMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}

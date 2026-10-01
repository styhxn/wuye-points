package com.wuye.points.repository.spec;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Product;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilters(
            String propertyCompanyId,
            String merchantId,
            String keyword,
            String category,
            String status,
            boolean publicOnly,
            String merchantCategory,
            List<String> consumedMerchantIds) {
        return withFilters(propertyCompanyId, merchantId, keyword, category, status,
                publicOnly, merchantCategory, consumedMerchantIds, null);
    }

    public static Specification<Product> withFilters(
            String propertyCompanyId,
            String merchantId,
            String keyword,
            String category,
            String status,
            boolean publicOnly,
            String merchantCategory,
            List<String> consumedMerchantIds,
            Boolean recommended) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            // 过滤已软删除的商品
            predicates.add(cb.isNull(root.get("deletedAt")));

            if (StringUtils.hasText(propertyCompanyId)) {
                Subquery<String> merchantSub = query.subquery(String.class);
                var merchantRoot = merchantSub.from(Merchant.class);
                merchantSub.select(merchantRoot.get("id"));
                var merchantPredicates = new java.util.ArrayList<Predicate>();
                merchantPredicates.add(cb.equal(merchantRoot.get("propertyCompanyId"), propertyCompanyId));
                if (publicOnly) {
                    merchantPredicates.add(cb.equal(merchantRoot.get("auditStatus"), "approved"));
                    merchantPredicates.add(cb.equal(merchantRoot.get("status"), "active"));
                }
                if (StringUtils.hasText(merchantCategory)) {
                    merchantPredicates.add(cb.equal(merchantRoot.get("category"), merchantCategory));
                }
                merchantSub.where(cb.and(merchantPredicates.toArray(new Predicate[0])));
                predicates.add(root.get("merchantId").in(merchantSub));
            }

            if (StringUtils.hasText(merchantId)) {
                predicates.add(cb.equal(root.get("merchantId"), merchantId));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            } else if (publicOnly) {
                predicates.add(cb.equal(root.get("status"), "active"));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                // 关键词同时匹配「商品名称」与「所属商家名称」：
                // 例如输入"便民"，既返回名称含"便民"的商品，也返回"便民超市"名下商品。
                Subquery<String> merchantNameSub = query.subquery(String.class);
                var merchantNameRoot = merchantNameSub.from(Merchant.class);
                merchantNameSub.select(merchantNameRoot.get("id"));
                merchantNameSub.where(cb.like(merchantNameRoot.get("name"), like));
                predicates.add(cb.or(
                        cb.like(root.get("name"), like),
                        root.get("merchantId").in(merchantNameSub)));
            }
            if (!CollectionUtils.isEmpty(consumedMerchantIds)) {
                predicates.add(root.get("merchantId").in(consumedMerchantIds));
            }
            if (recommended != null) {
                predicates.add(cb.equal(root.get("isRecommended"), recommended));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

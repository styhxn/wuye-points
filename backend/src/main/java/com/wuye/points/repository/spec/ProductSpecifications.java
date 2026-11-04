package com.wuye.points.repository.spec;

import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantServiceCommunity;
import com.wuye.points.entity.Product;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    /**
     * 楼长「分享商品给本单元」的可选范围：本物业下、服务本小区、商家审核通过且在营、商品在售未删除。
     *
     * <p>「服务本小区」有两种登记方式，任一命中即算：商家声明全小区通用
     * （{@code merchants.serve_all_communities=1}，历史行为 NULL 时按默认值 1 处理），
     * 或在 {@code merchant_service_communities} 里显式绑定了该小区。</p>
     */
    public static Specification<Product> shareableInCommunity(
            String propertyCompanyId, String communityId, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), "active"));

            predicates.add(cb.or(
                    root.get("merchantId").in(serveAllMerchantIds(query, cb, propertyCompanyId, communityId)),
                    root.get("merchantId").in(boundMerchantIds(query, cb, propertyCompanyId, communityId))));

            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.like(root.get("name"), like));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Subquery<String> serveAllMerchantIds(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String propertyCompanyId,
            String communityId) {
        Subquery<String> sub = query.subquery(String.class);
        var merchant = sub.from(Merchant.class);
        sub.select(merchant.get("id"));
        sub.where(
                cb.equal(merchant.get("propertyCompanyId"), propertyCompanyId),
                cb.equal(merchant.get("auditStatus"), "approved"),
                cb.equal(merchant.get("status"), "active"),
                cb.or(cb.isTrue(merchant.get("serveAllCommunities")), cb.isNull(merchant.get("serveAllCommunities"))));
        return sub;
    }

    private static Subquery<String> boundMerchantIds(
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            String propertyCompanyId,
            String communityId) {
        Subquery<String> sub = query.subquery(String.class);
        var merchant = sub.from(Merchant.class);
        var binding = sub.from(MerchantServiceCommunity.class);
        sub.select(merchant.get("id"));
        sub.where(
                cb.equal(merchant.get("propertyCompanyId"), propertyCompanyId),
                cb.equal(merchant.get("auditStatus"), "approved"),
                cb.equal(merchant.get("status"), "active"),
                cb.equal(binding.get("merchantId"), merchant.get("id")),
                cb.equal(binding.get("communityId"), communityId));
        return sub;
    }
}

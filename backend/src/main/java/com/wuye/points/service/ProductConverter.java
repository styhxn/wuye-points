package com.wuye.points.service;

import com.wuye.points.dto.product.ProductDetailResponse;
import com.wuye.points.dto.product.ProductListItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Product;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {

    public ProductListItemResponse toListItem(Product product,
                                              Merchant merchant,
                                              boolean includeMemberPrice) {
        return ProductListItemResponse.builder()
                .id(product.getId())
                .merchantId(product.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .name(product.getName())
                .description(product.getDescription())
                .coverUrl(product.getCoverUrl())
                .price(product.getPrice())
                .memberPrice(includeMemberPrice ? product.getMemberPrice() : null)
                .pointPrice(product.getPointPrice())
                .stock(defaultStock(product))
                .category(product.getCategory())
                .status(product.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(product.getCreatedAt()))
                .build();
    }

    public ProductDetailResponse toDetail(Product product,
                                          Merchant merchant,
                                          boolean includeMemberPrice) {
        ProductListItemResponse base = toListItem(product, merchant, includeMemberPrice);
        return ProductDetailResponse.builder()
                .id(base.getId())
                .merchantId(base.getMerchantId())
                .merchantName(base.getMerchantName())
                .name(base.getName())
                .description(base.getDescription())
                .coverUrl(base.getCoverUrl())
                .price(base.getPrice())
                .memberPrice(base.getMemberPrice())
                .pointPrice(base.getPointPrice())
                .stock(base.getStock())
                .category(base.getCategory())
                .status(base.getStatus())
                .createdAt(base.getCreatedAt())
                .updatedAt(DateTimeUtil.toIsoUtc(product.getUpdatedAt()))
                .build();
    }

    private Integer defaultStock(Product product) {
        return product.getStock() != null ? product.getStock() : 0;
    }
}

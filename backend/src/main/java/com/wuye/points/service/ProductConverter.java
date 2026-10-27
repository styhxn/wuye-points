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
                .shopId(product.getMerchantId())
                .shopName(merchant != null ? merchant.getName() : null)
                .shopType(resolveShopType(merchant))
                .shopStatus(merchant != null ? merchant.getStatus() : null)
                .shopOpen(merchant != null && "active".equals(merchant.getStatus()))
                .shopOwnerResidentId("resident".equals(resolveShopType(merchant)) && merchant != null
                        ? merchant.getResidentId() : null)
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
                .shopId(base.getShopId())
                .shopName(base.getShopName())
                .shopType(base.getShopType())
                .shopStatus(base.getShopStatus())
                .shopOpen(base.getShopOpen())
                .shopOwnerResidentId(base.getShopOwnerResidentId())
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

    /** 业主小店店铺主体（merchant_source=resident_shop）与普通商家分流，供前端选择详情页路由。 */
    private String resolveShopType(Merchant merchant) {
        if (merchant == null) {
            return null;
        }
        return "resident_shop".equals(merchant.getMerchantSource()) ? "resident" : "merchant";
    }
}

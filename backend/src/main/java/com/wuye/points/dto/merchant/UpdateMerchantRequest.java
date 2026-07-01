package com.wuye.points.dto.merchant;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UpdateMerchantRequest {

    @Size(max = 100, message = "商家名称长度不能超过100")
    private String name;

    private String description;

    private List<String> coverUrls;

    @Size(max = 500, message = "视频URL长度不能超过500")
    private String videoUrl;

    @Size(max = 50, message = "营业时间长度不能超过50")
    private String businessHours;

    @Size(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    private BigDecimal deliveryFee;

    private BigDecimal freeDeliveryThreshold;

    /** 仅 platform_admin 可设置 */
    private String merchantLevel;

    private Integer rankOrder;
}

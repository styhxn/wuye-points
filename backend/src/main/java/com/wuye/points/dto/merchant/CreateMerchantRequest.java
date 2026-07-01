package com.wuye.points.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateMerchantRequest {

    @NotBlank(message = "商家名称不能为空")
    @Size(max = 100, message = "商家名称长度不能超过100")
    private String name;

    @NotBlank(message = "品类不能为空")
    private String category;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String contactPhone;

    @Size(max = 200, message = "地址长度不能超过200")
    private String address;

    private List<String> coverUrls;

    @Size(max = 500, message = "视频URL长度不能超过500")
    private String videoUrl;

    private String description;

    @Size(max = 50, message = "营业时间长度不能超过50")
    private String businessHours;

    private BigDecimal deliveryFee;

    private BigDecimal freeDeliveryThreshold;

    /** 申请入驻时建议的抽佣比例（审核时可调整） */
    private BigDecimal commissionRate;

    /** 申请入驻时建议的积分兑换比（审核时可调整） */
    private BigDecimal pointExchangeRate;
}

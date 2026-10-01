package com.wuye.points.dto.canteen;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端创建社区食堂商家（主商家 / 子商家）请求。
 * 复用 merchant 登录角色；merchantType 固定为 canteen，与普通商品商家隔离。
 */
@Data
@Schema(description = "创建社区食堂商家请求（主商家或子商家）")
public class CreateCanteenMerchantRequest {

    @NotBlank(message = "商家名称必填")
    @Schema(description = "商家名称", example = "幸福社区食堂")
    private String name;

    @NotBlank(message = "店主手机号必填")
    @Schema(description = "店主手机号（须在该物业下已注册；用于登录商家端，建议用独立账号避免与口福餐厅等串户）",
            example = "13800001111")
    private String contactPhone;

    @NotBlank(message = "物业公司ID必填")
    @Schema(description = "物业公司ID", example = "pc_demo001")
    private String propertyCompanyId;

    @Schema(description = "是否主商家（餐饮公司，可购买额度/给用户充定向余额）；false=子商家", example = "true")
    private Boolean isMain;

    @Schema(description = "经营类目", example = "社区食堂")
    private String category;

    @Schema(description = "简介")
    private String description;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "营业时间", example = "09:00-21:00")
    private String businessHours;

    @Schema(description = "视频URL")
    private String videoUrl;

    @Schema(description = "封面图URL列表（取第一张作为主图）")
    private List<String> coverUrls;

    @Schema(description = "配送费", example = "0")
    private BigDecimal deliveryFee;

    @Schema(description = "免配送阈值", example = "0")
    private BigDecimal freeDeliveryThreshold;

    @Schema(description = "配送范围：both/self/community", example = "both")
    private String deliveryScope;
}

package com.wuye.points.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建商品请求")
public class CreateProductRequest {

    /** 不传则取当前商家 */
    @Size(max = 32, message = "merchantId格式不正确")
    @Schema(description = "商家ID，不传则取当前商家", example = "m_001")
    private String merchantId;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称长度不能超过100")
    @Schema(description = "商品名称", example = "精美绿植盆栽", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "商品描述", example = "适合阳台摆放的绿植盆栽，净化空气")
    private String description;

    @Size(max = 500, message = "封面URL长度不能超过500")
    @Schema(description = "封面图片URL", example = "https://example.com/cover.jpg")
    private String coverUrl;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @Schema(description = "商品价格", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "业主价必须大于0")
    @Schema(description = "业主优惠价", example = "25.00")
    private BigDecimal memberPrice;

    @Min(value = 1, message = "积分兑换价必须大于0")
    @Schema(description = "积分兑换价", example = "500")
    private Integer pointPrice;

    @Min(value = 0, message = "库存不能为负数")
    @Schema(description = "库存数量", example = "100")
    private Integer stock;

    @Size(max = 50, message = "分类长度不能超过50")
    @Schema(description = "商品分类", example = "绿植")
    private String category;

    @Pattern(regexp = "active|inactive|sold_out", message = "商品状态只能为active、inactive或sold_out")
    @Schema(description = "商品状态", example = "active", allowableValues = {"active", "inactive", "sold_out"})
    private String status;
}

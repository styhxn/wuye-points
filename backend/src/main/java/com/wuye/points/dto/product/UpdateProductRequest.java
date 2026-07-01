package com.wuye.points.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

    @Size(max = 100, message = "商品名称长度不能超过100")
    private String name;

    private String description;

    @Size(max = 500, message = "封面URL长度不能超过500")
    private String coverUrl;

    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "业主价必须大于0")
    private BigDecimal memberPrice;

    @Min(value = 1, message = "积分兑换价必须大于0")
    private Integer pointPrice;

    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Size(max = 50, message = "分类长度不能超过50")
    private String category;

    private String status;
}

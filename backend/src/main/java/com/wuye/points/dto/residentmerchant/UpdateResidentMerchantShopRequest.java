package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新我的小店店铺资料请求")
public class UpdateResidentMerchantShopRequest {

    @Size(max = 100, message = "店铺名称长度不能超过100")
    @Schema(description = "店铺名称（不传则不修改）")
    private String shopName;

    @Size(max = 500, message = "店铺简介长度不能超过500")
    @Schema(description = "店铺简介（不传则不修改）")
    private String description;

    @Size(max = 50, message = "营业时间长度不能超过50")
    @Schema(description = "营业时间（不传则不修改，如 08:00-22:00）")
    private String businessHours;

    @Size(max = 20, message = "联系电话长度不能超过20")
    @Schema(description = "联系电话（不传则不修改）")
    private String contactPhone;

    @Size(max = 500, message = "封面图URL长度不能超过500")
    @Schema(description = "店铺封面图URL（不传则不修改）")
    private String coverUrl;
}

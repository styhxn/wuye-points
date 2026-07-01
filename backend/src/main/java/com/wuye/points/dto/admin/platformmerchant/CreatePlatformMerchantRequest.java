package com.wuye.points.dto.admin.platformmerchant;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlatformMerchantRequest {

    @NotBlank(message = "商家名称不能为空")
    private String name;

    @NotBlank(message = "品类不能为空")
    private String category;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String address;
    private String coverUrl;
    private String description;
    private String businessHours;
}

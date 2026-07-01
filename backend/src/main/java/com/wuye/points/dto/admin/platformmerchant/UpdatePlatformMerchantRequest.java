package com.wuye.points.dto.admin.platformmerchant;

import lombok.Data;

@Data
public class UpdatePlatformMerchantRequest {

    private String name;
    private String category;
    private String contactPhone;
    private String address;
    private String coverUrl;
    private String description;
    private String businessHours;
    private String status;
}

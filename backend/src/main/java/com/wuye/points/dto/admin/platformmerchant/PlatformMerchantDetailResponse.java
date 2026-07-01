package com.wuye.points.dto.admin.platformmerchant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlatformMerchantDetailResponse {

    private String id;
    private String name;
    private String category;
    private String coverUrl;
    private String contactPhone;
    private String address;
    private String description;
    private String businessHours;
    private long linkedPropertyCount;
    private String status;
    private String createdAt;
    private String updatedAt;
}

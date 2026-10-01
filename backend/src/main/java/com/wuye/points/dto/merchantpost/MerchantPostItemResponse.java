package com.wuye.points.dto.merchantpost;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MerchantPostItemResponse {

    private String id;
    private String merchantId;
    private String merchantName;
    private String title;
    private String content;
    private String imageUrls;
    private String videoUrl;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

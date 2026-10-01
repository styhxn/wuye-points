package com.wuye.points.dto.merchantpost;

import lombok.Data;

@Data
public class UpdateMerchantPostRequest {

    private String title;
    private String content;
    private String imageUrls;
    private String videoUrl;
    private String status;
}

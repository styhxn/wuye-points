package com.wuye.points.dto.merchantpost;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMerchantPostRequest {

    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    /** JSON 数组字符串 */
    private String imageUrls;

    private String videoUrl;

    /** draft/published，默认 published */
    private String status;
}

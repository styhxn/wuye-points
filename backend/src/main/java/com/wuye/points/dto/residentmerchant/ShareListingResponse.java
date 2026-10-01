package com.wuye.points.dto.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "分享链接响应")
public class ShareListingResponse {

    private String shareUrl;
    private String posterImageUrl;
    private String qrcodeUrl;
    private String visibility;
}

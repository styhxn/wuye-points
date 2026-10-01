package com.wuye.points.dto.profilereward;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 信息采集统计分享链接响应
 */
@Data
@Builder
@Schema(description = "信息采集统计分享链接响应")
public class StatsShareResponse {

    @Schema(description = "分享链接", example = "https://test.yuanxusoftware.top/h5/stats?propertyCompanyId=pc_xxx")
    private String shareUrl;

    @Schema(description = "海报图片URL", example = "https://test.yuanxusoftware.top/api/v1/static/posters/stats.png")
    private String posterImageUrl;

    @Schema(description = "二维码URL", example = "https://test.yuanxusoftware.top/h5/stats?propertyCompanyId=pc_xxx&format=qrcode")
    private String qrcodeUrl;
}

package com.wuye.points.dto.merchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 进件资料图片上传响应（微信媒体上传后返回 media_id）。
 */
@Schema(description = "进件资料图片上传响应")
@Data
@Builder
public class MediaUploadResponse {

    @Schema(description = "微信媒体ID（有效期约3天，用于提交进件）")
    private String mediaId;
}

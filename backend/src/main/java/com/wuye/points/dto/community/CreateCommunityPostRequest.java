package com.wuye.points.dto.community;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发帖请求")
public class CreateCommunityPostRequest {

    @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "周末小区花园有跳蚤市场，欢迎大家来逛~")
    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 2000, message = "帖子内容不能超过2000字")
    private String content;

    @Schema(description = "图片URL列表（先调用文件上传接口，category=community）")
    private List<String> imageUrls;
}

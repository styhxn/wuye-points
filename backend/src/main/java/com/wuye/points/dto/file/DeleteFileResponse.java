package com.wuye.points.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "删除文件响应")
public class DeleteFileResponse {

    @Schema(description = "文件ID", example = "file_a1b2c3")
    private String fileId;

    @Schema(description = "是否删除成功", example = "true")
    private Boolean deleted;
}

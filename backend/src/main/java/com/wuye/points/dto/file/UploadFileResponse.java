package com.wuye.points.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "上传文件响应")
public class UploadFileResponse {

    @Schema(description = "文件ID", example = "file_a1b2c3")
    private String fileId;

    @Schema(description = "原始文件名", example = "合同扫描件.pdf")
    private String originalName;

    @Schema(description = "存储文件名", example = "2026/01/file_a1b2c3.pdf")
    private String storedName;

    @Schema(description = "文件访问地址", example = "https://cdn.example.com/files/2026/01/file_a1b2c3.pdf")
    private String url;

    @Schema(description = "缩略图地址", example = "https://cdn.example.com/files/2026/01/file_a1b2c3_thumb.jpg")
    private String thumbnailUrl;

    @Schema(description = "文件大小(字节)", example = "1048576")
    private Long size;

    @Schema(description = "MIME类型", example = "application/pdf")
    private String mimeType;

    @Schema(description = "文件分类", example = "合同")
    private String category;
}

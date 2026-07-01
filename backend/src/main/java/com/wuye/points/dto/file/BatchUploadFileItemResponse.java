package com.wuye.points.dto.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchUploadFileItemResponse {

    private String fileId;
    private String originalName;
    private String storedName;
    private String url;
    private String thumbnailUrl;
    private Long size;
    private String mimeType;
    private String category;
    private Integer order;
}

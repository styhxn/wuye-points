package com.wuye.points.dto.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteFileResponse {

    private String fileId;
    private Boolean deleted;
}

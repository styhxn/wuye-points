package com.wuye.points.dto.message;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarkMessageReadResponse {

    private String id;
    private String readStatus;
    private String updatedAt;
}

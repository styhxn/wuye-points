package com.wuye.points.dto.resident;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResidentStatusResponse {

    private String id;
    private String status;
    private String updatedAt;
}

package com.wuye.points.dto.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateServiceOrderStatusResponse {

    private String id;
    private String orderStatus;
    private String updatedAt;
}

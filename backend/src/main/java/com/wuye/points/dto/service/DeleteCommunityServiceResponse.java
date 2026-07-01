package com.wuye.points.dto.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteCommunityServiceResponse {

    private String id;
    private String status;
}

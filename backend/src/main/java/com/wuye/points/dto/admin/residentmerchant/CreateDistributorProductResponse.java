package com.wuye.points.dto.admin.residentmerchant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "一级经销商上架商品响应")
public class CreateDistributorProductResponse {

    private String id;
}

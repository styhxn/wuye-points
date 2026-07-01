package com.wuye.points.dto.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteProductResponse {

    private String id;
    private String status;
    private String deletedAt;
}

package com.wuye.points.dto.delivery;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CompleteDeliveryRequest {

    @Size(max = 5, message = "配送凭证最多5张")
    private List<String> proofImageUrls;

    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;
}

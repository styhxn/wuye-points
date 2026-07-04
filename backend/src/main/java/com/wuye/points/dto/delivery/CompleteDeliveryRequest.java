package com.wuye.points.dto.delivery;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "完成配送请求")
@Data
public class CompleteDeliveryRequest {

    @Schema(description = "配送凭证图片URL列表")
    @Size(max = 5, message = "配送凭证最多5张")
    private List<String> proofImageUrls;

    @Schema(description = "备注", example = "已送达")
    @Size(max = 200, message = "备注长度不能超过200")
    private String remark;
}

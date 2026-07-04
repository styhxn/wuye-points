package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Schema(description = "提交收集信息请求")
@Data
public class SubmitCollectRequest {

    @Schema(description = "收集的数据", example = "{\"name\": \"张三\", \"phone\": \"13800138000\"}", required = true)
    @NotEmpty(message = "collectedData不能为空")
    private Map<String, Object> collectedData;
}

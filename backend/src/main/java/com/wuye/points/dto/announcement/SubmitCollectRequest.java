package com.wuye.points.dto.announcement;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

@Data
public class SubmitCollectRequest {

    @NotEmpty(message = "collectedData不能为空")
    private Map<String, Object> collectedData;
}

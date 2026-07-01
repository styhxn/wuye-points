package com.wuye.points.dto.opinion;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateOpinionBoxRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String startTime;

    @NotBlank(message = "结束时间不能为空")
    private String endTime;
}

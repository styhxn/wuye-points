package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "收集字段请求")
@Data
public class CollectFieldRequest {

    @Schema(description = "字段键名", example = "name")
    private String key;

    @Schema(description = "字段显示名称", example = "姓名")
    private String name;

    @Schema(description = "字段类型", example = "TEXT", allowableValues = {"TEXT", "SELECT", "RADIO", "CHECKBOX", "DATE"})
    private String type;

    @Schema(description = "是否必填", example = "true")
    private Boolean required;

    @Schema(description = "选项列表", example = "[\"选项A\", \"选项B\"]")
    private List<String> options;
}

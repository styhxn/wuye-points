package com.wuye.points.dto.admin.pointclear;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RunPointClearRequest {

    @NotBlank(message = "清零基准日期不能为空")
    private String clearDate;

    private Boolean dryRun;

    private List<String> residentIds;

    private String propertyCompanyId;
}

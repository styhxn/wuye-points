package com.wuye.points.dto.propertyoperator;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreatePropertyOperatorRequest {

    @NotBlank
    private String phone;

    @NotBlank
    private String name;

    @NotBlank
    private String password;

    private List<String> communityIds;

    private List<String> buildingNos;

    private String permissionPresetCode;

    /** property_leader / property_operator，默认 property_operator */
    private String propertySubRole;

    /** 平台管理员创建时必填 */
    private String propertyCompanyId;
}

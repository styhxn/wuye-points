package com.wuye.points.dto.communityentity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BindPropertyCompanyRequest {

    @NotBlank
    private String propertyCompanyId;

    private String permissionLevel;
}

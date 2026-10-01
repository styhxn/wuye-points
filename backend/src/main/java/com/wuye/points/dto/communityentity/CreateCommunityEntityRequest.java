package com.wuye.points.dto.communityentity;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommunityEntityRequest {

    @NotBlank
    private String name;

    private String description;

    private String adminResidentId;
}

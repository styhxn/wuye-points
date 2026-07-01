package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FamilyPointBalanceResponse {

    private String familyId;
    private String familyName;
    private Integer pointBalance;
    private Integer memberCount;
}

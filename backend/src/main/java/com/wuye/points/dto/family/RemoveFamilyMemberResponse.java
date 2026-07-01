package com.wuye.points.dto.family;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RemoveFamilyMemberResponse {

    private String memberId;
    private String familyId;
    private String removedAt;
    private String pointsHandling;
    private String coinsHandling;
}

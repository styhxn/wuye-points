package com.wuye.points.dto.family;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddFamilyMemberResponse {

    private String memberId;
    private String familyId;
    private String name;
    private String phone;
    private String relation;
    private Boolean isOwner;
    private String joinedAt;
}

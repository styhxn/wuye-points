package com.wuye.points.dto.family;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FamilyMemberListResponse {

    private List<FamilyMemberItemResponse> list;
}

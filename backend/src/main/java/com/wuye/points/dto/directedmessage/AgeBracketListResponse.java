package com.wuye.points.dto.directedmessage;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AgeBracketListResponse {

    private List<AgeBracketItemResponse> list;
}

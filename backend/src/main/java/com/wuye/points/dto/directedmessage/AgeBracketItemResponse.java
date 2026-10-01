package com.wuye.points.dto.directedmessage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AgeBracketItemResponse {

    private String id;
    private String label;
    private Integer minAge;
    private Integer maxAge;
    private Integer sortOrder;
}

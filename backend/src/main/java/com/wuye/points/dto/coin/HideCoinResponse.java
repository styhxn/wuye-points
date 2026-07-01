package com.wuye.points.dto.coin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HideCoinResponse {

    private String residentId;
    private Boolean coinHidden;
    private String updatedAt;
}

package com.wuye.points.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BindPhoneResponse {

    private String phone;
    private Boolean bound;
    private String boundAt;
}

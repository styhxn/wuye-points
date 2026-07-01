package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAccountResponse {

    private String id;
    private String name;
    private String phone;
    private String role;
    private String email;
}

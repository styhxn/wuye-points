package com.wuye.points.dto.propertycompany;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreatePropertyCompanyResponse {

    private String id;
    private String name;
    private String status;
    private List<CommunityItemResponse> communities;
    private AdminAccountResponse adminAccount;
    private String defaultConfigs;
    private String createdAt;
}

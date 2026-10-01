package com.wuye.points.dto.communityentity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CommunityPropertyBindingItemResponse {

    private String bindingId;
    private String propertyCompanyId;
    private String propertyCompanyName;
    private String permissionLevel;
    private List<String> allowedModules;
    private String boundAt;
}

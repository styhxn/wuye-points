package com.wuye.points.dto.announcement;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAnnouncementRequest {

    @Size(max = 200, message = "title长度不能超过200")
    private String title;

    private String content;
    private String announcementType;
    private String communityId;
    private String merchantId;
    private List<String> coverUrls;
    private List<String> targetRoles;
    private List<String> targetBuildings;
    private Boolean collectEnabled;
    private List<CollectFieldRequest> collectFields;
    private String status;
}

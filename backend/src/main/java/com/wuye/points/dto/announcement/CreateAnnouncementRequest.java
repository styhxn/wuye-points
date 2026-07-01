package com.wuye.points.dto.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateAnnouncementRequest {

    @NotBlank(message = "title不能为空")
    @Size(max = 200, message = "title长度不能超过200")
    private String title;

    @NotBlank(message = "content不能为空")
    private String content;

    @NotBlank(message = "announcementType不能为空")
    private String announcementType;

    private String propertyCompanyId;
    private String communityId;
    private String merchantId;
    private List<String> coverUrls;
    private List<String> targetRoles;
    private List<String> targetBuildings;
    private Boolean collectEnabled;
    private List<CollectFieldRequest> collectFields;
    private String status;
}

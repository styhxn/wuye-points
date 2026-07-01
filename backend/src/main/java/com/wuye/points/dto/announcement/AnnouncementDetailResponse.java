package com.wuye.points.dto.announcement;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnnouncementDetailResponse {

    private String id;
    private String title;
    private String content;
    private String announcementType;
    private String publisherId;
    private String publisherName;
    private String publisherRole;
    private List<String> coverUrls;
    private Boolean collectEnabled;
    private String collectDeadline;
    private Boolean hasCollected;
    private String readStatus;
    private String publishedAt;
    private String createdAt;
    private String propertyCompanyId;
    private String communityId;
    private String merchantId;
    private List<String> targetRoles;
    private List<String> targetBuildings;
    private List<CollectFieldResponse> collectFields;
    private String status;
    private String updatedAt;
}

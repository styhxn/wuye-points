package com.wuye.points.dto.announcement;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnnouncementListItemResponse {

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
}

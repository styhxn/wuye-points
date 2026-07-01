package com.wuye.points.dto.opinion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpinionBoxDetailResponse {

    private String id;
    private String title;
    private String content;
    private String publisherId;
    private String publisherName;
    private String familyId;
    private String propertyCompanyId;
    private String startTime;
    private String endTime;
    private Integer totalVotes;
    private Integer supportCount;
    private Integer opposeCount;
    private Integer abstainCount;
    private String status;
    private Boolean hasVoted;
    private String myVoteOption;
    private Boolean resultPublished;
    private String createdAt;
    private String updatedAt;
}

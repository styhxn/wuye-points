package com.wuye.points.dto.opinion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VoteOpinionBoxResponse {

    private String voteRecordId;
    private String opinionBoxId;
    private String familyId;
    private String voteOption;
    private Integer totalVotes;
    private Integer supportCount;
    private Integer opposeCount;
    private Integer abstainCount;
    private String votedAt;
}

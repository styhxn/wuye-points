package com.wuye.points.dto.opinion;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OpinionBoxResultResponse {

    private String opinionBoxId;
    private String title;
    private String status;
    private Integer totalVotes;
    private Integer supportCount;
    private Integer opposeCount;
    private Integer abstainCount;
    private BigDecimal supportRate;
    private BigDecimal opposeRate;
    private BigDecimal abstainRate;
    private Boolean resultPublished;
    private String endTime;
    private String publishedAt;
}

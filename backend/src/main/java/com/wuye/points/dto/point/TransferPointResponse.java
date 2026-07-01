package com.wuye.points.dto.point;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferPointResponse {

    private String recordId;
    private String fromMemberId;
    private String toMemberId;
    private Integer amount;
    private Integer fromBalance;
    private Integer toBalance;
    private String transferredAt;
}

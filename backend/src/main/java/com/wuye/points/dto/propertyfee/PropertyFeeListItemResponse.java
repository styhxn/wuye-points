package com.wuye.points.dto.propertyfee;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PropertyFeeListItemResponse {

    private String id;
    private String residentId;
    private String residentName;
    private String familyId;
    private String communityId;
    private String communityName;
    private String building;
    private String room;
    private String feeType;
    private String feeTypeName;
    private String period;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private Integer pointDeducted;
    private BigDecimal coinDeducted;
    private String paymentStatus;
    private String dueDate;
    private String paidAt;
    private Boolean autoDeduct;
    private Integer overdueDays;
    private BigDecimal lateFee;
    private String createdAt;
}

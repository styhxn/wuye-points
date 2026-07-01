package com.wuye.points.dto.order;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderDetailResponse extends OrderListItemResponse {

    private String residentId;
    private String propertyCompanyId;
    private String address;
    private String contactPhone;
    private String remark;
    private String cancelledAt;
    private Integer version;
    private String updatedAt;
}

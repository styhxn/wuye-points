package com.wuye.points.dto.admin.pointpool;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdjustPointPoolResponse {

    private String id;
    private String poolType;
    private BigDecimal amount;
    private BigDecimal balance;
    private String description;
    private String createdAt;
}

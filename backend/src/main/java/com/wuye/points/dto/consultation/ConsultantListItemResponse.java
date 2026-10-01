package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "咨询师列表项")
public class ConsultantListItemResponse {

    private String id;
    private String name;
    private String title;
    private String organization;
    private String specialty;
    private String avatarUrl;
    private String category;
    private BigDecimal chatPrice;
    private BigDecimal appointmentPrice;
    private Boolean appointmentEnabled;
    private String auditStatus;
    private String status;
    private String statusCode;
    private String createdAt;
}

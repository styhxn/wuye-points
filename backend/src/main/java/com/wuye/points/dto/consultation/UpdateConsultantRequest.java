package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新咨询师")
public class UpdateConsultantRequest {

    private String name;
    private String title;
    private String organization;
    private String specialty;
    private String avatarUrl;
    private BigDecimal chatPrice;
    private BigDecimal appointmentPrice;
    private Boolean appointmentEnabled;
    private String phone;
    private String introduction;
    private String status;
}

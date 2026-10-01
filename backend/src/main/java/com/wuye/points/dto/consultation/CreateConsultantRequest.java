package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "创建咨询师")
public class CreateConsultantRequest {

    @Schema(description = "物业公司ID（未传时从登录态解析）")
    private String propertyCompanyId;

    @NotBlank
    private String category;

    @NotBlank
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
}

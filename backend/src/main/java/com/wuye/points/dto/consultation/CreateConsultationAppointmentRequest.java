package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "提交预约面诊")
public class CreateConsultationAppointmentRequest {

    @NotBlank
    private String consultantId;

    @NotBlank
    private String appointmentTime;

    private String patientName;
    private String phone;
    private String remark;
}

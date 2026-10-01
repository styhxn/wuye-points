package com.wuye.points.dto.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "预约面诊项")
public class ConsultationAppointmentItemResponse {

    private String id;
    private String consultantId;
    private String consultantName;
    private String appointmentTime;
    private String patientName;
    private String phone;
    private String remark;
    private String status;
    private String statusCode;
    private String createdAt;
}

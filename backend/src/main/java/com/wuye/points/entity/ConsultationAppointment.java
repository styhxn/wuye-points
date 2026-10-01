package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "consultation_appointments")
@EqualsAndHashCode(callSuper = true)
public class ConsultationAppointment extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "consultant_id", length = 32, nullable = false)
    private String consultantId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Column(name = "patient_name", length = 50)
    private String patientName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "status", length = 20)
    private String status;
}

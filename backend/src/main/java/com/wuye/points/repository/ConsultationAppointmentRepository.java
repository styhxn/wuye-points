package com.wuye.points.repository;

import com.wuye.points.entity.ConsultationAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationAppointmentRepository extends JpaRepository<ConsultationAppointment, String> {

    Page<ConsultationAppointment> findByResidentIdOrderByCreatedAtDesc(String residentId, Pageable pageable);
}

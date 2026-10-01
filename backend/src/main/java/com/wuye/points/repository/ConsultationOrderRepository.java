package com.wuye.points.repository;

import com.wuye.points.entity.ConsultationOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationOrderRepository extends JpaRepository<ConsultationOrder, String> {
}

package com.wuye.points.repository;

import com.wuye.points.entity.ServiceRequestOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestOrderRepository extends JpaRepository<ServiceRequestOrder, String> {

    Page<ServiceRequestOrder> findByResidentIdOrderByCreatedAtDesc(String residentId, Pageable pageable);
}

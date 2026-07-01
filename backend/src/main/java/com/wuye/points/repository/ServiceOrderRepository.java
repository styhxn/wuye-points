package com.wuye.points.repository;

import com.wuye.points.entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, String>,
        JpaSpecificationExecutor<ServiceOrder> {
    Page<ServiceOrder> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);
    Page<ServiceOrder> findByResidentId(String residentId, Pageable pageable);
    Optional<ServiceOrder> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);
}

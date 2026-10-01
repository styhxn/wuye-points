package com.wuye.points.repository;

import com.wuye.points.entity.Consultant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ConsultantRepository extends JpaRepository<Consultant, String>, JpaSpecificationExecutor<Consultant> {

    Page<Consultant> findByCategoryAndAuditStatusAndStatus(
            String category, String auditStatus, String status, Pageable pageable);

    Page<Consultant> findByAuditStatusAndStatus(String auditStatus, String status, Pageable pageable);
}

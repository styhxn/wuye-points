package com.wuye.points.repository;

import com.wuye.points.entity.DirectedMessageTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DirectedMessageTaskRepository extends JpaRepository<DirectedMessageTask, String>,
        JpaSpecificationExecutor<DirectedMessageTask> {

    long countByPropertyCompanyIdAndCreatedAtGreaterThanEqual(
            String propertyCompanyId, LocalDateTime start);
}

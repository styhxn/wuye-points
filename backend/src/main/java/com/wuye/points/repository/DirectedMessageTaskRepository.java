package com.wuye.points.repository;

import com.wuye.points.entity.DirectedMessageTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface DirectedMessageTaskRepository extends JpaRepository<DirectedMessageTask, String>,
        JpaSpecificationExecutor<DirectedMessageTask> {

    long countByPropertyCompanyIdAndCreatedAtGreaterThanEqual(
            String propertyCompanyId, LocalDateTime start);

    long countByCreatedAtGreaterThanEqual(LocalDateTime start);

    @Query("""
            SELECT COUNT(t) AS taskCount,
                   COALESCE(SUM(t.recipientCount), 0) AS recipientCount,
                   COALESCE(SUM(t.readCount), 0) AS readCount
            FROM DirectedMessageTask t
            WHERE t.propertyCompanyId = :propertyCompanyId
            """)
    TaskSummary summarizeByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("""
            SELECT COUNT(t) AS taskCount,
                   COALESCE(SUM(t.recipientCount), 0) AS recipientCount,
                   COALESCE(SUM(t.readCount), 0) AS readCount
            FROM DirectedMessageTask t
            """)
    TaskSummary summarizeAll();

    interface TaskSummary {
        Long getTaskCount();

        Long getRecipientCount();

        Long getReadCount();
    }
}

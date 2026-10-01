package com.wuye.points.repository;

import com.wuye.points.entity.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContentReportRepository extends JpaRepository<ContentReport, String>,
        JpaSpecificationExecutor<ContentReport> {

    boolean existsByReporterIdAndTargetTypeAndTargetId(String reporterId, String targetType, String targetId);
}

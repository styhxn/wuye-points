package com.wuye.points.repository;

import com.wuye.points.entity.CanteenDirectedFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 社区食堂-定向余额消费/退款流水 Repository
 */
@Repository
public interface CanteenDirectedFlowRepository
        extends JpaRepository<CanteenDirectedFlow, String>,
        JpaSpecificationExecutor<CanteenDirectedFlow> {

    List<CanteenDirectedFlow> findByOrderId(String orderId);

    List<CanteenDirectedFlow> findByResidentId(String residentId);

    Page<CanteenDirectedFlow> findByResidentId(String residentId, Pageable pageable);
}

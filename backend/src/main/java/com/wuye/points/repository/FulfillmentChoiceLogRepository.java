package com.wuye.points.repository;

import com.wuye.points.entity.FulfillmentChoiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FulfillmentChoiceLogRepository extends JpaRepository<FulfillmentChoiceLog, String>,
        JpaSpecificationExecutor<FulfillmentChoiceLog> {

    List<FulfillmentChoiceLog> findByOrderIdOrderByCreatedAtDesc(String orderId);
}

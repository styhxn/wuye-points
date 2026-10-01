package com.wuye.points.repository;

import com.wuye.points.entity.CanteenDirectedRecharge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 社区食堂-定向充值流水 Repository
 */
@Repository
public interface CanteenDirectedRechargeRepository
        extends JpaRepository<CanteenDirectedRecharge, String>,
        JpaSpecificationExecutor<CanteenDirectedRecharge> {

    List<CanteenDirectedRecharge> findByMainMerchantId(String mainMerchantId);

    Page<CanteenDirectedRecharge> findByMainMerchantId(String mainMerchantId, Pageable pageable);
}

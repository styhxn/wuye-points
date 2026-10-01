package com.wuye.points.repository;

import com.wuye.points.entity.WithdrawalDisbursement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithdrawalDisbursementRepository extends JpaRepository<WithdrawalDisbursement, String> {

    /** 查询待重试/失败的流水，供补偿调度使用 */
    List<WithdrawalDisbursement> findByStatusIn(List<String> statuses);
}

package com.wuye.points.repository;

import com.wuye.points.entity.MerchantTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MerchantTransferRepository extends JpaRepository<MerchantTransfer, String> {

    /** 同一业务下是否已生成转账计划（幂等，防重复入账）。 */
    boolean existsByBizIdAndOwnerType(String bizId, String ownerType);

    List<MerchantTransfer> findByStatusIn(Collection<String> statuses);

    Optional<MerchantTransfer> findByWxTransferBillId(String wxTransferBillId);
}

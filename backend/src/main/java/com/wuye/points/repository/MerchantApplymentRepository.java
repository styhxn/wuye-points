package com.wuye.points.repository;

import com.wuye.points.entity.MerchantApplyment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MerchantApplymentRepository extends JpaRepository<MerchantApplyment, String> {

    Optional<MerchantApplyment> findByMerchantId(String merchantId);

    /** 非终态申请单（submitted/auditing/legal_validating/signing），供状态推进调度扫描。 */
    List<MerchantApplyment> findByApplymentStateIn(Collection<String> states);
}

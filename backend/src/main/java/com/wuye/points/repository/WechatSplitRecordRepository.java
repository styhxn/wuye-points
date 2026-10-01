package com.wuye.points.repository;

import com.wuye.points.entity.WechatSplitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 微信分账流水 Repository。
 */
public interface WechatSplitRecordRepository extends JpaRepository<WechatSplitRecord, String> {

    List<WechatSplitRecord> findByStatus(String status);

    boolean existsByOrderId(String orderId);

    Optional<WechatSplitRecord> findBySplitNo(String splitNo);

    List<WechatSplitRecord> findByWithdrawStatus(String withdrawStatus);

    List<WechatSplitRecord> findByStatusIn(List<String> statuses);

    List<WechatSplitRecord> findByOrderIdAndStatusIn(String orderId, List<String> statuses);

    List<WechatSplitRecord> findByGroupNo(String groupNo);

    boolean existsByOrderIdAndOwnerType(String orderId, String ownerType);

    @Query("""
            SELECT COALESCE(SUM(s.freezeAmount), 0) FROM WechatSplitRecord s
            WHERE s.ownerType = :ownerType AND s.ownerId = :ownerId AND s.status IN :statuses
            """)
    BigDecimal sumFreezeAmountByOwnerAndStatusIn(@Param("ownerType") String ownerType,
                                                 @Param("ownerId") String ownerId,
                                                 @Param("statuses") List<String> statuses);
}

package com.wuye.points.repository;

import com.wuye.points.entity.UserCoupon;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户优惠券 Repository
 */
@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM UserCoupon c WHERE c.id = :id")
    Optional<UserCoupon> findByIdForUpdate(@Param("id") String id);

    /** 统计某用户已持有某模板的有效券数量（不含已过期/已使用），用于每人限领校验 */
    @Query("SELECT COUNT(c) FROM UserCoupon c WHERE c.templateId = :templateId AND c.residentId = :residentId "
            + "AND c.status IN ('unused', 'locked', 'used')")
    long countActiveHeld(@Param("templateId") String templateId, @Param("residentId") String residentId);

    /** 某用户券列表（按状态过滤；status 为空返回全部） */
    @Query("SELECT c FROM UserCoupon c WHERE c.residentId = :residentId "
            + "AND (:status IS NULL OR c.status = :status) ORDER BY c.createdAt DESC")
    List<UserCoupon> findByResident(@Param("residentId") String residentId,
                                    @Param("status") String status);

    /** 某订单锁定的券（用于支付/取消时状态流转） */
    @Query("SELECT c FROM UserCoupon c WHERE c.orderId = :orderId AND c.status IN ('locked', 'used')")
    List<UserCoupon> findByOrderId(@Param("orderId") String orderId);

    /** 过期扫描：将未使用且已过期的券置为 expired（由调度批量处理） */
    @Query("SELECT c FROM UserCoupon c WHERE c.status = 'unused' AND c.expireAt IS NOT NULL AND c.expireAt < :now")
    List<UserCoupon> findExpired(@Param("now") LocalDateTime now);
}
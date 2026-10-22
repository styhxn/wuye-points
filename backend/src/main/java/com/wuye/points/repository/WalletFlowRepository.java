package com.wuye.points.repository;

import com.wuye.points.entity.WalletFlow;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WalletFlowRepository extends JpaRepository<WalletFlow, String> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<WalletFlow> findByIdempotencyKey(String idempotencyKey);

    List<WalletFlow> findByDistributionRecordId(String distributionRecordId);

    /** 按主体 + 余额类型汇总钱包流水金额（钱包「卡 A」可提现余额聚合） */
    @Query("select coalesce(sum(w.amount), 0) from WalletFlow w where w.ownerId = :ownerId and w.balanceType = :balanceType")
    BigDecimal sumAmountByOwnerIdAndBalanceType(@Param("ownerId") String ownerId, @Param("balanceType") String balanceType);

    /** 按主体 + 流水类型汇总金额（商家自配配送费分成兜底入账聚合，entryType=merchant_delivery_fee_share） */
    @Query("select coalesce(sum(w.amount), 0) from WalletFlow w where w.ownerId = :ownerId and w.ownerType = 'merchant' and w.entryType = :entryType")
    BigDecimal sumAmountByOwnerIdAndEntryType(@Param("ownerId") String ownerId, @Param("entryType") String entryType);

    /** 物业经理收益流水查询（ownerType=property_manager，ownerId=经理 residentId） */
    List<WalletFlow> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(String ownerType, String ownerId);

    /** 物业经理收益流水分页查询 */
    Page<WalletFlow> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(String ownerType, String ownerId, Pageable pageable);

    /** 按主体类型 + 主体 + 余额类型汇总金额（物业经理可提现聚合，ownerType=property_manager，避免与其他角色钱包串户） */
    @Query("select coalesce(sum(w.amount), 0) from WalletFlow w where w.ownerType = :ownerType and w.ownerId = :ownerId and w.balanceType = :balanceType")
    BigDecimal sumAmountByOwnerTypeAndOwnerIdAndBalanceType(@Param("ownerType") String ownerType, @Param("ownerId") String ownerId, @Param("balanceType") String balanceType);
}

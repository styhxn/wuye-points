package com.wuye.points.repository;

import com.wuye.points.entity.PropertyCoinFundLedger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PropertyCoinFundLedgerRepository extends JpaRepository<PropertyCoinFundLedger, String>,
        JpaSpecificationExecutor<PropertyCoinFundLedger> {

    Optional<PropertyCoinFundLedger> findByBizTypeAndBizIdAndEntryType(
            String bizType, String bizId, String entryType);

    Page<PropertyCoinFundLedger> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM PropertyCoinFundLedger l " +
           "WHERE l.propertyCompanyId = :propertyCompanyId AND l.direction = 'credit'")
    BigDecimal sumCreditByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM PropertyCoinFundLedger l " +
           "WHERE l.propertyCompanyId = :propertyCompanyId AND l.direction = 'debit'")
    BigDecimal sumDebitByPropertyCompanyId(@Param("propertyCompanyId") String propertyCompanyId);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM PropertyCoinFundLedger l " +
           "WHERE l.merchantId = :merchantId AND l.entryType = 'merchant_settlement'")
    BigDecimal sumMerchantSettlementByMerchantId(@Param("merchantId") String merchantId);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM PropertyCoinFundLedger l " +
           "WHERE l.propertyCompanyId = :propertyCompanyId AND l.entryType = :entryType")
    BigDecimal sumAmountByPropertyCompanyIdAndEntryType(@Param("propertyCompanyId") String propertyCompanyId,
                                                        @Param("entryType") String entryType);

    Page<PropertyCoinFundLedger> findByPropertyCompanyIdAndEntryType(
            String propertyCompanyId, String entryType, Pageable pageable);

    Page<PropertyCoinFundLedger> findByEntryType(String entryType, Pageable pageable);

    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM PropertyCoinFundLedger l " +
           "WHERE l.entryType = :entryType AND l.createdAt >= :start AND l.createdAt < :end " +
           "AND (:propertyCompanyId IS NULL OR l.propertyCompanyId = :propertyCompanyId)")
    BigDecimal sumAmountByEntryTypeAndPeriod(@Param("entryType") String entryType,
                                             @Param("propertyCompanyId") String propertyCompanyId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}

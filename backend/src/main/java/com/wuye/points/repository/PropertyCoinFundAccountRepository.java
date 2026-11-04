package com.wuye.points.repository;

import com.wuye.points.entity.PropertyCoinFundAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyCoinFundAccountRepository extends JpaRepository<PropertyCoinFundAccount, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM PropertyCoinFundAccount a WHERE a.propertyCompanyId = :propertyCompanyId")
    Optional<PropertyCoinFundAccount> findByIdForUpdate(@Param("propertyCompanyId") String propertyCompanyId);
}

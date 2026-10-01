package com.wuye.points.repository;

import com.wuye.points.entity.PropertyBankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyBankCardRepository extends JpaRepository<PropertyBankCard, String> {

    List<PropertyBankCard> findByPropertyCompanyIdOrderByIsDefaultDescCreatedAtDesc(String propertyCompanyId);

    Optional<PropertyBankCard> findFirstByPropertyCompanyIdAndIsDefaultTrueAndStatus(
            String propertyCompanyId, String status);
}

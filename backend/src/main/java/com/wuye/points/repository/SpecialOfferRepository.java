package com.wuye.points.repository;

import com.wuye.points.entity.SpecialOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialOfferRepository extends JpaRepository<SpecialOffer, String>,
        JpaSpecificationExecutor<SpecialOffer> {

    long countByPropertyCompanyIdAndStatus(String propertyCompanyId, String status);
}

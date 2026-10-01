package com.wuye.points.repository;

import com.wuye.points.entity.MerchantTransferAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantTransferAccountRepository extends JpaRepository<MerchantTransferAccount, String> {

    Optional<MerchantTransferAccount> findByOwnerTypeAndOwnerIdAndChannel(String ownerType, String ownerId, String channel);

    List<MerchantTransferAccount> findByOwnerTypeAndOwnerId(String ownerType, String ownerId);

    List<MerchantTransferAccount> findByVerifiedTrue();
}

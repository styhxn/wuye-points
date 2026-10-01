package com.wuye.points.repository;

import com.wuye.points.entity.WechatSplitReceiverAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 微信收款账户 Repository。
 */
public interface WechatSplitReceiverAccountRepository extends JpaRepository<WechatSplitReceiverAccount, String> {

    Optional<WechatSplitReceiverAccount> findByOwnerTypeAndOwnerId(String ownerType, String ownerId);
}

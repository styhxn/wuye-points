package com.wuye.points.payment;

import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.repository.WalletFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 钱包「两张卡」聚合服务（HTML ②-5）：把同一主体（商家/配送员/居民/物业等）的资金拆成
 * 平台内部钱包（卡 A）与 微信 分账冻结（卡 B）两类，供前端钱包页、技工/商家工作台统一展示。
 *
 * <p>纯只读聚合，不写任何库，不改动资金流，可安全调用。</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletCardService {

    private final WalletFlowRepository walletFlowRepository;
    private final WechatSplitRecordRepository wechatSplitRecordRepository;
    private final WechatSplitReceiverAccountService wechatSplitReceiverAccountService;

    /** 卡 B 冻结中：已冻结/已完结/提现中，尚未到卡 */
    private static final List<String> FROZEN_STATUSES = List.of("frozen", "finished", "withdrawing");
    /** 卡 B 已结算：已完结或已提现，已落袋 */
    private static final List<String> SETTLED_STATUSES = List.of("finished", "withdrawing", "withdrawn");

    /**
     * 聚合某主体的两张卡。
     *
     * @param ownerType 主体类型（与 {@code WechatSplitOwnerTypes} / {@code wallet_flows.owner_type} 一致）
     * @param ownerId   主体 ID
     */
    public WalletCardsDTO getWalletCards(String ownerType, String ownerId) {
        BigDecimal legacy = walletFlowRepository.sumAmountByOwnerIdAndBalanceType(ownerId, "withdrawable");
        BigDecimal freezing = wechatSplitRecordRepository.sumFreezeAmountByOwnerAndStatusIn(ownerType, ownerId, FROZEN_STATUSES);
        BigDecimal settled = wechatSplitRecordRepository.sumFreezeAmountByOwnerAndStatusIn(ownerType, ownerId, SETTLED_STATUSES);
        boolean hasWechatSplit = StringUtils.hasText(wechatSplitReceiverAccountService.resolveAccountNo(ownerType, ownerId));
        return new WalletCardsDTO(
                legacy == null ? BigDecimal.ZERO : legacy,
                freezing == null ? BigDecimal.ZERO : freezing,
                settled == null ? BigDecimal.ZERO : settled,
                hasWechatSplit);
    }
}

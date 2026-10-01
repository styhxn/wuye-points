package com.wuye.points.scheduler;

import com.wuye.points.entity.CoinWithdrawal;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.repository.CoinWithdrawalRepository;
import com.wuye.points.service.PropertyCoinService;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 自动提现执行器：每个商家在独立事务中执行，单商家失败仅回滚自身，不影响其他商家。
 */
@Component
@RequiredArgsConstructor
public class AutoWithdrawalExecutor {

    private final CoinWithdrawalRepository coinWithdrawalRepository;
    private final PropertyCoinService propertyCoinService;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 单商家自动提现，使用 REQUIRES_NEW 独立事务，确保单商家失败不影响整批。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createAutoWithdrawal(Merchant merchant, Resident merchantUser,
                                     BigDecimal coinAmount, BigDecimal feeRate) {
        BigDecimal feeAmount = coinAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = coinAmount.subtract(feeAmount);

        propertyCoinService.spendCoins(
                merchantUser,
                coinAmount,
                "manual",
                null,
                "自动提现扣除",
                "system",
                false);

        CoinWithdrawal withdrawal = new CoinWithdrawal();
        withdrawal.setId(idGenerator.generateId("cw"));
        withdrawal.setMerchantId(merchant.getId());
        withdrawal.setPropertyCompanyId(merchant.getPropertyCompanyId());
        withdrawal.setCoinAmount(coinAmount);
        withdrawal.setCashAmount(coinAmount);
        withdrawal.setFeeRate(feeRate);
        withdrawal.setFeeAmount(feeAmount);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setAuditStatus("pending_audit");
        coinWithdrawalRepository.save(withdrawal);
    }
}

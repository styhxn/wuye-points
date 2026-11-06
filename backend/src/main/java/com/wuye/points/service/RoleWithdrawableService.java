package com.wuye.points.service;

import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.payment.WechatSplitOwnerTypes;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.WalletFlowRepository;
import com.wuye.points.repository.WithdrawalRecordRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 各角色「可提现余额 + 提现手续费率」的唯一算法。
 *
 * <p>提现申请校验、分账所得页、各角色钱包/收益页都从这里取数，保证同一账号在不同接口看到的
 * {@code withdrawableAmount} 是同一个函数算出的同一个数（历史问题：五处各写一遍减法，
 * 物业经理展示端漏扣微信分账已到账额，两个页面能对出两个余额）。</p>
 *
 * <p>通用式：可提现 = 已入账收益 − 已占用提现 − 微信分账已实时到账，下限 0。
 * 其中「已占用提现」按通道不同取不同状态集：分账台账类角色的 completed 提现不写负向钱包流水，
 * 因此要扣 pending+approved+completed；住户钱包/统一收益账户在提现完成时已写负向流水，
 * 只扣在途，避免重复扣减。两类口径都与各自原有校验保持一致。</p>
 */
@Service
@RequiredArgsConstructor
public class RoleWithdrawableService {

    /** 手续费承担方：提现手续费从申请金额内扣，实到 = amount − feeAmount。 */
    public static final String FEE_BEARER_PAYEE = "payee";

    /** 商家现金提现 / 物业币兑换未配置物业公司覆盖费率时的默认费率。 */
    private static final BigDecimal DEFAULT_MERCHANT_WITHDRAWAL_FEE_RATE = new BigDecimal("0.0060");

    private static final String BALANCE_TYPE_WITHDRAWABLE = "withdrawable";

    /**
     * 分账台账类角色：收益直接来自 {@code distribution_records} 的本人份额列。
     */
    @Getter
    public enum ShareLedgerRole {

        COORDINATOR("coordinator", WechatSplitOwnerTypes.COORDINATOR),
        SECTOR_LEADER("sector_leader", WechatSplitOwnerTypes.SECTOR_LEADER),
        INDIVIDUAL_LEADER("individual_leader", WechatSplitOwnerTypes.INDIVIDUAL_LEADER);

        private final String withdrawalType;
        private final WechatSplitOwnerTypes splitOwnerType;

        ShareLedgerRole(String withdrawalType, WechatSplitOwnerTypes splitOwnerType) {
            this.withdrawalType = withdrawalType;
            this.splitOwnerType = splitOwnerType;
        }
    }

    /**
     * @param creditedAmount         该身份已入账的收益累计（含退款负向冲正，不扣提现）
     * @param occupiedWithdrawalAmount 已从收益中扣减的提现占用额
     * @param pendingWithdrawalAmount  在途提现（pending + approved）
     * @param withdrawableAmount       唯一可提现余额，与提现申请校验用的同一个数
     * @param feeRate                  该物业公司适用的提现手续费率
     */
    public record RoleBalance(BigDecimal creditedAmount,
                              BigDecimal occupiedWithdrawalAmount,
                              BigDecimal pendingWithdrawalAmount,
                              BigDecimal withdrawableAmount,
                              BigDecimal feeRate) {
    }

    private final DistributionRecordRepository distributionRecordRepository;
    private final WithdrawalRecordRepository withdrawalRecordRepository;
    private final WalletFlowRepository walletFlowRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final WithdrawableService withdrawableService;
    private final UnifiedEarningsService unifiedEarningsService;
    private final PlatformSettingService platformSetting;

    /**
     * 统筹 / 板块负责人 / 个体负责人：按分账台账身份维度算可提现。
     *
     * @param identityId 该角色的身份记录 ID（提现单的 applicantId 用的就是它）
     */
    @Transactional(readOnly = true)
    public RoleBalance forShareLedgerRole(ShareLedgerRole role, String identityId, String propertyCompanyId) {
        BigDecimal earned = defaultDecimal(switch (role) {
            case COORDINATOR -> distributionRecordRepository.sumCoordinatorShareByCoordinatorId(identityId);
            case SECTOR_LEADER -> distributionRecordRepository.sumSectorLeaderShareBySectorLeaderId(identityId);
            case INDIVIDUAL_LEADER ->
                    distributionRecordRepository.sumIndividualLeaderShareByIndividualLeaderId(identityId);
        });
        BigDecimal occupied = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(identityId, role.getWithdrawalType(), null));
        BigDecimal pending = defaultDecimal(withdrawalRecordRepository
                .sumOccupiedAmountByApplicantAndType(identityId, role.getWithdrawalType()));
        // 微信分账已实时到账的分成不再走内部提现（防一笔钱分两次付）
        BigDecimal splitSettled = withdrawableService.calcSplitSettledAmount(
                role.getSplitOwnerType().name(), identityId);
        return new RoleBalance(earned, occupied, pending,
                floorZero(earned.subtract(occupied).subtract(splitSettled)), feeRate(propertyCompanyId));
    }

    /**
     * 单元楼长：可提现走账号统一收益账户（楼长分成 + 业主卖货 + 名下商家），
     * 与 {@code POST /building-leaders/{id}/withdrawals} 的校验同源；
     * creditedAmount 只报楼长身份自身的分账入账，便于与分账页交叉核对。
     */
    @Transactional(readOnly = true)
    public RoleBalance forBuildingLeader(String buildingLeaderId, String residentId, String propertyCompanyId) {
        return forBuildingLeader(buildingLeaderId, residentId, propertyCompanyId,
                unifiedEarningsService.calc(residentId));
    }

    /** 同一请求内已取过统一收益账户快照时复用，避免重复聚合。 */
    public RoleBalance forBuildingLeader(String buildingLeaderId, String residentId, String propertyCompanyId,
                                         UnifiedEarningsService.UnifiedEarnings unified) {
        BigDecimal occupied = defaultDecimal(unified.withdrawnAmount()).add(defaultDecimal(unified.pendingAmount()));
        return new RoleBalance(
                defaultDecimal(distributionRecordRepository
                        .sumBuildingLeaderShareByBuildingLeaderId(buildingLeaderId)),
                occupied.setScale(2, RoundingMode.HALF_UP),
                defaultDecimal(unified.pendingAmount()),
                defaultDecimal(unified.withdrawableAmount()),
                feeRate(propertyCompanyId));
    }

    /**
     * 物业经理：收益来源是经理抽成的钱包流水（含冲正），主体键用经理的 residentId；
     * 提现占用额按经理身份（applicantId=经理身份 ID）统计。
     */
    @Transactional(readOnly = true)
    public RoleBalance forPropertyManager(String managerId, String managerResidentId, String propertyCompanyId) {
        BigDecimal earned = defaultDecimal(walletFlowRepository.sumAmountByOwnerTypeAndOwnerIdAndBalanceType(
                "property_manager", managerResidentId, BALANCE_TYPE_WITHDRAWABLE));
        BigDecimal occupied = defaultDecimal(withdrawalRecordRepository
                .sumAmountByApplicant(managerId, "property_manager", null));
        BigDecimal pending = defaultDecimal(withdrawalRecordRepository
                .sumOccupiedAmountByApplicantAndType(managerId, "property_manager"));
        // 主体键与分账记录一致：ownerType=PROPERTY_MANAGER、ownerId=经理 residentId
        BigDecimal splitSettled = withdrawableService.calcSplitSettledAmount(
                WechatSplitOwnerTypes.PROPERTY_MANAGER.name(), managerResidentId);
        return new RoleBalance(earned, occupied, pending,
                floorZero(earned.subtract(occupied).subtract(splitSettled)), feeRate(propertyCompanyId));
    }

    /**
     * 住户钱包（奖励金转钱包后的余额）：completed 提现已在完成时写负向流水，只扣在途。
     */
    @Transactional(readOnly = true)
    public RoleBalance forResidentWallet(String residentId, String propertyCompanyId) {
        BigDecimal balance = defaultDecimal(walletFlowRepository.sumAmountByOwnerTypeAndOwnerIdAndBalanceType(
                "resident", residentId, BALANCE_TYPE_WITHDRAWABLE));
        BigDecimal pending = defaultDecimal(withdrawalRecordRepository
                .sumOccupiedAmountByApplicantAndType(residentId, "resident_wallet"));
        return new RoleBalance(balance, pending, pending,
                floorZero(balance.subtract(pending)), feeRate(propertyCompanyId));
    }

    /**
     * 业主卖货钱包：与楼长同属账号统一收益账户，可提现同源。
     */
    @Transactional(readOnly = true)
    public RoleBalance forResidentSeller(String residentId, String propertyCompanyId) {
        UnifiedEarningsService.UnifiedEarnings unified = unifiedEarningsService.calc(residentId);
        BigDecimal occupied = defaultDecimal(unified.withdrawnAmount()).add(defaultDecimal(unified.pendingAmount()));
        return new RoleBalance(defaultDecimal(unified.totalEarning()), occupied.setScale(2, RoundingMode.HALF_UP),
                defaultDecimal(unified.pendingAmount()), defaultDecimal(unified.withdrawableAmount()),
                feeRate(propertyCompanyId));
    }

    /**
     * 配送提现的在途占用（pending + approved）。
     * applicantId 与提现单一致：普通配送员用住户 ID，快递负责人用负责人身份 ID。
     */
    public BigDecimal courierPendingWithdrawal(String applicantId) {
        return defaultDecimal(withdrawalRecordRepository
                .sumOccupiedAmountByApplicantAndType(applicantId, "courier"));
    }

    /**
     * 物业公司维度的提现手续费率：{@code property_companies.withdrawal_fee_rate} 覆盖值优先，
     * 未配置取平台默认；平台总开关或角色费率开关关闭时为 0。
     */
    public BigDecimal feeRate(String propertyCompanyId) {
        return platformSetting.resolveWithdrawalFeeRate(overrideRate(propertyCompanyId),
                PlatformSettingService.FEE_CATEGORY_ROLE);
    }

    /**
     * 商家现金提现与物业币兑换的费率：{@code property_companies.withdrawal_fee_rate} 覆盖值优先，
     * 未配置（或为负）取默认 0.0060。
     *
     * <p>注意：这条链路<b>不受</b>平台提现手续费总开关与分类开关约束，与角色提现费率口径不同，
     * 差异已提交老板待统一；此处先保证校验与展示用同一个函数。</p>
     */
    public BigDecimal merchantWithdrawalFeeRate(BigDecimal propertyOverrideRate) {
        if (propertyOverrideRate == null || propertyOverrideRate.compareTo(BigDecimal.ZERO) < 0) {
            return DEFAULT_MERCHANT_WITHDRAWAL_FEE_RATE;
        }
        return propertyOverrideRate;
    }

    /** {@link #merchantWithdrawalFeeRate(BigDecimal)} 的按物业公司 ID 取值版。 */
    public BigDecimal merchantWithdrawalFeeRateForCompany(String propertyCompanyId) {
        PropertyCompany company = StringUtils.hasText(propertyCompanyId)
                ? propertyCompanyRepository.findById(propertyCompanyId).orElse(null) : null;
        return merchantWithdrawalFeeRate(company == null ? null : company.getWithdrawalFeeRate());
    }

    private BigDecimal overrideRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return null;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getWithdrawalFeeRate)
                .orElse(null);
    }

    private BigDecimal floorZero(BigDecimal value) {
        BigDecimal result = defaultDecimal(value);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    private static BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

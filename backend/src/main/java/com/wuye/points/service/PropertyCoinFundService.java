package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.coinfund.PropertyCoinFundLedgerItemResponse;
import com.wuye.points.dto.coinfund.PropertyCoinFundSummaryResponse;
import com.wuye.points.dto.coinfund.TransferPropertyCoinFundRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.entity.PropertyCoinFundAccount;
import com.wuye.points.entity.PropertyCoinFundLedger;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCoinFundAccountRepository;
import com.wuye.points.repository.PropertyCoinFundLedgerRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物业币现金资金归属台账。
 *
 * <p>充值现金进入平台直连商户号后，平台按物业公司记一笔代管负债；
 * 商家物业币应收结算、平台向物业转账都从该物业公司的代管资金出账。
 * 本服务只处理现金归属，不改变用户物业币余额。</p>
 */
@Service
@RequiredArgsConstructor
public class PropertyCoinFundService {

    private final PropertyCoinFundAccountRepository accountRepository;
    private final PropertyCoinFundLedgerRepository ledgerRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final RoleGuard roleGuard;

    /** 充值到账：平台收到现金，对物业公司形成等额代管资金（幂等）。 */
    @Transactional
    public void recordRecharge(PaymentRecord record) {
        if (record == null || !StringUtils.hasText(record.getPropertyCompanyId())) {
            return;
        }
        BigDecimal amount = money(record.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (ledgerRepository.findByBizTypeAndBizIdAndEntryType(
                "coin_recharge", record.getId(), PropertyCoinFundLedger.ENTRY_RECHARGE_IN).isPresent()) {
            return;
        }
        PropertyCoinFundAccount account = lockAccount(record.getPropertyCompanyId());
        BigDecimal balance = nz(account.getBalance()).add(amount);
        account.setBalance(balance);
        account.setTotalIn(nz(account.getTotalIn()).add(amount));
        accountRepository.save(account);

        PropertyCoinFundLedger ledger = new PropertyCoinFundLedger();
        ledger.setId(idGenerator.generateId("pcf"));
        ledger.setPropertyCompanyId(record.getPropertyCompanyId());
        ledger.setEntryType(PropertyCoinFundLedger.ENTRY_RECHARGE_IN);
        ledger.setDirection(PropertyCoinFundLedger.DIRECTION_CREDIT);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(balance);
        ledger.setBizType("coin_recharge");
        ledger.setBizId(record.getId());
        ledger.setResidentId(record.getResidentId());
        ledger.setDescription("物业币充值到账（平台代管）");
        ledgerRepository.save(ledger);
    }

    /** 商家物业币应收结算完成：代管资金出账（幂等，按提现单号）。 */
    @Transactional
    public void recordMerchantSettlement(MerchantWithdrawal withdrawal) {
        if (withdrawal == null || !StringUtils.hasText(withdrawal.getPropertyCompanyId())) {
            return;
        }
        // 按提现单全额出账：手续费归平台，不再属于物业代管资金；
        // 若按实到金额出账，手续费会永久留在物业余额里，导致物业多记一笔。
        BigDecimal amount = money(withdrawal.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (ledgerRepository.findByBizTypeAndBizIdAndEntryType(
                "merchant_withdrawal", withdrawal.getId(),
                PropertyCoinFundLedger.ENTRY_MERCHANT_SETTLEMENT).isPresent()) {
            return;
        }
        PropertyCoinFundAccount account = lockAccount(withdrawal.getPropertyCompanyId());
        BigDecimal balance = nz(account.getBalance()).subtract(amount);
        account.setBalance(balance);
        account.setTotalOut(nz(account.getTotalOut()).add(amount));
        accountRepository.save(account);

        PropertyCoinFundLedger ledger = new PropertyCoinFundLedger();
        ledger.setId(idGenerator.generateId("pcf"));
        ledger.setPropertyCompanyId(withdrawal.getPropertyCompanyId());
        ledger.setEntryType(PropertyCoinFundLedger.ENTRY_MERCHANT_SETTLEMENT);
        ledger.setDirection(PropertyCoinFundLedger.DIRECTION_DEBIT);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(balance);
        ledger.setBizType("merchant_withdrawal");
        ledger.setBizId(withdrawal.getId());
        ledger.setMerchantId(withdrawal.getMerchantId());
        ledger.setDescription("商家物业币应收结算出账");
        ledger.setOperatorId(UserContext.currentUserId());
        ledgerRepository.save(ledger);
    }

    /** 平台向物业公司划转代管资金（debit）。 */
    @Transactional
    public PropertyCoinFundLedgerItemResponse transferToProperty(TransferPropertyCoinFundRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        String propertyCompanyId = request.getPropertyCompanyId();
        propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        BigDecimal amount = money(request.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "划转金额必须大于0");
        }

        PropertyCoinFundAccount account = lockAccount(propertyCompanyId);
        BigDecimal balance = nz(account.getBalance());
        if (amount.compareTo(balance) > 0) {
            throw new BizException(ErrorCode.PROPERTY_COIN_FUND_INSUFFICIENT,
                    "代管资金余额不足（当前 ¥" + balance.toPlainString() + "）");
        }
        BigDecimal balanceAfter = balance.subtract(amount);
        account.setBalance(balanceAfter);
        account.setTotalOut(nz(account.getTotalOut()).add(amount));
        accountRepository.save(account);

        PropertyCoinFundLedger ledger = new PropertyCoinFundLedger();
        ledger.setId(idGenerator.generateId("pcf"));
        ledger.setPropertyCompanyId(propertyCompanyId);
        ledger.setEntryType(PropertyCoinFundLedger.ENTRY_PROPERTY_TRANSFER);
        ledger.setDirection(PropertyCoinFundLedger.DIRECTION_DEBIT);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(balanceAfter);
        ledger.setBizType("property_transfer");
        ledger.setBizId(ledger.getId());
        ledger.setDescription(StringUtils.hasText(request.getRemark())
                ? request.getRemark() : "平台向物业公司划转物业币代管资金");
        ledger.setOperatorId(UserContext.currentUserId());
        ledger.setTransferProof(request.getTransferProof());
        ledgerRepository.save(ledger);
        return toItem(ledger);
    }

    @Transactional(readOnly = true)
    public List<PropertyCoinFundSummaryResponse> listSummary(String propertyCompanyId) {
        String scoped = resolveScopedPropertyCompanyId(propertyCompanyId);
        List<PropertyCompany> companies = StringUtils.hasText(scoped)
                ? propertyCompanyRepository.findById(scoped).map(List::of).orElse(List.of())
                : propertyCompanyRepository.findAll();
        Map<String, PropertyCoinFundAccount> accounts = accountRepository
                .findAllById(companies.stream().map(PropertyCompany::getId).toList()).stream()
                .collect(Collectors.toMap(PropertyCoinFundAccount::getPropertyCompanyId, Function.identity()));
        return companies.stream().map(company -> {
            PropertyCoinFundAccount account = accounts.get(company.getId());
            BigDecimal balance = account != null ? nz(account.getBalance()) : BigDecimal.ZERO;
            BigDecimal totalIn = account != null ? nz(account.getTotalIn()) : BigDecimal.ZERO;
            BigDecimal totalOut = account != null ? nz(account.getTotalOut()) : BigDecimal.ZERO;
            return PropertyCoinFundSummaryResponse.builder()
                    .propertyCompanyId(company.getId())
                    .propertyName(company.getName())
                    .balance(money(balance))
                    .totalIn(money(totalIn))
                    .totalOut(money(totalOut))
                    .merchantSettlementAmount(money(ledgerRepository
                            .sumAmountByPropertyCompanyIdAndEntryType(
                                    company.getId(), PropertyCoinFundLedger.ENTRY_MERCHANT_SETTLEMENT)))
                    .fundingGapAmount(money(balance.min(BigDecimal.ZERO).abs()))
                    .build();
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PropertyCoinFundLedgerItemResponse> listLedger(
            int page, int pageSize, String propertyCompanyId, String entryType) {
        String scoped = resolveScopedPropertyCompanyId(propertyCompanyId);
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(pageSize, 1),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<PropertyCoinFundLedger> result;
        if (StringUtils.hasText(scoped) && StringUtils.hasText(entryType)) {
            result = ledgerRepository.findByPropertyCompanyIdAndEntryType(scoped, entryType, pageable);
        } else if (StringUtils.hasText(scoped)) {
            result = ledgerRepository.findByPropertyCompanyId(scoped, pageable);
        } else if (StringUtils.hasText(entryType)) {
            result = ledgerRepository.findByEntryType(entryType, pageable);
        } else {
            result = ledgerRepository.findAll(pageable);
        }
        return PageResponse.of(result.getContent().stream().map(this::toItem).toList(),
                page, pageSize, result.getTotalElements());
    }

    /** 商家已从资金台账结算的物业币应收累计（用于应收对账）。 */
    @Transactional(readOnly = true)
    public BigDecimal sumMerchantSettled(String merchantId) {
        return money(ledgerRepository.sumMerchantSettlementByMerchantId(merchantId));
    }

    private PropertyCoinFundAccount lockAccount(String propertyCompanyId) {
        return accountRepository.findByIdForUpdate(propertyCompanyId).orElseGet(() -> {
            PropertyCoinFundAccount account = new PropertyCoinFundAccount();
            account.setPropertyCompanyId(propertyCompanyId);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalIn(BigDecimal.ZERO);
            account.setTotalOut(BigDecimal.ZERO);
            return accountRepository.save(account);
        });
    }

    private String resolveScopedPropertyCompanyId(String requested) {
        if (roleGuard.isPlatformAdmin()) {
            return requested;
        }
        roleGuard.requireAnyRole("property_admin");
        String current = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(requested) && !requested.equals(current)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return current;
    }

    private PropertyCoinFundLedgerItemResponse toItem(PropertyCoinFundLedger ledger) {
        return PropertyCoinFundLedgerItemResponse.builder()
                .id(ledger.getId())
                .propertyCompanyId(ledger.getPropertyCompanyId())
                .entryType(ledger.getEntryType())
                .direction(ledger.getDirection())
                .amount(money(ledger.getAmount()))
                .balanceAfter(money(ledger.getBalanceAfter()))
                .bizType(ledger.getBizType())
                .bizId(ledger.getBizId())
                .residentId(ledger.getResidentId())
                .merchantId(ledger.getMerchantId())
                .description(ledger.getDescription())
                .operatorId(ledger.getOperatorId())
                .transferProof(ledger.getTransferProof())
                .createdAt(DateTimeUtil.toIsoUtc(ledger.getCreatedAt()))
                .build();
    }

    private static BigDecimal money(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

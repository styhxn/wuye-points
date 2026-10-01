package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.admin.coinmallrule.MallRulesResponse;
import com.wuye.points.dto.admin.coinmallrule.UpdateMallRulesRequest;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 物业币商城规则服务
 */
@Service
@RequiredArgsConstructor
public class PropertyCoinMallRuleService {

    private static final BigDecimal DEFAULT_COIN_MALL_MAX_RATIO = new BigDecimal("1.0000");
    private static final BigDecimal DEFAULT_COIN_MALL_MIN_AMOUNT = new BigDecimal("0.01");

    private final PropertyCompanyRepository propertyCompanyRepository;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public MallRulesResponse getMallRules(String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        PropertyCompany company = resolveReadableCompany(propertyCompanyId);
        return toResponse(company);
    }

    @Transactional
    public MallRulesResponse updateMallRules(UpdateMallRulesRequest request) {
        roleGuard.requireAnyRole("property_admin");
        PropertyCompany company = getCurrentPropertyCompany();
        applyMallRules(company, request.getCoinMallEnabled(), request.getCoinMallMaxRatio(),
                request.getCoinMallMinAmount());
        propertyCompanyRepository.save(company);
        return toResponse(company);
    }

    /**
     * 校验并返回允许使用的物业币金额；requestedCoin 超限直接报错（不自动截断）。
     */
    public BigDecimal assertCoinAllowed(PropertyCompany company,
                                        BigDecimal totalAmount,
                                        BigDecimal requestedCoin,
                                        BigDecimal availableBalance) {
        BigDecimal requested = normalizeMoney(requestedCoin);
        if (requested.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (requested.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "使用物业币不能为负数");
        }

        if (!resolveEnabled(company.getCoinMallEnabled())) {
            throw new BizException(ErrorCode.COIN_MALL_DISABLED);
        }

        BigDecimal minAmount = resolveMinAmount(company.getCoinMallMinAmount());
        BigDecimal orderAmount = defaultAmount(totalAmount);
        if (orderAmount.compareTo(minAmount) < 0) {
            throw new BizException(ErrorCode.COIN_BELOW_MIN_AMOUNT);
        }

        BigDecimal maxByRatio = floor2(orderAmount.multiply(resolveRatio(company.getCoinMallMaxRatio())));
        BigDecimal maxByBalance = defaultAmount(availableBalance);
        BigDecimal allowedCoin = requested.min(maxByRatio).min(maxByBalance);

        if (requested.compareTo(allowedCoin) > 0) {
            if (requested.compareTo(maxByRatio) > 0) {
                throw new BizException(ErrorCode.COIN_EXCEED_MAX_RATIO);
            }
            throw new BizException(ErrorCode.COIN_INSUFFICIENT);
        }
        return requested;
    }

    /**
     * 试算：返回最大可抵扣与实际采用金额（可截断展示，不抛错）。
     */
    public CoinAllowance previewCoinAllowance(PropertyCompany company,
                                              BigDecimal totalAmount,
                                              BigDecimal requestedCoin,
                                              BigDecimal availableBalance) {
        BigDecimal orderAmount = defaultAmount(totalAmount);
        BigDecimal requested = normalizeMoney(requestedCoin);
        boolean enabled = resolveEnabled(company.getCoinMallEnabled());
        BigDecimal minAmount = resolveMinAmount(company.getCoinMallMinAmount());
        BigDecimal maxRatio = resolveRatio(company.getCoinMallMaxRatio());
        BigDecimal maxByRatio = floor2(orderAmount.multiply(maxRatio));
        BigDecimal maxByBalance = defaultAmount(availableBalance);

        String reasonIfCapped = null;
        BigDecimal maxCoinAllowed = BigDecimal.ZERO;
        if (!enabled) {
            reasonIfCapped = "该物业未开启物业币抵扣";
        } else if (orderAmount.compareTo(minAmount) < 0) {
            reasonIfCapped = "未达物业币最低使用金额";
        } else {
            maxCoinAllowed = maxByRatio.min(maxByBalance);
            if (maxByBalance.compareTo(maxByRatio) < 0) {
                reasonIfCapped = "受物业币余额限制";
            } else if (maxRatio.compareTo(BigDecimal.ONE) < 0) {
                reasonIfCapped = "受最大抵扣比例限制";
            }
        }

        BigDecimal coinUsed = requested.min(maxCoinAllowed);
        if (requested.compareTo(BigDecimal.ZERO) > 0 && coinUsed.compareTo(requested) < 0) {
            if (!enabled) {
                reasonIfCapped = "该物业未开启物业币抵扣";
            } else if (orderAmount.compareTo(minAmount) < 0) {
                reasonIfCapped = "未达物业币最低使用金额";
            } else if (requested.compareTo(maxByRatio) > 0) {
                reasonIfCapped = "超过最大抵扣比例";
            } else if (requested.compareTo(maxByBalance) > 0) {
                reasonIfCapped = "物业币余额不足";
            }
        }

        BigDecimal cashAmount = orderAmount.subtract(coinUsed).max(BigDecimal.ZERO);
        return new CoinAllowance(orderAmount, maxCoinAllowed, coinUsed, cashAmount, reasonIfCapped);
    }

    public void applyMallRules(PropertyCompany company,
                               Boolean coinMallEnabled,
                               BigDecimal coinMallMaxRatio,
                               BigDecimal coinMallMinAmount) {
        if (coinMallEnabled != null) {
            company.setCoinMallEnabled(coinMallEnabled);
        }
        if (coinMallMaxRatio != null) {
            validateRatio(coinMallMaxRatio);
            company.setCoinMallMaxRatio(coinMallMaxRatio);
        }
        if (coinMallMinAmount != null) {
            validateMinAmount(coinMallMinAmount);
            company.setCoinMallMinAmount(coinMallMinAmount);
        }
    }

    private PropertyCompany resolveReadableCompany(String propertyCompanyId) {
        if (StringUtils.hasText(propertyCompanyId)) {
            if (!roleGuard.isPlatformAdmin()) {
                String current = UserContext.currentPropertyCompanyId();
                if (!propertyCompanyId.equals(current)) {
                    throw new BizException(ErrorCode.PERMISSION_DENIED, "无权查询其他物业公司规则");
                }
            }
            return propertyCompanyRepository.findById(propertyCompanyId)
                    .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
        }
        return getCurrentPropertyCompany();
    }

    private PropertyCompany getCurrentPropertyCompany() {
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND, "当前用户未关联物业公司");
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.PROPERTY_COMPANY_NOT_FOUND));
    }

    private MallRulesResponse toResponse(PropertyCompany company) {
        return MallRulesResponse.builder()
                .propertyCompanyId(company.getId())
                .coinMallEnabled(resolveEnabled(company.getCoinMallEnabled()))
                .coinMallMaxRatio(resolveRatio(company.getCoinMallMaxRatio()))
                .coinMallMinAmount(resolveMinAmount(company.getCoinMallMinAmount()))
                .build();
    }

    private void validateRatio(BigDecimal ratio) {
        if (ratio.compareTo(BigDecimal.ZERO) < 0 || ratio.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "coinMallMaxRatio必须在0~1之间");
        }
    }

    private void validateMinAmount(BigDecimal minAmount) {
        if (minAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, "coinMallMinAmount不能小于0");
        }
    }

    private Boolean resolveEnabled(Boolean value) {
        return value != null ? value : Boolean.TRUE;
    }

    private BigDecimal resolveRatio(BigDecimal value) {
        return value != null ? value : DEFAULT_COIN_MALL_MAX_RATIO;
    }

    private BigDecimal resolveMinAmount(BigDecimal value) {
        return value != null ? value : DEFAULT_COIN_MALL_MIN_AMOUNT;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return defaultAmount(value).setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal floor2(BigDecimal value) {
        return value.setScale(2, RoundingMode.DOWN);
    }

    public record CoinAllowance(
            BigDecimal totalAmount,
            BigDecimal maxCoinAllowed,
            BigDecimal coinUsed,
            BigDecimal cashAmount,
            String reasonIfCapped
    ) {
    }
}

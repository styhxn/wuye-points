package com.wuye.points.util;

import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 积分/物业币功能开关的服务端拦截（spec 5.1）。
 * pointEnabled / coinEnabled = false 时，写接口须拒绝，不能仅靠前端隐藏。
 * 未关联物业公司或物业公司不存在时按开启处理（兼容历史数据）。
 */
@Component
@RequiredArgsConstructor
public class FeatureSwitchGuard {

    private final PropertyCompanyRepository propertyCompanyRepository;

    public void assertPointEnabled(String propertyCompanyId) {
        if (!isPointEnabled(propertyCompanyId)) {
            throw new BizException(ErrorCode.POINT_FEATURE_DISABLED);
        }
    }

    public void assertCoinEnabled(String propertyCompanyId) {
        if (!isCoinEnabled(propertyCompanyId)) {
            throw new BizException(ErrorCode.COIN_FEATURE_DISABLED);
        }
    }

    public boolean isPointEnabled(String propertyCompanyId) {
        PropertyCompany company = loadCompany(propertyCompanyId);
        return company == null || !Boolean.FALSE.equals(company.getPointEnabled());
    }

    public boolean isCoinEnabled(String propertyCompanyId) {
        PropertyCompany company = loadCompany(propertyCompanyId);
        return company == null || !Boolean.FALSE.equals(company.getCoinEnabled());
    }

    private PropertyCompany loadCompany(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return null;
        }
        return propertyCompanyRepository.findById(propertyCompanyId).orElse(null);
    }
}

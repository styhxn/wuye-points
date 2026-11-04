package com.wuye.points.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.MerchantPropertyConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("MerchantConverter pointExchangeRate 映射")
class MerchantConverterPointExchangeRateTest {

    private final MerchantConverter converter = new MerchantConverter(new ObjectMapper());

    private Merchant merchant() {
        Merchant merchant = new Merchant();
        merchant.setId("mch_001");
        merchant.setName("测试商家");
        merchant.setPropertyCompanyId("pc_001");
        merchant.setStatus("active");
        merchant.setAuditStatus("approved");
        return merchant;
    }

    @Test
    @DisplayName("返回商家挂接配置的积分兑换比")
    void returnsConfiguredRate() {
        MerchantPropertyConfig config = new MerchantPropertyConfig();
        config.setPointExchangeRate(new BigDecimal("80.0000"));

        MerchantDetailResponse response =
                converter.toDetail(merchant(), config, List.of(), true);

        assertEquals(new BigDecimal("80.0000"), response.getPointExchangeRate());
    }

    @Test
    @DisplayName("配置缺失时回退系统默认 100，避免前端本地试算除零")
    void fallsBackToDefaultWhenConfigMissing() {
        MerchantDetailResponse response =
                converter.toDetail(merchant(), null, List.of(), true);

        assertEquals(new BigDecimal("100.0000"), response.getPointExchangeRate());
    }
}

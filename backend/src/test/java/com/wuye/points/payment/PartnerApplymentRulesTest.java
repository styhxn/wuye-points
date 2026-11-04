package com.wuye.points.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 进件结算规则映射回归测试：值与微信官方《入驻结算规则、行业属性及特殊资质》公示表一致。
 */
class PartnerApplymentRulesTest {

    @Test
    @DisplayName("企业取 716、个体户取 719；小微不在受理范围")
    void settlementIdBySubjectType() {
        assertEquals("716", PartnerApplymentRules.settlementIdFor("ENTERPRISE"));
        assertEquals("719", PartnerApplymentRules.settlementIdFor("INDIVIDUAL"));
        assertNull(PartnerApplymentRules.settlementIdFor("MICRO"));
    }

    @Test
    @DisplayName("默认行业为居民生活服务，且免特殊资质")
    void defaultIndustrySupported() {
        assertEquals("居民生活服务", PartnerApplymentRules.DEFAULT_INDUSTRY);
        assertTrue(PartnerApplymentRules.SUPPORTED_INDUSTRIES.contains(
                PartnerApplymentRules.DEFAULT_INDUSTRY));
        assertFalse(PartnerApplymentRules.NEEDS_QUALIFICATION.contains(
                PartnerApplymentRules.DEFAULT_INDUSTRY));
    }

    @Test
    @DisplayName("0.3% 专项行业（快递/物流/加油/民办学校）不在平台受理范围")
    void specialRuleIndustriesRejected() {
        assertTrue(PartnerApplymentRules.SPECIAL_RULE_INDUSTRIES.contains("快递"));
        assertFalse(PartnerApplymentRules.SUPPORTED_INDUSTRIES.contains("快递"));
        assertFalse(PartnerApplymentRules.SUPPORTED_INDUSTRIES.contains("加油/加气"));
        assertFalse(PartnerApplymentRules.SUPPORTED_INDUSTRIES.contains("物流"));
    }

    @Test
    @DisplayName("其他缴费需特殊资质")
    void otherPaymentNeedsQualification() {
        assertTrue(PartnerApplymentRules.SUPPORTED_INDUSTRIES.contains("其他缴费"));
        assertTrue(PartnerApplymentRules.NEEDS_QUALIFICATION.contains("其他缴费"));
    }
}

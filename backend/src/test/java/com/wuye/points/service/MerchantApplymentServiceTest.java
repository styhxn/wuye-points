package com.wuye.points.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 进件申请状态映射回归测试（本地状态机与微信 APPLYMENT_STATE_* 对齐）。
 */
class MerchantApplymentServiceTest {

    @Test
    @DisplayName("微信申请状态映射：完成/待签约/待账户验证/驳回")
    void localStateMapping() {
        assertEquals("finished", MerchantApplymentService.localStateOf("APPLYMENT_STATE_FINISHED", "auditing"));
        assertEquals("signing", MerchantApplymentService.localStateOf("APPLYMENT_STATE_TO_BE_SIGNED", "auditing"));
        assertEquals("signing", MerchantApplymentService.localStateOf("APPLYMENT_STATE_SIGNING", "auditing"));
        assertEquals("legal_validating",
                MerchantApplymentService.localStateOf("APPLYMENT_STATE_TO_BE_CONFIRMED", "auditing"));
        assertEquals("rejected", MerchantApplymentService.localStateOf("APPLYMENT_STATE_REJECTED", "auditing"));
        assertEquals("auditing", MerchantApplymentService.localStateOf("APPLYMENT_STATE_AUDITING", "submitted"));
    }

    @Test
    @DisplayName("未知状态保持原值，避免微信新增状态导致状态机倒退")
    void unknownStateKeepsFallback() {
        assertEquals("auditing", MerchantApplymentService.localStateOf("APPLYMENT_STATE_FUTURE", "auditing"));
    }
}

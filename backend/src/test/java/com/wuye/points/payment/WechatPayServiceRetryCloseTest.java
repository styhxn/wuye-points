package com.wuye.points.payment;

import com.wuye.points.entity.PaymentRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PaymentRecordRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

/**
 * 二次支付前置关单 {@code closeForRetry} 的行为约束。
 *
 * <p>核心是最后那次 {@code flush()}：生产库唯一键
 * {@code uk_payment_records_biz_pending(biz_type, biz_id, active_flag)}（active_flag 为生成列，
 * status=pending 时取 '1'，否则 NULL）要求旧流水的 UPDATE 必须先落库，否则紧随其后的
 * pending INSERT 撞键——Hibernate 的刷写顺序是 INSERT 早于 UPDATE，2026-11-06 物业币充值
 * 二次支付线上 409 就是这个原因。H2 用例库没有该生成列，只能在这里把 flush 钉死。</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("二次支付：遗留待支付流水关单")
class WechatPayServiceRetryCloseTest {

    @Mock private WechatHttpClient partnerClient;
    @Mock private WechatHttpClient directClient;
    @Mock private WechatPayProperties partnerProps;
    @Mock private WechatDirectProperties directProps;
    @Mock private EcommerceSplitProperties ecommerceProps;
    @Mock private PaymentRecordRepository paymentRecordRepository;
    @Mock private SnowflakeIdGenerator idGenerator;

    private WechatPayService service;
    private PaymentRecord stale;

    @BeforeEach
    void setUp() {
        service = spy(new WechatPayService(partnerClient, directClient, partnerProps, directProps,
                ecommerceProps, paymentRecordRepository, idGenerator));
        stale = new PaymentRecord();
        stale.setId("pr_old");
        stale.setBizType(PaymentBizType.COIN_RECHARGE);
        stale.setBizId("res_1");
        stale.setStatus("pending");
        stale.setOutTradeNo("wxo_old");
    }

    @Test
    @DisplayName("旧流水实际已支付：抛「已支付」提示，不关单也不新建")
    void alreadyPaid_stopsBeforeClose() {
        doReturn("SUCCESS").when(service).query(stale);

        BizException ex = assertThrows(BizException.class, () -> service.closeForRetry(
                stale, "物业币充值", "上一笔充值已支付成功，正在到账"));

        assertEquals(ErrorCode.ORDER_CONFLICT.getCode(), ex.getCode());
        assertEquals("上一笔充值已支付成功，正在到账", ex.getMessage());
        verify(service, never()).close(any());
        verify(paymentRecordRepository, never()).flush();
    }

    @Test
    @DisplayName("旧流水未支付：关单后必须 flush，否则新建 pending 流水撞唯一键（线上 409 根因）")
    void unpaid_closesThenFlushesBeforeCallerInserts() {
        doReturn("NOTPAY").when(service).query(stale);
        doAnswerClose();

        service.closeForRetry(stale, "物业币充值", "上一笔充值已支付成功");

        verify(service).close(stale);
        verify(paymentRecordRepository).flush();
    }

    @Test
    @DisplayName("微信关单失败：本地置 closed 兜底，二次支付不被永久挡住")
    void closeFailure_fallsBackToLocalClosedAndFlushes() {
        doReturn("NOTPAY").when(service).query(stale);
        doThrow(new WechatPayException("WECHAT_CLOSE_FAIL", "微信关单失败", null))
                .when(service).close(any());

        service.closeForRetry(stale, "商城订单", "上一笔支付已成功");

        assertEquals("closed", stale.getStatus());
        verify(paymentRecordRepository).save(stale);
        verify(paymentRecordRepository).flush();
    }

    @Test
    @DisplayName("查单异常（未配置/网络）不阻断：继续走关单并 flush")
    void queryFailure_stillTriesToClose() {
        doThrow(new WechatPayException("WECHAT_QUERY_FAIL", "查单失败", null)).when(service).query(any());
        doAnswerClose();

        service.closeForRetry(stale, "物业币充值", "上一笔充值已支付成功");

        verify(service).close(stale);
        verify(paymentRecordRepository).flush();
    }

    /** 与真实 close 一致：成功后置 closed 并保存（真实实现内部即如此）。 */
    private void doAnswerClose() {
        Mockito.doAnswer(invocation -> {
            PaymentRecord record = invocation.getArgument(0);
            record.setStatus("closed");
            paymentRecordRepository.save(record);
            return null;
        }).when(service).close(stale);
    }
}

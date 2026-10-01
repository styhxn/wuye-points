package com.wuye.points.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.entity.MerchantWithdrawal;
import com.wuye.points.entity.WithdrawalDisbursement;
import com.wuye.points.entity.WithdrawalRecord;
import com.wuye.points.repository.WithdrawalDisbursementRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自动提现服务：替代现有「后台审批 + 财务手动转账 + 上传凭证」的人工打款。
 *
 * <p>启用条件：{@code wechat-split.enabled=true} 且商户参数已配置（即 {@link WechatSplitApiService#isEnabled()}）。
 * 未启用时 {@link #tryAutoWithdrawMerchant} / {@link #tryAutoWithdrawRole} 直接返回 null，
 * 调用方自动回退到原人工凭证流程，不影响现有业务逻辑。</p>
 *
 * <p>打款生命周期写入 {@code withdrawal_disbursements} 流水表（init→calling→success/failed），
 * 支持补偿调度与对账；原提现单通过 disburse_* 字段记录最终结果。扫描提（applycash）为异步，
 * 成功以流水表 trade_no + 异步回调/对账单为准。</p>
 *
 * <p>当前为框架占位：{@code applyCash} 的真实字段（接收方结算卡 / 微信 账号、cash_type 等）
 * 待微信提供真实参数与测试环境后补全。{@code enabled=false} 期间本服务不会发起任何远程调用。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoWithdrawalService {

    private final WechatSplitApiService wechatSplitApiService;
    private final WithdrawalDisbursementRepository disbursementRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String CHANNEL = "auto_wechat";

    /**
     * 商家提现自动打款。返回微信流水号；未启用时返回 null（调用方回退人工）。
     *
     * <p>资金接口 applycash 为「商户维度」提现：以商户号 merchant_no 的身份发起，
     * 资金从该商户号结算余额划出。业务参数按资金接口文档：amt（分）、fee_amt（分）、
     * zero_arr_type（1 普通提现）、cash_type（2 已结金额提现，免手续费 fee_amt=0）。</p>
     */
    public String tryAutoWithdrawMerchant(MerchantWithdrawal w) {
        if (!wechatSplitApiService.isEnabled()) {
            return null;
        }
        WithdrawalDisbursement d = buildDisbursement(
                w.getId(), "merchant", w.getActualAmount(), "merchant", w.getMerchantId());
        LinkedHashMap<String, Object> biz = new LinkedHashMap<>();
        biz.put("amt", WechatSplitApiService.fen(w.getActualAmount()));
        biz.put("fee_amt", "0");          // 提已结金额（cash_type=2）免手续费
        biz.put("zero_arr_type", "1");    // 1 普通提现
        biz.put("cash_type", "2");        // 2 已结金额提现
        // TODO 待补：确认每个商家是否分配独立微信商户号（merchant_no），否则无法以商户维度 applycash；
        //       若商家仅持 微信 虚拟账户，则应改走 微信 账户提现（账户余额→结算卡），待文档确认
        return doApplyCash(d, biz, w::setDisburseStatus, w::setDisburseTradeNo, w::setDisburseAt);
    }

    /**
     * 角色提现（个体负责人 / 快递员等）自动打款。当前<b>恒返回 null（回退人工打款）</b>。
     *
     * <p>⚠️ 注意：自然人一般无微信商户号，<b>不能</b>走资金接口 applycash（商户维度提现），
     * 否则会误用平台商户号提现平台资金。需改走 微信 账户体系
     * （开户→分账入 微信 账户→账户余额提现到绑定结算卡）或平台代付，
     * 接口与字段待微信文档确认后再实现（本方法保持返回 null，避免误接）。</p>
     */
    public String tryAutoWithdrawRole(WithdrawalRecord r) {
        return null;
    }

    private WithdrawalDisbursement buildDisbursement(String withdrawalId, String withdrawalType,
                                                    BigDecimal amount, String payeeType, String payeeId) {
        WithdrawalDisbursement d = new WithdrawalDisbursement();
        d.setId(idGenerator.generateId("wd_"));
        d.setWithdrawalId(withdrawalId);
        d.setWithdrawalType(withdrawalType);
        d.setChannel(CHANNEL);
        d.setAmount(amount);
        d.setPayeeType(payeeType);
        d.setPayeeId(payeeId);
        d.setStatus("init");
        d.setRetryCount(0);
        return d;
    }

    /**
     * 执行打款并落库。成功回写提现单状态；异常则标记失败（调用方仍按人工兜底或重试）。
     */
    private String doApplyCash(WithdrawalDisbursement d, Map<String, Object> biz,
                               java.util.function.Consumer<String> setStatus,
                               java.util.function.Consumer<String> setTradeNo,
                               java.util.function.Consumer<LocalDateTime> setAt) {
        d.setStatus("calling");
        d.setLastTriedAt(LocalDateTime.now());
        try {
            d.setRequestBody(objectMapper.writeValueAsString(biz));
        } catch (JsonProcessingException ignore) {
            d.setRequestBody(biz.toString());
        }
        disbursementRepository.save(d);

        try {
            Map<String, Object> resp = wechatSplitApiService.applyCash(biz);
            d.setResponseBody(toJson(resp));
            String tradeNo = String.valueOf(resp.getOrDefault("trade_no", resp.getOrDefault("pay_trace_no", "")));
            d.setTradeNo(tradeNo);
            d.setStatus("success");
            disbursementRepository.save(d);

            setStatus.accept("success");
            setTradeNo.accept(tradeNo);
            setAt.accept(LocalDateTime.now());
            return tradeNo;
        } catch (Exception e) {
            d.setStatus("failed");
            d.setErrorMsg(truncate(e.getMessage()));
            disbursementRepository.save(d);

            setStatus.accept("failed");
            setAt.accept(null);
            log.error("[AutoWithdraw] 自动打款失败 withdrawalId={} reason={}", d.getWithdrawalId(), e.getMessage());
            // 返回 null：调用方（审核完成分支）按当前设计仍标记提现为 completed（财务可后续人工补打）
            return null;
        }
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            return String.valueOf(o);
        }
    }

    private String truncate(String s) {
        return s == null ? null : (s.length() > 500 ? s.substring(0, 500) : s);
    }
}

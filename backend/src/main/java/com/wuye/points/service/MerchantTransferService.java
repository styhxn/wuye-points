package com.wuye.points.service;

import cn.hutool.json.JSONObject;
import com.wuye.points.entity.DistributionRecord;
import com.wuye.points.entity.MerchantTransfer;
import com.wuye.points.entity.MerchantTransferAccount;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.payment.EcommerceSplitProperties;
import com.wuye.points.payment.EcommerceTransferApiService;
import com.wuye.points.payment.WechatCertManager;
import com.wuye.points.payment.WechatNotifyService;
import com.wuye.points.payment.WechatPayProperties;
import com.wuye.points.repository.DistributionRecordRepository;
import com.wuye.points.repository.MerchantTransferAccountRepository;
import com.wuye.points.repository.MerchantTransferRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 四层树内部结算转账服务（微信商家转账，电商收付通阶段2）。
 *
 * <p>电商收付通模式下平台佣金经分账到平台商户号，本服务在分账完结后按
 * {@link DistributionRecord} 四层树金额生成转账计划（幂等），经 {@code merchant_transfer_accounts}
 * 校验收款账号后通过微信商家转账出款（默认微信零钱，可切银行卡）。</p>
 *
 * <p>状态机：pending → processing → success / failed / closed。
 * 转账失败（授权未完成/余额不足等）保持 pending 自动重试（上限 {@value #MAX_RETRY} 次），
 * 超时未终态由 {@code TransferScheduler} 查单推进，结果也可经异步通知回调落地。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantTransferService {

    /** 四层树角色类型。 */
    public static final Set<String> OWNER_TYPES =
            Set.of("PROPERTY", "COORDINATOR", "SECTOR_LEADER", "INDIVIDUAL_LEADER");

    /** 单笔转账最大自动重试次数（超过后置 failed 待人工处理）。 */
    public static final int MAX_RETRY = 5;

    /** 已发起（processing）但超时未终态的转账单查单间隔。 */
    public static final long QUERY_AFTER_MINUTES = 15;

    private final MerchantTransferRepository transferRepository;
    private final MerchantTransferAccountRepository accountRepository;
    private final DistributionRecordRepository distributionRecordRepository;
    private final EcommerceTransferApiService transferApiService;
    private final EcommerceSplitProperties ecommerceProps;
    private final WechatPayProperties wechatPayProperties;
    private final WechatNotifyService wechatNotifyService;
    private final WechatCertManager certManager;
    private final SnowflakeIdGenerator idGenerator;

    // ---------------------------------------------------------------------
    // 转账计划生成
    // ---------------------------------------------------------------------

    /**
     * 分账完结（平台佣金到账）后调用：按订单四层树金额生成转账计划（幂等）。
     * 由 {@code WechatSplitService.ecommerceFinish} 在完结成功后触发。
     */
    @Transactional
    public void planForFinishedOrder(String orderId) {
        if (!ecommerceProps.isEnabled() || !StringUtils.hasText(orderId)) {
            return;
        }
        DistributionRecord dist = distributionRecordRepository.findFirstByOrderId(orderId).orElse(null);
        if (dist == null) {
            return;
        }
        plan(dist, "PROPERTY", dist.getPropertyCompanyId(), dist.getPropertyShare());
        plan(dist, "COORDINATOR", dist.getCoordinatorId(), dist.getCoordinatorShare());
        plan(dist, "SECTOR_LEADER", dist.getSectorLeaderId(), dist.getSectorLeaderShare());
        plan(dist, "INDIVIDUAL_LEADER", dist.getIndividualLeaderId(), dist.getIndividualLeaderShare());
    }

    private void plan(DistributionRecord dist, String ownerType, String ownerId, BigDecimal amount) {
        if (!isPositive(amount) || !StringUtils.hasText(ownerId)) {
            return;
        }
        if (transferRepository.existsByBizIdAndOwnerType(dist.getOrderId(), ownerType)) {
            return; // 幂等：已生成过该角色计划
        }
        MerchantTransfer t = new MerchantTransfer();
        t.setId(idGenerator.generateId("mt"));
        t.setOwnerType(ownerType);
        t.setOwnerId(ownerId);
        t.setBizId(dist.getOrderId());
        t.setAmount(money(amount));
        t.setChannel(ecommerceProps.getTransferChannel());
        t.setStatus("pending");
        t.setRetryCount(0);
        transferRepository.save(t);
        log.info("[转账] 生成转账计划 orderId={} ownerType={} ownerId={} amount={}",
                dist.getOrderId(), ownerType, ownerId, amount);
    }

    // ---------------------------------------------------------------------
    // 转账执行（调度）
    // ---------------------------------------------------------------------

    /** 调度入口：推进 pending 转账（账号校验 → 微信出款）与 processing 超时查单。 */
    @Transactional
    public void processTransfers() {
        if (!ecommerceProps.isEnabled()) {
            return;
        }
        for (MerchantTransfer t : transferRepository.findByStatusIn(List.of("pending"))) {
            try {
                dispatch(t);
            } catch (Exception e) {
                log.error("[转账] 出款异常 transferId={}", t.getId(), e);
            }
        }
        LocalDateTime queryBefore = LocalDateTime.now().minusMinutes(QUERY_AFTER_MINUTES);
        for (MerchantTransfer t : transferRepository.findByStatusIn(List.of("processing"))) {
            if (t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(queryBefore)) {
                continue;
            }
            try {
                advanceByQuery(t);
            } catch (Exception e) {
                log.error("[转账] 查单异常 transferId={}", t.getId(), e);
            }
        }
    }

    private void dispatch(MerchantTransfer t) {
        if (t.getRetryCount() != null && t.getRetryCount() >= MAX_RETRY) {
            t.setStatus("failed");
            t.setFailReason("自动重试次数超限，请人工处理");
            transferRepository.save(t);
            return;
        }
        MerchantTransferAccount account = resolveAccount(t);
        if (account == null || !Boolean.TRUE.equals(account.getVerified())) {
            t.setFailReason("收款账号未绑定或未核验，待账号绑定后自动重试");
            transferRepository.save(t);
            return;
        }
        int fen = fen(t.getAmount());
        if (fen <= 0) {
            t.setStatus("failed");
            t.setFailReason("转账金额不合法（必须大于0）");
            transferRepository.save(t);
            return;
        }
        try {
            String billId;
            if ("zero".equals(t.getChannel())) {
                billId = transferApiService.transferToZero(t.getId(), account.getOpenid(), fen,
                        "物业积分内部结算", null);
            } else {
                // 银行卡密文基于绑定时的平台证书；证书轮换后旧密文不可用，需重新绑定后自动重试
                String currentSerial = certManager.anySerial();
                if (!StringUtils.hasText(account.getCertSerial())
                        || !account.getCertSerial().equals(currentSerial)) {
                    t.setFailReason("微信平台证书已轮换，请重新绑定收款银行卡后自动重试");
                    transferRepository.save(t);
                    log.warn("[转账] 银行卡证书不匹配（绑定证书 {}，当前 {}）transferId={}，等待重新绑定",
                            account.getCertSerial(), currentSerial, t.getId());
                    return;
                }
                billId = transferApiService.transferToBank(t.getId(), account.getCertSerial(),
                        account.getBankName(), account.getBankAccountNoEnc(),
                        account.getAccountNameEnc(), fen, "物业积分内部结算");
            }
            if (!StringUtils.hasText(billId)) {
                throw new BizException(ErrorCode.WECHAT_API_ERROR, "微信未返回 transfer_bill_id");
            }
            t.setWxTransferBillId(billId);
            t.setWxBatchId(billId);
            t.setStatus("processing");
            t.setPayeeAccount(maskPayee(t.getChannel(), account));
            t.setFailReason(null);
            transferRepository.save(t);
        } catch (Exception e) {
            // 保持 pending：授权/限额/账户问题在用户处理后下一周期可自动恢复
            t.setRetryCount((t.getRetryCount() == null ? 0 : t.getRetryCount()) + 1);
            t.setFailReason(truncate(e.getMessage()));
            transferRepository.save(t);
            log.warn("[转账] 出款失败（待重试）transferId={} retry={}", t.getId(), t.getRetryCount(), e);
        }
    }

    private void advanceByQuery(MerchantTransfer t) {
        JSONObject wx = transferApiService.queryTransfer(t.getId());
        String state = wx.getStr("state");
        String failReason = wx.getStr("fail_reason");
        if ("SUCCESS".equals(state)) {
            t.setStatus("success");
            t.setFailReason(null);
        } else if ("FAIL".equals(state) || "CANCELING".equals(state) || "CLOSED".equals(state)) {
            t.setStatus("failed");
            t.setFailReason(StringUtils.hasText(failReason) ? failReason : "微信转账失败:" + state);
        }
        // WAIT_PAY/SENDING 等中间态保持 processing，下轮再查
        if (t.getStatus().equals("failed") || t.getStatus().equals("success")) {
            log.info("[转账] 查单终态 transferId={} state={}", t.getId(), state);
        }
        transferRepository.save(t);
    }

    /** 转账结果异步通知（对应 /api/v1/payment/ecommerce-transfer/notify）。 */
    @Transactional
    public Map<String, String> handleTransferNotify(String rawBody, HttpHeaders headers) {
        try {
            Map<String, Object> decoded = wechatNotifyService.decrypt(rawBody, headers);
            String outBillNo = (String) decoded.get("out_bill_no");
            String state = (String) decoded.get("state");
            String failReason = (String) decoded.get("fail_reason");
            if (!StringUtils.hasText(outBillNo)) {
                return Map.of("code", "SUCCESS", "message", "缺少 out_bill_no");
            }
            MerchantTransfer t = transferRepository.findById(outBillNo).orElse(null);
            if (t == null) {
                log.warn("[转账回调] 无对应转账单 out_bill_no={}", outBillNo);
                return Map.of("code", "SUCCESS", "message", "无对应记录");
            }
            if ("SUCCESS".equals(state)) {
                t.setStatus("success");
                t.setFailReason(null);
            } else if ("FAIL".equals(state)) {
                t.setStatus("failed");
                t.setFailReason(StringUtils.hasText(failReason) ? failReason : "微信转账失败");
            }
            transferRepository.save(t);
            log.info("[转账回调] out_bill_no={} state={}", outBillNo, state);
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (Exception e) {
            log.error("[转账回调] 处理异常", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // 辅助
    // ---------------------------------------------------------------------

    private MerchantTransferAccount resolveAccount(MerchantTransfer t) {
        return accountRepository.findByOwnerTypeAndOwnerIdAndChannel(
                t.getOwnerType(), t.getOwnerId(), t.getChannel()).orElse(null);
    }

    private static String maskPayee(String channel, MerchantTransferAccount account) {
        if ("bank".equals(channel) && StringUtils.hasText(account.getBankAccountNoEnc())) {
            String raw = account.getBankAccountNoEnc();
            return raw.length() > 4 ? "****" + raw.substring(raw.length() - 4) : "****";
        }
        if (StringUtils.hasText(account.getOpenid())) {
            String o = account.getOpenid();
            return o.length() > 8 ? o.substring(0, 4) + "****" + o.substring(o.length() - 4) : "****";
        }
        return null;
    }

    private static int fen(BigDecimal yuan) {
        if (yuan == null) {
            return 0;
        }
        return yuan.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private static BigDecimal money(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isPositive(BigDecimal v) {
        return v != null && v.compareTo(BigDecimal.ZERO) > 0;
    }

    private static String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}

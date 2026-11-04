package com.wuye.points.scheduler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.payment.WechatSplitApiService;
import com.wuye.points.payment.WechatSplitService;
import com.wuye.points.repository.WechatSplitRecordRepository;
import com.wuye.points.service.WechatSplitNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分账查单对账调度（普通分账 /v3/profitsharing 的结果落地）。
 *
 * <p><b>解决的问题（P0-1）</b>：微信不推送普通分账的异步结果，{@code WechatSplitNotifyService}
 * 的状态映射此前无任何生产调用方，本地 {@code frozen} 记录与微信侧实际状态可能背离，
 * 而 {@code SplitWindowScheduler} 只读本地状态就盲发完结。本调度定时查单，把微信侧
 * PROCESSING / FINISHED / CLOSED 经状态映射落地，CLOSED 落 freeze_failed 交由
 * {@link SplitFailureScheduler} 补偿重试。</p>
 *
 * <p><b>金额核对（外部对账最小闭环，P2-9 部分实现）</b>：比对微信侧各接收方实分金额与
 * 本地冻结金额，差异记 ERROR 日志待人工核查。完整的微信对账文件下载比对仍未实现，
 * 见 {@code SplitWindowScheduler#reconcileDaily} 的 TODO。</p>
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SplitReconcileScheduler {

    /** 冻结后 10 分钟才查单，避免与冻结请求竞态（微信侧异步处理中）。 */
    private static final long QUERY_DELAY_MINUTES = 10;

    private final WechatSplitRecordRepository splitRecordRepository;
    private final WechatSplitService wechatSplitService;
    private final WechatSplitApiService wechatSplitApiService;

    @Scheduled(cron = "0 10/30 * * * ?")
    @SchedulerLock(name = "split_reconcile_query", lockAtMostFor = "PT20M", lockAtLeastFor = "PT1M")
    public void reconcileByQuery() {
        if (!wechatSplitService.isEnabled() || !wechatSplitApiService.isEnabled()) {
            return;
        }
        List<WechatSplitRecord> frozen = splitRecordRepository.findByStatus("frozen");
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(QUERY_DELAY_MINUTES);
        for (WechatSplitRecord split : frozen) {
            if (!StringUtils.hasText(split.getSplitNo()) || !StringUtils.hasText(split.getWechatOutTradeNo())) {
                continue;
            }
            // 电商收付通分账走 /v3/ecommerce/profitsharing，不能拿普通分账查单接口对账
            if (wechatSplitService.isEcommerceSplitRecord(split)) {
                continue;
            }
            // 刚冻结的记录跳过（微信侧可能仍在处理），老记录才查单
            if (split.getCreatedAt() != null && split.getCreatedAt().isAfter(cutoff)) {
                continue;
            }
            try {
                // 查单自动附带该笔分账的 sub_mchid（服务商分账必需，直连模式为空）
                JSONObject wx = wechatSplitService.querySplitResult(split);
                if (wx == null) {
                    continue;
                }
                // 官方「查询分账结果」应答的状态字段为 status（PROCESSING/FINISHED），
                // 兼容历史误读的 state 写法，避免旧数据/文档混用导致对账静默失效
                String wxStatus = wx.getStr("status");
                if (!StringUtils.hasText(wxStatus)) {
                    wxStatus = wx.getStr("state");
                }
                if (!StringUtils.hasText(wxStatus)) {
                    continue;
                }
                syncStatus(split, wxStatus);
                reconcileAmount(split, wx);
            } catch (Exception e) {
                log.error("[分账对账] 查单处理异常 splitNo={}", split.getSplitNo(), e);
            }
        }
    }

    /** 微信状态 → 本地状态落地（终态记录不被改写）。 */
    private void syncStatus(WechatSplitRecord split, String wxStatus) {
        if (WechatSplitNotifyService.isTerminal(split.getStatus())) {
            return;
        }
        String local = WechatSplitNotifyService.mapSplitStatus(wxStatus);
        if (local.equals(split.getStatus())) {
            return;
        }
        log.warn("[分账对账] 状态修正 splitNo={} 本地 {} → 微信 {} → {}",
                split.getSplitNo(), split.getStatus(), wxStatus, local);
        split.setStatus(local);
        if ("freeze_failed".equals(local)) {
            split.setRemark("微信侧分账已关闭(CLOSED)，待补偿重试");
        }
        splitRecordRepository.save(split);
    }

    /** 金额核对：微信侧本接收方实分金额（分） vs 本地冻结金额（分）。 */
    private void reconcileAmount(WechatSplitRecord split, JSONObject wx) {
        JSONArray receivers = wx.getJSONArray("receivers");
        if (receivers == null || !StringUtils.hasText(split.getPayeeAccount())) {
            return;
        }
        for (int i = 0; i < receivers.size(); i++) {
            JSONObject r = receivers.getJSONObject(i);
            if (!split.getPayeeAccount().equals(r.getStr("account"))) {
                continue;
            }
            long wxFen = r.getLong("amount", 0L);
            long localFen = Long.parseLong(WechatSplitApiService.fen(split.getFreezeAmount()));
            if (wxFen != localFen) {
                log.error("[分账对账][金额差异] splitNo={} account={} 微信={}分 本地={}分，需人工核查",
                        split.getSplitNo(), split.getPayeeAccount(), wxFen, localFen);
            }
        }
    }
}

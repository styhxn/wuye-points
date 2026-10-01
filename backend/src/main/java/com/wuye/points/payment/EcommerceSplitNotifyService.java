package com.wuye.points.payment;

import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.repository.WechatSplitRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 电商收付通分账结果回调处理（POST /v3/ecommerce/profitsharing/orders 的异步通知）。
 *
 * <p>请求分账接口多数时候同步返回结果，回调作为异步兜底：{@code status=SUCCESS} 时把同
 * {@code out_order_no} 下 {@code freeze_failed} 的记录修正为 {@code frozen}（防误判失败重放）；
 * {@code status=FAIL} 时把 {@code frozen} 记录置 {@code freeze_failed}，交由
 * {@link SplitFailureScheduler} 补偿重试。</p>
 *
 * <p>验签解密复用 {@link WechatNotifyService}（支付/分账/提现共用 APIv3 通知规范）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EcommerceSplitNotifyService {

    private final WechatNotifyService wechatNotifyService;
    private final WechatSplitRecordRepository splitRecordRepository;

    /**
     * @return 返给微信的应答体：SUCCESS 返回 200，FAIL 返回 500 由微信重推
     */
    public Map<String, String> handleSplitNotify(String rawBody, HttpHeaders headers) {
        try {
            Map<String, Object> decoded = wechatNotifyService.decrypt(rawBody, headers);
            String outOrderNo = (String) decoded.get("out_order_no");
            String subMchid = (String) decoded.get("sub_mchid");
            String orderId = (String) decoded.get("order_id");
            String status = (String) decoded.get("status");
            log.info("[电商分账回调] out_order_no={} sub_mchid={} order_id={} status={}",
                    outOrderNo, subMchid, orderId, status);

            if (outOrderNo == null) {
                return Map.of("code", "SUCCESS", "message", "缺少 out_order_no");
            }
            List<WechatSplitRecord> records = splitRecordRepository.findByGroupNo(outOrderNo);
            if (records.isEmpty()) {
                log.warn("[电商分账回调] 无对应分账记录 out_order_no={}", outOrderNo);
                return Map.of("code", "SUCCESS", "message", "无对应记录");
            }

            if ("SUCCESS".equalsIgnoreCase(status)) {
                for (WechatSplitRecord s : records) {
                    if ("freeze_failed".equals(s.getStatus())) {
                        s.setStatus("frozen");
                        s.setWechatSubTradeNo(orderId);
                        s.setRemark(null);
                        splitRecordRepository.save(s);
                    }
                }
            } else if ("FAIL".equalsIgnoreCase(status)) {
                for (WechatSplitRecord s : records) {
                    if ("frozen".equals(s.getStatus())) {
                        s.setStatus("freeze_failed");
                        s.setRemark("电商分账回调返回失败，待补偿重试");
                        splitRecordRepository.save(s);
                    }
                }
            }
            return Map.of("code", "SUCCESS", "message", "成功");
        } catch (WechatNotifyVerifyException e) {
            log.warn("[电商分账回调] 验签/解密失败: {}", e.getMessage());
            return Map.of("code", "FAIL", "message", e.getMessage());
        } catch (Exception e) {
            log.error("[电商分账回调] 处理异常", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }
}

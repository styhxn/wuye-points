package com.wuye.points.service;

import com.wuye.points.constant.FulfillmentConstants;
import com.wuye.points.entity.FulfillmentChoiceLog;
import com.wuye.points.repository.FulfillmentChoiceLogRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 履约方式选择审计留痕服务。
 *
 * <p>所有履约方式变更（商家选择 / 超时兜底 / 管理员覆盖）均通过本服务写入
 * {@code fulfillment_choice_logs}，与主业务事务解耦（REQUIRES_NEW），
 * 确保即便主流程回滚也能保留操作记录。
 */
@Service
@RequiredArgsConstructor
public class FulfillmentAuditService {

    private final FulfillmentChoiceLogRepository fulfillmentChoiceLogRepository;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String orderId,
                       String merchantId,
                       String actorType,
                       String actorId,
                       String action,
                       String beforeMode,
                       String afterMode,
                       String remark,
                       String deliveryId) {
        FulfillmentChoiceLog log = new FulfillmentChoiceLog();
        log.setId(idGenerator.generateId("fcl"));
        log.setOrderId(orderId);
        log.setMerchantId(merchantId);
        log.setActorType(actorType);
        log.setActorId(actorId);
        log.setAction(action);
        log.setBeforeMode(beforeMode);
        log.setAfterMode(afterMode);
        log.setRemark(remark);
        log.setDeliveryId(deliveryId);
        log.setChoiceAt(LocalDateTime.now());
        fulfillmentChoiceLogRepository.save(log);
    }

    /** 便捷方法：商家主动选择 */
    public void recordMerchantChoose(String orderId, String merchantId, String beforeMode,
                                     String afterMode, String deliveryId) {
        record(orderId, merchantId, "merchant", merchantId, FulfillmentConstants.AuditAction.CHOOSE,
                beforeMode, afterMode, null, deliveryId);
    }

    /** 便捷方法：超时自动兜底 */
    public void recordTimeout(String orderId, String merchantId, String fallbackMode, String deliveryId) {
        record(orderId, merchantId, "system", "system", FulfillmentConstants.AuditAction.TIMEOUT,
                FulfillmentConstants.FULFILLMENT_MODE_PENDING_CHOICE, fallbackMode,
                "超过履约选择时限，默认" + (FulfillmentConstants.FULFILLMENT_MODE_COURIER_HALL.equals(fallbackMode)
                        ? "平台配送" : "商家自配"), deliveryId);
    }

    /** 便捷方法：管理员强制覆盖 */
    public void recordOverride(String orderId, String merchantId, String adminId,
                               String beforeMode, String afterMode, String deliveryId, String remark) {
        record(orderId, merchantId, "admin", adminId, FulfillmentConstants.AuditAction.OVERRIDE,
                beforeMode, afterMode, remark, deliveryId);
    }
}

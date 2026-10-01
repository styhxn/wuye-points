package com.wuye.points.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.entity.ArrearsReminderAudit;
import com.wuye.points.entity.ArrearsReminderPreview;
import com.wuye.points.entity.ArrearsReminderRequest;
import com.wuye.points.repository.ArrearsReminderAuditRepository;
import com.wuye.points.repository.ArrearsReminderPreviewRepository;
import com.wuye.points.repository.ArrearsReminderRequestRepository;
import com.wuye.points.util.SensitiveMasker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 欠费催缴的运行时状态存储（持久化版本）。
 * <p>
 * 预览快照、发送幂等（requestId）与审计记录均落库，应用重启不丢失。
 * 公共 API 与内存版本保持一致，调用方（PropertyFeeService）无需改动。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArrearsReminderStateStore {

    /** 预览快照有效期（分钟） */
    private static final int PREVIEW_TTL_MINUTES = 30;

    private final ArrearsReminderPreviewRepository previewRepository;
    private final ArrearsReminderRequestRepository requestRepository;
    private final ArrearsReminderAuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    /** 保存一次预览快照（内部生成 token 与过期时间），返回可直接下发的快照对象 */
    public PreviewSnapshot savePreview(String propertyCompanyId, String communityId, String building,
                                       String feeType, List<String> residentIds, BigDecimal totalArrearsAmount) {
        String token = "prev_" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(PREVIEW_TTL_MINUTES);
        ArrearsReminderPreview entity = new ArrearsReminderPreview();
        entity.setToken(token);
        entity.setPropertyCompanyId(propertyCompanyId);
        entity.setCommunityId(communityId);
        entity.setBuilding(building);
        entity.setFeeType(feeType);
        entity.setResidentIds(toJson(residentIds));
        entity.setRecipientCount(residentIds == null ? 0 : residentIds.size());
        entity.setTotalArrearsAmount(totalArrearsAmount);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(LocalDateTime.now());
        previewRepository.save(entity);
        log.info("savePreview 快照已写入数据库 token={} expiresAt={} recipientCount={}",
                SensitiveMasker.maskSecret(token), expiresAt, entity.getRecipientCount());
        return toSnapshot(entity);
    }

    /** 按 token 消费预览快照；不存在或已过期抛出 PREVIEW_EXPIRED */
    public PreviewSnapshot consumePreview(String token) {
        if (token == null || token.isBlank()) {
            log.warn("consumePreview 收到空 token，可能前端未正确回传 previewToken");
            throw new BizException(ErrorCode.PREVIEW_EXPIRED);
        }
        ArrearsReminderPreview entity = previewRepository.findById(token).orElse(null);
        if (entity == null) {
            log.warn("consumePreview token={} 在表中不存在（预览可能未落库或已被消费）",
                    SensitiveMasker.maskSecret(token));
            throw new BizException(ErrorCode.PREVIEW_EXPIRED);
        }
        LocalDateTime now = LocalDateTime.now();
        if (entity.getExpiresAt().isBefore(now)) {
            log.warn("consumePreview token={} 已过期 expiresAt={} now={}",
                    SensitiveMasker.maskSecret(token), entity.getExpiresAt(), now);
            previewRepository.delete(entity);
            throw new BizException(ErrorCode.PREVIEW_EXPIRED);
        }
        log.info("consumePreview token={} 命中并消费 expiresAt={} recipientCount={}",
                SensitiveMasker.maskSecret(token), entity.getExpiresAt(), entity.getRecipientCount());
        previewRepository.delete(entity);
        return toSnapshot(entity);
    }

    /** 判断 requestId 是否已处理（幂等） */
    public boolean isProcessed(String requestId) {
        return requestRepository.existsById(requestId);
    }

    /** 标记 requestId 已处理 */
    public void markProcessed(String requestId) {
        ArrearsReminderRequest entity = new ArrearsReminderRequest();
        entity.setRequestId(requestId);
        entity.setCreatedAt(LocalDateTime.now());
        requestRepository.save(entity);
    }

    /** 写入一条审计 / 批次结果记录 */
    @SuppressWarnings("unchecked")
    public void recordAudit(Map<String, Object> entry) {
        ArrearsReminderAudit audit = new ArrearsReminderAudit();
        // 主键兜底：若调用方未携带 id，则本地生成，避免插入 null 主键触发 DataAccessException。
        String auditId = asString(entry.get("id"));
        if (auditId == null || auditId.isEmpty()) {
            auditId = "audit_" + System.nanoTime() + "_" + Thread.currentThread().getId();
        }
        audit.setId(auditId);
        audit.setBatchId(asString(entry.get("batchId")));
        audit.setRequestId(asString(entry.get("requestId")));
        audit.setPropertyCompanyId(asString(entry.get("propertyCompanyId")));
        audit.setCommunityId(asString(entry.get("communityId")));
        audit.setBuilding(asString(entry.get("building")));
        audit.setFeeType(asString(entry.get("feeType")));
        audit.setChannels(asString(entry.get("channels")));
        audit.setRecipientCount(toInt(entry.get("recipientCount")));
        audit.setNotifiedCount(toInt(entry.get("notifiedCount")));
        audit.setSkippedCount(toInt(entry.get("skippedCount")));
        audit.setFailedCount(toInt(entry.get("failedCount")));
        audit.setInAppSuccess(toInt(entry.get("inAppSuccess")));
        audit.setInAppFailed(toInt(entry.get("inAppFailed")));
        audit.setWechatSuccess(toInt(entry.get("wechatSuccess")));
        audit.setWechatSkipped(toInt(entry.get("wechatSkipped")));
        audit.setWechatFailed(toInt(entry.get("wechatFailed")));
        audit.setTotalArrearsAmount((BigDecimal) entry.get("totalArrearsAmount"));
        audit.setOperator(asString(entry.get("operator")));
        audit.setCreatedAt(LocalDateTime.now());
        auditRepository.save(audit);
    }

    // ===================== 内部转换 =====================

    private PreviewSnapshot toSnapshot(ArrearsReminderPreview e) {
        return new PreviewSnapshot(e.getToken(), e.getPropertyCompanyId(), e.getCommunityId(),
                e.getBuilding(), e.getFeeType(), fromJson(e.getResidentIds()),
                e.getRecipientCount(), e.getTotalArrearsAmount(), e.getExpiresAt());
    }

    private String toJson(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids == null ? Collections.emptyList() : new ArrayList<>(ids));
        } catch (Exception ex) {
            throw new IllegalStateException("序列化 residentIds 失败", ex);
        }
    }

    private List<String> fromJson(String s) {
        if (s == null || s.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(s, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private static String asString(Object o) {
        return o == null ? null : o.toString();
    }

    private static int toInt(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** 预览快照值对象（与消费方约定保持一致） */
    @Getter
    public static class PreviewSnapshot {
        private final String token;
        private final String propertyCompanyId;
        private final String communityId;
        private final String building;
        private final String feeType;
        private final List<String> residentIds;
        private final int recipientCount;
        private final BigDecimal totalArrearsAmount;
        private final LocalDateTime expiresAt;

        public PreviewSnapshot(String token, String propertyCompanyId, String communityId, String building,
                               String feeType, List<String> residentIds, int recipientCount,
                               BigDecimal totalArrearsAmount, LocalDateTime expiresAt) {
            this.token = token;
            this.propertyCompanyId = propertyCompanyId;
            this.communityId = communityId;
            this.building = building;
            this.feeType = feeType;
            this.residentIds = residentIds;
            this.recipientCount = recipientCount;
            this.totalArrearsAmount = totalArrearsAmount;
            this.expiresAt = expiresAt;
        }
    }
}

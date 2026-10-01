package com.wuye.points.service;

import com.wuye.points.dto.admin.recovery.RecoveryRecordResponse;
import com.wuye.points.dto.admin.recovery.UpdateRecoveryRequest;
import com.wuye.points.dto.common.PageRequest;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.SplitRecoveryRecord;
import com.wuye.points.entity.WechatSplitRecord;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.SplitRecoveryRecordRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 分账回退待追回台账服务（阶段2）。
 *
 * <p>电商收付通分账后退款：个人接收方（快递员 PERSONAL_OPENID）不支持微信回退，
 * 由平台先行垫付并登记本台账（{@code split_recovery_records}），运营线下追回后核销。
 * 登记入口由 {@code WechatSplitService.retryReverse} 调用（幂等：同分账记录只记一次）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SplitRecoveryService {

    private final SplitRecoveryRecordRepository recoveryRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final RoleGuard roleGuard;

    /**
     * 登记个人接收方不可回退的待追回记录（幂等）。由分账回退链路在事务内调用。
     */
    @Transactional
    public void recordPersonalReverse(WechatSplitRecord split) {
        if (split == null || split.getId() == null) {
            return;
        }
        if (recoveryRepository.findBySplitRecordId(split.getId()).isPresent()) {
            return;
        }
        SplitRecoveryRecord r = new SplitRecoveryRecord();
        r.setId(idGenerator.generateId("srr"));
        r.setSplitRecordId(split.getId());
        r.setOrderId(split.getOrderId());
        r.setOwnerType(split.getOwnerType());
        r.setOwnerId(split.getOwnerId());
        r.setAmount(split.getFreezeAmount());
        r.setStatus("pending");
        r.setRemark("个人接收方不支持微信回退，平台垫付待追回");
        recoveryRepository.save(r);
        log.warn("[回退台账] 登记待追回 splitId={} orderId={} ownerType={} ownerId={} amount={}",
                split.getId(), split.getOrderId(), split.getOwnerType(), split.getOwnerId(), split.getFreezeAmount());
    }

    /** 台账分页查询（管理端）。 */
    public PageResponse<RecoveryRecordResponse> list(PageRequest pageRequest, String status, String ownerType) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Specification<SplitRecoveryRecord> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (StringUtils.hasText(status)) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (StringUtils.hasText(ownerType)) {
                ps.add(cb.equal(root.get("ownerType"), ownerType));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        Page<SplitRecoveryRecord> page = recoveryRepository.findAll(spec, pageRequest.toPageable());
        return PageResponse.from(page.map(this::toResponse));
    }

    /** 标记已追回（线下追回成功）。 */
    @Transactional
    public RecoveryRecordResponse markRecovered(String id, UpdateRecoveryRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        SplitRecoveryRecord r = requireRecord(id);
        if (!"pending".equals(r.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "仅待追回(pending)状态可标记已追回");
        }
        r.setStatus("recovered");
        r.setRemark(mergeRemark(r.getRemark(), request, "已线下追回"));
        recoveryRepository.save(r);
        log.info("[回退台账] 已追回 id={} orderId={}", id, r.getOrderId());
        return toResponse(r);
    }

    /** 核销（确认无法追回，做坏账处理）。 */
    @Transactional
    public RecoveryRecordResponse writeOff(String id, UpdateRecoveryRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        SplitRecoveryRecord r = requireRecord(id);
        if (!"pending".equals(r.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "仅待追回(pending)状态可核销");
        }
        r.setStatus("written_off");
        r.setRemark(mergeRemark(r.getRemark(), request, "已核销"));
        recoveryRepository.save(r);
        log.info("[回退台账] 已核销 id={} orderId={}", id, r.getOrderId());
        return toResponse(r);
    }

    // ---------------------------------------------------------------------
    // 辅助
    // ---------------------------------------------------------------------

    private SplitRecoveryRecord requireRecord(String id) {
        return recoveryRepository.findById(id).orElseThrow(
                () -> new BizException(ErrorCode.RECOVERY_NOT_FOUND));
    }

    private static String mergeRemark(String current, UpdateRecoveryRequest request, String action) {
        String addition = request != null ? request.getRemark() : null;
        if (StringUtils.hasText(addition)) {
            return action + "：" + addition.trim();
        }
        return action;
    }

    private RecoveryRecordResponse toResponse(SplitRecoveryRecord r) {
        return RecoveryRecordResponse.builder()
                .id(r.getId())
                .splitRecordId(r.getSplitRecordId())
                .orderId(r.getOrderId())
                .ownerType(r.getOwnerType())
                .ownerId(r.getOwnerId())
                .amount(r.getAmount())
                .status(r.getStatus())
                .remark(r.getRemark())
                .createdAt(r.getCreatedAt())
                .build();
    }
}

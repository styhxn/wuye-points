package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.communitypoint.CommunityPointPoolResponse;
import com.wuye.points.dto.communitypoint.CommunityPointRecordItemResponse;
import com.wuye.points.entity.Community;
import com.wuye.points.entity.CommunityPointPool;
import com.wuye.points.entity.CommunityPointRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityPointPoolRepository;
import com.wuye.points.repository.CommunityPointRecordRepository;
import com.wuye.points.repository.CommunityRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.FeatureSwitchGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 小区建设积分（集体积分）服务
 */
@Service
@RequiredArgsConstructor
public class CommunityPointPoolService {

    private static final String SOURCE_ORDER_CONTRIBUTION = "order_contribution";
    private static final String SOURCE_MANUAL_ADJUST = "manual_adjust";

    private final CommunityPointPoolRepository communityPointPoolRepository;
    private final CommunityPointRecordRepository communityPointRecordRepository;
    private final CommunityRepository communityRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final FeatureSwitchGuard featureSwitchGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public CommunityPointPoolResponse getPool(String communityId) {
        validateCommunityId(communityId);
        roleGuard.requireAnyRole("resident", "property_admin", "platform_admin");
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        if (!roleGuard.isPlatformAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(community.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }
        CommunityPointPool pool = communityPointPoolRepository.findByCommunityId(communityId)
                .orElse(null);
        if (pool == null) {
            return CommunityPointPoolResponse.builder()
                    .communityId(communityId)
                    .propertyCompanyId(community.getPropertyCompanyId())
                    .balance(BigDecimal.ZERO)
                    .totalIn(BigDecimal.ZERO)
                    .totalOut(BigDecimal.ZERO)
                    .updatedAt(null)
                    .build();
        }
        return toPoolResponse(pool);
    }

    @Transactional(readOnly = true)
    public List<CommunityPointPoolResponse> listMyCommunityPools() {
        roleGuard.requireAnyRole("resident", "property_admin", "platform_admin");
        String currentUserId = UserContext.currentUserId();
        if (currentUserId == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }

        if (roleGuard.isPlatformAdmin()) {
            List<CommunityPointPool> pools = communityPointPoolRepository.findAll();
            return pools.stream().map(this::toPoolResponse).toList();
        }

        Resident resident = residentRepository.findById(currentUserId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));

        if (roleGuard.isPropertyAdmin()) {
            String propertyCompanyId = resident.getPropertyCompanyId();
            if (!StringUtils.hasText(propertyCompanyId)) {
                return List.of();
            }
            return communityPointPoolRepository.findByPropertyCompanyId(propertyCompanyId)
                    .stream().map(this::toPoolResponse).toList();
        }

        String communityId = resident.getCommunityId();
        if (!StringUtils.hasText(communityId)) {
            return List.of();
        }
        return communityPointPoolRepository.findByCommunityId(communityId)
                .stream().map(this::toPoolResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<CommunityPointRecordItemResponse> listRecords(
            String communityId, int page, int pageSize) {
        validateCommunityId(communityId);
        roleGuard.requireAnyRole("resident", "property_admin", "platform_admin");
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        if (!roleGuard.isPlatformAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(community.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED);
            }
        }
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<CommunityPointRecord> recordPage = communityPointRecordRepository
                .findByCommunityIdOrderByCreatedAtDesc(communityId, pageable);
        List<CommunityPointRecordItemResponse> list = recordPage.getContent().stream()
                .map(this::toRecordItem)
                .toList();
        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    /**
     * 注入建设积分（内部接口，给订单分成时调用）
     */
    @Transactional
    public CommunityPointRecord contribute(String communityId, BigDecimal amount, String remark, String orderId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "注入金额必须大于0");
        }
        validateCommunityId(communityId);
        CommunityPointPool pool = getOrCreatePoolWithLock(communityId);

        BigDecimal newBalance = defaultDecimal(pool.getBalance()).add(amount);
        pool.setBalance(newBalance);
        pool.setTotalIn(defaultDecimal(pool.getTotalIn()).add(amount));
        communityPointPoolRepository.save(pool);

        CommunityPointRecord record = new CommunityPointRecord();
        record.setId(idGenerator.generateId("cpr"));
        record.setCommunityId(communityId);
        record.setOrderId(orderId);
        record.setAmount(amount);
        record.setSource(SOURCE_ORDER_CONTRIBUTION);
        record.setRemark(remark);
        return communityPointRecordRepository.save(record);
    }

    /**
     * 物业管理员手动调整建设积分
     */
    @Transactional
    public CommunityPointRecordItemResponse adminAdjust(String communityId, BigDecimal amount, String remark) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "调整金额不能为0");
        }
        validateCommunityId(communityId);
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        // 5.1：积分功能关闭时拒绝建设积分调整
        featureSwitchGuard.assertPointEnabled(community.getPropertyCompanyId());

        if (!roleGuard.isPlatformAdmin()) {
            String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
            if (currentPropertyCompanyId == null
                    || !currentPropertyCompanyId.equals(community.getPropertyCompanyId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作该小区建设积分");
            }
        }

        CommunityPointPool pool = getOrCreatePoolWithLock(communityId);
        BigDecimal newBalance = defaultDecimal(pool.getBalance()).add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.POINT_POOL_INSUFFICIENT, "建设积分池余额不足");
        }

        pool.setBalance(newBalance);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            pool.setTotalIn(defaultDecimal(pool.getTotalIn()).add(amount));
        } else {
            pool.setTotalOut(defaultDecimal(pool.getTotalOut()).add(amount.abs()));
        }
        communityPointPoolRepository.save(pool);

        CommunityPointRecord record = new CommunityPointRecord();
        record.setId(idGenerator.generateId("cpr"));
        record.setCommunityId(communityId);
        record.setAmount(amount);
        record.setSource(SOURCE_MANUAL_ADJUST);
        record.setRemark(remark);
        CommunityPointRecord saved = communityPointRecordRepository.save(record);
        return toRecordItem(saved);
    }

    private CommunityPointPool getOrCreatePoolWithLock(String communityId) {
        return communityPointPoolRepository.findWithLockByCommunityId(communityId)
                .orElseGet(() -> createPool(communityId));
    }

    private CommunityPointPool createPool(String communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "小区不存在"));
        CommunityPointPool pool = new CommunityPointPool();
        pool.setId(idGenerator.generateId("cpp"));
        pool.setCommunityId(communityId);
        pool.setPropertyCompanyId(community.getPropertyCompanyId());
        pool.setBalance(BigDecimal.ZERO);
        pool.setTotalIn(BigDecimal.ZERO);
        pool.setTotalOut(BigDecimal.ZERO);
        return communityPointPoolRepository.save(pool);
    }

    private void validateCommunityId(String communityId) {
        if (!StringUtils.hasText(communityId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "小区ID不能为空");
        }
    }

    private CommunityPointPoolResponse toPoolResponse(CommunityPointPool pool) {
        return CommunityPointPoolResponse.builder()
                .id(pool.getId())
                .communityId(pool.getCommunityId())
                .propertyCompanyId(pool.getPropertyCompanyId())
                .balance(defaultDecimal(pool.getBalance()))
                .totalIn(defaultDecimal(pool.getTotalIn()))
                .totalOut(defaultDecimal(pool.getTotalOut()))
                .updatedAt(DateTimeUtil.toIsoUtc(pool.getUpdatedAt()))
                .build();
    }

    private CommunityPointRecordItemResponse toRecordItem(CommunityPointRecord record) {
        return CommunityPointRecordItemResponse.builder()
                .id(record.getId())
                .communityId(record.getCommunityId())
                .orderId(record.getOrderId())
                .amount(record.getAmount())
                .source(record.getSource())
                .remark(record.getRemark())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}

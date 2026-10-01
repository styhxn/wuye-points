package com.wuye.points.service;

import com.wuye.points.dto.admin.transfer.TransferToPropertyListItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PointRecordRepository;
import com.wuye.points.repository.PropertyCoinRecordRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端 — 住户「转给物业」流水查看（对账用，无需审批，即时扣款）。
 * 数据源为 point_records / property_coin_records 中 source = transfer_to_property 的记录。
 */
@Service
@RequiredArgsConstructor
public class AdminTransferToPropertyService {

    private static final String SOURCE_TRANSFER_TO_PROPERTY = "transfer_to_property";

    private final PointRecordRepository pointRecordRepository;
    private final PropertyCoinRecordRepository coinRecordRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;

    /**
     * 积分转物业流水列表。
     * Auth: Required | Roles: property_admin, platform_admin, coordinator, sector_leader, individual_leader
     */
    @Transactional(readOnly = true)
    public PageResponse<TransferToPropertyListItemResponse> listPointTransfers(
            int page, int pageSize, String propertyCompanyId, String residentId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader", "individual_leader");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        validateResidentId(residentId);

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<PointRecord> recordPage = pointRecordRepository.findAll(
                pointTransferSpec(scopedPropertyCompanyId, residentId), pageable);

        Map<String, Resident> residentMap = loadResidentMap(
                recordPage.getContent().stream().map(PointRecord::getResidentId).toList());

        List<TransferToPropertyListItemResponse> list = recordPage.getContent().stream()
                .map(record -> toItem("point", record.getId(), record.getResidentId(),
                        record.getAmount() != null ? BigDecimal.valueOf(record.getAmount()) : BigDecimal.ZERO,
                        record.getDescription(), record.getPropertyCompanyId(), record.getCreatedAt(),
                        residentMap.get(record.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    /**
     * 物业币转物业流水列表。
     * Auth: Required | Roles: property_admin, platform_admin, coordinator, sector_leader, individual_leader
     */
    @Transactional(readOnly = true)
    public PageResponse<TransferToPropertyListItemResponse> listCoinTransfers(
            int page, int pageSize, String propertyCompanyId, String residentId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader", "individual_leader");
        String scopedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);
        validateResidentId(residentId);

        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<PropertyCoinRecord> recordPage = coinRecordRepository.findAll(
                coinTransferSpec(scopedPropertyCompanyId, residentId), pageable);

        Map<String, Resident> residentMap = loadResidentMap(
                recordPage.getContent().stream().map(PropertyCoinRecord::getResidentId).toList());

        List<TransferToPropertyListItemResponse> list = recordPage.getContent().stream()
                .map(record -> toItem("coin", record.getId(), record.getResidentId(),
                        record.getAmount() != null ? record.getAmount().abs() : BigDecimal.ZERO,
                        record.getDescription(), record.getPropertyCompanyId(), record.getCreatedAt(),
                        residentMap.get(record.getResidentId())))
                .toList();

        return PageResponse.of(list, page, pageSize, recordPage.getTotalElements());
    }

    private Specification<PointRecord> pointTransferSpec(String propertyCompanyId, String residentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("source"), SOURCE_TRANSFER_TO_PROPERTY));
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Specification<PropertyCoinRecord> coinTransferSpec(String propertyCompanyId, String residentId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("source"), SOURCE_TRANSFER_TO_PROPERTY));
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(residentId)) {
                predicates.add(cb.equal(root.get("residentId"), residentId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private TransferToPropertyListItemResponse toItem(String type, String id, String residentId,
                                                      BigDecimal amount, String remark,
                                                      String propertyCompanyId, java.time.LocalDateTime createdAt,
                                                      Resident resident) {
        return TransferToPropertyListItemResponse.builder()
                .id(id)
                .type(type)
                .residentId(residentId)
                .residentName(resident != null ? resident.getName() : null)
                .residentPhone(resident != null ? PhoneUtil.mask(resident.getPhone()) : null)
                .amount(amount)
                .remark(remark)
                .propertyCompanyId(propertyCompanyId)
                .createdAt(createdAt != null ? DateTimeUtil.toIsoUtc(createdAt) : null)
                .build();
    }

    private Map<String, Resident> loadResidentMap(List<String> residentIds) {
        if (residentIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> distinctIds = residentIds.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return residentRepository.findAllById(distinctIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r));
    }

    private void validateResidentId(String residentId) {
        if (StringUtils.hasText(residentId) && !residentId.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId格式不正确");
        }
    }
}

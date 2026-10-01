package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.propertyfeereport.ArrearsReportGroupItem;
import com.wuye.points.dto.propertyfeereport.ArrearsReportItemResponse;
import com.wuye.points.dto.propertyfeereport.ArrearsReportResponse;
import com.wuye.points.entity.PropertyFee;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.PropertyFeeRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.PropertyFeeSpecifications;
import com.wuye.points.util.RoleGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物业费欠费报表服务（A11）
 */
@Service
@RequiredArgsConstructor
public class PropertyFeeReportService {

    private static final List<String> ARREARS_STATUS = List.of("unpaid", "partial", "overdue");

    private final PropertyFeeRepository propertyFeeRepository;
    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;

    @Transactional(readOnly = true)
    public ArrearsReportResponse generateArrearsReport(String propertyCompanyId,
                                                       String communityId,
                                                       String building,
                                                       String feeType) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String scopedPropertyCompanyId = resolveScopedPropertyCompanyId(propertyCompanyId);

        List<PropertyFee> fees = loadArrearsFees(scopedPropertyCompanyId, communityId, building, feeType);
        List<ArrearsReportItemResponse> items = buildItems(fees);

        BigDecimal totalArrears = items.stream()
                .map(ArrearsReportItemResponse::getArrearsAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ArrearsReportGroupItem> groups = groupByBuilding(items);

        return ArrearsReportResponse.builder()
                .items(items)
                .totalArrearsAmount(totalArrears.setScale(2, RoundingMode.HALF_UP))
                .totalCount(items.size())
                .groupByBuilding(groups)
                .build();
    }

    @Transactional(readOnly = true)
    public String exportArrearsCsv(String propertyCompanyId,
                                   String communityId,
                                   String building,
                                   String feeType) {
        ArrearsReportResponse response = generateArrearsReport(
                propertyCompanyId, communityId, building, feeType);

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM 防止 Excel 乱码
        sb.append("\uFEFF");
        sb.append("住户ID,住户姓名,手机号,楼栋,单元,房号,费用类型,账期,应缴金额,已缴金额,欠缴金额,到期日,逾期天数,物业费ID\n");
        for (ArrearsReportItemResponse item : response.getItems()) {
            sb.append(csv(item.getResidentId())).append(',')
                    .append(csv(item.getResidentName())).append(',')
                    .append(csv(item.getPhone())).append(',')
                    .append(csv(item.getBuilding())).append(',')
                    .append(csv(item.getUnit())).append(',')
                    .append(csv(item.getRoom())).append(',')
                    .append(csv(item.getFeeType())).append(',')
                    .append(csv(item.getPeriod())).append(',')
                    .append(num(item.getAmount())).append(',')
                    .append(num(item.getPaidAmount())).append(',')
                    .append(num(item.getArrearsAmount())).append(',')
                    .append(item.getDueDate() != null ? item.getDueDate().toString() : "").append(',')
                    .append(item.getDaysOverdue() != null ? item.getDaysOverdue() : "").append(',')
                    .append(csv(item.getFeeId())).append('\n');
        }
        sb.append("\n汇总,欠费总额:,").append(num(response.getTotalArrearsAmount()))
                .append(",欠费笔数:,").append(response.getTotalCount()).append('\n');
        return sb.toString();
    }

    private List<PropertyFee> loadArrearsFees(String propertyCompanyId, String communityId,
                                              String building, String feeType) {
        Specification<PropertyFee> spec = PropertyFeeSpecifications.withFilters(
                propertyCompanyId, null, null, communityId, feeType, null, null, null);
        // 在 spec 基础上追加欠费状态过滤
        Specification<PropertyFee> arrearsSpec = (root, query, cb) ->
                root.get("paymentStatus").in(ARREARS_STATUS);
        spec = spec.and(arrearsSpec);

        List<PropertyFee> fees = propertyFeeRepository.findAll(spec);
        // building 需关联 resident 表过滤
        if (StringUtils.hasText(building)) {
            Map<String, Resident> residentMap = loadResidents(
                    fees.stream().map(PropertyFee::getResidentId).distinct().toList());
            fees = fees.stream()
                    .filter(fee -> {
                        Resident r = residentMap.get(fee.getResidentId());
                        return r != null && building.equals(r.getBuilding());
                    })
                    .toList();
        }
        return fees;
    }

    private List<ArrearsReportItemResponse> buildItems(List<PropertyFee> fees) {
        Map<String, Resident> residentMap = loadResidents(
                fees.stream().map(PropertyFee::getResidentId).distinct().toList());
        LocalDate today = LocalDate.now();

        return fees.stream()
                .map(fee -> {
                    Resident resident = residentMap.get(fee.getResidentId());
                    BigDecimal amount = nz(fee.getAmount());
                    BigDecimal paid = nz(fee.getPaidAmount());
                    BigDecimal arrears = amount.subtract(paid).max(BigDecimal.ZERO);
                    Integer daysOverdue = fee.getDueDate() != null
                            ? (int) ChronoUnit.DAYS.between(fee.getDueDate(), today) : null;
                    if (daysOverdue != null && daysOverdue < 0) {
                        daysOverdue = 0;
                    }
                    return ArrearsReportItemResponse.builder()
                            .residentId(fee.getResidentId())
                            .residentName(resident != null ? resident.getName() : null)
                            .phone(resident != null ? resident.getPhone() : null)
                            .building(resident != null ? resident.getBuilding() : null)
                            .unit(resident != null ? resident.getUnit() : null)
                            .room(resident != null ? resident.getRoom() : null)
                            .feeType(fee.getFeeType())
                            .period(fee.getPeriod())
                            .periodStart(fee.getPeriodStart())
                            .periodEnd(fee.getPeriodEnd())
                            .amount(amount.setScale(2, RoundingMode.HALF_UP))
                            .paidAmount(paid.setScale(2, RoundingMode.HALF_UP))
                            .arrearsAmount(arrears.setScale(2, RoundingMode.HALF_UP))
                            .dueDate(fee.getDueDate())
                            .daysOverdue(daysOverdue)
                            .feeId(fee.getId())
                            .build();
                })
                .sorted(Comparator.comparing(ArrearsReportItemResponse::getBuilding,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ArrearsReportItemResponse::getRoom,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ArrearsReportItemResponse::getPeriod,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<ArrearsReportGroupItem> groupByBuilding(List<ArrearsReportItemResponse> items) {
        Map<String, List<ArrearsReportItemResponse>> map = items.stream()
                .filter(it -> StringUtils.hasText(it.getBuilding()))
                .collect(Collectors.groupingBy(
                        ArrearsReportItemResponse::getBuilding,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<ArrearsReportGroupItem> groups = new ArrayList<>();
        for (Map.Entry<String, List<ArrearsReportItemResponse>> entry : map.entrySet()) {
            BigDecimal total = entry.getValue().stream()
                    .map(ArrearsReportItemResponse::getArrearsAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            groups.add(ArrearsReportGroupItem.builder()
                    .building(entry.getKey())
                    .totalAmount(total.setScale(2, RoundingMode.HALF_UP))
                    .count(entry.getValue().size())
                    .build());
        }
        return groups;
    }

    private Map<String, Resident> loadResidents(List<String> residentIds) {
        if (residentIds.isEmpty()) {
            return Map.of();
        }
        return residentRepository.findAllById(residentIds).stream()
                .collect(Collectors.toMap(Resident::getId, r -> r, (a, b) -> a));
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String num(BigDecimal value) {
        return value != null ? value.toPlainString() : "";
    }

    private String resolveScopedPropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (!StringUtils.hasText(requestedPropertyCompanyId)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
            }
            if (!requestedPropertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return requestedPropertyCompanyId;
        }
        String currentPropertyCompanyId = UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(currentPropertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED);
        }
        return currentPropertyCompanyId;
    }
}

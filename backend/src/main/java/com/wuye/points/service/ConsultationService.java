package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.consultation.ConsultantDetailResponse;
import com.wuye.points.dto.consultation.ConsultantListItemResponse;
import com.wuye.points.dto.consultation.ConsultationAppointmentItemResponse;
import com.wuye.points.dto.consultation.ConsultationChatOrderResponse;
import com.wuye.points.dto.consultation.CreateConsultationAppointmentRequest;
import com.wuye.points.dto.consultation.CreateConsultationChatOrderRequest;
import com.wuye.points.entity.Consultant;
import com.wuye.points.entity.ConsultationAppointment;
import com.wuye.points.entity.ConsultationOrder;
import com.wuye.points.entity.PropertyCompany;
import com.wuye.points.entity.Resident;
import com.wuye.points.enums.ResidentStatus;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ConsultantRepository;
import com.wuye.points.repository.ConsultationAppointmentRepository;
import com.wuye.points.repository.ConsultationOrderRepository;
import com.wuye.points.repository.PropertyCompanyRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private static final BigDecimal DEFAULT_POINT_TO_FEE_RATE = new BigDecimal("0.01");
    private static final Set<String> CATEGORIES = Set.of("medical", "education", "elderly");
    private static final Set<String> PAYMENT_METHODS = Set.of("point", "property_coin", "mixed");

    private static final Map<String, String> APPOINTMENT_STATUS_LABELS = Map.of(
            "pending", "待确认",
            "confirmed", "已确认",
            "rejected", "已拒绝",
            "completed", "已完成",
            "cancelled", "已取消");

    private final ConsultantRepository consultantRepository;
    private final ConsultationOrderRepository consultationOrderRepository;
    private final ConsultationAppointmentRepository consultationAppointmentRepository;
    private final ResidentRepository residentRepository;
    private final PropertyCompanyRepository propertyCompanyRepository;
    private final PointService pointService;
    private final PropertyCoinService propertyCoinService;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public PageResponse<ConsultantListItemResponse> listConsultants(
            String category, int page, int pageSize, String keyword) {
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        if (StringUtils.hasText(category) && !CATEGORIES.contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category 须为 medical/education/elderly");
        }

        Specification<Consultant> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            predicates = cb.and(predicates, cb.equal(root.get("auditStatus"), "approved"));
            predicates = cb.and(predicates, cb.equal(root.get("status"), "active"));
            if (StringUtils.hasText(category)) {
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            }
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates = cb.and(predicates, cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(keyword)) {
                String like = "%" + keyword.trim() + "%";
                predicates = cb.and(predicates, cb.or(
                        cb.like(root.get("name"), like),
                        cb.like(root.get("specialty"), like),
                        cb.like(root.get("organization"), like),
                        cb.like(root.get("title"), like)));
            }
            return predicates;
        };

        PageRequest pageable = PageRequest.of(page - 1, Math.min(Math.max(pageSize, 1), 100));
        Page<Consultant> result = consultantRepository.findAll(spec, pageable);
        List<ConsultantListItemResponse> list = result.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ConsultantDetailResponse getConsultant(String id) {
        Consultant consultant = getApprovedConsultant(id);
        return toDetail(consultant);
    }

    @Transactional
    public ConsultationChatOrderResponse createChatOrder(String consultantId, CreateConsultationChatOrderRequest request) {
        roleGuard.requireAnyRole("resident");
        validatePaymentMethod(request.getPaymentMethod());
        Resident resident = getActiveResident(UserContext.currentUserId());
        Consultant consultant = getApprovedConsultant(consultantId);
        assertSameProperty(resident.getPropertyCompanyId(), consultant.getPropertyCompanyId());

        BigDecimal amount = consultant.getChatPrice() == null ? BigDecimal.ZERO : consultant.getChatPrice();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "咨询师暂未设置咨询价格");
        }

        PaymentBreakdown breakdown = resolvePaymentBreakdown(
                request.getPaymentMethod(), amount, request.getPointUsed(), request.getCoinUsed(),
                resident.getPropertyCompanyId());

        ConsultationOrder order = new ConsultationOrder();
        order.setId(idGenerator.generateId("co"));
        order.setConsultantId(consultant.getId());
        order.setResidentId(resident.getId());
        order.setAmount(amount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus("paid");
        consultationOrderRepository.save(order);

        executePaymentBreakdown(resident, order.getId(), breakdown, "图文咨询");

        return ConsultationChatOrderResponse.builder()
                .orderId(order.getId())
                .conversationPeerId(consultant.getId())
                .build();
    }

    @Transactional
    public ConsultationAppointmentItemResponse createAppointment(CreateConsultationAppointmentRequest request) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        Consultant consultant = getApprovedConsultant(request.getConsultantId());
        assertSameProperty(resident.getPropertyCompanyId(), consultant.getPropertyCompanyId());
        if (!Boolean.TRUE.equals(consultant.getAppointmentEnabled())) {
            throw new BizException(ErrorCode.CONSULTATION_APPOINTMENT_UNAVAILABLE);
        }

        ConsultationAppointment appointment = new ConsultationAppointment();
        appointment.setId(idGenerator.generateId("ca"));
        appointment.setConsultantId(consultant.getId());
        appointment.setResidentId(resident.getId());
        appointment.setAppointmentTime(adminSupport.parseIsoDateTime(request.getAppointmentTime()));
        appointment.setPatientName(StringUtils.hasText(request.getPatientName()) ? request.getPatientName().trim() : null);
        appointment.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : resident.getPhone());
        appointment.setRemark(StringUtils.hasText(request.getRemark()) ? request.getRemark().trim() : null);
        appointment.setStatus("pending");
        consultationAppointmentRepository.save(appointment);

        return toAppointmentItem(appointment, consultant.getName());
    }

    @Transactional(readOnly = true)
    public PageResponse<ConsultationAppointmentItemResponse> listMyAppointments(int page, int pageSize) {
        roleGuard.requireAnyRole("resident");
        Resident resident = getActiveResident(UserContext.currentUserId());
        if (page < 1) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        PageRequest pageable = PageRequest.of(page - 1, Math.min(Math.max(pageSize, 1), 100));
        Page<ConsultationAppointment> result =
                consultationAppointmentRepository.findByResidentIdOrderByCreatedAtDesc(resident.getId(), pageable);

        Map<String, String> consultantNames = result.getContent().stream()
                .map(ConsultationAppointment::getConsultantId)
                .distinct()
                .collect(java.util.stream.Collectors.toMap(
                        id -> id,
                        id -> consultantRepository.findById(id).map(Consultant::getName).orElse(null)));

        List<ConsultationAppointmentItemResponse> list = result.getContent().stream()
                .map(a -> toAppointmentItem(a, consultantNames.get(a.getConsultantId())))
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    private Consultant getApprovedConsultant(String id) {
        Consultant consultant = consultantRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_NOT_FOUND, "咨询师不存在"));
        if (!"approved".equals(consultant.getAuditStatus()) || !"active".equals(consultant.getStatus())) {
            throw new BizException(ErrorCode.SERVICE_UNAVAILABLE, "咨询师暂不可用");
        }
        return consultant;
    }

    private ConsultantListItemResponse toListItem(Consultant c) {
        return ConsultantListItemResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .title(c.getTitle())
                .organization(c.getOrganization())
                .specialty(c.getSpecialty())
                .avatarUrl(c.getAvatarUrl())
                .category(c.getCategory())
                .chatPrice(c.getChatPrice())
                .appointmentPrice(c.getAppointmentPrice())
                .appointmentEnabled(Boolean.TRUE.equals(c.getAppointmentEnabled()))
                .status("启用")
                .statusCode(c.getStatus())
                .build();
    }

    private ConsultantDetailResponse toDetail(Consultant c) {
        return ConsultantDetailResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .title(c.getTitle())
                .organization(c.getOrganization())
                .specialty(c.getSpecialty())
                .avatarUrl(c.getAvatarUrl())
                .category(c.getCategory())
                .chatPrice(c.getChatPrice())
                .appointmentPrice(c.getAppointmentPrice())
                .appointmentEnabled(Boolean.TRUE.equals(c.getAppointmentEnabled()))
                .introduction(c.getIntroduction())
                .status("启用")
                .statusCode(c.getStatus())
                .build();
    }

    private ConsultationAppointmentItemResponse toAppointmentItem(ConsultationAppointment a, String consultantName) {
        return ConsultationAppointmentItemResponse.builder()
                .id(a.getId())
                .consultantId(a.getConsultantId())
                .consultantName(consultantName)
                .appointmentTime(DateTimeUtil.toLocalDateTimeString(a.getAppointmentTime()))
                .patientName(a.getPatientName())
                .phone(a.getPhone())
                .remark(a.getRemark())
                .status(APPOINTMENT_STATUS_LABELS.getOrDefault(a.getStatus(), a.getStatus()))
                .statusCode(a.getStatus())
                .createdAt(DateTimeUtil.toLocalDateTimeString(a.getCreatedAt()))
                .build();
    }

    private void validatePaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod) || !PAYMENT_METHODS.contains(paymentMethod)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod须为 point/property_coin/mixed");
        }
    }

    private PaymentBreakdown resolvePaymentBreakdown(
            String paymentMethod,
            BigDecimal amount,
            Integer pointUsed,
            BigDecimal coinUsed,
            String propertyCompanyId) {
        return switch (paymentMethod) {
            case "property_coin" -> new PaymentBreakdown(0, amount, BigDecimal.ZERO);
            case "point" -> {
                BigDecimal rate = resolvePointToFeeRate(propertyCompanyId);
                int points = amount.divide(rate, 0, RoundingMode.UP).intValue();
                yield new PaymentBreakdown(points, BigDecimal.ZERO, BigDecimal.ZERO);
            }
            case "mixed" -> {
                int points = pointUsed != null ? pointUsed : 0;
                BigDecimal coins = coinUsed != null ? coinUsed : BigDecimal.ZERO;
                BigDecimal rate = resolvePointToFeeRate(propertyCompanyId);
                BigDecimal pointValue = rate.multiply(BigDecimal.valueOf(points))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal cashAmount = amount.subtract(pointValue).subtract(coins);
                if (cashAmount.compareTo(BigDecimal.ZERO) < 0) {
                    cashAmount = BigDecimal.ZERO;
                }
                if (pointValue.add(coins).add(cashAmount).compareTo(amount) < 0) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "混合支付金额不足");
                }
                yield new PaymentBreakdown(points, coins, cashAmount);
            }
            default -> throw new BizException(ErrorCode.PARAM_INVALID, "paymentMethod值无效");
        };
    }

    private void executePaymentBreakdown(
            Resident resident,
            String referenceId,
            PaymentBreakdown breakdown,
            String remark) {
        if (breakdown.pointUsed() > 0) {
            pointService.spendPoints(
                    resident, breakdown.pointUsed(), "consultation", referenceId, remark, null, false);
        }
        if (breakdown.coinUsed().compareTo(BigDecimal.ZERO) > 0) {
            propertyCoinService.spendCoins(
                    resident, breakdown.coinUsed(), "consultation", referenceId, remark, null, false);
        }
    }

    private BigDecimal resolvePointToFeeRate(String propertyCompanyId) {
        if (!StringUtils.hasText(propertyCompanyId)) {
            return DEFAULT_POINT_TO_FEE_RATE;
        }
        return propertyCompanyRepository.findById(propertyCompanyId)
                .map(PropertyCompany::getPointToFeeRate)
                .filter(rate -> rate != null && rate.compareTo(BigDecimal.ZERO) > 0)
                .orElse(DEFAULT_POINT_TO_FEE_RATE);
    }

    private void assertSameProperty(String residentPropertyCompanyId, String consultantPropertyCompanyId) {
        if (!StringUtils.hasText(residentPropertyCompanyId)
                || !residentPropertyCompanyId.equals(consultantPropertyCompanyId)) {
            throw new BizException(ErrorCode.PROPERTY_OPERATOR_OUT_OF_SCOPE);
        }
    }

    private Resident getActiveResident(String id) {
        Resident resident = residentRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (ResidentStatus.disabled.name().equals(resident.getStatus())
                || ResidentStatus.frozen.name().equals(resident.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        return resident;
    }

    private record PaymentBreakdown(int pointUsed, BigDecimal coinUsed, BigDecimal cashAmount) {
    }
}

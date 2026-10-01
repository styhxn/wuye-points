package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.consultation.AuditConsultantRequest;
import com.wuye.points.dto.consultation.ConsultantDetailResponse;
import com.wuye.points.dto.consultation.ConsultantListItemResponse;
import com.wuye.points.dto.consultation.ConsultationSettingsResponse;
import com.wuye.points.dto.consultation.CreateConsultantRequest;
import com.wuye.points.dto.consultation.UpdateConsultantRequest;
import com.wuye.points.dto.consultation.UpdateConsultationSettingsRequest;
import com.wuye.points.entity.Consultant;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ConsultantRepository;
import com.wuye.points.repository.spec.ConsultantSpecifications;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.DateTimeUtil;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminConsultationService {

    private static final Set<String> CATEGORIES = Set.of("medical", "education", "elderly");
    private static final Set<String> AUDIT_STATUSES = Set.of("approved", "rejected");

    private final ConsultantRepository consultantRepository;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;
    private final PlatformSettingService platformSettingService;

    @Transactional(readOnly = true)
    public PageResponse<ConsultantListItemResponse> listConsultants(
            int page,
            int pageSize,
            String category,
            String keyword,
            String status,
            String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        String resolvedPropertyCompanyId = adminSupport.resolvePropertyCompanyId(propertyCompanyId);

        Page<Consultant> result = consultantRepository.findAll(
                ConsultantSpecifications.withFilters(resolvedPropertyCompanyId, category, keyword, status),
                pageable);

        List<ConsultantListItemResponse> list = result.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(list, page, pageSize, result.getTotalElements());
    }

    @Transactional
    public ConsultantDetailResponse createConsultant(
            CreateConsultantRequest request, String queryPropertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validateCategory(request.getCategory());
        String requestedPropertyCompanyId = StringUtils.hasText(request.getPropertyCompanyId())
                ? request.getPropertyCompanyId()
                : queryPropertyCompanyId;
        String propertyCompanyId = adminSupport.resolvePropertyCompanyId(requestedPropertyCompanyId);
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "propertyCompanyId不能为空");
        }

        Consultant consultant = new Consultant();
        consultant.setId(idGenerator.generateId("cst"));
        consultant.setPropertyCompanyId(propertyCompanyId);
        consultant.setCategory(request.getCategory());
        consultant.setName(request.getName().trim());
        consultant.setTitle(request.getTitle());
        consultant.setOrganization(request.getOrganization());
        consultant.setSpecialty(request.getSpecialty());
        consultant.setAvatarUrl(request.getAvatarUrl());
        consultant.setChatPrice(request.getChatPrice() == null ? BigDecimal.ZERO : request.getChatPrice());
        consultant.setAppointmentPrice(request.getAppointmentPrice());
        consultant.setAppointmentEnabled(request.getAppointmentEnabled() == null
                || request.getAppointmentEnabled());
        consultant.setIntroduction(request.getIntroduction());
        consultant.setAuditStatus("pending_audit");
        consultant.setStatus("active");
        consultantRepository.save(consultant);
        return toDetail(consultant);
    }

    @Transactional
    public ConsultantDetailResponse updateConsultant(String id, UpdateConsultantRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Consultant consultant = getManageableConsultant(id);
        if (StringUtils.hasText(request.getName())) {
            consultant.setName(request.getName().trim());
        }
        if (request.getTitle() != null) {
            consultant.setTitle(request.getTitle());
        }
        if (request.getOrganization() != null) {
            consultant.setOrganization(request.getOrganization());
        }
        if (request.getSpecialty() != null) {
            consultant.setSpecialty(request.getSpecialty());
        }
        if (request.getAvatarUrl() != null) {
            consultant.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getChatPrice() != null) {
            consultant.setChatPrice(request.getChatPrice());
        }
        if (request.getAppointmentEnabled() != null) {
            consultant.setAppointmentEnabled(request.getAppointmentEnabled());
        }
        if (request.getAppointmentPrice() != null) {
            consultant.setAppointmentPrice(request.getAppointmentPrice());
        }
        if (request.getIntroduction() != null) {
            consultant.setIntroduction(request.getIntroduction());
        }
        if (StringUtils.hasText(request.getStatus())) {
            consultant.setStatus(request.getStatus());
        }
        consultantRepository.save(consultant);
        return toDetail(consultant);
    }

    @Transactional
    public ConsultantDetailResponse auditConsultant(String id, AuditConsultantRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String auditStatus = request.resolveAuditStatus();
        if (!StringUtils.hasText(auditStatus) || !AUDIT_STATUSES.contains(auditStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "auditResult须为 approved/rejected");
        }
        Consultant consultant = getManageableConsultant(id);
        if (!"pending_audit".equals(consultant.getAuditStatus())) {
            throw new BizException(ErrorCode.AUDIT_ALREADY_PROCESSED);
        }
        consultant.setAuditStatus(auditStatus);
        if ("rejected".equals(auditStatus)) {
            consultant.setStatus("inactive");
        }
        consultantRepository.save(consultant);
        return toDetail(consultant);
    }

    @Transactional(readOnly = true)
    public ConsultationSettingsResponse getSettings() {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        return ConsultationSettingsResponse.builder()
                .commissionRate(platformSettingService.getConsultationCommissionRate())
                .build();
    }

    @Transactional
    public ConsultationSettingsResponse updateSettings(UpdateConsultationSettingsRequest request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        try {
            platformSettingService.setConsultationCommissionRate(request.getCommissionRate());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE, ex.getMessage());
        }
        return getSettings();
    }

    private Consultant getManageableConsultant(String id) {
        Consultant consultant = consultantRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.SERVICE_NOT_FOUND, "咨询师不存在"));
        adminSupport.assertSameProperty(consultant.getPropertyCompanyId());
        return consultant;
    }

    private void validateCategory(String category) {
        if (!StringUtils.hasText(category) || !CATEGORIES.contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category 须为 medical/education/elderly");
        }
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
                .auditStatus(c.getAuditStatus())
                .status(c.getStatus())
                .statusCode(c.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(c.getCreatedAt()))
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
                .auditStatus(c.getAuditStatus())
                .status(c.getStatus())
                .statusCode(c.getStatus())
                .createdAt(DateTimeUtil.toIsoUtc(c.getCreatedAt()))
                .build();
    }
}

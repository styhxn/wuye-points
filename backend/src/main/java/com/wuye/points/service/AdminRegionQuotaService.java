package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.RegionQuota;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.RegionQuotaRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * 区域/板块额度配额维护。
 */
@Service
@RequiredArgsConstructor
public class AdminRegionQuotaService {

    private static final Set<String> VALID_QUOTA_TYPES = Set.of("point", "amount");
    private static final Set<String> VALID_STATUS = Set.of("active", "disabled");

    private final RegionQuotaRepository regionQuotaRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public List<RegionQuota> list(String sector) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String pcId = UserContext.currentPropertyCompanyId();
        return StringUtils.hasText(sector)
                ? regionQuotaRepository.findByPropertyCompanyIdAndSectorOrderByPeriodStartDesc(pcId, sector)
                : regionQuotaRepository.findByPropertyCompanyIdOrderByPeriodStartDesc(pcId);
    }

    @Transactional
    public RegionQuota create(RegionQuota request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validate(request);
        RegionQuota quota = new RegionQuota();
        quota.setId(idGenerator.generateId("rq"));
        quota.setPropertyCompanyId(UserContext.currentPropertyCompanyId());
        apply(quota, request);
        return regionQuotaRepository.save(quota);
    }

    @Transactional
    public RegionQuota update(String id, RegionQuota request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        RegionQuota quota = regionQuotaRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "配额不存在"));
        if (request.getQuotaAmount() != null) {
            BigDecimal used = quota.getUsedAmount() == null ? BigDecimal.ZERO : quota.getUsedAmount();
            if (request.getQuotaAmount().compareTo(used) < 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "配额总量不能低于已使用量");
            }
            quota.setQuotaAmount(request.getQuotaAmount());
        }
        if (request.getPeriodStart() != null) {
            quota.setPeriodStart(request.getPeriodStart());
        }
        if (request.getPeriodEnd() != null) {
            quota.setPeriodEnd(request.getPeriodEnd());
        }
        if (request.getStatus() != null) {
            if (!VALID_STATUS.contains(request.getStatus())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status 必须为 active 或 disabled");
            }
            quota.setStatus(request.getStatus());
        }
        if (request.getRemark() != null) {
            quota.setRemark(request.getRemark());
        }
        return regionQuotaRepository.save(quota);
    }

    @Transactional
    public void delete(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        regionQuotaRepository.deleteById(id);
    }

    private void apply(RegionQuota quota, RegionQuota request) {
        quota.setSector(request.getSector());
        quota.setQuotaType(request.getQuotaType());
        quota.setQuotaAmount(request.getQuotaAmount());
        quota.setUsedAmount(BigDecimal.ZERO);
        quota.setPeriodStart(request.getPeriodStart());
        quota.setPeriodEnd(request.getPeriodEnd());
        quota.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "active");
        quota.setRemark(request.getRemark());
    }

    private void validate(RegionQuota request) {
        if (!StringUtils.hasText(request.getSector())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "板块/区域不能为空");
        }
        if (!VALID_QUOTA_TYPES.contains(request.getQuotaType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "quotaType 必须为 point 或 amount");
        }
        if (request.getQuotaAmount() == null || request.getQuotaAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "配额总量必须大于0");
        }
        if (request.getPeriodStart() == null || request.getPeriodEnd() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "周期起止日期不能为空");
        }
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "周期结束日期不能早于开始日期");
        }
    }
}

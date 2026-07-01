package com.wuye.points.util;

import com.wuye.points.context.UserContext;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class AdminSupport {

    private final RoleGuard roleGuard;

    public AdminSupport(RoleGuard roleGuard) {
        this.roleGuard = roleGuard;
    }

    public String resolvePropertyCompanyId(String requestedPropertyCompanyId) {
        roleGuard.requireAnyRole("platform_admin", "property_admin");
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)
                    && !requestedPropertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return requestedPropertyCompanyId;
        }
        return UserContext.currentPropertyCompanyId();
    }

    public void assertSameProperty(String targetPropertyCompanyId) {
        roleGuard.assertSamePropertyOrPlatform(targetPropertyCompanyId);
    }

    public Pageable buildPageable(int page, int pageSize, String sortField) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        Sort sort = Sort.by(Sort.Direction.DESC, StringUtils.hasText(sortField) ? sortField : "createdAt");
        return PageRequest.of(safePage - 1, safePageSize, sort);
    }

    public LocalDateTime parseIsoDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
        } catch (Exception ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式不正确");
        }
    }

    public LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            throw new BizException(ErrorCode.PARAM_INVALID, "日期格式应为YYYY-MM-DD");
        }
    }

    public void validateIdPrefix(String id, String prefix) {
        if (!StringUtils.hasText(id) || !id.startsWith(prefix)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }
}

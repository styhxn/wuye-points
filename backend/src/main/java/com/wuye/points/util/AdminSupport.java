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
        // 2026-09-07 前端问题 C：统筹只读查看本物业数据，平台/物业可管理；
        // coordinator 非本物业 ID 的过滤参数会被下方强制忽略为本人所属物业，天然越权防护
        roleGuard.requireAnyRole("platform_admin", "property_admin", "coordinator");
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)
                    && !requestedPropertyCompanyId.startsWith("pc_")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
            }
            return requestedPropertyCompanyId;
        }
        if (roleGuard.isCoordinator()
                && StringUtils.hasText(requestedPropertyCompanyId)
                && !requestedPropertyCompanyId.equals(UserContext.currentPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权查看其他物业公司数据");
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
        return parseIsoDateTime(value, false);
    }

    /**
     * 2026-09-08：§25.1 约定 startTime/endTime 支持 ISO8601、`yyyy-MM-dd HH:mm:ss`、纯日期三种格式，
     * 此前仅支持 ISO8601，传其余格式会直接 400。
     *
     * @param endOfDay 纯日期时是否取当天 23:59:59（结束时间用）
     */
    public LocalDateTime parseIsoDateTime(String value, boolean endOfDay) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String text = value.trim();
        try {
            return LocalDateTime.ofInstant(Instant.parse(text), ZoneOffset.UTC);
        } catch (Exception ignored) {
            // 继续尝试其余格式
        }
        try {
            return LocalDateTime.parse(text.replace(' ', 'T'));
        } catch (Exception ignored) {
            // 继续尝试纯日期
        }
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        } catch (Exception ignored) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "日期格式不正确（" + value + "），支持 ISO8601 / yyyy-MM-dd HH:mm:ss / yyyy-MM-dd");
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

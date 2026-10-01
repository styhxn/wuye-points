package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.entity.NavigationItem;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.NavigationItemRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 手机端导航栏配置（后台维护）。
 */
@Service
@RequiredArgsConstructor
public class AdminNavigationService {

    private static final Set<String> VALID_POSITIONS = Set.of("bottom", "top");
    private static final Set<String> VALID_LINK_TYPES = Set.of("page", "tab", "h5", "none");
    private static final Set<String> VALID_MODULES =
            Set.of("home", "service", "activity", "resident_merchant");

    private final NavigationItemRepository navigationItemRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional(readOnly = true)
    public List<NavigationItem> list(String propertyCompanyId) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String pcId = propertyCompanyId != null ? propertyCompanyId : UserContext.currentPropertyCompanyId();
        if (!StringUtils.hasText(pcId)) {
            return navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc();
        }
        return navigationItemRepository.findByPropertyCompanyIdOrderBySortOrderAsc(pcId);
    }

    @Transactional
    public NavigationItem create(NavigationItem request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        validate(request);
        NavigationItem item = new NavigationItem();
        item.setId(idGenerator.generateId("nav"));
        apply(item, request);
        if (!StringUtils.hasText(item.getPropertyCompanyId())) {
            item.setPropertyCompanyId(UserContext.currentPropertyCompanyId());
        }
        return navigationItemRepository.save(item);
    }

    @Transactional
    public NavigationItem update(String id, NavigationItem request) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        NavigationItem item = navigationItemRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "导航项不存在"));
        validate(request);
        apply(item, request);
        return navigationItemRepository.save(item);
    }

    @Transactional
    public void delete(String id) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        navigationItemRepository.deleteById(id);
    }

    /**
     * 手机端拉取导航：可按模块过滤（home/service/activity/resident_merchant），
     * 优先本物业公司配置，未配置时回落平台默认。
     */
    @Transactional(readOnly = true)
    public List<NavigationItem> listForApp(String module) {
        String pcId = UserContext.currentPropertyCompanyId();
        List<NavigationItem> items = StringUtils.hasText(pcId)
                ? navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc(pcId)
                : List.of();
        if (items.isEmpty()) {
            items = navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc();
        }
        if (StringUtils.hasText(module)) {
            items = items.stream().filter(i -> module.equals(i.getModule())).toList();
        }
        return items;
    }

    private void apply(NavigationItem item, NavigationItem request) {
        item.setPosition(request.getPosition());
        item.setModule(StringUtils.hasText(request.getModule()) ? request.getModule() : "home");
        item.setTitle(request.getTitle());
        item.setIcon(request.getIcon());
        item.setLinkType(request.getLinkType());
        item.setLinkUrl(request.getLinkUrl());
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setEnabled(request.getEnabled() == null || request.getEnabled());
        item.setVisibleRoles(request.getVisibleRoles());
        item.setPropertyCompanyId(request.getPropertyCompanyId());
    }

    private void validate(NavigationItem request) {
        if (!VALID_POSITIONS.contains(request.getPosition())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "position 必须为 bottom 或 top");
        }
        if (StringUtils.hasText(request.getModule()) && !VALID_MODULES.contains(request.getModule())) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "module 必须为 home/service/activity/resident_merchant");
        }
        if (!VALID_LINK_TYPES.contains(request.getLinkType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "linkType 必须为 page/tab/h5/none");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "标题不能为空");
        }
    }
}

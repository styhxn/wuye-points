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
    public List<NavigationItem> list(String propertyCompanyId, String module) {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        String pcId = propertyCompanyId != null ? propertyCompanyId : UserContext.currentPropertyCompanyId();
        List<NavigationItem> items;
        if (!StringUtils.hasText(pcId)) {
            items = navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc();
        } else {
            items = navigationItemRepository.findByPropertyCompanyIdOrderBySortOrderAsc(pcId);
        }
        if (StringUtils.hasText(module)) {
            items = items.stream().filter(i -> module.equals(i.getModule())).toList();
        }
        return items;
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
     * 优先本物业公司配置，该模块一条都没配时回落平台默认。
     *
     * <p>条目31：小程序首页九宫格调用时只传 {@code module=home}、不传 position，
     * 并把返回当成整组导航直接渲染。此前 position 缺省被固定成 bottom，
     * 管理端配在 top 的导航项在首页永远查不到，表现就是「后台配好并启用了，首页一个都不显示，
     * 只有显式按顶部查才出现」。现在缺省不限位置，只有显式传 position 才过滤。</p>
     *
     * <p>回落判定按模块粒度做：本物业配了别的模块（如 service）不该把 home 的平台默认项一起挡掉。</p>
     */
    @Transactional(readOnly = true)
    public List<NavigationItem> listForApp(String module, String position) {
        String pcId = UserContext.currentPropertyCompanyId();
        List<NavigationItem> tenantItems = StringUtils.hasText(pcId)
                ? navigationItemRepository.findByPropertyCompanyIdAndEnabledTrueOrderBySortOrderAsc(pcId)
                : List.of();
        List<NavigationItem> platformItems =
                navigationItemRepository.findByPropertyCompanyIdIsNullAndEnabledTrueOrderBySortOrderAsc();

        List<NavigationItem> candidates = tenantItems;
        if (StringUtils.hasText(module)) {
            candidates = byModule(tenantItems, module);
            if (candidates.isEmpty()) {
                candidates = byModule(platformItems, module);
            }
        } else if (candidates.isEmpty()) {
            candidates = platformItems;
        }

        if (StringUtils.hasText(position)) {
            candidates = filterByPosition(candidates, position);
        }
        return candidates;
    }

    private List<NavigationItem> byModule(List<NavigationItem> items, String module) {
        return items.stream().filter(i -> module.equals(i.getModule())).toList();
    }

    private List<NavigationItem> filterByPosition(List<NavigationItem> items, String position) {
        return items.stream().filter(i -> position.equals(i.getPosition())).toList();
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

package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.specialoffer.CreateSpecialOfferRequest;
import com.wuye.points.dto.specialoffer.SpecialOfferConverter;
import com.wuye.points.dto.specialoffer.SpecialOfferDetailResponse;
import com.wuye.points.dto.specialoffer.SpecialOfferListItemResponse;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.SpecialOffer;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.OrderRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.SpecialOfferRepository;
import com.wuye.points.util.AdminSupport;
import com.wuye.points.util.PermissionGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpecialOfferService {

    /** 写库状态：draft 仅管理端可见；published 为 C 端（住户）唯一可见状态 */
    private static final Set<String> VALID_STATUS = Set.of("draft", "published", "ended", "archived");
    private static final Set<String> VALID_TARGET_TYPES = Set.of("all", "tag", "building", "role");

    /**
     * 2026-09-08：文档 §25.1 允许传中文目标类型，此前后端直接按英文枚举校验会导致 400。
     */
    private static final Map<String, String> TARGET_TYPE_ALIASES = Map.of(
            "全体业主", "all",
            "全部业主", "all",
            "指定小区", "building",
            "小区", "building",
            "指定商户", "role",
            "商户", "role",
            "指定标签", "tag",
            "标签", "tag");

    /**
     * 2026-09-08：文档 §25.1 允许传中文状态；「待发布 / active」为历史别名，统一归一为 published
     * （系统不存在独立的「待发布」态：未发布即 draft，已发布即 published）。
     */
    private static final Map<String, String> STATUS_ALIASES = Map.of(
            "草稿", "draft",
            "待发布", "published",
            "已发布", "published",
            "active", "published",
            "已结束", "ended",
            "已归档", "archived",
            "已删除", "archived");

    private final SpecialOfferRepository specialOfferRepository;
    private final SpecialOfferConverter specialOfferConverter;
    private final ResidentRepository residentRepository;
    private final OrderRepository orderRepository;
    private final RoleGuard roleGuard;
    private final PermissionGuard permissionGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    // ------------------------------------------------------------------ 写操作

    @Transactional
    public SpecialOfferDetailResponse create(CreateSpecialOfferRequest request) {
        requireOfferWrite();

        String targetType = normalizeTargetType(request.getTargetType(), true);
        String propertyCompanyId = resolvePropertyCompanyId(null);
        if (!StringUtils.hasText(propertyCompanyId)) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无法确定当前账号所属物业公司，无法创建特惠");
        }
        String status = normalizeStatus(request.getStatus(), "draft");

        SpecialOffer offer = new SpecialOffer();
        offer.setId(idGenerator.generateId("so"));
        offer.setTitle(request.getTitle().trim());
        offer.setContent(request.getContent());
        offer.setTargetType(targetType);
        offer.setTargetTags(specialOfferConverter.toJson(
                StringUtils.hasText(request.getTargetTags()) ? List.of(request.getTargetTags().split(",")) : null));
        offer.setPublisherId(UserContext.currentUserId());
        offer.setPublisherRole(UserContext.currentRole());
        offer.setPropertyCompanyId(propertyCompanyId);
        offer.setCommunityId(request.getCommunityId());
        offer.setMerchantId(request.getMerchantId());
        offer.setCoverUrl(request.getCoverUrl());
        offer.setDiscountInfo(request.getDiscountInfo());
        offer.setMinConsumption(request.getMinConsumption());
        offer.setTotalQuota(request.getTotalQuota());
        offer.setPerUserQuota(request.getPerUserQuota());
        offer.setUsedQuota(0);
        offer.setStartTime(adminSupport.parseIsoDateTime(request.getStartTime(), false));
        offer.setEndTime(adminSupport.parseIsoDateTime(request.getEndTime(), true));
        offer.setStatus(status);

        specialOfferRepository.save(offer);
        return specialOfferConverter.toDetail(offer);
    }

    @Transactional
    public SpecialOfferDetailResponse update(String id, CreateSpecialOfferRequest request) {
        requireOfferWrite();
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);

        if (StringUtils.hasText(request.getTitle())) {
            offer.setTitle(request.getTitle().trim());
        }
        if (StringUtils.hasText(request.getContent())) {
            offer.setContent(request.getContent());
        }
        if (StringUtils.hasText(request.getTargetType())) {
            offer.setTargetType(normalizeTargetType(request.getTargetType(), true));
        }
        if (request.getTargetTags() != null) {
            offer.setTargetTags(specialOfferConverter.toJson(
                    StringUtils.hasText(request.getTargetTags()) ? List.of(request.getTargetTags().split(",")) : null));
        }
        if (request.getCommunityId() != null) {
            offer.setCommunityId(request.getCommunityId());
        }
        if (request.getMerchantId() != null) {
            offer.setMerchantId(request.getMerchantId());
        }
        if (request.getCoverUrl() != null) {
            offer.setCoverUrl(request.getCoverUrl());
        }
        if (request.getDiscountInfo() != null) {
            offer.setDiscountInfo(request.getDiscountInfo());
        }
        if (request.getMinConsumption() != null) {
            offer.setMinConsumption(request.getMinConsumption());
        }
        if (request.getTotalQuota() != null) {
            offer.setTotalQuota(request.getTotalQuota());
        }
        if (request.getPerUserQuota() != null) {
            offer.setPerUserQuota(request.getPerUserQuota());
        }
        if (StringUtils.hasText(request.getStartTime())) {
            offer.setStartTime(adminSupport.parseIsoDateTime(request.getStartTime(), false));
        }
        if (StringUtils.hasText(request.getEndTime())) {
            offer.setEndTime(adminSupport.parseIsoDateTime(request.getEndTime(), true));
        }
        if (StringUtils.hasText(request.getStatus())) {
            offer.setStatus(normalizeStatus(request.getStatus(), offer.getStatus()));
        }

        specialOfferRepository.save(offer);
        return specialOfferConverter.toDetail(offer);
    }

    /**
     * 2026-09-08：新增「立即发布」动作（草稿 -> 已发布），避免前端只能靠 PUT 猜状态值。
     */
    @Transactional
    public SpecialOfferDetailResponse publish(String id) {
        requireOfferWrite();
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);
        if ("archived".equals(offer.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "已归档的特惠不能发布，请新建");
        }
        offer.setStatus("published");
        specialOfferRepository.save(offer);
        return specialOfferConverter.toDetail(offer);
    }

    @Transactional
    public SpecialOfferDetailResponse unpublish(String id) {
        requireOfferWrite();
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);
        offer.setStatus("draft");
        specialOfferRepository.save(offer);
        return specialOfferConverter.toDetail(offer);
    }

    @Transactional
    public SpecialOfferDetailResponse delete(String id) {
        requireOfferWrite();
        validateId(id);

        SpecialOffer offer = getManageableOffer(id);
        offer.setStatus("archived");
        specialOfferRepository.save(offer);

        return specialOfferConverter.toDetail(offer);
    }

    // ------------------------------------------------------------------ 读操作

    /**
     * 特惠列表：管理端按原逻辑（本物业全量，可按 targetType/status 过滤）；
     * 住户端（resident）走 §25.6 C 端可见规则。
     */
    @Transactional(readOnly = true)
    public PageResponse<SpecialOfferListItemResponse> list(
            int page, int pageSize, String targetType, String status) {
        if (roleGuard.isResident()) {
            return listForResident(page, pageSize);
        }
        roleGuard.requireAnyRole(
                "property_admin", "platform_admin", "sector_leader", "coordinator");

        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        String normalizedTargetType = normalizeTargetType(targetType, false);
        String normalizedStatus = normalizeStatus(status, null);

        String propertyCompanyId = resolvePropertyCompanyId(null);
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");

        Page<SpecialOffer> offerPage = specialOfferRepository.findAll(
                manageSpec(propertyCompanyId, normalizedTargetType, normalizedStatus), pageable);

        Map<String, Resident> publisherMap = specialOfferConverter.loadPublishers(offerPage.getContent());
        Map<String, Merchant> merchantMap =
                specialOfferConverter.loadMerchants(offerPage.getContent());

        List<SpecialOfferListItemResponse> list = offerPage.getContent().stream()
                .map(offer -> specialOfferConverter.toListItem(offer, publisherMap, merchantMap))
                .toList();

        return PageResponse.of(list, page, pageSize, offerPage.getTotalElements());
    }

    /**
     * §25.6 C 端（住户）可见规则：
     * <ol>
     *   <li>物业公司 = 当前住户所属物业（强制）；</li>
     *   <li>status = published（草稿 draft / 已结束 ended / 已归档 archived 均不可见）；</li>
     *   <li>时间窗：startTime 为空或 startTime &le; now；endTime 为空或 endTime &ge; now；</li>
     *   <li>targetType：all 全物业可见；building 需小区匹配（未填小区视为全物业）；
     *       role 需住户消费过该商户（未填商户视为全物业）；tag 暂无住户标签体系，按全物业可见；</li>
     *   <li>不按 publisherRole 过滤：统筹/物业/平台/板块负责人发布的记录一视同仁。</li>
     * </ol>
     */
    @Transactional(readOnly = true)
    public PageResponse<SpecialOfferListItemResponse> listForResident(int page, int pageSize) {
        if (pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        Resident resident = residentRepository.findById(UserContext.currentUserId()).orElse(null);
        String propertyCompanyId = resolveResidentPropertyCompanyId(resident);
        if (propertyCompanyId == null) {
            return PageResponse.of(List.of(), page, pageSize, 0);
        }

        List<SpecialOffer> candidates = specialOfferRepository.findAll(
                residentVisibleSpec(propertyCompanyId, LocalDateTime.now()),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        List<SpecialOffer> visible = new ArrayList<>();
        Set<String> consumedMerchantIds = null;
        for (SpecialOffer offer : candidates) {
            if (isVisibleToResident(offer, resident)) {
                visible.add(offer);
                continue;
            }
            if ("role".equals(offer.getTargetType()) && StringUtils.hasText(offer.getMerchantId())) {
                if (consumedMerchantIds == null) {
                    consumedMerchantIds = Set.copyOf(
                            orderRepository.findDistinctMerchantIdsByResidentId(resident.getId()));
                }
                if (consumedMerchantIds.contains(offer.getMerchantId())) {
                    visible.add(offer);
                }
            }
        }

        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        int from = Math.min((safePage - 1) * safePageSize, visible.size());
        int to = Math.min(from + safePageSize, visible.size());
        List<SpecialOffer> paged = visible.subList(from, to);

        Map<String, Resident> publisherMap = specialOfferConverter.loadPublishers(paged);
        Map<String, Merchant> merchantMap = specialOfferConverter.loadMerchants(paged);

        List<SpecialOfferListItemResponse> list = paged.stream()
                .map(offer -> specialOfferConverter.toListItem(offer, publisherMap, merchantMap))
                .toList();

        return PageResponse.of(list, safePage, safePageSize, visible.size());
    }

    @Transactional(readOnly = true)
    public SpecialOfferDetailResponse getById(String id) {
        validateId(id);
        if (roleGuard.isResident()) {
            return specialOfferConverter.toDetail(getResidentVisibleOffer(id));
        }
        roleGuard.requireAnyRole("property_admin", "platform_admin", "coordinator", "sector_leader");
        return specialOfferConverter.toDetail(getManageableOffer(id));
    }

    // ------------------------------------------------------------------ 内部实现

    private boolean isVisibleToResident(SpecialOffer offer, Resident resident) {
        String type = offer.getTargetType();
        if (type == null || "all".equals(type) || "tag".equals(type)) {
            return true;
        }
        if ("building".equals(type)) {
            String communityId = offer.getCommunityId();
            return !StringUtils.hasText(communityId) || communityId.equals(resident.getCommunityId());
        }
        return false;
    }

    private SpecialOffer getResidentVisibleOffer(String id) {
        Resident resident = residentRepository.findById(UserContext.currentUserId()).orElse(null);
        String propertyCompanyId = resolveResidentPropertyCompanyId(resident);
        SpecialOffer offer = specialOfferRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在"));
        if (propertyCompanyId == null || !propertyCompanyId.equals(offer.getPropertyCompanyId())
                || !"published".equals(offer.getStatus())) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (offer.getStartTime() != null && offer.getStartTime().isAfter(now)) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
        }
        if (offer.getEndTime() != null && offer.getEndTime().isBefore(now)) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
        }
        if (!isVisibleToResident(offer, resident)) {
            if ("role".equals(offer.getTargetType()) && StringUtils.hasText(offer.getMerchantId())) {
                boolean consumed = orderRepository
                        .findDistinctMerchantIdsByResidentId(resident.getId())
                        .contains(offer.getMerchantId());
                if (!consumed) {
                    throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
                }
                return offer;
            }
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
        }
        return offer;
    }

    private String resolveResidentPropertyCompanyId(Resident resident) {
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(propertyCompanyId)) {
            return propertyCompanyId;
        }
        return resident != null ? resident.getPropertyCompanyId() : null;
    }

    private Specification<SpecialOffer> manageSpec(String propertyCompanyId,
                                                   String targetType,
                                                   String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(propertyCompanyId)) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            }
            if (StringUtils.hasText(targetType)) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /** C 端可见：本物业 + 已发布 + 时间窗内 */
    private Specification<SpecialOffer> residentVisibleSpec(String propertyCompanyId, LocalDateTime now) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("propertyCompanyId"), propertyCompanyId));
            predicates.add(cb.equal(root.get("status"), "published"));
            predicates.add(cb.or(
                    cb.isNull(root.get("startTime")),
                    cb.lessThanOrEqualTo(root.get("startTime"), now)));
            predicates.add(cb.or(
                    cb.isNull(root.get("endTime")),
                    cb.greaterThanOrEqualTo(root.get("endTime"), now)));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * @param required true 表示缺值/非法直接 400；false 表示允许空（列表过滤参数）
     */
    private String normalizeTargetType(String targetType, boolean required) {
        if (!StringUtils.hasText(targetType)) {
            if (required) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                        "targetType不能为空；可选 all/building/role/tag（全体业主/指定小区/指定商户/指定标签）");
            }
            return null;
        }
        String raw = targetType.trim();
        String aliased = TARGET_TYPE_ALIASES.get(raw);
        String normalized = aliased != null ? aliased : raw.toLowerCase();
        if (!VALID_TARGET_TYPES.contains(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "targetType不合法：" + targetType + "；可选 all/building/role/tag（全体业主/指定小区/指定商户/指定标签）");
        }
        return normalized;
    }

    private String normalizeStatus(String status, String fallback) {
        if (!StringUtils.hasText(status)) {
            return fallback;
        }
        String raw = status.trim();
        String aliased = STATUS_ALIASES.get(raw);
        String normalized = aliased != null ? aliased : raw.toLowerCase();
        if (!VALID_STATUS.contains(normalized)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "status不合法：" + status + "；可选 draft/published/ended/archived（草稿/已发布/已结束/已归档）");
        }
        return normalized;
    }

    /**
     * 2026-09-08 前端问题：统筹（coordinator）按 §25 可创建/编辑/删除本物业特惠。
     * 统筹走 promotion.push 权限码校验，物业/平台管理员走角色校验；数据范围由
     * {@link #resolvePropertyCompanyId} 与 {@link #getManageableOffer} 收敛到本物业。
     */
    private void requireOfferWrite() {
        // 2026-09-09 前端问题：板块负责人创建/删除特惠 403。
        // §25.1/§25.4/§25.5 的 Roles 含 sector_leader 且未要求其持有 promotion.push，
        // 此前与 coordinator 走同一分支（requireAdmin 只允许 property_admin/platform_admin）导致 403。
        // 现板块负责人按角色放行，数据范围由 resolvePropertyCompanyId / getManageableOffer 收敛到本物业。
        if (roleGuard.isSectorLeader()) {
            return;
        }
        if (roleGuard.isCoordinator()) {
            permissionGuard.requirePermissionOrAdminAllowingCoordinator("promotion.push");
            return;
        }
        roleGuard.requireAnyRole("property_admin", "platform_admin");
    }

    private SpecialOffer getManageableOffer(String id) {
        SpecialOffer offer = specialOfferRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在"));

        if (roleGuard.isPlatformAdmin()) {
            return offer;
        }

        String propertyCompanyId = currentPropertyCompanyId();
        if (propertyCompanyId == null
                || offer.getPropertyCompanyId() == null
                || !propertyCompanyId.equals(offer.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.ANNOUNCEMENT_NOT_FOUND, "特惠不存在");
        }
        return offer;
    }

    private String resolvePropertyCompanyId(String requestedPropertyCompanyId) {
        if (roleGuard.isPlatformAdmin()) {
            if (StringUtils.hasText(requestedPropertyCompanyId)) {
                if (!requestedPropertyCompanyId.startsWith("pc_")) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "propertyCompanyId格式不正确");
                }
                return requestedPropertyCompanyId;
            }
            return null;
        }
        return currentPropertyCompanyId();
    }

    /**
     * 当前操作人的物业公司归属。JWT 未携带 propertyCompanyId 时（板块负责人/统筹等居民衍生角色
     * 的历史 Token）回退到居民档案，避免这些角色创建特惠时 property_company_id 为空。
     */
    private String currentPropertyCompanyId() {
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        if (StringUtils.hasText(propertyCompanyId)) {
            return propertyCompanyId;
        }
        return residentRepository.findById(UserContext.currentUserId())
                .map(Resident::getPropertyCompanyId)
                .orElse(null);
    }

    private void validateId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("so_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ID格式不正确");
        }
    }
}

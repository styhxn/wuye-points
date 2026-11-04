package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.buildingleader.PublishUnitNoticeRequest;
import com.wuye.points.dto.buildingleader.ShareUnitProductRequest;
import com.wuye.points.dto.buildingleader.ShareUnitProductResponse;
import com.wuye.points.dto.buildingleader.ShareableProductItemResponse;
import com.wuye.points.dto.buildingleader.UnitNoticeItemResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.entity.Announcement;
import com.wuye.points.entity.BuildingLeader;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Message;
import com.wuye.points.entity.NeighborBlock;
import com.wuye.points.entity.Product;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.AnnouncementRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.MerchantServiceCommunityRepository;
import com.wuye.points.repository.MessageRepository;
import com.wuye.points.repository.NeighborBlockRepository;
import com.wuye.points.repository.ProductRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.repository.spec.ProductSpecifications;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 楼长工作台「本单元运营」：发布单元通知 + 分享商品给本单元。
 *
 * <p>两条都复用既有表，不新建业务表：</p>
 * <ul>
 *   <li>通知 = {@code announcements} 行 + V37 的 {@code target_units} 定向列
 *       （公告列表/详情按住户 unit 过滤，见 AnnouncementService）；</li>
 *   <li>分享 = 一条 {@code content_type=product} 的产品推荐公告
 *       + 按 {@code messages} 逐户落地的商品卡片消息（{@code card_type=product}）。</li>
 * </ul>
 *
 * <p>定向范围（小区 + 单元）一律取自本人 active 楼长记录，前端不可传，
 * 因此楼长无法越单元投放。</p>
 */
@Service
@RequiredArgsConstructor
public class BuildingLeaderUnitOpsService {

    private static final int NOTICE_DAILY_LIMIT = 3;
    private static final int PRODUCT_SHARE_DAILY_LIMIT = 3;
    private static final String CONTENT_TYPE_NOTICE = "notification";
    private static final String CONTENT_TYPE_PRODUCT = "product";
    private static final String PUBLISHER_ROLE_BUILDING_LEADER = "building_leader";

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementConverter announcementConverter;
    private final MessageRepository messageRepository;
    private final ProductRepository productRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantServiceCommunityRepository merchantServiceCommunityRepository;
    private final ResidentRepository residentRepository;
    private final NeighborBlockRepository neighborBlockRepository;
    private final LeaderIdentityGuard leaderIdentityGuard;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    // ------------------------------------------------------------------ 发布单元通知

    @Transactional
    public UnitNoticeItemResponse publishNotice(PublishUnitNoticeRequest request) {
        BuildingLeader leader = currentActiveLeader();
        assertWithinDailyLimit(leader.getResidentId(), CONTENT_TYPE_NOTICE,
                NOTICE_DAILY_LIMIT, ErrorCode.UNIT_NOTICE_DAILY_LIMIT);

        Announcement announcement = newUnitAnnouncement(leader, CONTENT_TYPE_NOTICE, "announcement_board");
        announcement.setTitle(request.getTitle().trim());
        announcement.setContent(request.getContent().trim());
        announcement.setCoverUrls(announcementConverter.toJson(request.getCoverUrls()));
        announcementRepository.save(announcement);

        return toNoticeItem(announcement);
    }

    @Transactional(readOnly = true)
    public PageResponse<UnitNoticeItemResponse> listMyNotices(int page, int pageSize) {
        BuildingLeader leader = currentActiveLeader();
        return pageNotices(leader.getResidentId(), CONTENT_TYPE_NOTICE, page, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResponse<UnitNoticeItemResponse> listMyShares(int page, int pageSize) {
        BuildingLeader leader = currentActiveLeader();
        return pageNotices(leader.getResidentId(), CONTENT_TYPE_PRODUCT, page, pageSize);
    }

    // ------------------------------------------------------------------ 分享商品给本单元

    @Transactional(readOnly = true)
    public PageResponse<ShareableProductItemResponse> listShareableProducts(
            int page, int pageSize, String keyword) {
        BuildingLeader leader = currentActiveLeader();
        Pageable pageable = PageRequest.of(safePage(page) - 1, safePageSize(pageSize),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> productPage = productRepository.findAll(
                ProductSpecifications.shareableInCommunity(
                        leader.getPropertyCompanyId(), leader.getCommunityId(), keyword),
                pageable);

        Map<String, Merchant> merchantMap = loadMerchants(productPage.getContent());
        List<ShareableProductItemResponse> list = productPage.getContent().stream()
                .map(product -> toShareableItem(product, merchantMap.get(product.getMerchantId())))
                .toList();
        return PageResponse.of(list, safePage(page), safePageSize(pageSize), productPage.getTotalElements());
    }

    @Transactional
    public ShareUnitProductResponse shareProduct(ShareUnitProductRequest request) {
        BuildingLeader leader = currentActiveLeader();
        assertWithinDailyLimit(leader.getResidentId(), CONTENT_TYPE_PRODUCT,
                PRODUCT_SHARE_DAILY_LIMIT, ErrorCode.UNIT_PRODUCT_SHARE_DAILY_LIMIT);

        Product product = productRepository.findById(request.getProductId())
                .filter(item -> item.getDeletedAt() == null && "active".equals(item.getStatus()))
                .orElseThrow(() -> new BizException(ErrorCode.PRODUCT_NOT_FOUND));
        assertProductServesCommunity(product, leader);

        // 先算收件人，避免「一条都没送达却留下公告」
        List<String> recipients = resolveUnitRecipients(leader);
        String shareText = buildShareText(product, request.getRemark());

        Announcement announcement = newUnitAnnouncement(leader, CONTENT_TYPE_PRODUCT, "chat");
        announcement.setProductId(product.getId());
        announcement.setMerchantId(product.getMerchantId());
        announcement.setTitle(product.getName());
        announcement.setContent(shareText);
        announcementRepository.save(announcement);

        int sent = fanOutProductCard(leader, product, announcement, recipients, shareText);

        return ShareUnitProductResponse.builder()
                .announcementId(announcement.getId())
                .productId(product.getId())
                .productName(product.getName())
                .coverUrl(product.getCoverUrl())
                .price(product.getPrice())
                .unit(leader.getUnit())
                .recipientCount(sent)
                .sentAt(DateTimeUtil.toIsoUtc(announcement.getPublishedAt()))
                .build();
    }

    // ------------------------------------------------------------------ 内部实现

    /** 当前登录住户的 active 楼长记录；单元通知与商品分享的定向范围都取自它 */
    private BuildingLeader currentActiveLeader() {
        roleGuard.requireAnyRole("resident");
        BuildingLeader leader = leaderIdentityGuard
                .activeBuildingLeader(UserContext.currentUserId(), UserContext.currentPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.BUILDING_LEADER_NOT_FOUND, "当前用户不是楼长"));
        if (!StringUtils.hasText(leader.getCommunityId()) || !StringUtils.hasText(leader.getUnit())) {
            throw new BizException(ErrorCode.BUILDING_LEADER_NOT_FOUND, "楼长岗位未绑定小区或单元，请联系物业");
        }
        return leader;
    }

    /** 楼长发的公告：单元与角色定向由后端写死，正文一律直接发布（与活动公告同口径，不走物业审核） */
    private Announcement newUnitAnnouncement(BuildingLeader leader, String contentType, String deliveryChannel) {
        Announcement announcement = new Announcement();
        announcement.setId(idGenerator.generateId("ann"));
        announcement.setAnnouncementType("community");
        announcement.setContentType(contentType);
        announcement.setDeliveryChannel(deliveryChannel);
        announcement.setPropertyCompanyId(leader.getPropertyCompanyId());
        announcement.setCommunityId(leader.getCommunityId());
        announcement.setPublisherId(leader.getResidentId());
        announcement.setPublisherRole(PUBLISHER_ROLE_BUILDING_LEADER);
        announcement.setTargetRoles(announcementConverter.toJson(List.of("resident")));
        announcement.setTargetUnits(announcementConverter.toJson(List.of(leader.getUnit())));
        announcement.setCollectEnabled(false);
        announcement.setStatus("published");
        announcement.setPublishedAt(LocalDateTime.now());
        return announcement;
    }

    private void assertWithinDailyLimit(String publisherId, String contentType,
                                        int limit, ErrorCode limitError) {
        // 与 created_at 同一时钟源（JVM 默认时区的 LocalDateTime），避免跨时区部署时限频失效
        LocalDateTime dayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        long todayCount = announcementRepository
                .countByPublisherIdAndContentTypeAndCreatedAtGreaterThanEqual(publisherId, contentType, dayStart);
        if (todayCount >= limit) {
            throw new BizException(limitError);
        }
    }

    /** 商品所属商家须在本物业下、审核通过且在营，并声明服务该楼长所在小区 */
    private void assertProductServesCommunity(Product product, BuildingLeader leader) {
        Merchant merchant = merchantRepository.findById(product.getMerchantId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        boolean sameProperty = leader.getPropertyCompanyId() != null
                && leader.getPropertyCompanyId().equals(merchant.getPropertyCompanyId());
        boolean operable = "approved".equals(merchant.getAuditStatus()) && "active".equals(merchant.getStatus());
        // serve_all_communities 为空按建表默认值 1（全小区通用）处理
        boolean servesCommunity = !Boolean.FALSE.equals(merchant.getServeAllCommunities())
                || merchantServiceCommunityRepository.existsByMerchantIdAndCommunityId(
                        merchant.getId(), leader.getCommunityId());
        if (!sameProperty || !operable || !servesCommunity) {
            throw new BizException(ErrorCode.PRODUCT_NOT_IN_COMMUNITY);
        }
    }

    /** 本单元其他 active 住户，剔除本人和已屏蔽该楼长的住户 */
    private List<String> resolveUnitRecipients(BuildingLeader leader) {
        List<String> memberIds = residentRepository.findByPropertyCompanyIdAndCommunityIdAndUnitAndStatus(
                        leader.getPropertyCompanyId(), leader.getCommunityId(), leader.getUnit(), "active")
                .stream()
                .map(Resident::getId)
                .filter(id -> !id.equals(leader.getResidentId()))
                .toList();
        if (memberIds.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "本单元暂无其他住户");
        }
        Set<String> blockedIds = neighborBlockRepository
                .findByBlockerIdInAndBlockedId(memberIds, leader.getResidentId())
                .stream()
                .map(NeighborBlock::getBlockerId)
                .collect(Collectors.toSet());
        return memberIds.stream().filter(id -> !blockedIds.contains(id)).toList();
    }

    private int fanOutProductCard(BuildingLeader leader, Product product, Announcement announcement,
                                  List<String> recipients, String shareText) {
        int sent = 0;
        for (String recipientId : recipients) {
            Message message = new Message();
            message.setId(idGenerator.generateId("msg"));
            message.setFromId(leader.getResidentId());
            message.setToId(recipientId);
            message.setMessageType("resident_message");
            message.setChatType("private");
            message.setSenderIdentity("personal");
            message.setContent(shareText);
            message.setReadStatus("unread");
            message.setPropertyCompanyId(leader.getPropertyCompanyId());
            message.setCardType("product");
            message.setCardId(product.getId());
            message.setRelatedTaskId(announcement.getId());
            messageRepository.save(message);
            sent++;
        }
        return sent;
    }

    private String buildShareText(Product product, String remark) {
        StringBuilder text = new StringBuilder("【楼长推荐】")
                .append(product.getName())
                .append(" ￥").append(amount(product.getPrice()));
        if (StringUtils.hasText(remark)) {
            text.append("｜").append(remark.trim());
        }
        return text.toString();
    }

    private PageResponse<UnitNoticeItemResponse> pageNotices(
            String publisherId, String contentType, int page, int pageSize) {
        Pageable pageable = PageRequest.of(safePage(page) - 1, safePageSize(pageSize),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Announcement> announcementPage = announcementRepository
                .findByPublisherIdAndContentType(publisherId, contentType, pageable);
        List<UnitNoticeItemResponse> list = announcementPage.getContent().stream()
                .map(this::toNoticeItem)
                .toList();
        return PageResponse.of(list, safePage(page), safePageSize(pageSize), announcementPage.getTotalElements());
    }

    private UnitNoticeItemResponse toNoticeItem(Announcement announcement) {
        return UnitNoticeItemResponse.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .contentType(announcement.getContentType())
                .targetUnits(announcementConverter.parseStringList(announcement.getTargetUnits()))
                .deliveryChannel(announcement.getDeliveryChannel())
                .productId(announcement.getProductId())
                .status(announcement.getStatus())
                .publishedAt(DateTimeUtil.toIsoUtc(announcement.getPublishedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(announcement.getCreatedAt()))
                .build();
    }

    private Map<String, Merchant> loadMerchants(List<Product> products) {
        List<String> merchantIds = products.stream()
                .map(Product::getMerchantId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (merchantIds.isEmpty()) {
            return Map.of();
        }
        return merchantRepository.findAllById(merchantIds).stream()
                .collect(Collectors.toMap(Merchant::getId, Function.identity(), (a, b) -> a));
    }

    private ShareableProductItemResponse toShareableItem(Product product, Merchant merchant) {
        return ShareableProductItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .coverUrl(product.getCoverUrl())
                .description(product.getDescription())
                .price(product.getPrice())
                .memberPrice(product.getMemberPrice())
                .pointPrice(product.getPointPrice())
                .stock(product.getStock())
                .category(product.getCategory())
                .merchantId(product.getMerchantId())
                .merchantName(merchant != null ? merchant.getName() : null)
                .build();
    }

    private String amount(BigDecimal value) {
        return value == null ? "0.00" : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private int safePage(int page) {
        return Math.max(page, 1);
    }

    private int safePageSize(int pageSize) {
        if (pageSize < 1 || pageSize > 100) {
            throw new BizException(ErrorCode.PARAM_OUT_OF_RANGE);
        }
        return pageSize;
    }
}

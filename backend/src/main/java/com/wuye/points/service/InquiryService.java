package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.inquiry.CreateInquiryRequest;
import com.wuye.points.dto.inquiry.InquiryDetailResponse;
import com.wuye.points.dto.inquiry.InquiryListItemResponse;
import com.wuye.points.dto.inquiry.InquiryReplyRequest;
import com.wuye.points.dto.message.CreateMessageRequest;
import com.wuye.points.entity.Inquiry;
import com.wuye.points.entity.Merchant;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.InquiryRepository;
import com.wuye.points.repository.MerchantRepository;
import com.wuye.points.repository.ResidentRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private static final Set<String> REPLY_ACTIONS = Set.of("quote", "accept", "reject");

    private final InquiryRepository inquiryRepository;
    private final MerchantRepository merchantRepository;
    private final ResidentRepository residentRepository;
    private final MessageService messageService;
    private final RoleGuard roleGuard;
    private final AdminSupport adminSupport;
    private final SnowflakeIdGenerator idGenerator;

    @Transactional
    public InquiryDetailResponse createInquiry(CreateInquiryRequest request) {
        roleGuard.requireAnyRole("resident");
        adminSupport.validateIdPrefix(request.getMerchantId(), "mch_");

        String currentUserId = UserContext.currentUserId();
        Resident resident = residentRepository.findById(currentUserId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        String propertyCompanyId = resident.getPropertyCompanyId();

        Merchant merchant = merchantRepository.findByIdAndPropertyCompanyId(
                        request.getMerchantId(), propertyCompanyId)
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
        if (!"active".equals(merchant.getStatus())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND, "商家已下线");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setId(idGenerator.generateId("inq"));
        inquiry.setPropertyCompanyId(propertyCompanyId);
        inquiry.setInquirerId(currentUserId);
        inquiry.setMerchantId(merchant.getId());
        inquiry.setProductId(request.getProductId());
        inquiry.setProductName(request.getProductName());
        inquiry.setContent(request.getContent());
        inquiry.setStatus("pending");
        inquiryRepository.save(inquiry);

        return toDetail(inquiry);
    }

    @Transactional(readOnly = true)
    public PageResponse<InquiryListItemResponse> listMyInquiries(int page, int pageSize) {
        roleGuard.requireAnyRole("resident");

        String currentUserId = UserContext.currentUserId();
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<Inquiry> inquiryPage = inquiryRepository.findByInquirerIdOrderByCreatedAtDesc(currentUserId, pageable);

        List<InquiryListItemResponse> list = inquiryPage.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(list, page, pageSize, inquiryPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<InquiryListItemResponse> listMerchantInquiries(int page, int pageSize) {
        roleGuard.requireAnyRole("merchant");

        Merchant merchant = resolveCurrentMerchant();
        Pageable pageable = adminSupport.buildPageable(page, pageSize, "createdAt");
        Page<Inquiry> inquiryPage = inquiryRepository.findByMerchantIdOrderByCreatedAtDesc(
                merchant.getId(), pageable);

        List<InquiryListItemResponse> list = inquiryPage.getContent().stream()
                .map(this::toListItem)
                .toList();
        return PageResponse.of(list, page, pageSize, inquiryPage.getTotalElements());
    }

    @Transactional
    public InquiryDetailResponse replyInquiry(String id, InquiryReplyRequest request) {
        adminSupport.validateIdPrefix(id, "inq_");
        String action = request.getAction();
        if (!StringUtils.hasText(action) || !REPLY_ACTIONS.contains(action)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action不合法");
        }

        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.PARAM_INVALID, "询价记录不存在"));

        if ("quote".equals(action)) {
            roleGuard.requireAnyRole("merchant");
            Merchant merchant = resolveCurrentMerchant();
            if (!inquiry.getMerchantId().equals(merchant.getId())) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权回复非本商家的询价");
            }
            if (request.getQuotedPrice() == null) {
                throw new BizException(ErrorCode.PARAM_MISSING, "quotedPrice不能为空");
            }
            if (!"pending".equals(inquiry.getStatus()) && !"quoted".equals(inquiry.getStatus())) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "询价状态不允许报价");
            }
            inquiry.setQuotedPrice(request.getQuotedPrice());
            inquiry.setQuotedRemark(request.getQuotedRemark());
            inquiry.setStatus("quoted");
            inquiry.setQuotedAt(LocalDateTime.now());
            inquiryRepository.save(inquiry);
            // B2: 报价后自动通过聊天会话发送报价消息给询价人
            sendInquiryMessage(inquiry.getInquirerId(), buildQuoteMessageContent(inquiry));
        } else if ("accept".equals(action)) {
            roleGuard.requireAnyRole("resident");
            String currentUserId = UserContext.currentUserId();
            if (!inquiry.getInquirerId().equals(currentUserId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作非本人的询价");
            }
            if (!"quoted".equals(inquiry.getStatus())) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "仅可接受已报价的询价");
            }
            inquiry.setStatus("accepted");
            inquiry.setClosedAt(LocalDateTime.now());
            inquiryRepository.save(inquiry);
            // B2: 接受报价后通知商家
            String merchantUserId = resolveMerchantUserId(inquiry.getMerchantId());
            if (merchantUserId != null) {
                sendInquiryMessage(merchantUserId,
                        "用户已接受您的报价（询价单号：" + inquiry.getId() + "）");
            }
        } else { // reject
            roleGuard.requireAnyRole("resident");
            String currentUserId = UserContext.currentUserId();
            if (!inquiry.getInquirerId().equals(currentUserId)) {
                throw new BizException(ErrorCode.PERMISSION_DENIED, "无权操作非本人的询价");
            }
            if (!"quoted".equals(inquiry.getStatus())) {
                throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "仅可拒绝已报价的询价");
            }
            inquiry.setStatus("rejected");
            inquiry.setClosedAt(LocalDateTime.now());
            inquiryRepository.save(inquiry);
            // B2: 拒绝报价后通知商家
            String merchantUserId = resolveMerchantUserId(inquiry.getMerchantId());
            if (merchantUserId != null) {
                sendInquiryMessage(merchantUserId,
                        "用户已拒绝您的报价（询价单号：" + inquiry.getId() + "）");
            }
        }

        return toDetail(inquiry);
    }

    /** B2: 通过消息系统发送询价相关通知（发送者为当前用户），使用独立事务避免消息失败影响主流程 */
    private void sendInquiryMessage(String toUserId, String content) {
        try {
            CreateMessageRequest msg = new CreateMessageRequest();
            msg.setToId(toUserId);
            msg.setContent(content);
            msg.setMessageType("community_message");
            msg.setChatType("private");
            msg.setToRole(null);
            messageService.sendMessageAsync(msg);
        } catch (Exception ignored) {
            // 聊天消息发送失败不影响询价主流程
        }
    }

    /** 通过 merchantId 反查商家用户 residentId */
    private String resolveMerchantUserId(String merchantId) {
        return merchantRepository.findById(merchantId)
                .map(Merchant::getContactPhone)
                .flatMap(residentRepository::findByPhone)
                .map(Resident::getId)
                .orElse(null);
    }

    private String buildQuoteMessageContent(Inquiry inquiry) {
        StringBuilder sb = new StringBuilder();
        sb.append("【报价通知】询价单号：").append(inquiry.getId());
        if (inquiry.getProductName() != null) {
            sb.append("，商品：").append(inquiry.getProductName());
        }
        sb.append("，报价：").append(inquiry.getQuotedPrice()).append("元");
        if (inquiry.getQuotedRemark() != null) {
            sb.append("，备注：").append(inquiry.getQuotedRemark());
        }
        return sb.toString();
    }

    private Merchant resolveCurrentMerchant() {
        Resident merchantUser = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (!StringUtils.hasText(merchantUser.getPhone())) {
            throw new BizException(ErrorCode.MERCHANT_NOT_FOUND);
        }
        return merchantRepository
                .findFirstByContactPhoneAndPropertyCompanyIdOrderByCreatedAtDesc(
                        merchantUser.getPhone(), merchantUser.getPropertyCompanyId())
                .orElseThrow(() -> new BizException(ErrorCode.MERCHANT_NOT_FOUND));
    }

    private InquiryListItemResponse toListItem(Inquiry inquiry) {
        return InquiryListItemResponse.builder()
                .id(inquiry.getId())
                .propertyCompanyId(inquiry.getPropertyCompanyId())
                .inquirerId(inquiry.getInquirerId())
                .merchantId(inquiry.getMerchantId())
                .productId(inquiry.getProductId())
                .productName(inquiry.getProductName())
                .content(inquiry.getContent())
                .quotedPrice(inquiry.getQuotedPrice())
                .quotedRemark(inquiry.getQuotedRemark())
                .status(inquiry.getStatus())
                .quotedAt(DateTimeUtil.toIsoUtc(inquiry.getQuotedAt()))
                .closedAt(DateTimeUtil.toIsoUtc(inquiry.getClosedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(inquiry.getCreatedAt()))
                .build();
    }

    private InquiryDetailResponse toDetail(Inquiry inquiry) {
        return InquiryDetailResponse.builder()
                .id(inquiry.getId())
                .propertyCompanyId(inquiry.getPropertyCompanyId())
                .inquirerId(inquiry.getInquirerId())
                .merchantId(inquiry.getMerchantId())
                .productId(inquiry.getProductId())
                .productName(inquiry.getProductName())
                .content(inquiry.getContent())
                .quotedPrice(inquiry.getQuotedPrice())
                .quotedRemark(inquiry.getQuotedRemark())
                .status(inquiry.getStatus())
                .quotedAt(DateTimeUtil.toIsoUtc(inquiry.getQuotedAt()))
                .closedAt(DateTimeUtil.toIsoUtc(inquiry.getClosedAt()))
                .createdAt(DateTimeUtil.toIsoUtc(inquiry.getCreatedAt()))
                .updatedAt(DateTimeUtil.toIsoUtc(inquiry.getUpdatedAt()))
                .build();
    }
}

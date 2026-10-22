package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.community.ForumBanRequest;
import com.wuye.points.dto.community.ResidentForumBanItemResponse;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.ResidentForumBan;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.CommunityCommentRepository;
import com.wuye.points.repository.CommunityPostRepository;
import com.wuye.points.repository.ResidentForumBanRepository;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.PhoneUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 社区论坛禁言（影子封禁）。
 *
 * <p>物业公司禁止某住户发帖/评论：被禁用户无感知，发帖/评论照常成功且本人可见，
 * 其他住户不可见（内容查询层按 shadow_banned + 作者可见性过滤）。</p>
 *
 * <p>禁言/解禁的计数口径：帖子 comment_count 只统计「对外可见」的评论。
 * 禁言时先把该作者已计数评论从各帖扣回再打标；解禁时先加回再取消打标，
 * 保证「评论数」与实际可见列表始终一致。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumBanService {

    private final ResidentForumBanRepository banRepository;
    private final ResidentRepository residentRepository;
    private final CommunityPostRepository postRepository;
    private final CommunityCommentRepository commentRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 当前用户是否处于生效中的论坛禁言（永久或未到期）。
     * 写入路径（发帖/评论）调用：命中时不拦截，仅用于内容打标。
     */
    @Transactional(readOnly = true)
    public boolean isCurrentlyBanned(Resident resident) {
        if (resident == null) {
            return false;
        }
        Optional<ResidentForumBan> ban = findActiveBan(resident.getId());
        if (ban.isEmpty()) {
            return false;
        }
        ResidentForumBan active = ban.get();
        LocalDateTime expiresAt = active.getExpiresAt();
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * 管理端禁言某住户（影子封禁）。
     * 权限：property_admin / platform_admin；数据范围限本物业公司（platform_admin 无归属物业时可跨物业）。
     */
    @Transactional
    public ResidentForumBanItemResponse ban(String targetResidentId, ForumBanRequest request) {
        Resident admin = requireAdmin();
        Resident target = requireTargetResident(targetResidentId, admin);

        Optional<ResidentForumBan> existing = findActiveBan(target.getId());
        if (existing.isPresent()) {
            throw new BizException(ErrorCode.FORUM_BAN_ALREADY_ACTIVE, "该用户已被禁言，无需重复操作");
        }

        LocalDateTime expiresAt = null;
        if (StringUtils.hasText(request.getExpiresAt())) {
            expiresAt = DateTimeUtil.parseLocalDateTime(request.getExpiresAt().trim());
            if (expiresAt == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "expiresAt 格式不正确（支持 yyyy-MM-ddTHH:mm:ss）");
            }
            if (!expiresAt.isAfter(LocalDateTime.now())) {
                throw new BizException(ErrorCode.PARAM_INVALID, "expiresAt 必须晚于当前时间");
            }
        }

        ResidentForumBan ban = new ResidentForumBan();
        ban.setId(idGenerator.generateId("rfb"));
        ban.setResidentId(target.getId());
        ban.setPropertyCompanyId(target.getPropertyCompanyId());
        ban.setCommunityId(target.getCommunityId());
        ban.setReason(StringUtils.hasText(request.getReason()) ? request.getReason().trim() : null);
        ban.setBannedBy(admin.getId());
        ban.setBannedAt(LocalDateTime.now());
        ban.setExpiresAt(expiresAt);
        ban.setStatus("active");
        banRepository.save(ban);

        applyShadowFlags(target.getId(), true);
        log.info("[论坛禁言] 已禁言: residentId={}, by={}, expiresAt={}, reason={}",
                target.getId(), admin.getId(), expiresAt, ban.getReason());
        return toItemResponse(ban, target, admin);
    }

    /**
     * 管理端解除禁言：恢复内容可见并回补评论计数。
     */
    @Transactional
    public ResidentForumBanItemResponse unban(String targetResidentId) {
        Resident admin = requireAdmin();
        Resident target = requireTargetResident(targetResidentId, admin);

        ResidentForumBan ban = findActiveBan(target.getId())
                .orElseThrow(() -> new BizException(ErrorCode.FORUM_BAN_NOT_FOUND, "该用户没有被生效中的禁言记录"));
        liftBan(ban, admin, "lifted");
        log.info("[论坛禁言] 已解除: residentId={}, by={}", target.getId(), admin.getId());
        return toItemResponse(ban, target, admin);
    }

    /**
     * 管理端禁言列表（分页，按物业公司范围）。
     */
    @Transactional(readOnly = true)
    public PageResponse<ResidentForumBanItemResponse> listBans(int page, int pageSize, String status) {
        Resident admin = requireAdmin();
        int p = Math.max(0, page - 1);
        int size = Math.min(Math.max(pageSize, 1), 100);
        Specification<ResidentForumBan> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(admin.getPropertyCompanyId())) {
                predicates.add(cb.equal(root.get("propertyCompanyId"), admin.getPropertyCompanyId()));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            query.orderBy(cb.desc(root.get("bannedAt")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        var result = banRepository.findAll(spec, PageRequest.of(p, size, Sort.by(Sort.Direction.DESC, "bannedAt")));
        List<ResidentForumBan> bans = result.getContent();
        Map<String, Resident> residentMap = bans.isEmpty() ? Map.of()
                : residentRepository.findAllById(bans.stream()
                        .map(ResidentForumBan::getResidentId).distinct().toList())
                .stream().collect(Collectors.toMap(Resident::getId, r -> r, (a, b) -> a));
        Map<String, Resident> adminMap = bans.isEmpty() ? Map.of()
                : residentRepository.findAllById(bans.stream()
                        .map(ResidentForumBan::getBannedBy).filter(StringUtils::hasText).distinct().toList())
                .stream().collect(Collectors.toMap(Resident::getId, r -> r, (a, b) -> a));
        List<ResidentForumBanItemResponse> list = bans.stream()
                .map(b -> toItemResponse(b, residentMap.get(b.getResidentId()), adminMap.get(b.getBannedBy())))
                .toList();
        return PageResponse.of(list, page, size, result.getTotalElements());
    }

    /**
     * 到期自动解除（调度器调用）：找到已到期但仍 active 的禁言，逐条按 lifted 同款逻辑恢复。
     *
     * @return 本次自动解除的禁言条数
     */
    @Transactional
    public int liftExpiredBans() {
        List<ResidentForumBan> expired = banRepository
                .findByStatusAndExpiresAtNotNullAndExpiresAtLessThanEqual("active", LocalDateTime.now());
        for (ResidentForumBan ban : expired) {
            try {
                liftBan(ban, null, "expired");
                log.info("[论坛禁言] 到期自动解除: banId={}, residentId={}", ban.getId(), ban.getResidentId());
            } catch (Exception e) {
                log.error("[论坛禁言] 到期自动解除失败: banId={}", ban.getId(), e);
            }
        }
        return expired.size();
    }

    // ------------------------------------------------------------------ 内部

    /** 解除禁言：先回补计数/取消打标，再落状态（与 ban 的计数调整严格互逆） */
    private void liftBan(ResidentForumBan ban, Resident operator, String status) {
        postRepository.incrementCommentCountForUnbannedAuthor(ban.getResidentId());
        postRepository.updateShadowBannedByAuthor(ban.getResidentId(), false);
        commentRepository.updateShadowBannedByAuthor(ban.getResidentId(), false);
        ban.setStatus(status);
        ban.setLiftedBy(operator != null ? operator.getId() : null);
        ban.setLiftedAt(LocalDateTime.now());
        banRepository.save(ban);
    }

    /** 禁言生效：先把该作者已计数评论从各帖扣回，再给内容打标（顺序不可颠倒） */
    private void applyShadowFlags(String residentId, boolean banned) {
        if (banned) {
            postRepository.decrementCommentCountForBannedAuthor(residentId);
        } else {
            postRepository.incrementCommentCountForUnbannedAuthor(residentId);
        }
        postRepository.updateShadowBannedByAuthor(residentId, banned);
        commentRepository.updateShadowBannedByAuthor(residentId, banned);
    }

    private Optional<ResidentForumBan> findActiveBan(String residentId) {
        return banRepository.findFirstByResidentIdAndStatusOrderByBannedAtDesc(residentId, "active");
    }

    private Resident requireAdmin() {
        roleGuard.requireAnyRole("property_admin", "platform_admin");
        Resident admin = residentRepository.findById(UserContext.currentUserId())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        return admin;
    }

    private Resident requireTargetResident(String targetResidentId, Resident admin) {
        if (!StringUtils.hasText(targetResidentId) || !targetResidentId.startsWith("res_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "residentId 格式不正确");
        }
        Resident target = residentRepository.findById(targetResidentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND, "用户不存在"));
        // 数据范围：管理员有归属物业时必须同物业；platform_admin 无归属物业（如平台超管）可跨物业
        if (StringUtils.hasText(admin.getPropertyCompanyId())
                && !admin.getPropertyCompanyId().equals(target.getPropertyCompanyId())) {
            throw new BizException(ErrorCode.PERMISSION_DENIED, "无权禁言其他物业公司的用户");
        }
        return target;
    }

    private ResidentForumBanItemResponse toItemResponse(ResidentForumBan ban, Resident target, Resident operator) {
        return ResidentForumBanItemResponse.builder()
                .id(ban.getId())
                .residentId(ban.getResidentId())
                .residentName(target != null ? target.getName() : null)
                .residentPhone(target != null ? PhoneUtil.mask(target.getPhone()) : null)
                .propertyCompanyId(ban.getPropertyCompanyId())
                .reason(ban.getReason())
                .bannedBy(ban.getBannedBy())
                .bannedByName(operator != null ? operator.getName() : null)
                .bannedAt(DateTimeUtil.toIsoUtc(ban.getBannedAt()))
                .expiresAt(ban.getExpiresAt() != null
                        ? ban.getExpiresAt().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null)
                .status(ban.getStatus())
                .build();
    }
}

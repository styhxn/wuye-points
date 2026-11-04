package com.wuye.points.payment;

import com.wuye.points.dto.admin.settlement.AutoRegisterReceiverResult;
import com.wuye.points.dto.admin.settlement.AutoRegisterReceiversRequest;
import com.wuye.points.dto.admin.settlement.ReceiverPrincipalResponse;
import com.wuye.points.dto.admin.settlement.WechatSplitReceiverRequest;
import com.wuye.points.dto.admin.settlement.WechatSplitReceiverResponse;
import com.wuye.points.entity.Resident;
import com.wuye.points.entity.WechatSplitReceiverAccount;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.WechatSplitReceiverAccountRepository;
import com.wuye.points.util.SensitiveMasker;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 微信分账收款账户管理（运营后台）。
 *
 * <p>维护 {@code wechat_split_receiver_accounts}：物业公司（商户号）、协调员/负责人/楼长/经理/快递员
 * （个人 openid）。手填通道只接受商户号，个人 openid 一律由 {@link #autoRegister} 按主体反查写入。
 * 只有 verified=true 的账户才会在订单完成时作为分账接收方；缺户/待核实的记录保持 {@code skipped}，
 * 由 {@code SplitWindowScheduler#retrySkippedSplitRecords}（每日 03:20）在账户补齐后自动补分。</p>
 *
 * <p>微信接收方类型按账户号格式推导：8~20 位纯数字 = 商户号（MERCHANT_ID，可回退），
 * 其余 = 个人 openid（PERSONAL_OPENID，落微信零钱）。实际的
 * {@code /v3/profitsharing/receivers/add} 在首次分账时按商家特约商户号惰性调用（幂等），
 * 运营无需为每个商家重复登记。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatSplitReceiverAdminService {

    /** 平台主体走服务商自身商户号特判，不允许在账户表登记（避免覆盖平台佣金落点）。 */
    private static final String PLATFORM = WechatSplitOwnerTypes.PLATFORM.name();

    private static final Set<String> ALLOWED_OWNER_TYPES = Arrays.stream(WechatSplitOwnerTypes.values())
            .map(Enum::name)
            .filter(t -> !PLATFORM.equals(t))
            .collect(Collectors.toUnmodifiableSet());

    private final WechatSplitReceiverAccountRepository repository;
    private final SnowflakeIdGenerator idGenerator;
    private final SplitReceiverPrincipalResolver principalResolver;

    @Transactional(readOnly = true)
    public List<WechatSplitReceiverResponse> list(String ownerType) {
        List<WechatSplitReceiverAccount> list = StringUtils.hasText(ownerType)
                ? repository.findByOwnerTypeOrderByCreatedAtDesc(ownerType.trim().toUpperCase())
                : repository.findAllByOrderByCreatedAtDesc();
        return list.stream().map(this::toResponse).toList();
    }

    /**
     * 新增或覆盖同一主体的收款账户（ownerType + ownerId 唯一）。
     *
     * <p>管理端手填只接受微信商户号。openid 是打款地址，抄错一位会静默打给另一个人，
     * 且 (ownerType, ownerId) 唯一索引一旦被错误值占用就无法再绑回正确账号，
     * 因此个人接收方一律走 {@link #autoRegister}，由后端按主体反查 openid。</p>
     */
    @Transactional
    public WechatSplitReceiverResponse save(WechatSplitReceiverRequest request) {
        String ownerType = normalizeOwnerType(request.getOwnerType());
        String accountNo = request.getAccountNo().trim();
        if (!"MERCHANT_ID".equals(receiverType(accountNo))) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "个人 openid 不支持手填登记，请改用 POST /admin/wechat-split/receivers/auto"
                            + "（只传 ownerType + ownerIds，openid 由后端按主体反查）");
        }
        return upsert(ownerType, request.getOwnerId().trim(), accountNo, request.getAccountName(),
                request.getVerified() == null || request.getVerified());
    }

    private WechatSplitReceiverResponse upsert(String ownerType, String ownerId, String accountNo,
                                               String accountName, boolean verified) {
        validateAccount(ownerType, accountNo, accountName);

        WechatSplitReceiverAccount account = repository
                .findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .orElseGet(() -> {
                    WechatSplitReceiverAccount a = new WechatSplitReceiverAccount();
                    a.setId(idGenerator.generateId("wsra_"));
                    a.setOwnerType(ownerType);
                    a.setOwnerId(ownerId);
                    a.setCreatedAt(LocalDateTime.now());
                    return a;
                });
        account.setAccountNo(accountNo);
        account.setAccountName(trimToNull(accountName));
        account.setCurrency("CNY");
        account.setVerified(verified);
        account.setUpdatedAt(LocalDateTime.now());
        repository.save(account);
        log.info("[分账账户] 保存收款账户 ownerType={} ownerId={} receiverType={} verified={}",
                ownerType, ownerId, receiverType(accountNo), account.isVerified());
        return toResponse(account);
    }

    @Transactional
    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "收款账户不存在: " + id);
        }
        repository.deleteById(id);
        log.info("[分账账户] 删除收款账户 id={}", id);
    }

    // ---------------------------------------------------------------------
    // 按主体自动登记（openid 由后端反查，前端不碰这串字符）
    // ---------------------------------------------------------------------

    /**
     * 批量预检个人接收方主体：能否登记、是否已登记。openid 只回脱敏值。
     */
    @Transactional(readOnly = true)
    public List<ReceiverPrincipalResponse> listPrincipals(String ownerType, List<String> ownerIds) {
        String type = requirePersonalOwnerType(ownerType);
        List<ReceiverPrincipalResponse> items = new ArrayList<>();
        if (ownerIds == null) {
            return items;
        }
        for (String rawId : ownerIds) {
            String ownerId = rawId == null ? null : rawId.trim();
            if (StringUtils.hasText(ownerId)) {
                items.add(toPrincipalResponse(type, ownerId));
            }
        }
        return items;
    }

    /**
     * 按主体批量登记个人接收方。逐条返回结果：个别人未认领微信身份不阻断整批，
     * 但会以 {@code skipped} + 原因回给运营，避免"看起来登记成功了"。
     */
    @Transactional
    public List<AutoRegisterReceiverResult> autoRegister(AutoRegisterReceiversRequest request) {
        String type = requirePersonalOwnerType(request.getOwnerType());
        boolean verified = request.getVerified() == null || request.getVerified();
        List<AutoRegisterReceiverResult> results = new ArrayList<>();
        for (String rawId : request.getOwnerIds()) {
            String ownerId = rawId == null ? null : rawId.trim();
            if (StringUtils.hasText(ownerId)) {
                results.add(autoRegisterOne(type, ownerId, verified));
            }
        }
        return results;
    }

    private String requirePersonalOwnerType(String ownerType) {
        String type = normalizeOwnerType(ownerType);
        if (!principalResolver.supportsPersonal(type)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "主体 " + type + " 不支持按 openid 自动登记：物业公司请登记商户号，平台固定用服务商商户号");
        }
        return type;
    }

    private ReceiverPrincipalResponse toPrincipalResponse(String type, String ownerId) {
        Resident resident = principalResolver.loadResident(type, ownerId);
        WechatSplitReceiverAccount existing = repository.findByOwnerTypeAndOwnerId(type, ownerId).orElse(null);
        ReceiverPrincipalResponse.ReceiverPrincipalResponseBuilder builder = ReceiverPrincipalResponse.builder()
                .ownerType(type)
                .ownerId(ownerId)
                .registered(existing != null)
                .receiverAccountId(existing == null ? null : existing.getId());
        if (resident == null) {
            return builder.openidAvailable(false)
                    .unavailableReason(principalResolver.describe(type) + "主体记录不存在或未关联住户账号")
                    .build();
        }
        builder.name(resident.getName())
                .phoneMasked(SensitiveMasker.maskPhone(resident.getPhone()))
                .propertyCompanyId(resident.getPropertyCompanyId());
        String openid = resident.getOpenid();
        if (!StringUtils.hasText(openid)) {
            return builder.openidAvailable(false)
                    .unavailableReason("该账号未认领微信身份（无 openid），请本人在小程序登录一次")
                    .build();
        }
        if (!openid.matches("[A-Za-z0-9_-]{20,64}")) {
            return builder.openidAvailable(false).openidMasked(maskOpenid(openid))
                    .unavailableReason("openid 格式异常（应为 20~64 位字母/数字/_/-），请先修复该账号的微信身份")
                    .build();
        }
        return builder.openidAvailable(true).openidMasked(maskOpenid(openid)).build();
    }

    private AutoRegisterReceiverResult autoRegisterOne(String type, String ownerId, boolean verified) {
        Resident resident = principalResolver.loadResident(type, ownerId);
        if (resident == null) {
            return skipped(ownerId, null,
                    principalResolver.describe(type) + "主体记录不存在或未关联住户账号");
        }
        String openid = resident.getOpenid();
        if (!StringUtils.hasText(openid)) {
            return skipped(ownerId, resident.getName(), "该账号未认领微信身份（无 openid），请本人在小程序登录一次");
        }
        // 个人 openid 不传姓名：微信会校验实名一致性，住户表姓名未必等于微信实名，填错反而被拒
        try {
            WechatSplitReceiverResponse saved = upsert(type, ownerId, openid, null, verified);
            return AutoRegisterReceiverResult.builder()
                    .ownerId(ownerId)
                    .name(resident.getName())
                    .status("registered")
                    .openidMasked(maskOpenid(openid))
                    .receiverAccountId(saved.getId())
                    .build();
        } catch (BizException e) {
            return skipped(ownerId, resident.getName(), e.getMessage());
        }
    }

    private static AutoRegisterReceiverResult skipped(String ownerId, String name, String reason) {
        return AutoRegisterReceiverResult.builder()
                .ownerId(ownerId).name(name).status("skipped").reason(reason).build();
    }

    private static String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) {
            return openid;
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }

    private String normalizeOwnerType(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new BizException(ErrorCode.PARAM_MISSING, "主体类型不能为空");
        }
        String ownerType = raw.trim().toUpperCase();
        if (!ALLOWED_OWNER_TYPES.contains(ownerType)) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "不支持的主体类型: " + raw + "（平台主体固定使用服务商商户号，无需登记）");
        }
        return ownerType;
    }

    private void validateAccount(String ownerType, String accountNo, String accountName) {
        String receiverType = receiverType(accountNo);
        if ("MERCHANT_ID".equals(receiverType)) {
            if (!StringUtils.hasText(accountName)) {
                throw new BizException(ErrorCode.PARAM_MISSING, "商户号接收方必须填写商户全称（添加接收方时微信要求）");
            }
            if (!WechatSplitOwnerTypes.PROPERTY.name().equals(ownerType)
                    && !WechatSplitOwnerTypes.MERCHANT.name().equals(ownerType)) {
                log.warn("[分账账户] 主体 {} 登记了商户号收款账户 {}，请确认业务关系",
                        ownerType, accountNo);
            }
            return;
        }
        if (!accountNo.matches("[A-Za-z0-9_-]{20,64}")) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "个人收款账户应为微信 openid（20~64 位字母/数字/_/-），或 8~20 位纯数字商户号");
        }
        // 个人 openid 的 name 为选传：传了微信会校验实名一致性，填错反而报错；
        // 建议填写，但允许留空（微信按无姓名提交）。
    }

    /** 8~20 位纯数字视为微信商户号，其余视为个人 openid（与分账回退能力判断保持一致）。 */
    static String receiverType(String accountNo) {
        return accountNo != null && accountNo.trim().matches("\\d{8,20}") ? "MERCHANT_ID" : "PERSONAL_OPENID";
    }

    private WechatSplitReceiverResponse toResponse(WechatSplitReceiverAccount a) {
        String accountNo = a.getAccountNo();
        String type = receiverType(accountNo);
        boolean merchant = "MERCHANT_ID".equals(type);
        return WechatSplitReceiverResponse.builder()
                .id(a.getId())
                .ownerType(a.getOwnerType())
                .ownerId(a.getOwnerId())
                .receiverType(type)
                // 商户号本身不是登录凭证可明文；openid 是打款地址，只下发脱敏值
                .accountNo(merchant ? accountNo : null)
                .accountNoMasked(merchant ? accountNo : maskOpenid(accountNo))
                .accountName(a.getAccountName())
                .verified(a.isVerified())
                .reversable(merchant)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }
}

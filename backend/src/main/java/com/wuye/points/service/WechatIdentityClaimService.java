package com.wuye.points.service;

import com.wuye.points.dto.admin.resident.ClaimWechatIdentityRequest;
import com.wuye.points.dto.admin.resident.ClaimWechatIdentityResponse;
import com.wuye.points.dto.admin.resident.WechatClaimSourceResponse;
import com.wuye.points.entity.Resident;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.ResidentRepository;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SensitiveMasker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 微信身份认领：把 openid 从「幽灵记录」迁到管理端预建的账号上。
 *
 * <p><b>要解决的问题</b>：小程序登录只按 openid 找人（{@code AuthService.wechatLogin}），查不到就新建一条
 * 住户记录。因此管理端预建的账号（有手机号、openid 为空）被本人登录时<b>不会</b>被识别，
 * 微信身份落在另一条新建记录上，而那条记录绑手机号时又会因手机号已被占用而失败
 * —— 结果是「管理端账号有手机号没 openid、微信记录有 openid 没手机号」，
 * 分账个人接收方按 ownerId 取 openid 时永远取不到。</p>
 *
 * <p><b>本服务只做身份迁移，不做账号合并</b>：订单、钱包、家庭关系等一律不动。
 * 迁移前置校验保证不会把来源记录上的资金锁死；来源记录迁移后降级为一条无微信身份的普通账号，
 * 可走既有的账号注销流程处理。</p>
 *
 * <p>openid 全程只以脱敏形式出入参，避免变成可复制粘贴的打款地址。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatIdentityClaimService {

    private final ResidentRepository residentRepository;
    private final RoleGuard roleGuard;

    /**
     * 列出可与目标账号配对的候选微信身份（同手机号、且自身带 openid 的其他住户记录）。
     */
    @Transactional(readOnly = true)
    public List<WechatClaimSourceResponse> listClaimSources(String targetResidentId) {
        Resident target = requireTarget(targetResidentId);
        if (!StringUtils.hasText(target.getPhone())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "目标账号没有手机号，无法按手机号匹配微信身份");
        }
        List<WechatClaimSourceResponse> items = new ArrayList<>();
        for (Resident candidate : residentRepository.findAllByPhone(target.getPhone())) {
            if (candidate.getId().equals(target.getId())) {
                continue;
            }
            items.add(toSourceResponse(target, candidate));
        }
        return items;
    }

    /**
     * 认领：把 source 的 openid/unionid 迁到 target。
     */
    @Transactional
    public ClaimWechatIdentityResponse claim(String targetResidentId, ClaimWechatIdentityRequest request) {
        Resident target = requireTarget(targetResidentId);
        if (StringUtils.hasText(target.getOpenid())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "该账号已绑定微信身份，无需认领");
        }
        Resident source = residentRepository.findById(request.getSourceResidentId().trim())
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        if (source.getId().equals(target.getId())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "来源与目标为同一账号");
        }
        if (!StringUtils.hasText(source.getOpenid())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "来源记录没有微信身份（无 openid），请本人先在小程序登录一次");
        }
        if (!source.getOpenid().matches("[A-Za-z0-9_-]{20,64}")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "来源记录的 openid 格式异常，不能用于分账收款");
        }
        if (!source.getPhone().equals(target.getPhone())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "两条记录的手机号不一致，不能认领");
        }
        assertNoFundsOnSource(source);

        String openid = source.getOpenid();
        String unionid = source.getUnionid();

        // 先清空来源再写目标：residents.openid 是唯一索引，反序会撞 Duplicate entry。
        source.setOpenid(null);
        source.setUnionid(null);
        source.setWechatBound(false);
        residentRepository.saveAndFlush(source);

        target.setOpenid(openid);
        target.setUnionid(unionid);
        target.setWechatBound(true);
        residentRepository.save(target);

        log.info("[微信身份认领] target={} source={} openid={}",
                target.getId(), source.getId(), maskOpenid(openid));
        return ClaimWechatIdentityResponse.builder()
                .residentId(target.getId())
                .sourceResidentId(source.getId())
                .openidMasked(maskOpenid(openid))
                .wechatBound(true)
                .message("已认领微信身份。请通知本人在小程序重新登录一次，使登录态切到该账号；"
                        + "来源记录已降级为无微信身份的普通账号，可另行注销。")
                .build();
    }

    private Resident requireTarget(String residentId) {
        Resident target = residentRepository.findById(residentId)
                .orElseThrow(() -> new BizException(ErrorCode.RESIDENT_NOT_FOUND));
        roleGuard.assertSamePropertyOrPlatform(target.getPropertyCompanyId());
        return target;
    }

    /** 迁移会让来源记录失去登录入口，先确认它没挂着钱，否则钱就锁死在没人能登录的账号里。 */
    private void assertNoFundsOnSource(Resident source) {
        int points = source.getPointBalance() == null ? 0 : source.getPointBalance();
        BigDecimal coins = source.getCoinBalance() == null ? BigDecimal.ZERO : source.getCoinBalance();
        if (points != 0 || coins.compareTo(BigDecimal.ZERO) != 0) {
            throw new BizException(ErrorCode.PARAM_INVALID,
                    "来源记录仍有积分 " + points + " / 物业币 " + coins.toPlainString()
                            + "，请先消费或转出后再认领（迁移后该记录无法再用微信登录）");
        }
    }

    private WechatClaimSourceResponse toSourceResponse(Resident target, Resident candidate) {
        boolean hasOpenid = StringUtils.hasText(candidate.getOpenid());
        int points = candidate.getPointBalance() == null ? 0 : candidate.getPointBalance();
        BigDecimal coins = candidate.getCoinBalance() == null ? BigDecimal.ZERO : candidate.getCoinBalance();
        boolean fundsClear = points == 0 && coins.compareTo(BigDecimal.ZERO) == 0;
        String reason = !hasOpenid ? "该记录没有微信身份，不能作为来源"
                : !fundsClear ? "该记录仍挂有积分/物业币，需先清零"
                : !candidate.getPropertyCompanyId().equals(target.getPropertyCompanyId())
                        ? "注意：该记录属于另一家物业公司（认领只迁微信身份，不迁数据）" : null;
        return WechatClaimSourceResponse.builder()
                .residentId(candidate.getId())
                .name(candidate.getName())
                .phoneMasked(SensitiveMasker.maskPhone(candidate.getPhone()))
                .propertyCompanyId(candidate.getPropertyCompanyId())
                .openidMasked(hasOpenid ? maskOpenid(candidate.getOpenid()) : null)
                .pointBalance(points)
                .coinBalance(coins)
                .claimable(hasOpenid && fundsClear)
                .reason(reason)
                .build();
    }

    private static String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) {
            return openid;
        }
        return openid.substring(0, 4) + "****" + openid.substring(openid.length() - 4);
    }
}

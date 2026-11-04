package com.wuye.points.controller;

import com.wuye.points.dto.admin.settlement.AutoRegisterReceiverResult;
import com.wuye.points.dto.admin.settlement.AutoRegisterReceiversRequest;
import com.wuye.points.dto.admin.settlement.ReceiverPrincipalResponse;
import com.wuye.points.dto.admin.settlement.WechatSplitReceiverRequest;
import com.wuye.points.dto.admin.settlement.WechatSplitReceiverResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.payment.WechatSplitReceiverAdminService;
import com.wuye.points.util.RoleGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 — 微信分账收款账户。
 *
 * <p>维护物业公司商户号与个人（协调员/负责人/楼长/经理/快递员）openid。平台佣金固定落服务商
 * 自身商户号，不在本接口登记。账户补齐并置 verified 后，缺户的历史分账由补偿调度自动补分。</p>
 */
@Tag(name = "管理端 — 微信分账收款账户", description = "分账接收方微信账户的查询/登记/删除")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWechatSplitReceiverController {

    private final WechatSplitReceiverAdminService receiverService;
    private final RoleGuard roleGuard;

    @Operation(summary = "查询分账收款账户",
            description = "可按主体类型过滤。账户号展示字段为 accountNoMasked（商户号明文、openid 脱敏）；"
                    + "accountNo 仅商户号下发。角色: platform_admin")
    @GetMapping("/wechat-split/receivers")
    public ApiResponse<List<WechatSplitReceiverResponse>> list(
            @RequestParam(required = false) String ownerType) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(receiverService.list(ownerType));
    }

    @Operation(summary = "新增/覆盖分账收款账户（仅商户号）",
            description = "同一主体（ownerType + ownerId）唯一，重复提交即覆盖。"
                    + "只接受 8~20 位纯数字微信商户号；个人 openid 手填已下线，请改用 /wechat-split/receivers/auto。"
                    + "角色: platform_admin")
    @PostMapping("/wechat-split/receivers")
    public ApiResponse<WechatSplitReceiverResponse> save(
            @Valid @RequestBody WechatSplitReceiverRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(receiverService.save(request));
    }

    @Operation(summary = "个人接收方主体预检",
            description = "按主体ID批量查询：该主体有无可用 openid、是否已登记为接收方。"
                    + "openid 只回脱敏值，前端不要也不会拿到明文。ownerIds 支持重复参数或逗号分隔。"
                    + "角色: platform_admin")
    @GetMapping("/wechat-split/receivers/principals")
    public ApiResponse<List<ReceiverPrincipalResponse>> listPrincipals(
            @Parameter(description = "主体类型：COURIER/COORDINATOR/SECTOR_LEADER/INDIVIDUAL_LEADER/"
                    + "BUILDING_LEADER/PROPERTY_MANAGER/RESIDENT/MERCHANT") @RequestParam String ownerType,
            @Parameter(description = "主体ID列表（角色表主键）") @RequestParam List<String> ownerIds) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(receiverService.listPrincipals(ownerType, ownerIds));
    }

    @Operation(summary = "按主体批量登记个人接收方",
            description = "前端只传 ownerType + ownerIds，openid 由后端按主体反查住户账号后写入，"
                    + "不经前端提交。逐条返回 registered/skipped 与原因，个别人缺微信身份不阻断整批。"
                    + "角色: platform_admin")
    @PostMapping("/wechat-split/receivers/auto")
    public ApiResponse<List<AutoRegisterReceiverResult>> autoRegister(
            @Valid @RequestBody AutoRegisterReceiversRequest request) {
        roleGuard.requireAnyRole("platform_admin");
        return ApiResponse.success(receiverService.autoRegister(request));
    }

    @Operation(summary = "删除分账收款账户", description = "角色: platform_admin")
    @DeleteMapping("/wechat-split/receivers/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        roleGuard.requireAnyRole("platform_admin");
        receiverService.delete(id);
        return ApiResponse.success(null);
    }
}

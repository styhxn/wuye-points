package com.wuye.points.controller;

import com.wuye.points.dto.admin.coinmallrule.MallRulesResponse;
import com.wuye.points.dto.admin.coinmallrule.UpdateMallRulesRequest;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.service.PropertyCoinMallRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端 — 物业币商城规则
 */
@Tag(name = "管理端 — 物业币商城规则", description = "物业币商城规则查询与更新")
@RestController
@RequestMapping("/admin/property-coin/mall-rules")
@RequiredArgsConstructor
public class AdminPropertyCoinMallRuleController {

    private final PropertyCoinMallRuleService propertyCoinMallRuleService;

    @Operation(summary = "查询物业币商城规则",
            description = "查询物业币商城规则。角色: property_admin, platform_admin。"
                    + "platform_admin 可通过 propertyCompanyId 指定物业公司")
    @GetMapping
    public ApiResponse<MallRulesResponse> getMallRules(
            @Parameter(description = "物业公司ID（仅 platform_admin 可跨物业查询）")
            @RequestParam(required = false) String propertyCompanyId) {
        return ApiResponse.success(propertyCoinMallRuleService.getMallRules(propertyCompanyId));
    }

    @Operation(summary = "更新物业币商城规则", description = "更新当前物业公司物业币商城规则。角色: property_admin")
    @PutMapping
    public ApiResponse<MallRulesResponse> updateMallRules(@Valid @RequestBody UpdateMallRulesRequest request) {
        return ApiResponse.success(propertyCoinMallRuleService.updateMallRules(request));
    }
}

package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.propertybankcard.PropertyBankCardItemResponse;
import com.wuye.points.entity.PropertyBankCard;
import com.wuye.points.service.AdminPropertyBankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "物业银行卡模块", description = "物业公司收款银行卡维护")
@RestController
@RequestMapping("/admin/property-bank-cards")
@RequiredArgsConstructor
public class AdminPropertyBankCardController {

    private final AdminPropertyBankCardService adminPropertyBankCardService;

    @Operation(summary = "银行卡列表", description = "账号脱敏展示。Roles: property_admin, platform_admin")
    @GetMapping
    public ApiResponse<List<PropertyBankCardItemResponse>> list() {
        return ApiResponse.success(adminPropertyBankCardService.list());
    }

    @Operation(summary = "新增银行卡", description = "Roles: property_admin, platform_admin")
    @PostMapping
    public ApiResponse<PropertyBankCardItemResponse> create(@RequestBody PropertyBankCard request) {
        return ApiResponse.success(adminPropertyBankCardService.create(request));
    }

    @Operation(summary = "修改银行卡", description = "仅更新非 null 字段。Roles: property_admin, platform_admin")
    @PutMapping("/{id}")
    public ApiResponse<PropertyBankCardItemResponse> update(
            @Parameter(description = "银行卡ID") @PathVariable String id,
            @RequestBody PropertyBankCard request) {
        return ApiResponse.success(adminPropertyBankCardService.update(id, request));
    }

    @Operation(summary = "删除银行卡", description = "Roles: property_admin, platform_admin")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@Parameter(description = "银行卡ID") @PathVariable String id) {
        adminPropertyBankCardService.delete(id);
        return ApiResponse.success(null);
    }
}

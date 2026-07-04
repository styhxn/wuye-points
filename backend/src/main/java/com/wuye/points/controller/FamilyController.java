package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.family.AddFamilyMemberRequest;
import com.wuye.points.dto.family.AddFamilyMemberResponse;
import com.wuye.points.dto.family.CreateFamilyRequest;
import com.wuye.points.dto.family.FamilyDetailResponse;
import com.wuye.points.dto.family.FamilyMemberContributionResponse;
import com.wuye.points.dto.family.FamilyMemberListResponse;
import com.wuye.points.dto.family.FamilyResponse;
import com.wuye.points.dto.family.RemoveFamilyMemberResponse;
import com.wuye.points.dto.family.UpdateFamilyRequest;
import com.wuye.points.service.FamilyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 家庭模块
 */
@Tag(name = "家庭模块", description = "家庭创建、成员管理、家庭信息")
@RestController
@RequestMapping("/families")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    /**
     * 创建家庭
     * Auth: Required | Roles: resident（业主）
     */
    @Operation(summary = "创建家庭", description = "创建家庭，需登录，角色：resident（业主）")
    @PostMapping
    public ApiResponse<FamilyResponse> createFamily(@Valid @RequestBody CreateFamilyRequest request) {
        return ApiResponse.success(familyService.createFamily(request));
    }

    /**
     * 获取当前用户家庭信息
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "获取当前用户家庭信息", description = "获取当前用户家庭信息，需登录，角色：resident")
    @GetMapping("/me")
    public ApiResponse<FamilyDetailResponse> getMyFamily() {
        return ApiResponse.success(familyService.getMyFamily());
    }

    /**
     * 更新当前用户家庭信息
     * Auth: Required | Roles: resident（仅户主）
     */
    @Operation(summary = "更新当前用户家庭信息", description = "更新当前用户家庭信息，需登录，角色：resident（仅户主）")
    @PutMapping("/me")
    public ApiResponse<FamilyDetailResponse> updateMyFamily(@Valid @RequestBody UpdateFamilyRequest request) {
        return ApiResponse.success(familyService.updateMyFamily(request));
    }

    /**
     * 家庭成员列表
     * Auth: Required | Roles: resident（家庭成员）、property_admin、platform_admin
     */
    @Operation(summary = "家庭成员列表", description = "获取家庭成员列表，需登录，角色：resident（家庭成员）、property_admin、platform_admin")
    @GetMapping("/{id}/members")
    public ApiResponse<FamilyMemberListResponse> listMembers(
            @Parameter(description = "家庭ID", example = "1") @PathVariable String id) {
        return ApiResponse.success(familyService.listMembers(id));
    }

    /**
     * 添加家庭成员
     * Auth: Required | Roles: resident（仅户主）
     */
    @Operation(summary = "添加家庭成员", description = "添加家庭成员，需登录，角色：resident（仅户主）")
    @PostMapping("/{id}/members")
    public ApiResponse<AddFamilyMemberResponse> addMember(
            @Parameter(description = "家庭ID", example = "1") @PathVariable String id,
            @Valid @RequestBody AddFamilyMemberRequest request) {
        return ApiResponse.success(familyService.addMember(id, request));
    }

    /**
     * 家庭成员贡献比例
     * Auth: Required | Roles: resident
     */
    @Operation(summary = "家庭成员贡献比例", description = "获取家庭成员贡献比例，需登录，角色：resident")
    @GetMapping("/me/contributions")
    public ApiResponse<List<FamilyMemberContributionResponse>> getMemberContributions() {
        return ApiResponse.success(familyService.getMemberContributions());
    }

    /**
     * 移除家庭成员
     * Auth: Required | Roles: resident（仅户主）
     */
    @Operation(summary = "移除家庭成员", description = "移除家庭成员，需登录，角色：resident（仅户主）")
    @DeleteMapping("/{id}/members/{memberId}")
    public ApiResponse<RemoveFamilyMemberResponse> removeMember(
            @Parameter(description = "家庭ID", example = "1") @PathVariable String id,
            @Parameter(description = "成员ID", example = "1") @PathVariable String memberId,
            @Parameter(description = "移除原因") @RequestParam(required = false) String reason) {
        return ApiResponse.success(familyService.removeMember(id, memberId, reason));
    }
}

package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.communitypoint.CommunityPointPoolResponse;
import com.wuye.points.dto.communitypoint.CommunityPointRecordItemResponse;
import com.wuye.points.dto.communitypoint.ContributeToCommunityRequest;
import com.wuye.points.service.CommunityPointPoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "小区建设积分（集体积分）", description = "小区建设积分池查询、流水、手动调整")
@RestController
@RequiredArgsConstructor
public class CommunityPointPoolController {

    private final CommunityPointPoolService communityPointPoolService;

    @Operation(summary = "我的小区建设积分", description = "查询当前用户所在小区的建设积分池。角色: resident, property_admin, platform_admin")
    @GetMapping("/community-points/my-pool")
    public ApiResponse<List<CommunityPointPoolResponse>> getMyPool() {
        return ApiResponse.success(communityPointPoolService.listMyCommunityPools());
    }

    @Operation(summary = "指定小区建设积分", description = "查询指定小区的建设积分池。角色: resident, property_admin, platform_admin")
    @GetMapping("/community-points/{communityId}")
    public ApiResponse<CommunityPointPoolResponse> getPool(
            @Parameter(description = "小区ID", required = true) @PathVariable String communityId) {
        return ApiResponse.success(communityPointPoolService.getPool(communityId));
    }

    @Operation(summary = "小区建设积分流水", description = "分页查询指定小区的建设积分流水。角色: resident, property_admin, platform_admin")
    @GetMapping("/community-points/{communityId}/records")
    public ApiResponse<PageResponse<CommunityPointRecordItemResponse>> listRecords(
            @Parameter(description = "小区ID", required = true) @PathVariable String communityId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(communityPointPoolService.listRecords(communityId, page, pageSize));
    }

    @Operation(summary = "手动调整小区建设积分", description = "物业管理员手动调整建设积分池。角色: property_admin, platform_admin")
    @PostMapping("/admin/community-points/{communityId}/adjust")
    public ApiResponse<CommunityPointRecordItemResponse> adminAdjust(
            @Parameter(description = "小区ID", required = true) @PathVariable String communityId,
            @Valid @RequestBody ContributeToCommunityRequest request) {
        BigDecimal amount = request.getAmount();
        String remark = request.getRemark();
        return ApiResponse.success(communityPointPoolService.adminAdjust(communityId, amount, remark));
    }
}

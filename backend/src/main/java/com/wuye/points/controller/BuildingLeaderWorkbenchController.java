package com.wuye.points.controller;

import com.wuye.points.dto.buildingleader.BuildingLeaderMyResponse;
import com.wuye.points.dto.buildingleader.PublishUnitNoticeRequest;
import com.wuye.points.dto.buildingleader.ShareUnitProductRequest;
import com.wuye.points.dto.buildingleader.ShareUnitProductResponse;
import com.wuye.points.dto.buildingleader.ShareableProductItemResponse;
import com.wuye.points.dto.buildingleader.UnitNoticeItemResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.distribution.DistributionRecordItemResponse;
import com.wuye.points.dto.rolewithdrawal.ApplyRoleWithdrawalRequest;
import com.wuye.points.dto.rolewithdrawal.RoleWithdrawalItemResponse;
import com.wuye.points.service.BuildingLeaderUnitOpsService;
import com.wuye.points.service.BuildingLeaderWorkbenchService;
import com.wuye.points.service.DistributionService;
import com.wuye.points.service.RoleWithdrawalService;
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

/**
 * 单元楼长工作台：本人信息、分成明细、提现。
 */
@Tag(name = "单元楼长工作台", description = "楼长个人信息、分成明细、提现申请与记录")
@RestController
@RequestMapping("/building-leaders")
@RequiredArgsConstructor
public class BuildingLeaderWorkbenchController {

    private final BuildingLeaderWorkbenchService buildingLeaderWorkbenchService;
    private final BuildingLeaderUnitOpsService buildingLeaderUnitOpsService;
    private final DistributionService distributionService;
    private final RoleWithdrawalService roleWithdrawalService;

    @Operation(summary = "获取当前楼长信息", description = "含累计分成、已提现、在途提现、可提现余额。角色: resident（须为启用中的单元楼长）")
    @GetMapping("/my")
    public ApiResponse<BuildingLeaderMyResponse> getMyInfo() {
        return ApiResponse.success(buildingLeaderWorkbenchService.getMyInfo());
    }

    @Operation(summary = "我的分成明细", description = "楼长查看本栋订单分成明细（仅本人）。角色: resident（须为启用中的单元楼长）")
    @GetMapping("/my/distribution-records")
    public ApiResponse<PageResponse<DistributionRecordItemResponse>> listMyDistributionRecords(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(distributionService.listMyRecordsForBuildingLeader(page, pageSize));
    }

    @Operation(summary = "楼长申请提现",
            description = "按累计分成申请提现，金额不超过可提现余额（累计分成 - 已提现 - 在途）。角色: resident（须为启用中的单元楼长）")
    @PostMapping("/{id}/withdrawals")
    public ApiResponse<RoleWithdrawalItemResponse> applyWithdrawal(
            @Parameter(description = "楼长ID bl_") @PathVariable String id,
            @Valid @RequestBody ApplyRoleWithdrawalRequest request) {
        return ApiResponse.success(roleWithdrawalService.applyForBuildingLeader(id, request));
    }

    @Operation(summary = "楼长提现记录", description = "楼长提现记录列表。角色: resident（须为启用中的单元楼长）")
    @GetMapping("/{id}/withdrawals")
    public ApiResponse<PageResponse<RoleWithdrawalItemResponse>> listWithdrawals(
            @Parameter(description = "楼长ID bl_") @PathVariable String id,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "审核状态") @RequestParam(required = false) String auditStatus,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(roleWithdrawalService.listForBuildingLeader(
                id, page, pageSize, auditStatus, startDate, endDate));
    }

    @Operation(summary = "发布单元通知",
            description = "面向本单元住户发布通知，复用公告表 + 单元定向。定向范围（小区+单元）取自本人楼长记录，"
                    + "前端不可传；直接发布不走物业审核；每人每日限3条。角色: resident（须为启用中的单元楼长）")
    @PostMapping("/my/notices")
    public ApiResponse<UnitNoticeItemResponse> publishUnitNotice(
            @Valid @RequestBody PublishUnitNoticeRequest request) {
        return ApiResponse.success(buildingLeaderUnitOpsService.publishNotice(request));
    }

    @Operation(summary = "我发布的单元通知",
            description = "楼长查看本人发布过的单元通知（含其他单元住户不可见的记录）。角色: resident（须为启用中的单元楼长）")
    @GetMapping("/my/notices")
    public ApiResponse<PageResponse<UnitNoticeItemResponse>> listMyUnitNotices(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(buildingLeaderUnitOpsService.listMyNotices(page, pageSize));
    }

    @Operation(summary = "本小区可分享商品",
            description = "楼长分享前的选品列表：本物业下、商家审核通过且在营、服务本楼长所在小区"
                    + "（serve_all_communities=1 或显式绑定该小区）的在售商品。角色: resident（须为启用中的单元楼长）")
    @GetMapping("/my/shareable-products")
    public ApiResponse<PageResponse<ShareableProductItemResponse>> listShareableProducts(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商品名称关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(buildingLeaderUnitOpsService.listShareableProducts(page, pageSize, keyword));
    }

    @Operation(summary = "分享商品给本单元",
            description = "生成一条本单元定向的产品推荐公告，并给本单元其他住户各落一条带商品卡片的站内消息"
                    + "（card_type=product，剔除本人及已屏蔽该楼长的住户）。角色: resident（须为启用中的单元楼长）")
    @PostMapping("/my/product-shares")
    public ApiResponse<ShareUnitProductResponse> shareUnitProduct(
            @Valid @RequestBody ShareUnitProductRequest request) {
        return ApiResponse.success(buildingLeaderUnitOpsService.shareProduct(request));
    }

    @Operation(summary = "我的商品分享记录",
            description = "楼长查看本人的分享记录。角色: resident（须为启用中的单元楼长）")
    @GetMapping("/my/product-shares")
    public ApiResponse<PageResponse<UnitNoticeItemResponse>> listMyUnitProductShares(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数", example = "20") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(buildingLeaderUnitOpsService.listMyShares(page, pageSize));
    }
}
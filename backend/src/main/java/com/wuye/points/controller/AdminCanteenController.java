package com.wuye.points.controller;

import com.wuye.points.dto.canteen.AdminRechargeResponse;
import com.wuye.points.dto.canteen.BindingCreateRequest;
import com.wuye.points.dto.canteen.BindingResponse;
import com.wuye.points.dto.canteen.BindingUpdateRequest;
import com.wuye.points.dto.canteen.CanteenSettingsRequest;
import com.wuye.points.dto.canteen.CanteenSettingsResponse;
import com.wuye.points.dto.canteen.CreateCanteenMerchantRequest;
import com.wuye.points.dto.canteen.DirectedFlowResponse;
import com.wuye.points.dto.canteen.QuotaAdjustRequest;
import com.wuye.points.dto.canteen.QuotaAdjustResponse;
import com.wuye.points.dto.canteen.QuotaRecordResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.dto.merchant.MerchantDetailResponse;
import com.wuye.points.service.AdminCanteenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 社区食堂-管理端接口（主商家标记、绑定管理、配置、三类流水查询与导出）
 */
@Tag(name = "管理端 — 社区食堂", description = "主商家子商家绑定、手续费配置、额度/充值/消费流水")
@RestController
@RequiredArgsConstructor
public class AdminCanteenController {

    private final AdminCanteenService adminCanteenService;

    @Operation(summary = "标记/取消主商家", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/canteen/merchants/{merchantId}/main")
    public ApiResponse<Void> setMainMerchant(
            @Parameter(description = "商家ID") @PathVariable String merchantId,
            @Parameter(description = "是否主商家") @RequestParam boolean main) {
        adminCanteenService.setMainMerchant(merchantId, main);
        return ApiResponse.success(null);
    }

    @Operation(summary = "创建社区食堂商家（主/子）",
            description = "新建食堂主商家或子商家，merchantType=canteen 与普通商品商家隔离。"
                    + "角色: property_admin, platform_admin。店主手机号须在该物业下已注册。")
    @PostMapping("/admin/canteen/merchants")
    public ApiResponse<MerchantDetailResponse> createCanteenMerchant(
            @Valid @RequestBody CreateCanteenMerchantRequest request) {
        return ApiResponse.success(adminCanteenService.createCanteenMerchant(request));
    }

    @Operation(summary = "绑定子商家到主商家", description = "角色: property_admin, platform_admin")
    @PostMapping("/admin/canteen/bindings")
    public ApiResponse<BindingResponse> createBinding(@Valid @RequestBody BindingCreateRequest request) {
        return ApiResponse.success(adminCanteenService.createBinding(request));
    }

    @Operation(summary = "手动调整主商家可用充值额度",
            description = "免手续费直接入账（落一条 paid 额度流水），用于调试/运营兜底。"
                    + "角色: property_admin, platform_admin。仅社区食堂主商家可调。")
    @PostMapping("/admin/canteen/merchants/{merchantId}/quota-adjust")
    public ApiResponse<QuotaAdjustResponse> adjustQuota(
            @Parameter(description = "主商家ID") @PathVariable String merchantId,
            @Valid @RequestBody QuotaAdjustRequest request) {
        return ApiResponse.success(QuotaAdjustResponse.builder()
                .merchantId(merchantId)
                .amount(request.getAmount())
                .remainingQuota(adminCanteenService.adjustQuota(merchantId, request))
                .build());
    }

    @Operation(summary = "解绑", description = "角色: property_admin, platform_admin")
    @DeleteMapping("/admin/canteen/bindings/{id}")
    public ApiResponse<Void> deleteBinding(@PathVariable String id) {
        adminCanteenService.deleteBinding(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "调整供货优惠比例", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/canteen/bindings/{id}/rate")
    public ApiResponse<BindingResponse> updateBindingRate(
            @PathVariable String id,
            @Valid @RequestBody BindingUpdateRequest request) {
        return ApiResponse.success(adminCanteenService.updateBindingRate(id, request));
    }

    @Operation(summary = "绑定列表", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/canteen/bindings")
    public ApiResponse<PageResponse<BindingResponse>> listBindings(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "主商家ID") @RequestParam(required = false) String mainMerchantId,
            @Parameter(description = "子商家ID") @RequestParam(required = false) String subMerchantId) {
        return ApiResponse.success(adminCanteenService.listBindings(page, pageSize, mainMerchantId, subMerchantId));
    }

    @Operation(summary = "查询手续费配置", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/canteen/settings")
    public ApiResponse<CanteenSettingsResponse> getSettings() {
        return ApiResponse.success(adminCanteenService.getSettings());
    }

    @Operation(summary = "更新手续费配置", description = "角色: property_admin, platform_admin")
    @PutMapping("/admin/canteen/settings")
    public ApiResponse<CanteenSettingsResponse> updateSettings(@Valid @RequestBody CanteenSettingsRequest request) {
        return ApiResponse.success(adminCanteenService.updateSettings(request));
    }

    @Operation(summary = "额度购买流水", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/canteen/quota-purchases")
    public ApiResponse<PageResponse<QuotaRecordResponse>> listQuotaPurchases(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "主商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "状态 pending/paid/refunded/closed") @RequestParam(required = false) String status,
            @Parameter(description = "开始日期 yyyy-MM-dd") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminCanteenService.listQuotaPurchases(
                page, pageSize, merchantId, status, startDate, endDate));
    }

    @Operation(summary = "定向充值流水", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/canteen/recharges")
    public ApiResponse<PageResponse<AdminRechargeResponse>> listRecharges(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "主商家ID") @RequestParam(required = false) String mainMerchantId,
            @Parameter(description = "用户ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "开始日期 yyyy-MM-dd") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminCanteenService.listRecharges(
                page, pageSize, mainMerchantId, residentId, startDate, endDate));
    }

    @Operation(summary = "定向余额消费/退款流水", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/canteen/flows")
    public ApiResponse<PageResponse<DirectedFlowResponse>> listFlows(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "主商家体系ID") @RequestParam(required = false) String mainMerchantId,
            @Parameter(description = "消费商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "用户ID") @RequestParam(required = false) String residentId,
            @Parameter(description = "类型 consume_deduct/refund_back") @RequestParam(required = false) String flowType,
            @Parameter(description = "开始日期 yyyy-MM-dd") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd") @RequestParam(required = false) String endDate) {
        return ApiResponse.success(adminCanteenService.listFlows(
                page, pageSize, mainMerchantId, merchantId, residentId, flowType, startDate, endDate));
    }

    @Operation(summary = "三类流水导出CSV", description = "角色: property_admin, platform_admin")
    @GetMapping("/admin/canteen/export")
    public void exportCsv(
            @Parameter(description = "类型 quota_purchase/recharge/directed_flow") @RequestParam String type,
            @Parameter(description = "主商家ID") @RequestParam(required = false) String merchantId,
            @Parameter(description = "开始日期 yyyy-MM-dd") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd") @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws IOException {
        String csv = switch (type) {
            case "quota_purchase" -> exportQuotaPurchases(merchantId, startDate, endDate);
            case "recharge" -> exportRecharges(merchantId, startDate, endDate);
            case "directed_flow" -> exportFlows(merchantId, startDate, endDate);
            default -> throw new com.wuye.points.exception.BizException(
                    com.wuye.points.exception.ErrorCode.PARAM_INVALID, "type需为 quota_purchase/recharge/directed_flow");
        };
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=canteen_" + type + ".csv");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}); // UTF-8 BOM，Excel 中文兼容
        response.getOutputStream().write(csv.getBytes(StandardCharsets.UTF_8));
        response.getOutputStream().flush();
    }

    private String exportQuotaPurchases(String merchantId, String startDate, String endDate) {
        PageResponse<QuotaRecordResponse> data = adminCanteenService.listQuotaPurchases(
                1, 1000, merchantId, null, startDate, endDate);
        StringBuilder sb = new StringBuilder("流水ID,商家ID,购买金额,手续费,到账额度,状态,创建时间\n");
        List<QuotaRecordResponse> list = data.getList();
        if (list != null) {
            for (QuotaRecordResponse r : list) {
                sb.append(join(r.getId(), merchantId, r.getAmount(), r.getFeeAmount(),
                        r.getActualQuota(), r.getStatus(), r.getCreatedAt()));
            }
        }
        return sb.toString();
    }

    private String exportRecharges(String merchantId, String startDate, String endDate) {
        PageResponse<AdminRechargeResponse> data = adminCanteenService.listRecharges(
                1, 1000, merchantId, null, startDate, endDate);
        StringBuilder sb = new StringBuilder("流水ID,主商家ID,用户ID,用户姓名,手机号,充值金额,充值后余额,备注,创建时间\n");
        List<AdminRechargeResponse> list = data.getList();
        if (list != null) {
            for (AdminRechargeResponse r : list) {
                sb.append(join(r.getId(), r.getMainMerchantId(), r.getResidentId(), r.getResidentName(),
                        r.getResidentPhone(), r.getAmount(), r.getBalanceAfter(), r.getRemark(), r.getCreatedAt()));
            }
        }
        return sb.toString();
    }

    private String exportFlows(String merchantId, String startDate, String endDate) {
        PageResponse<DirectedFlowResponse> data = adminCanteenService.listFlows(
                1, 1000, merchantId, null, null, null, startDate, endDate);
        StringBuilder sb = new StringBuilder("流水ID,主商家体系,消费商家,类型,金额,变动后余额,创建时间\n");
        List<DirectedFlowResponse> list = data.getList();
        if (list != null) {
            for (DirectedFlowResponse r : list) {
                sb.append(join(r.getId(), r.getMainMerchantName(), r.getMerchantName(),
                        r.getFlowType(), r.getAmount(), r.getBalanceAfter(), r.getCreatedAt()));
            }
        }
        return sb.toString();
    }

    private String join(Object... cells) {
        return java.util.Arrays.stream(cells)
                .map(c -> c == null ? "" : c.toString().replace(",", "，").replace("\n", " "))
                .collect(Collectors.joining(",")) + "\n";
    }
}

package com.wuye.points.controller;

import com.wuye.points.dto.canteen.DirectedBalanceResponse;
import com.wuye.points.dto.canteen.DirectedFlowResponse;
import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.common.PageResponse;
import com.wuye.points.service.CanteenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区食堂-用户端接口（我的定向余额、我的定向余额流水）
 */
@Tag(name = "用户端 — 社区食堂", description = "我的定向余额、定向余额流水")
@RestController
@RequestMapping("/canteen")
@RequiredArgsConstructor
public class CanteenController {

    private final CanteenService canteenService;

    @Operation(summary = "我的定向余额列表", description = "按主商家体系展示定向余额。角色: resident")
    @GetMapping("/my-balances")
    public ApiResponse<List<DirectedBalanceResponse>> getMyBalances() {
        return ApiResponse.success(canteenService.getMyDirectedBalances());
    }

    @Operation(summary = "我的定向余额流水", description = "充值/消费/退款流水。角色: resident")
    @GetMapping("/flows")
    public ApiResponse<PageResponse<DirectedFlowResponse>> getMyFlows(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.success(canteenService.getMyDirectedFlows(page, pageSize));
    }
}

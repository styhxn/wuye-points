package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.sectorleader.SectorLeaderMyResponse;
import com.wuye.points.service.SectorLeaderWorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "板块负责人工作台", description = "板块负责人个人信息、板块商家、板块排名、板块特惠")
@RestController
@RequestMapping("/sector-leaders")
@RequiredArgsConstructor
public class SectorLeaderWorkbenchController {

    private final SectorLeaderWorkbenchService sectorLeaderWorkbenchService;

    @Operation(summary = "获取当前板块负责人信息")
    @GetMapping("/my")
    public ApiResponse<SectorLeaderMyResponse> getMyInfo() {
        return ApiResponse.success(sectorLeaderWorkbenchService.getMyInfo());
    }
}

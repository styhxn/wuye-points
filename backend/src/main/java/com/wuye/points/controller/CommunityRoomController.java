package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.communityroom.AddRoomRequest;
import com.wuye.points.dto.communityroom.AddRoomsBatchRequest;
import com.wuye.points.dto.communityroom.AvailableRoomListResponse;
import com.wuye.points.dto.communityroom.RoomListResponse;
import com.wuye.points.dto.communityroom.RoomManageItemResponse;
import com.wuye.points.dto.communityroom.RoomStructureResponse;
import com.wuye.points.dto.communityroom.UpdateRoomRequest;
import com.wuye.points.service.CommunityRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 社区房号（楼宇结构）模块（A12）
 */
@Tag(name = "社区房号（楼宇结构）", description = "可选房号查询、楼宇结构、房号管理（新增/删除/锁定）")
@RestController
@RequestMapping("/communities")
@RequiredArgsConstructor
public class CommunityRoomController {

    private final CommunityRoomService communityRoomService;

    @Operation(summary = "可选房号列表",
            description = "查询社区可选房号（未占用且未锁定的房号）。Auth: Required")
    @GetMapping("/{id}/available-rooms")
    public ApiResponse<AvailableRoomListResponse> listAvailableRooms(
            @Parameter(description = "社区ID") @PathVariable String id,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building,
            @Parameter(description = "单元") @RequestParam(required = false) String unit) {
        return ApiResponse.success(communityRoomService.listAvailableRooms(id, building, unit));
    }

    @Operation(summary = "楼宇结构",
            description = "查询社区楼宇结构（按 楼栋-单元-楼层-房号 聚合，含房号状态）。Auth: Required")
    @GetMapping("/{id}/room-structure")
    public ApiResponse<RoomStructureResponse> getRoomStructure(
            @Parameter(description = "社区ID") @PathVariable String id,
            @Parameter(description = "楼栋") @RequestParam(required = false) String building) {
        return ApiResponse.success(communityRoomService.getRoomStructure(id, building));
    }

    @Operation(summary = "房号列表（管理）",
            description = "查询社区全部房号（含空置/已入住/锁定）。Auth: Required | Roles: property_admin, platform_admin")
    @GetMapping("/{communityId}/rooms")
    public ApiResponse<RoomListResponse> listRooms(
            @Parameter(description = "社区ID") @PathVariable String communityId) {
        return ApiResponse.success(communityRoomService.listRooms(communityId));
    }

    @Operation(summary = "新增房号（管理）",
            description = "在楼宇结构中新增一个空置房号。Auth: Required | Roles: property_admin, platform_admin")
    @PostMapping("/{communityId}/rooms")
    public ApiResponse<RoomManageItemResponse> addRoom(
            @Parameter(description = "社区ID") @PathVariable String communityId,
            @Valid @RequestBody AddRoomRequest request) {
        return ApiResponse.success(communityRoomService.addRoom(communityId, request));
    }

    @Operation(summary = "批量新增房号（管理）",
            description = "在同一楼栋下批量新增空置房号。Auth: Required | Roles: property_admin, platform_admin")
    @PostMapping("/{communityId}/rooms/batch")
    public ApiResponse<List<RoomManageItemResponse>> addRoomsBatch(
            @Parameter(description = "社区ID") @PathVariable String communityId,
            @Valid @RequestBody AddRoomsBatchRequest request) {
        return ApiResponse.success(communityRoomService.addRoomsBatch(communityId, request));
    }

    @Operation(summary = "更新房号（管理）",
            description = "锁定/解锁房号或调整楼层。Auth: Required | Roles: property_admin, platform_admin")
    @PatchMapping("/{communityId}/rooms/{roomId}")
    public ApiResponse<RoomManageItemResponse> updateRoom(
            @Parameter(description = "社区ID") @PathVariable String communityId,
            @Parameter(description = "房号ID") @PathVariable String roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        return ApiResponse.success(communityRoomService.updateRoom(communityId, roomId, request));
    }

    @Operation(summary = "删除房号（管理）",
            description = "删除空置房号（已入住不可删）。Auth: Required | Roles: property_admin, platform_admin")
    @DeleteMapping("/{communityId}/rooms/{roomId}")
    public ApiResponse<Void> deleteRoom(
            @Parameter(description = "社区ID") @PathVariable String communityId,
            @Parameter(description = "房号ID") @PathVariable String roomId) {
        communityRoomService.deleteRoom(communityId, roomId);
        return ApiResponse.success(null);
    }
}

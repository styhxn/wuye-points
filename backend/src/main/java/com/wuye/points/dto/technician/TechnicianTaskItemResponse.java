package com.wuye.points.dto.technician;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "技工工单列表项")
public class TechnicianTaskItemResponse {

    @Schema(description = "工单ID（服务订单ID）")
    private String id;

    @Schema(description = "标题（服务名）")
    private String title;

    @Schema(description = "描述/备注")
    private String description;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "住户ID（聊天入口：POST /messages 的 toId）", example = "res_b7m9x1")
    private String residentId;

    @Schema(description = "住户头像")
    private String residentAvatarUrl;

    @Schema(description = "该住户发来的未读私聊数（未读角标）", example = "0")
    private Integer unreadCount;

    @Schema(description = "前端工单状态：pending/assigned/in_progress/completed/cancelled")
    private String status;

    @Schema(description = "同 status，兼容字段")
    private String statusCode;

    @Schema(description = "预约时间")
    private String preferredTime;

    @Schema(description = "创建时间")
    private String createdAt;

    @Schema(description = "更新时间")
    private String updatedAt;

    @Schema(description = "完成时间（已完成时有值）")
    private String completedAt;
}

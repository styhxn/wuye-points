package com.wuye.points.dto.admin.coinfreeze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "积分冻结记录项响应")
public class CoinFreezeRecordItemResponse {

    @Schema(description = "记录ID", example = "FREEZE001")
    private String id;

    @Schema(description = "住户ID", example = "RES001")
    private String residentId;

    @Schema(description = "住户姓名", example = "张三")
    private String residentName;

    @Schema(description = "住户电话", example = "13800138000")
    private String residentPhone;

    @Schema(description = "住户楼栋", example = "1号楼")
    private String residentBuilding;

    @Schema(description = "住户房间号", example = "101")
    private String residentRoom;

    @Schema(description = "操作类型", example = "freeze")
    private String action;

    @Schema(description = "金额", example = "100.00")
    private String amount;

    @Schema(description = "原因", example = "违规操作冻结")
    private String reason;

    @Schema(description = "操作人ID", example = "ADMIN001")
    private String operatorId;

    @Schema(description = "创建时间", example = "2026-01-01 12:00:00")
    private String createdAt;
}

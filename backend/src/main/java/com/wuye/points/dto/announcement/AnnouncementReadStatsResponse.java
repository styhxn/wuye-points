package com.wuye.points.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "公告已读统计")
public class AnnouncementReadStatsResponse {

    private String announcementId;
    private String deliveryChannel;
    private long targetCount;
    private long readCount;
    private long unreadCount;
    private double readRate;
    private List<BuildingReadStat> byBuilding;

    @Data
    @Builder
    public static class BuildingReadStat {
        private String buildingNo;
        private long readCount;
        private long unreadCount;
    }
}

package com.wuye.points.dto.announcement;

import com.wuye.points.dto.common.PageResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class AnnouncementCollectListResponse {

    private String announcementId;
    private String announcementTitle;
    private long totalResponses;
    private BigDecimal responseRate;
    private List<CollectFieldResponse> fields;
    private List<CollectItemResponse> list;
    private PageResponse.Pagination pagination;

    @Data
    @Builder
    public static class CollectItemResponse {
        private String id;
        private String residentId;
        private String residentName;
        private String phone;
        private String building;
        private Map<String, Object> collectedData;
        private String submittedAt;
    }
}

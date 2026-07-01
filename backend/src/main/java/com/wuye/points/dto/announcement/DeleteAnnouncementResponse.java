package com.wuye.points.dto.announcement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeleteAnnouncementResponse {

    private String id;
    private String status;
}

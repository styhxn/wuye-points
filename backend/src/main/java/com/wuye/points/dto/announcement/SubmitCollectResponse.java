package com.wuye.points.dto.announcement;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitCollectResponse {

    private String id;
    private String announcementId;
    private String residentId;
    private String submittedAt;
}

package com.wuye.points.dto.directedmessage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateDirectedMessageRequest {

    @NotBlank
    @Size(max = 128)
    private String title;

    @NotBlank
    @Size(max = 2000)
    private String content;

    private List<String> imageUrls;

    @NotBlank
    private String officialSenderType;

    private List<String> filterBuildings;

    private String filterGender;

    private List<String> filterAgeBracketIds;

    /** 按商家消费筛选（仅向消费过这些商家的住户推送） */
    private List<String> merchantIds;

    /** 按产品购买筛选（仅向购买过这些产品的住户推送） */
    private List<String> productIds;

    private String propertyCompanyId;
}

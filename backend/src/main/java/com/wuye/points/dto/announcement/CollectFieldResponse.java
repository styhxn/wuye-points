package com.wuye.points.dto.announcement;

import lombok.Data;

import java.util.List;

@Data
public class CollectFieldResponse {

    private String key;
    private String name;
    private String type;
    private Boolean required;
    private List<String> options;
}

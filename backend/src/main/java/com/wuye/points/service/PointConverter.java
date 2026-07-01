package com.wuye.points.service;

import com.wuye.points.dto.point.PointRecordItemResponse;
import com.wuye.points.entity.PointRecord;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

@Component
public class PointConverter {

    public PointRecordItemResponse toRecordItem(PointRecord record) {
        return PointRecordItemResponse.builder()
                .id(record.getId())
                .residentId(record.getResidentId())
                .familyId(record.getFamilyId())
                .pointType(record.getPointType())
                .amount(record.getAmount())
                .balance(record.getBalance())
                .source(record.getSource())
                .sourceId(record.getSourceId())
                .description(record.getDescription())
                .operatorId(record.getOperatorId())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }
}

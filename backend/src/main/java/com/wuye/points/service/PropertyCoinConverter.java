package com.wuye.points.service;

import com.wuye.points.dto.coin.CoinRecordItemResponse;
import com.wuye.points.entity.PropertyCoinRecord;
import com.wuye.points.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PropertyCoinConverter {

    public CoinRecordItemResponse toRecordItem(PropertyCoinRecord record) {
        return CoinRecordItemResponse.builder()
                .id(record.getId())
                .residentId(record.getResidentId())
                .familyId(record.getFamilyId())
                .coinType(record.getCoinType())
                .amount(record.getAmount())
                .balance(record.getBalance())
                .source(record.getSource())
                .sourceId(record.getSourceId())
                .description(record.getDescription())
                .operatorId(record.getOperatorId())
                .createdAt(DateTimeUtil.toIsoUtc(record.getCreatedAt()))
                .build();
    }

    public BigDecimal defaultCoinBalance(BigDecimal balance) {
        return balance != null ? balance : BigDecimal.ZERO;
    }
}

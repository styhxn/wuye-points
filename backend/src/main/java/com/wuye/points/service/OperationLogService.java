package com.wuye.points.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuye.points.context.UserContext;
import com.wuye.points.entity.OperationLog;
import com.wuye.points.repository.OperationLogRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogRepository operationLogRepository;
    private final SnowflakeIdGenerator idGenerator;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String module, String action, String targetType, String targetId,
                    Object beforeData, Object afterData) {
        OperationLog logEntry = new OperationLog();
        logEntry.setId(idGenerator.generateId("ol"));
        logEntry.setOperatorId(UserContext.currentUserId());
        logEntry.setPropertyCompanyId(UserContext.currentPropertyCompanyId());
        logEntry.setModule(module);
        logEntry.setAction(action);
        logEntry.setTargetType(targetType);
        logEntry.setTargetId(targetId);
        logEntry.setBeforeData(toJson(beforeData));
        logEntry.setAfterData(toJson(afterData));
        operationLogRepository.save(logEntry);
    }

    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ex) {
            log.warn("操作日志序列化失败: {}", ex.getMessage());
            return String.valueOf(data);
        }
    }
}

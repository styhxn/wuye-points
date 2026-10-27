package com.wuye.points.service;

import com.wuye.points.entity.IdempotencyRecord;
import com.wuye.points.repository.IdempotencyRecordRepository;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 幂等记录读写。
 *
 * <p>所有方法使用 REQUIRES_NEW：processing 记录必须立即提交，后续并发请求才能看到；
 * 业务成功/失败后再独立更新/删除。业务事务与幂等记录不由同一事务包裹，崩溃窗口由 TTL 兜底。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String STATUS_PROCESSING = "processing";
    private static final String STATUS_COMPLETED = "completed";
    /** processing 超过该时长视为异常中断，可被新请求接管 */
    private static final int PROCESSING_TTL_MINUTES = 2;
    /** 成功后响应保留 2 小时，覆盖用户重试窗口；微信 payParams 不宜长期缓存 */
    private static final int COMPLETED_TTL_HOURS = 2;

    private final IdempotencyRecordRepository repository;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 尝试抢占幂等键。重复键会抛 DataIntegrityViolationException，由切面转查询已有记录。
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord tryBegin(String userId, String requestKey, String requestId,
                                      String method, String path) {
        IdempotencyRecord record = new IdempotencyRecord();
        record.setId(idGenerator.generateId("idem"));
        record.setUserId(userId);
        record.setRequestKey(requestKey);
        record.setRequestId(requestId);
        record.setMethod(method);
        record.setPath(path);
        record.setStatus(STATUS_PROCESSING);
        record.setExpiresAt(LocalDateTime.now().plusMinutes(PROCESSING_TTL_MINUTES));
        return repository.saveAndFlush(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public IdempotencyRecord find(String userId, String requestKey) {
        return repository.findByUserIdAndRequestKey(userId, requestKey).orElse(null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String id, int responseStatus, String responseBody) {
        repository.findById(id).ifPresent(record -> {
            record.setStatus(STATUS_COMPLETED);
            record.setResponseStatus(responseStatus);
            record.setResponseBody(responseBody);
            record.setExpiresAt(LocalDateTime.now().plusHours(COMPLETED_TTL_HOURS));
            repository.save(record);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(String id) {
        repository.deleteById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(String id) {
        repository.deleteById(id);
    }

    /** 清理过期记录（定时任务调用） */
    @Transactional
    public int cleanup() {
        long deleted = repository.deleteByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("[幂等] 清理过期记录 {} 条", deleted);
        }
        return (int) deleted;
    }
}

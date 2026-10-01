package com.wuye.points.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 雪花算法 ID 生成器
 * 用于生成带业务前缀的字符串 ID，如 res_b7m9x1
 */
@Component
public class SnowflakeIdGenerator {

    private final long epoch = 1672531200000L; // 2023-01-01
    private final long machineIdBits = 5L;
    private final long sequenceBits = 12L;
    private final long maxMachineId = ~(-1L << machineIdBits);
    private final long maxSequence = ~(-1L << sequenceBits);
    private final long machineIdShift = sequenceBits;
    private final long timestampShift = sequenceBits + machineIdBits;

    private long machineId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    @Value("${app.machine-id:1}")
    public void setMachineId(long machineId) {
        if (machineId > maxMachineId || machineId < 0) {
            throw new IllegalArgumentException("Machine ID out of range: " + machineId);
        }
        this.machineId = machineId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards, refusing to generate id for "
                    + (lastTimestamp - timestamp) + "ms");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - epoch) << timestampShift)
                | (machineId << machineIdShift)
                | sequence;
    }

    /**
     * 生成带前缀的字符串 ID，如 res_17a2f3c4d5e6f708。
     *
     * <p><b>必须使用完整 63 位雪花 ID（16 位 hex），严禁截断。</b>历史版本截取后 8 位 hex（仅 32 bit）：
     * 时间戳低 15 bit 约每 32.7 秒循环一次，sequence 归零后会在「相隔 32.7 秒整数倍的同一毫秒」
     * 复现完全相同的主键，导致 payment_records 等表出现 Duplicate entry for key PRIMARY
     * （下单高并发下可观测为大量主键冲突）。完整 ID 中时间戳占 46 bit，循环周期约 2235 年，可忽略。</p>
     *
     * <p>多实例部署时，每个实例必须配置互不相同的 {@code app.machine-id}（如环境变量
     * {@code MACHINE_ID}），否则跨实例仍可能同毫秒同 sequence 撞号。</p>
     */
    public String generateId(String prefix) {
        long id = nextId();
        return prefix + "_" + Long.toHexString(id);
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}

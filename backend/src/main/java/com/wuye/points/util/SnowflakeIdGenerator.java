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
     * 生成带前缀的字符串 ID，如 res_b7m9x1
     */
    public String generateId(String prefix) {
        long id = nextId();
        String hex = Long.toHexString(id);
        // 取后6-8位
        if (hex.length() > 8) {
            hex = hex.substring(hex.length() - 8);
        }
        return prefix + "_" + hex;
    }

    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}

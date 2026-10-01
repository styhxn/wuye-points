package com.wuye.points.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 信息采集奖励发放记录表 prr_xxxxxxxx
 * 注：表仅含 created_at，无 updated_at，故不继承 BaseEntity，
 *     仅声明 created_at 字段以适配 ddl-auto: validate。
 * 唯一约束：resident_id + field_name + reward_config_id
 */
@Data
@Entity
@Table(name = "profile_reward_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_resident_field_config", columnNames = {"resident_id", "field_name", "reward_config_id"})
})
@EntityListeners(AuditingEntityListener.class)
public class ProfileRewardRecord {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "field_name", length = 50, nullable = false)
    private String fieldName;

    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints;

    @Column(name = "reward_config_id", length = 32)
    private String rewardConfigId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

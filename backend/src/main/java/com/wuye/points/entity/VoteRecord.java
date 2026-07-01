package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 投票记录表
 * 对应 schema.sql 中 vote_records 表（id 前缀 vr_）
 */
@Data
@Entity
@Table(name = "vote_records")
@EqualsAndHashCode(callSuper = true)
public class VoteRecord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "opinion_box_id", length = 32, nullable = false)
    private String opinionBoxId;

    @Column(name = "resident_id", length = 32, nullable = false)
    private String residentId;

    @Column(name = "family_id", length = 32, nullable = false)
    private String familyId;

    @Column(name = "vote_option", length = 20, nullable = false)
    private String voteOption;
}

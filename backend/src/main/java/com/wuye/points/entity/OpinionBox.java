package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 意见箱表
 * 对应 schema.sql 中 opinion_boxes 表（id 前缀 ob_）
 */
@Data
@Entity
@Table(name = "opinion_boxes")
@EqualsAndHashCode(callSuper = true)
public class OpinionBox extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "publisher_id", length = 32, nullable = false)
    private String publisherId;

    @Column(name = "family_id", length = 32, nullable = false)
    private String familyId;

    @Column(name = "property_company_id", length = 32, nullable = false)
    private String propertyCompanyId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_votes")
    private Integer totalVotes;

    @Column(name = "support_count")
    private Integer supportCount;

    @Column(name = "oppose_count")
    private Integer opposeCount;

    @Column(name = "abstain_count")
    private Integer abstainCount;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "result_published")
    private Boolean resultPublished;
}

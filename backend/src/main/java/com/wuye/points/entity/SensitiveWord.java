package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 敏感词表
 * 对应 sensitive_words 表（id 前缀 sw_）
 * 发帖/评论时对内容做包含匹配，命中即拦截
 */
@Data
@Entity
@Table(name = "sensitive_words")
@EqualsAndHashCode(callSuper = true)
public class SensitiveWord extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "word", length = 100, nullable = false)
    private String word;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "level")
    private Integer level = 1;
}

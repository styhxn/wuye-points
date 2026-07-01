package com.wuye.points.entity;

import com.wuye.points.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件表
 * 对应 schema.sql 中 files 表（id 前缀 file_）
 */
@Data
@Entity
@Table(name = "files")
@EqualsAndHashCode(callSuper = true)
public class FileEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "original_name", length = 200, nullable = false)
    private String originalName;

    @Column(name = "stored_name", length = 200, nullable = false)
    private String storedName;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "mime_type", length = 50)
    private String mimeType;

    @Column(name = "size")
    private Long size;

    @Column(name = "category", length = 30)
    private String category;

    @Column(name = "uploader_id", length = 32)
    private String uploaderId;

    @Column(name = "property_company_id", length = 32)
    private String propertyCompanyId;
}

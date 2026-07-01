package com.wuye.points.repository;

import com.wuye.points.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文件 Repository
 * 注：uploader_id 已隐含 propertyCompanyId（上传者归属唯一物业）
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {

    List<FileEntity> findByUploaderId(String uploaderId);

    long countByUploaderIdAndCategory(String uploaderId, String category);
}

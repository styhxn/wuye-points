package com.wuye.points.service;

import cn.hutool.core.img.ImgUtil;
import com.wuye.points.context.UserContext;
import com.wuye.points.dto.file.BatchUploadFileItemResponse;
import com.wuye.points.dto.file.DeleteFileResponse;
import com.wuye.points.dto.file.FileDetailResponse;
import com.wuye.points.dto.file.UploadFileResponse;
import com.wuye.points.entity.FileEntity;
import com.wuye.points.exception.BizException;
import com.wuye.points.exception.ErrorCode;
import com.wuye.points.repository.FileRepository;
import com.wuye.points.util.DateTimeUtil;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private static final int AVATAR_THUMB_SIZE = 200;
    private static final Map<String, CategoryRule> CATEGORY_RULES = Map.of(
            "avatar", new CategoryRule(2 * 1024 * 1024L, Set.of("jpg", "jpeg", "png", "webp"), null, true),
            "announcement", new CategoryRule(5 * 1024 * 1024L, Set.of("jpg", "jpeg", "png", "webp"), 9, true),
            "merchant", new CategoryRule(5 * 1024 * 1024L, Set.of("jpg", "jpeg", "png", "webp"), 12, true),
            "service", new CategoryRule(5 * 1024 * 1024L, Set.of("jpg", "jpeg", "png", "webp"), 6, true),
            "activity", new CategoryRule(10 * 1024 * 1024L, Set.of("jpg", "jpeg", "png", "webp", "mp4"), null, true)
    );

    private final FileRepository fileRepository;
    private final RoleGuard roleGuard;
    private final SnowflakeIdGenerator idGenerator;

    @Value("${app.upload.path:/data/uploads}")
    private String uploadPath;

    @Value("${app.upload.base-url:http://localhost:8888/api/v1/static}")
    private String uploadBaseUrl;

    @Transactional
    public UploadFileResponse upload(MultipartFile file, String category) {
        requireAuthenticated();
        validateCategory(category);
        validateMultipartFile(file);

        String uploaderId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        assertCategoryUploadLimit(uploaderId, category, 1);

        return doUpload(file, category, uploaderId, propertyCompanyId);
    }

    @Transactional
    public List<BatchUploadFileItemResponse> uploadBatch(List<MultipartFile> files, String category) {
        requireAuthenticated();
        validateCategory(category);
        if (files == null || files.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "files不能为空");
        }

        String uploaderId = UserContext.currentUserId();
        String propertyCompanyId = UserContext.currentPropertyCompanyId();
        assertCategoryUploadLimit(uploaderId, category, files.size());

        List<BatchUploadFileItemResponse> items = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            validateMultipartFile(file);
            UploadFileResponse uploaded = doUpload(file, category, uploaderId, propertyCompanyId);
            items.add(BatchUploadFileItemResponse.builder()
                    .fileId(uploaded.getFileId())
                    .originalName(uploaded.getOriginalName())
                    .storedName(uploaded.getStoredName())
                    .url(uploaded.getUrl())
                    .thumbnailUrl(uploaded.getThumbnailUrl())
                    .size(uploaded.getSize())
                    .mimeType(uploaded.getMimeType())
                    .category(uploaded.getCategory())
                    .order(i)
                    .build());
        }
        return items;
    }

    @Transactional(readOnly = true)
    public FileDetailResponse getFile(String id) {
        requireAuthenticated();
        validateFileId(id);

        FileEntity entity = getReadableFile(id);
        return toDetail(entity);
    }

    @Transactional
    public DeleteFileResponse deleteFile(String id) {
        requireAuthenticated();
        validateFileId(id);

        FileEntity entity = getManageableFile(id);
        deletePhysicalFiles(entity);
        fileRepository.delete(entity);

        return DeleteFileResponse.builder()
                .fileId(id)
                .deleted(true)
                .build();
    }

    private UploadFileResponse doUpload(
            MultipartFile file,
            String category,
            String uploaderId,
            String propertyCompanyId) {
        CategoryRule rule = CATEGORY_RULES.get(category);
        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String extension = extractExtension(originalName);
        validateExtension(category, extension, rule);
        validateFileSize(category, file.getSize(), rule);

        String fileId = idGenerator.generateId("file");
        String storedName = buildStoredName(uploaderId, fileId, extension);
        String thumbStoredName = buildThumbStoredName(uploaderId, fileId, extension);

        Path categoryDir = resolveCategoryDir(category);
        Path targetPath = categoryDir.resolve(storedName);

        try {
            Files.createDirectories(categoryDir);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("文件存储失败: category={}, storedName={}", category, storedName, ex);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "文件存储失败");
        }

        String thumbnailUrl = null;
        if (rule.generateThumb() && isImageExtension(extension)) {
            thumbnailUrl = generateThumbnail(targetPath, categoryDir.resolve(thumbStoredName), category, extension);
        }

        String url = buildPublicUrl(category, storedName);
        if (thumbnailUrl == null && isImageExtension(extension)) {
            thumbnailUrl = url;
        }

        FileEntity entity = new FileEntity();
        entity.setId(fileId);
        entity.setOriginalName(originalName);
        entity.setStoredName(storedName);
        entity.setUrl(url);
        entity.setThumbnailUrl(thumbnailUrl);
        entity.setMimeType(resolveMimeType(extension, file.getContentType()));
        entity.setSize(file.getSize());
        entity.setCategory(category);
        entity.setUploaderId(uploaderId);
        entity.setPropertyCompanyId(propertyCompanyId);
        fileRepository.save(entity);

        return UploadFileResponse.builder()
                .fileId(entity.getId())
                .originalName(entity.getOriginalName())
                .storedName(entity.getStoredName())
                .url(entity.getUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .size(entity.getSize())
                .mimeType(entity.getMimeType())
                .category(entity.getCategory())
                .build();
    }

    private String generateThumbnail(Path sourcePath, Path thumbPath, String category, String extension) {
        try {
            BufferedImage image = ImgUtil.read(sourcePath.toFile());
            java.awt.Image scaled = ImgUtil.scale(image, AVATAR_THUMB_SIZE, AVATAR_THUMB_SIZE);
            String imageType = "png".equalsIgnoreCase(extension) ? "png" : "jpg";
            try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(thumbPath)) {
                ImgUtil.write(scaled, imageType, out);
            }
            return buildPublicUrl(category, thumbPath.getFileName().toString());
        } catch (Exception ex) {
            log.warn("缩略图生成失败: {}", sourcePath, ex);
            return buildPublicUrl(category, sourcePath.getFileName().toString());
        }
    }

    private void deletePhysicalFiles(FileEntity entity) {
        Path categoryDir = resolveCategoryDir(entity.getCategory());
        deleteIfExists(categoryDir.resolve(entity.getStoredName()));
        if (StringUtils.hasText(entity.getThumbnailUrl())) {
            String thumbName = entity.getStoredName().replaceFirst("(\\.[^.]+)$", "_thumb$1");
            deleteIfExists(categoryDir.resolve(thumbName));
        }
    }

    private void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ex) {
            log.warn("删除物理文件失败: {}", path, ex);
        }
    }

    private FileEntity getReadableFile(String id) {
        FileEntity entity = fileRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.FILE_NOT_FOUND));

        if (roleGuard.isPlatformAdmin()) {
            return entity;
        }

        String currentUserId = UserContext.currentUserId();
        if (currentUserId != null && currentUserId.equals(entity.getUploaderId())) {
            return entity;
        }

        if (roleGuard.isPropertyAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (propertyCompanyId != null && propertyCompanyId.equals(entity.getPropertyCompanyId())) {
                return entity;
            }
        }

        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private FileEntity getManageableFile(String id) {
        FileEntity entity = fileRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.FILE_NOT_FOUND));

        String currentUserId = UserContext.currentUserId();
        if (currentUserId != null && currentUserId.equals(entity.getUploaderId())) {
            return entity;
        }
        if (roleGuard.isPlatformAdmin()) {
            return entity;
        }
        if (roleGuard.isPropertyAdmin()) {
            String propertyCompanyId = UserContext.currentPropertyCompanyId();
            if (propertyCompanyId != null && propertyCompanyId.equals(entity.getPropertyCompanyId())) {
                return entity;
            }
        }
        throw new BizException(ErrorCode.PERMISSION_DENIED);
    }

    private void assertCategoryUploadLimit(String uploaderId, String category, int incomingCount) {
        CategoryRule rule = CATEGORY_RULES.get(category);
        if (rule.maxTotalCount() == null) {
            return;
        }
        long existingCount = fileRepository.countByUploaderIdAndCategory(uploaderId, category);
        if (existingCount + incomingCount > rule.maxTotalCount()) {
            throw new BizException(
                    ErrorCode.PARAM_OUT_OF_RANGE,
                    category + " 分类最多上传 " + rule.maxTotalCount() + " 个文件");
        }
    }

    private void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_MISSING, "file不能为空");
        }
    }

    private void validateCategory(String category) {
        if (!StringUtils.hasText(category) || !RoleGuard.FILE_CATEGORIES.contains(category)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "category值无效");
        }
    }

    private void validateFileId(String id) {
        if (!StringUtils.hasText(id) || !id.startsWith("file_")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件ID格式不正确");
        }
    }

    private void validateExtension(String category, String extension, CategoryRule rule) {
        if (!StringUtils.hasText(extension) || !rule.extensions().contains(extension.toLowerCase(Locale.ROOT))) {
            throw new BizException(ErrorCode.PARAM_INVALID, "文件格式不支持");
        }
    }

    private void validateFileSize(String category, long size, CategoryRule rule) {
        if (size <= 0) {
            throw new BizException(ErrorCode.PARAM_MISSING, "file不能为空");
        }
        if (size > rule.maxSize()) {
            throw new BizException(
                    ErrorCode.PARAM_OUT_OF_RANGE,
                    "文件大小超过限制（最大 " + formatSize(rule.maxSize()) + "）");
        }
    }

    private String normalizeOriginalName(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "upload.bin";
        }
        return Paths.get(originalFilename).getFileName().toString();
    }

    private String extractExtension(String originalName) {
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private String buildStoredName(String uploaderId, String fileId, String extension) {
        String safeUploaderId = StringUtils.hasText(uploaderId) ? uploaderId : "anonymous";
        return safeUploaderId + "_" + fileId.substring("file_".length()) + "." + extension;
    }

    private String buildThumbStoredName(String uploaderId, String fileId, String extension) {
        return buildStoredName(uploaderId, fileId, extension).replaceFirst("(\\.[^.]+)$", "_thumb$1");
    }

    private Path resolveCategoryDir(String category) {
        return Paths.get(uploadPath, category);
    }

    private String buildPublicUrl(String category, String storedName) {
        String base = uploadBaseUrl.endsWith("/")
                ? uploadBaseUrl.substring(0, uploadBaseUrl.length() - 1)
                : uploadBaseUrl;
        return base + "/" + category + "/" + storedName;
    }

    private String resolveMimeType(String extension, String contentType) {
        if (StringUtils.hasText(contentType) && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            return contentType;
        }
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            default -> "application/octet-stream";
        };
    }

    private boolean isImageExtension(String extension) {
        return Set.of("jpg", "jpeg", "png", "webp").contains(extension.toLowerCase(Locale.ROOT));
    }

    private String formatSize(long bytes) {
        if (bytes >= 1024 * 1024) {
            return (bytes / (1024 * 1024)) + "MB";
        }
        return (bytes / 1024) + "KB";
    }

    private FileDetailResponse toDetail(FileEntity entity) {
        return FileDetailResponse.builder()
                .fileId(entity.getId())
                .originalName(entity.getOriginalName())
                .storedName(entity.getStoredName())
                .url(entity.getUrl())
                .thumbnailUrl(entity.getThumbnailUrl())
                .size(entity.getSize())
                .mimeType(entity.getMimeType())
                .category(entity.getCategory())
                .uploaderId(entity.getUploaderId())
                .propertyCompanyId(entity.getPropertyCompanyId())
                .createdAt(DateTimeUtil.toIsoUtc(entity.getCreatedAt()))
                .build();
    }

    private void requireAuthenticated() {
        if (UserContext.currentUserId() == null) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
    }

    private record CategoryRule(long maxSize, Set<String> extensions, Integer maxTotalCount, boolean generateThumb) {
    }
}

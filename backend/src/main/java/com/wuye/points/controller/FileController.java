package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.file.BatchUploadFileItemResponse;
import com.wuye.points.dto.file.DeleteFileResponse;
import com.wuye.points.dto.file.FileDetailResponse;
import com.wuye.points.dto.file.UploadFileResponse;
import com.wuye.points.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件模块
 */
@Tag(name = "文件模块", description = "文件上传、批量上传、文件详情、删除")
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "文件上传", description = "上传单个文件")
    @PostMapping("/upload")
    public ApiResponse<UploadFileResponse> upload(
            @Parameter(description = "上传文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "文件分类") @RequestParam("category") String category) {
        return ApiResponse.success(fileService.upload(file, category));
    }

    @Operation(summary = "批量文件上传", description = "批量上传多个文件")
    @PostMapping("/upload-batch")
    public ApiResponse<List<BatchUploadFileItemResponse>> uploadBatch(
            @Parameter(description = "上传文件列表") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "文件分类") @RequestParam("category") String category) {
        return ApiResponse.success(fileService.uploadBatch(files, category));
    }

    @Operation(summary = "文件详情", description = "获取文件详细信息")
    @GetMapping("/{id}")
    public ApiResponse<FileDetailResponse> getFile(
            @Parameter(description = "文件ID") @PathVariable String id) {
        return ApiResponse.success(fileService.getFile(id));
    }

    @Operation(summary = "删除文件", description = "删除指定文件")
    @DeleteMapping("/{id}")
    public ApiResponse<DeleteFileResponse> deleteFile(
            @Parameter(description = "文件ID") @PathVariable String id) {
        return ApiResponse.success(fileService.deleteFile(id));
    }
}

package com.wuye.points.controller;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.dto.file.BatchUploadFileItemResponse;
import com.wuye.points.dto.file.DeleteFileResponse;
import com.wuye.points.dto.file.FileDetailResponse;
import com.wuye.points.dto.file.UploadFileResponse;
import com.wuye.points.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件模块
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ApiResponse<UploadFileResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {
        return ApiResponse.success(fileService.upload(file, category));
    }

    @PostMapping("/upload-batch")
    public ApiResponse<List<BatchUploadFileItemResponse>> uploadBatch(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("category") String category) {
        return ApiResponse.success(fileService.uploadBatch(files, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<FileDetailResponse> getFile(@PathVariable String id) {
        return ApiResponse.success(fileService.getFile(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<DeleteFileResponse> deleteFile(@PathVariable String id) {
        return ApiResponse.success(fileService.deleteFile(id));
    }
}

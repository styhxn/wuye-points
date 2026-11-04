package com.wuye.points.service;

import com.wuye.points.context.UserContext;
import com.wuye.points.dto.file.UploadFileResponse;
import com.wuye.points.entity.FileEntity;
import com.wuye.points.repository.FileRepository;
import com.wuye.points.util.LeaderIdentityGuard;
import com.wuye.points.util.RoleGuard;
import com.wuye.points.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private LeaderIdentityGuard leaderIdentityGuard;
    @Mock
    private SnowflakeIdGenerator idGenerator;

    @TempDir
    Path tempDir;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    @DisplayName("配送员可上传配送凭证（category=service）")
    void courier_canUploadProofImage() {
        UserContext.set(UserContext.builder()
                .userId("res_courier01")
                .role("courier")
                .propertyCompanyId("pc_demo001")
                .build());
        when(idGenerator.generateId(anyString())).thenReturn("file_test");
        when(fileRepository.countByUploaderIdAndCategory(anyString(), anyString())).thenReturn(0L);
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        FileService fileService = new FileService(fileRepository, new RoleGuard(leaderIdentityGuard), idGenerator);
        ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "uploadBaseUrl", "https://test.example.com/api/v1/static");

        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.jpg", "image/jpeg", new byte[]{1, 2, 3});

        UploadFileResponse response = fileService.upload(file, "service");

        assertEquals("file_test", response.getFileId());
        assertEquals("service", response.getCategory());
    }

    @Test
    @DisplayName("配送员可上传配送凭证（category=delivery_proof）")
    void courier_canUploadDeliveryProofWithDedicatedCategory() {
        FileService fileService = newFileService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.jpg", "image/jpeg", new byte[]{1, 2, 3});

        UploadFileResponse response = fileService.upload(file, "delivery_proof");

        assertEquals("file_test", response.getFileId());
        assertEquals("delivery_proof", response.getCategory());
    }

    @Test
    @DisplayName("category=delivery/proof 自动归一为 delivery_proof")
    void deliveryAliases_normalizedToDeliveryProof() {
        FileService fileService = newFileService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertEquals("delivery_proof", fileService.upload(file, "delivery").getCategory());
        assertEquals("delivery_proof", fileService.upload(file, "proof").getCategory());
    }

    @Test
    @DisplayName("配送凭证不设累计数量上限")
    void deliveryProof_hasNoLifetimeCountLimit() {
        FileService fileService = newFileService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "proof.mp4", "video/mp4", new byte[]{1, 2, 3});

        UploadFileResponse response = fileService.upload(file, "delivery_proof");

        assertEquals("delivery_proof", response.getCategory());
        org.mockito.Mockito.verify(fileRepository, org.mockito.Mockito.never())
                .countByUploaderIdAndCategory(anyString(), anyString());
    }

    @Test
    @DisplayName("提现转账凭证使用独立分类 withdrawal_proof，不设累计上限")
    void withdrawalProof_hasDedicatedCategoryWithoutCountLimit() {
        FileService fileService = newFileService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "transfer.jpg", "image/jpeg", new byte[]{1, 2, 3});

        UploadFileResponse response = fileService.upload(file, "withdrawal_proof");

        assertEquals("withdrawal_proof", response.getCategory());
        org.mockito.Mockito.verify(fileRepository, org.mockito.Mockito.never())
                .countByUploaderIdAndCategory(anyString(), anyString());
    }

    @Test
    @DisplayName("提现转账凭证不接受 mp4")
    void withdrawalProof_rejectsVideo() {
        FileService fileService = newFileService();
        MockMultipartFile file = new MockMultipartFile(
                "file", "transfer.mp4", "video/mp4", new byte[]{1, 2, 3});

        org.junit.jupiter.api.Assertions.assertThrows(
                com.wuye.points.exception.BizException.class,
                () -> fileService.upload(file, "withdrawal_proof"));
    }

    private FileService newFileService() {
        UserContext.set(UserContext.builder()
                .userId("res_courier01")
                .role("courier")
                .propertyCompanyId("pc_demo001")
                .build());
        when(idGenerator.generateId(anyString())).thenReturn("file_test");
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        FileService fileService = new FileService(fileRepository, new RoleGuard(leaderIdentityGuard), idGenerator);
        ReflectionTestUtils.setField(fileService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(fileService, "uploadBaseUrl", "https://test.example.com/api/v1/static");
        return fileService;
    }
}

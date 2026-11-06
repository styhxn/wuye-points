package com.wuye.points.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 全局异常处理器纯单元测试（不加载 Spring 上下文）。
 * 验证各异常类型映射到正确的 HTTP 状态码与业务错误码。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBizException_mapsCorrectStatusAndCode() {
        // 400: 积分不足
        assertStatusAndCode(handler.handleBizException(new BizException(40001, "积分不足")), HttpStatus.BAD_REQUEST, 40001);
        // 401: 令牌缺失（20001~20003 区间）
        assertStatusAndCode(handler.handleBizException(new BizException(20001, "未登录")), HttpStatus.UNAUTHORIZED, 20001);
        // 404: 资源不存在（30001~30002 区间）
        assertStatusAndCode(handler.handleBizException(new BizException(30001, "不存在")), HttpStatus.NOT_FOUND, 30001);
        // 409: 并发冲突（70003 区间）
        assertStatusAndCode(handler.handleBizException(new BizException(70003, "冲突")), HttpStatus.CONFLICT, 70003);
        // 500: 系统错误（99001 区间）
        assertStatusAndCode(handler.handleBizException(new BizException(99001, "系统错误")), HttpStatus.INTERNAL_SERVER_ERROR, 99001);
    }

    @Test
    void handleIllegalArgument_maps400() {
        ResponseEntity<?> resp = handler.handleIllegalArgument(new IllegalArgumentException("参数非法"));
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
    }

    @Test
    void handleOptimisticLock_maps409() {
        ResponseEntity<?> resp = handler.handleOptimisticLock(
                new OptimisticLockingFailureException("并发修改"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
    }

    @Test
    void handleBizException_unregisteredCode_maps400Not500() {
        // 回归：前端问题 2026-09-06（新建物业公司 500）——
        // 30005(RESIDENT_DUPLICATE_PHONE) 等历史未登记码必须按 400 业务错误返回，而不是 500「内部服务异常」
        assertStatusAndCode(handler.handleBizException(new BizException(30005, "该手机号在当前物业公司下已存在")),
                HttpStatus.BAD_REQUEST, 30005);
    }

    @Test
    void handleDataIntegrityViolation_maps409WithoutLeakingRootCause() {
        // 唯一约束冲突统一 409，且不把表名/索引名/重复值回显给前端
        ResponseEntity<?> resp = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("Duplicate entry 'test' for key 'uk_name'"),
                new MockHttpServletRequest("POST", "/api/v1/admin/role-accounts"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals(70003, ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getCode());
        assertEquals("数据冲突，请刷新后重试",
                ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getMessage());
    }

    @Test
    void handleDataIntegrityViolation_lengthOverflowGetsReadableMessage() {
        // 回归：2026-11-05 进件保存 409 实为 bank_account_type varchar(20) 存不下 27 字符枚举，
        // 却被统一文案伪装成「数据冲突，请刷新后重试」，商家无从下手。
        ResponseEntity<?> resp = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException(
                        "could not execute statement [Data truncation: Data too long for column 'bank_account_type' at row 1]"),
                new MockHttpServletRequest("POST", "/api/v1/merchants/my/applyment"));
        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        assertEquals(70003, ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getCode());
        assertEquals("提交内容超出字段长度限制，请检查填写内容后重试",
                ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getMessage());
    }

    @Test
    void handleMaxUploadSize_maps413WithReadableMessage() {
        // 回归：应用层挡下的超大文件必须回 413 JSON，而不是 500「内部服务异常」让前端解析不出原因
        ResponseEntity<?> resp = handler.handleMaxUploadSize(
                new MaxUploadSizeExceededException(50L * 1024 * 1024));
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, resp.getStatusCode());
        assertEquals(95002, ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getCode());
        assertEquals("上传文件超过服务端限制 50MB",
                ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getMessage());
    }

    @Test
    void handleException_maps500() {
        ResponseEntity<?> resp = handler.handleException(new RuntimeException("未知错误"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertNotNull(resp.getBody());
    }

    @SuppressWarnings("unchecked")
    private void assertStatusAndCode(ResponseEntity<?> resp, HttpStatus expectedStatus, int expectedCode) {
        assertEquals(expectedStatus, resp.getStatusCode());
        assertEquals(expectedCode, ((com.wuye.points.dto.common.ApiResponse<Object>) resp.getBody()).getCode());
    }
}

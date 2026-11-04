package com.wuye.points.exception;

import com.wuye.points.dto.common.ApiResponse;
import com.wuye.points.payment.WechatPayException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity
                .status(getHttpStatus(e.getCode()))
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<ApiResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiResponse.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        fe.getRejectedValue()))
                .collect(Collectors.toList());
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.PARAM_INVALID.getCode(), "参数校验失败", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型错误: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.PARAM_INVALID.getCode(), "参数类型错误: " + e.getName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BODY_PARSE_FAILED.getCode(), "请求体解析失败，请检查 JSON 格式"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        log.warn("接口不存在(NoResourceFound): {}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.ENDPOINT_NOT_FOUND.getCode(),
                        "接口不存在: " + e.getResourcePath()));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(NoHandlerFoundException e) {
        log.warn("接口不存在(NoHandlerFound): {}", e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.ENDPOINT_NOT_FOUND.getCode(),
                        "接口不存在: " + e.getRequestURL()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_SUPPORTED.getCode(),
                        "请求方法不支持: " + (e.getMethod() == null ? "" : e.getMethod())));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        log.warn("不支持的媒体类型: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(ErrorCode.MEDIA_TYPE_NOT_SUPPORTED.getCode(), "不支持的媒体类型"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.PARAM_INVALID.getCode(), "参数不合法"));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(OptimisticLockingFailureException e) {
        log.warn("乐观锁冲突: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ORDER_CONFLICT.getCode(), ErrorCode.ORDER_CONFLICT.getMessage()));
    }

    /**
     * 数据库完整性冲突：统一映射 409，根因只写日志，不把表名/列名/重复值回显给前端。
     * 业务层能识别的重复手机号/状态冲突仍应优先抛 BizException。
     * <p>日志必须带请求路径：响应端刻意不含根因，没有路径就没人能从 409 反查到是哪个接口哪个字段。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("数据完整性冲突 {} {}", request.getMethod(), request.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ORDER_CONFLICT.getCode(), "数据冲突，请刷新后重试"));
    }

    /**
     * 超过 spring.servlet.multipart 上限：回可解析的 413 JSON，而不是 500「内部服务异常」。
     * <p>nginx 的 client_max_body_size 在应用之前拦截，那种 413 是 nginx 的 HTML，走不到这里。
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        long max = e.getMaxUploadSize();
        String detail = max > 0 ? "上传文件超过服务端限制 " + (max / 1024 / 1024) + "MB" : "上传文件超过服务端限制";
        log.warn("上传文件超限: {}", detail);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error(ErrorCode.FILE_TOO_LARGE.getCode(), detail));
    }

    /**
     * 微信支付调用异常：日志保留微信错误码/原文；响应只给用户安全文案，避免第三方报文/密钥信息外泄。
     */
    @ExceptionHandler(WechatPayException.class)
    public ResponseEntity<ApiResponse<Void>> handleWechatPay(WechatPayException e) {
        log.error("微信支付异常 code={} message={}", e.getCode(), e.getWxMessage(), e);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(),
                        "支付服务暂时不可用，请稍后重试或联系客服"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }

    private HttpStatus getHttpStatus(int code) {
        // 优先使用 ErrorCode 枚举中声明的 HTTP 状态，未登记的历史码再走兼容白名单
        HttpStatus mapped = ErrorCode.findByCode(code)
                .map(errorCode -> HttpStatus.valueOf(errorCode.getHttpStatus()))
                .orElse(null);
        if (mapped != null) {
            return mapped;
        }
        if (code >= 20001 && code <= 20003) return HttpStatus.UNAUTHORIZED;
        if (code == 20004 || code == 20005) return HttpStatus.FORBIDDEN;
        if (code == 10001 || code == 10002 || code == 10003 || code == 10007
                || code == 30003
                || code == 40001 || code == 50001 || code == 93004
                || code == 94004 || code == 80001) return HttpStatus.BAD_REQUEST;
        if (code == 404 || code == 10004 || code == 30001 || code == 60001 || code == 60004 || code == 60006 || code == 70001 || code == 90001
                || code == 91001 || code == 91002 || code == 91003
                || code == 92001 || code == 92002 || code == 93001
                || code == 40002 || code == 90012 || code == 95001
                || code == 96001 || code == 96002 || code == 97001
                || code == 90004 || code == 90007 || code == 80002) {
            return HttpStatus.NOT_FOUND;
        }
        if (code == 10005) return HttpStatus.METHOD_NOT_ALLOWED;
        if (code == 10006) return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        if (code == 50002 || code == 93003) return HttpStatus.FORBIDDEN;
        if (code == 70003 || code == 60002 || code == 60003 || code == 70002 || code == 90002
                || code == 30002 || code == 30004 || code == 94001 || code == 94003
                || code == 90013 || code == 96003
                || code == 90003 || code == 90005 || code == 90006
                || code == 90008 || code == 90009 || code == 90010 || code == 90011
                || code == 93002 || code == 94002
                || code == 71002 || code == 80003
                || code == 30006 || code == 30007 || code == 30008 || code == 30009) return HttpStatus.CONFLICT;
        // 兜底：未登记的业务码按参数错误处理，避免一律 500「内部服务异常」
        return HttpStatus.BAD_REQUEST;
    }
}

package com.wuye.points.exception;

import com.wuye.points.dto.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.PARAM_INVALID.getCode(), e.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(OptimisticLockingFailureException e) {
        log.warn("乐观锁冲突: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ORDER_CONFLICT.getCode(), ErrorCode.ORDER_CONFLICT.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }

    private HttpStatus getHttpStatus(int code) {
        if (code >= 20001 && code <= 20005) return HttpStatus.UNAUTHORIZED;
        if (code == 10001 || code == 10002 || code == 10003
                || code == 30003
                || code == 40001 || code == 50001 || code == 93004
                || code == 94004) return HttpStatus.BAD_REQUEST;
        if (code == 404 || code == 30001 || code == 60001 || code == 60004 || code == 70001 || code == 90001
                || code == 91001 || code == 91002 || code == 91003
                || code == 92001 || code == 92002 || code == 93001
                || code == 40002 || code == 90012 || code == 95001
                || code == 96001 || code == 96002 || code == 97001) {
            return HttpStatus.NOT_FOUND;
        }
        if (code == 20004 || code == 50002 || code == 93003) return HttpStatus.FORBIDDEN;
        if (code == 70003 || code == 60002 || code == 60003 || code == 70002 || code == 90002
                || code == 30002 || code == 30004 || code == 94001 || code == 94003
                || code == 90013 || code == 96003) return HttpStatus.CONFLICT;
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

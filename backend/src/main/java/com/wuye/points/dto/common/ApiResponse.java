package com.wuye.points.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.wuye.points.util.DateTimeUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应格式")
public class ApiResponse<T> {
    @Schema(description = "业务状态码，0=成功，非0=错误", example = "0")
    private int code;

    @Schema(description = "状态描述", example = "success")
    private String message;

    @Schema(description = "业务数据")
    private T data;

    @Schema(description = "响应时间戳")
    private String timestamp;

    @Schema(description = "链路追踪ID（由 ApiResponseAdvice 统一回填）", example = "trace-8f3c1a2b")
    private String traceId;

    @Schema(description = "参数校验错误详情（仅400类错误）")
    private List<FieldError> errors;

    public ApiResponse() {
        this.timestamp = DateTimeUtil.toLocalDateTimeString(LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(0);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    public static <T> ApiResponse<T> error(int code, String message, List<FieldError> errors) {
        ApiResponse<T> response = error(code, message);
        response.setErrors(errors);
        return response;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "字段校验错误详情")
    public static class FieldError {
        @Schema(description = "字段名称", example = "title")
        private String field;
        @Schema(description = "错误信息", example = "标题不能为空")
        private String message;
        @Schema(description = "提交的值", example = "")
        private Object value;

        public FieldError(String field, String message, Object value) {
            this.field = field;
            this.message = message;
            this.value = value;
        }
    }
}

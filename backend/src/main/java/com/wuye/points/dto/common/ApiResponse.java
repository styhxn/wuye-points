package com.wuye.points.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private String timestamp;
    private List<FieldError> errors;

    public ApiResponse() {
        this.timestamp = Instant.now().toString();
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
    public static class FieldError {
        private String field;
        private String message;
        private Object value;

        public FieldError(String field, String message, Object value) {
            this.field = field;
            this.message = message;
            this.value = value;
        }
    }
}

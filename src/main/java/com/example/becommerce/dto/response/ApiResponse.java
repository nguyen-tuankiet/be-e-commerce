package com.example.becommerce.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * Unified API response wrapper.
 *
 * <pre>
 * Success:
 * {
 *   "success": true,
 *   "data": { ... }
 * }
 *
 * Error:
 * {
 *   "success": false,
 *   "error": { "code": "...", "message": "..." }
 * }
 *
 * Validation error:
 * {
 *   "success": false,
 *   "error": { "code": "VALIDATION_ERROR", "message": "...", "fields": { ... } }
 * }
 * </pre>
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data    = data;
        this.error   = error;
    }

    // ---- Success factory methods -----------------------------------

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null);
    }

    // ---- Error factory methods -------------------------------------

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message, null));
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> fields) {
        return new ApiResponse<>(false, null, new ErrorBody("VALIDATION_ERROR", message, fields));
    }

    // ---- Inner error body ------------------------------------------

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorBody {
        private final String code;
        private final String message;
        private final Map<String, String> fields;
    }
}

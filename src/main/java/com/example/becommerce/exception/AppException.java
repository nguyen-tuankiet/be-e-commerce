package com.example.becommerce.exception;

import com.example.becommerce.constant.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base application exception.
 * All business exceptions should extend this class.
 */
@Getter
public class AppException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public AppException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    // ---- Factory methods for common scenarios -------------------------

    public static AppException notFound(String message) {
        return new AppException(ErrorCode.NOT_FOUND, message, HttpStatus.NOT_FOUND);
    }

    public static AppException badRequest(String errorCode, String message) {
        return new AppException(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public static AppException unauthorized(String message) {
        return new AppException(ErrorCode.UNAUTHORIZED, message, HttpStatus.UNAUTHORIZED);
    }

    public static AppException forbidden(String message) {
        return new AppException(ErrorCode.FORBIDDEN, message, HttpStatus.FORBIDDEN);
    }

    public static AppException conflict(String errorCode, String message) {
        return new AppException(errorCode, message, HttpStatus.CONFLICT);
    }
}

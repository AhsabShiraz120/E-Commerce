package com.bookworm.common;

import com.bookworm.api.model.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Domain exception carrying a machine-readable error code from the OpenAPI
 * `ApiErrorCode` enum plus the HTTP status the handler should return.
 */
@Getter
public class ApiException extends RuntimeException {

    private final ApiErrorCode code;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public ApiException(HttpStatus status, ApiErrorCode code, String message) {
        this(status, code, message, null);
    }

    public ApiException(HttpStatus status, ApiErrorCode code, String message, Map<String, Object> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    // -- Convenience constructors -----------------------------------------------------------

    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, message);
    }

    public static ApiException conflict(ApiErrorCode code, String message) {
        return new ApiException(HttpStatus.CONFLICT, code, message);
    }

    public static ApiException unauthenticated(String message) {
        return new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED, message);
    }

    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, message);
    }

    public static ApiException validationFailed(String message, Map<String, Object> details) {
        return new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED, message, details);
    }

    public static ApiException paymentDeclined(String message) {
        return new ApiException(HttpStatus.PAYMENT_REQUIRED, ApiErrorCode.PAYMENT_DECLINED, message);
    }
}

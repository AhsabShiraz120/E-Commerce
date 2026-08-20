package com.bookworm.common;

import com.bookworm.api.model.ApiError;
import com.bookworm.api.model.ApiErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Maps every uncaught exception onto the {@link ApiError} envelope defined in
 * docs/openapi.yaml so the frontend gets consistent, machine-readable errors.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex) {
        return respond(ex.getStatus(), ex.getCode(), ex.getMessage(), ex.getDetails());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EntityNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage(),
                        (a, b) -> a
                ));
        return respond(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
                "One or more fields failed validation", fieldErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put(ex.getName(), "invalid type");
        return respond(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
                "Parameter '" + ex.getName() + "' has an invalid value", details);
    }

    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    public ResponseEntity<ApiError> handleAuth(AuthenticationException ex) {
        return respond(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED, ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return respond(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAnythingElse(Exception ex) {
        log.error("Unhandled exception", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR,
                "Something went wrong. Please try again.", null);
    }

    // ----------------------------------------------------------------------------

    private static ResponseEntity<ApiError> respond(HttpStatus status,
                                                    ApiErrorCode code,
                                                    String message,
                                                    Map<String, Object> details) {
        ApiError body = new ApiError()
                .code(code)
                .message(message == null ? code.getValue() : message);
        if (details != null) {
            body.setDetails(details);
        }
        return ResponseEntity.status(status).body(body);
    }
}

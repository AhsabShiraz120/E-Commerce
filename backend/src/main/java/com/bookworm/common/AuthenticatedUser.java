package com.bookworm.common;

import com.bookworm.api.model.ApiErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper for controllers to pull the current user id off the security
 * context. Throws {@link ApiException} unauthenticated if no principal is set,
 * which the global exception handler maps to a 401 with the spec's error
 * envelope.
 */
public final class AuthenticatedUser {

    private AuthenticatedUser() {}

    public static Long requireCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED,
                    "Authentication required");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Long l) {
            return l;
        }
        if (principal instanceof Number n) {
            return n.longValue();
        }
        if (principal instanceof String s) {
            return Long.valueOf(s);
        }
        throw new ApiException(HttpStatus.UNAUTHORIZED, ApiErrorCode.UNAUTHENTICATED,
                "Unrecognised principal type");
    }
}

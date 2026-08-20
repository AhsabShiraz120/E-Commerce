package com.bookworm.member.entity;

/**
 * Matches the CHECK constraint in V1__init.sql on {@code app_user.role} and the
 * {@code UserRole} enum in the generated OpenAPI model.
 */
public enum UserRole {
    GUEST,
    CUSTOMER,
    ADMIN
}

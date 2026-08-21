package com.bookworm.order.entity;

/**
 * Matches the CHECK constraint on {@code app_order.status} and the OpenAPI
 * {@code OrderStatus} enum. Legal transitions live in {@code OrderService.cancel}
 * and (later) the payment/shipping flows.
 */
public enum OrderStatus {
    PENDING,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}

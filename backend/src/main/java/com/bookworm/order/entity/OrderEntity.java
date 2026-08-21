package com.bookworm.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "address_id", nullable = false)
    private Long addressId;

    @Column(name = "subtotal_paise", nullable = false)
    private Integer subtotalPaise;

    @Column(name = "tax_paise", nullable = false)
    private Integer taxPaise;

    @Column(name = "shipping_paise", nullable = false)
    private Integer shippingPaise;

    @Column(name = "discount_paise", nullable = false)
    @Builder.Default
    private Integer discountPaise = 0;

    @Column(name = "gift_points_used", nullable = false)
    @Builder.Default
    private Integer giftPointsUsed = 0;

    @Column(name = "total_paise", nullable = false)
    private Integer totalPaise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "cancellable_until", nullable = false)
    private OffsetDateTime cancellableUntil;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();
}

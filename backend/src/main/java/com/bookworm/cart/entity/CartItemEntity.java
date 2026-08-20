package com.bookworm.cart.entity;

import com.bookworm.catalog.entity.BookEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_item",
       uniqueConstraints = @UniqueConstraint(name = "ux_cart_item_book", columnNames = {"cart_id", "book_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private CartEntity cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;

    @Column(nullable = false)
    private Integer quantity;
}

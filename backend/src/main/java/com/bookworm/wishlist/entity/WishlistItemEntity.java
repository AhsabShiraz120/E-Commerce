package com.bookworm.wishlist.entity;

import com.bookworm.catalog.entity.BookEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist_item",
       uniqueConstraints = @UniqueConstraint(name = "ux_wishlist_item_book", columnNames = {"wishlist_id", "book_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private WishlistEntity wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private BookEntity book;
}

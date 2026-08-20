package com.bookworm.catalog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "book")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private AuthorEntity author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private BrandEntity brand;

    @Column(nullable = false, length = 16)
    private String format;

    @Column(nullable = false, length = 32)
    @Builder.Default
    private String language = "English";

    @Column(name = "price_paise", nullable = false)
    private Integer pricePaise;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal rating = new BigDecimal("0.0");

    @Column(name = "copies_sold", nullable = false)
    @Builder.Default
    private Integer copiesSold = 0;

    @Column(name = "tentative_delivery_days", nullable = false)
    @Builder.Default
    private Integer tentativeDeliveryDays = 5;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_category",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<CategoryEntity> categories = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "book_tag", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "tag", length = 40, nullable = false)
    @Builder.Default
    private Set<String> tags = new HashSet<>();
}

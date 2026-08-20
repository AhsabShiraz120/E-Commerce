package com.bookworm.catalog;

import com.bookworm.catalog.entity.BookEntity;
import com.bookworm.catalog.entity.BrandEntity;
import com.bookworm.catalog.entity.CategoryEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

/**
 * Composable JPA Specifications for {@code /books} filter query.
 */
public final class BookSpecifications {

    private BookSpecifications() {}

    public static Specification<BookEntity> textSearch(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<BookEntity> inCategorySlugs(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return null;
        return (root, cq, cb) -> {
            cq.distinct(true);
            Join<BookEntity, CategoryEntity> join = root.join("categories");
            return join.get("slug").in(slugs);
        };
    }

    public static Specification<BookEntity> inBrandSlugs(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) return null;
        return (root, cq, cb) -> {
            Join<BookEntity, BrandEntity> join = root.join("brand");
            return join.get("slug").in(slugs);
        };
    }

    public static Specification<BookEntity> hasFormat(String format) {
        if (format == null || format.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(root.get("format"), format);
    }

    public static Specification<BookEntity> hasLanguage(String language) {
        if (language == null || language.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("language")), language.toLowerCase());
    }

    public static Specification<BookEntity> priceBetween(Integer min, Integer max) {
        if (min == null && max == null) return null;
        return (root, cq, cb) -> {
            if (min != null && max != null) return cb.between(root.get("pricePaise"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("pricePaise"), min);
            return cb.lessThanOrEqualTo(root.get("pricePaise"), max);
        };
    }

    public static Specification<BookEntity> minRating(BigDecimal min) {
        if (min == null) return null;
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("rating"), min);
    }
}

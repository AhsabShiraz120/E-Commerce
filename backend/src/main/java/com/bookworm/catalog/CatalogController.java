package com.bookworm.catalog;

import com.bookworm.api.CatalogApi;
import com.bookworm.api.model.*;
import com.bookworm.common.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CatalogController implements CatalogApi {

    private final CatalogService catalogService;
    private final RecommendationService recommendationService;

    @Override
    public ResponseEntity<List<Category>> listCategories() {
        return ResponseEntity.ok(catalogService.listCategories());
    }

    @Override
    public ResponseEntity<List<Brand>> listBrands() {
        return ResponseEntity.ok(catalogService.listBrands());
    }

    @Override
    public ResponseEntity<Author> getAuthor(Long id) {
        return ResponseEntity.ok(catalogService.getAuthor(id));
    }

    @Override
    public ResponseEntity<BookPage> listBooks(String q, List<String> category, List<String> brand,
                                              BookFormat format, String language,
                                              Integer priceMinPaise, Integer priceMaxPaise,
                                              BigDecimal minRating, String sort,
                                              Integer page, Integer size) {
        return ResponseEntity.ok(catalogService.listBooks(q, category, brand, format, language,
                priceMinPaise, priceMaxPaise, minRating, sort, page, size));
    }

    @Override
    public ResponseEntity<Book> getBook(Long id) {
        return ResponseEntity.ok(catalogService.getBook(id));
    }

    @Override
    public ResponseEntity<List<BookSummary>> getRelatedBooks(Long id) {
        return ResponseEntity.ok(catalogService.relatedBooks(id));
    }

    @Override
    public ResponseEntity<List<BookSummary>> getRecommendedBooks(Integer limit) {
        int n = limit == null ? 10 : Math.min(Math.max(limit, 1), 20);
        Long userId = tryCurrentUserId();
        return ResponseEntity.ok(recommendationService.recommendFor(userId, n));
    }

    @Override
    public ResponseEntity<ReviewPage> listReviews(Long id, Integer page, Integer size) {
        return ResponseEntity.ok(catalogService.listReviews(id, page, size));
    }

    @Override
    public ResponseEntity<Review> createReview(Long id, ReviewRequest reviewRequest) {
        Long userId = AuthenticatedUser.requireCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.createReview(id, userId, reviewRequest));
    }

    // Recommended endpoint tolerates anonymous callers (falls back to bestsellers)
    private static Long tryCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object p = auth.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof Number n) return n.longValue();
        if (p instanceof String s) {
            try { return Long.valueOf(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}

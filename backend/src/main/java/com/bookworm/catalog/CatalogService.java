package com.bookworm.catalog;

import com.bookworm.api.model.*;
import com.bookworm.catalog.entity.BookEntity;
import com.bookworm.catalog.entity.ReviewEntity;
import com.bookworm.catalog.repo.*;
import com.bookworm.common.ApiException;
import com.bookworm.common.PageResponses;
import com.bookworm.member.entity.UserEntity;
import com.bookworm.member.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.bookworm.catalog.BookSpecifications.*;

@Service
@RequiredArgsConstructor
public class CatalogService {

    static final int PAGE_MAX_SIZE = 100;
    private static final int RELATED_LIMIT = 6;

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final AuthorMapper authorMapper;
    private final BookMapper bookMapper;
    private final ReviewMapper reviewMapper;

    // ------------------------------- reference data -------------------------------

    @Transactional(readOnly = true)
    public List<Category> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(categoryMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<Brand> listBrands() {
        return brandRepository.findAllByOrderByNameAsc().stream()
                .map(brandMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Author getAuthor(Long id) {
        return authorRepository.findById(id)
                .map(authorMapper::toDto)
                .orElseThrow(() -> ApiException.notFound("Author " + id + " not found"));
    }

    // ------------------------------- books -------------------------------

    @Transactional(readOnly = true)
    public BookPage listBooks(String q, List<String> categorySlugs, List<String> brandSlugs,
                              BookFormat format, String language,
                              Integer priceMinPaise, Integer priceMaxPaise,
                              BigDecimal minRating, String sort,
                              Integer page, Integer size) {

        int pageIx = page == null ? 0 : Math.max(0, page);
        int sizeIx = size == null ? 24 : Math.min(Math.max(size, 1), PAGE_MAX_SIZE);

        Specification<BookEntity> spec = Specification.where(textSearch(q))
                .and(inCategorySlugs(categorySlugs))
                .and(inBrandSlugs(brandSlugs))
                .and(hasFormat(format == null ? null : format.getValue()))
                .and(hasLanguage(language))
                .and(priceBetween(priceMinPaise, priceMaxPaise))
                .and(minRating(minRating));

        Pageable pageable = PageRequest.of(pageIx, sizeIx, resolveSort(sort));
        Page<BookEntity> result = bookRepository.findAll(spec, pageable);

        List<BookSummary> content = result.getContent().stream().map(bookMapper::toSummary).toList();
        return new BookPage().content(content).meta(PageResponses.meta(result));
    }

    @Transactional(readOnly = true)
    public Book getBook(Long id) {
        BookEntity b = bookRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Book " + id + " not found"));
        return bookMapper.toDto(b);
    }

    @Transactional(readOnly = true)
    public List<BookSummary> relatedBooks(Long bookId) {
        BookEntity source = bookRepository.findById(bookId)
                .orElseThrow(() -> ApiException.notFound("Book " + bookId + " not found"));
        if (source.getBrand() == null || source.getCategories().isEmpty()) {
            return List.of();
        }
        List<Long> categoryIds = source.getCategories().stream().map(c -> c.getId()).toList();
        return bookRepository.findRelated(bookId, source.getBrand().getId(),
                        categoryIds, PageRequest.of(0, RELATED_LIMIT)).stream()
                .map(bookMapper::toSummary).toList();
    }

    // ------------------------------- reviews -------------------------------

    @Transactional(readOnly = true)
    public ReviewPage listReviews(Long bookId, Integer page, Integer size) {
        if (!bookRepository.existsById(bookId)) {
            throw ApiException.notFound("Book " + bookId + " not found");
        }
        int pageIx = page == null ? 0 : Math.max(0, page);
        int sizeIx = size == null ? 24 : Math.min(Math.max(size, 1), PAGE_MAX_SIZE);

        Page<ReviewEntity> result = reviewRepository.findAllByBookIdOrderByCreatedAtDesc(
                bookId, PageRequest.of(pageIx, sizeIx));

        List<Review> content = new ArrayList<>(result.getContent().size());
        for (ReviewEntity r : result.getContent()) {
            Review dto = reviewMapper.toDto(r);
            userRepository.findById(r.getUserId()).ifPresent(u ->
                    dto.setUserName(buildDisplayName(u))
            );
            content.add(dto);
        }
        return new ReviewPage().content(content).meta(PageResponses.meta(result));
    }

    @Transactional
    public Review createReview(Long bookId, Long userId, ReviewRequest req) {
        if (!bookRepository.existsById(bookId)) {
            throw ApiException.notFound("Book " + bookId + " not found");
        }
        if (reviewRepository.existsByBookIdAndUserId(bookId, userId)) {
            throw ApiException.conflict(ApiErrorCode.CONFLICT,
                    "You have already reviewed this book");
        }
        ReviewEntity r = ReviewEntity.builder()
                .bookId(bookId).userId(userId)
                .rating(req.getRating())
                .text(req.getText())
                .build();
        r = reviewRepository.save(r);
        recomputeBookRating(bookId);

        Review dto = reviewMapper.toDto(r);
        userRepository.findById(userId).ifPresent(u -> dto.setUserName(buildDisplayName(u)));
        return dto;
    }

    // ------------------------------- helpers -------------------------------

    private Sort resolveSort(String sort) {
        if (sort == null) return Sort.unsorted();
        return switch (sort) {
            case "price_asc"  -> Sort.by(Sort.Direction.ASC,  "pricePaise");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "pricePaise");
            case "newest"     -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating"     -> Sort.by(Sort.Direction.DESC, "rating");
            case "popular"    -> Sort.by(Sort.Direction.DESC, "copiesSold");
            default           -> Sort.unsorted(); // relevance == DB natural order
        };
    }

    private void recomputeBookRating(Long bookId) {
        Double avg = reviewRepository.averageRating(bookId);
        double v = avg == null ? 0.0 : Math.round(avg * 10.0) / 10.0;
        bookRepository.findById(bookId).ifPresent(b -> b.setRating(BigDecimal.valueOf(v)));
    }

    private static String buildDisplayName(UserEntity u) {
        String first = u.getFirstName() == null ? "" : u.getFirstName();
        String last  = u.getLastName()  == null ? "" : u.getLastName();
        String name  = (first + " " + last).trim();
        return name.isEmpty() ? u.getEmail() : name;
    }
}

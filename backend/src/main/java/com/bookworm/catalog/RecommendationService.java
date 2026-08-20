package com.bookworm.catalog;

import com.bookworm.api.model.BookSummary;
import com.bookworm.catalog.repo.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * v1: bestseller fallback for every caller. Order-history-aware algorithm
 * lands on feature/backend-recommendations (branch 8). Interface stable.
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public List<BookSummary> recommendFor(Long userId, int limit) {
        return bookRepository.findAllByOrderByCopiesSoldDesc(PageRequest.of(0, limit))
                .stream().map(bookMapper::toSummary).toList();
    }
}

package com.bookworm.catalog;

import com.bookworm.api.model.BookSummary;
import com.bookworm.catalog.entity.BookEntity;
import com.bookworm.catalog.entity.CategoryEntity;
import com.bookworm.catalog.repo.BookRepository;
import com.bookworm.order.entity.OrderStatus;
import com.bookworm.order.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Order-history-aware recommender per plan §3.
 *
 * <ol>
 *   <li>Anonymous or zero completed orders → bestseller fallback.
 *   <li>Gather {@code S} = distinct bookIds from the user's PAID/SHIPPED/DELIVERED orders.
 *   <li>Find {@code U} = other users who ordered any book in {@code S}.
 *   <li>Count books ordered by users in {@code U} (excluding {@code S}),
 *       weighted by {@code 1 / log2(2 + userOrderCount)} to normalise heavy buyers.
 *   <li>Filter to books whose categories overlap the categories of {@code S}.
 *   <li>Order by weighted score desc, then {@code copies_sold} desc.
 *   <li>If shorter than requested, pad with bestsellers.
 * </ol>
 */
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Set<OrderStatus> COMPLETED =
            Set.of(OrderStatus.PAID, OrderStatus.SHIPPED, OrderStatus.DELIVERED);

    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final BookMapper bookMapper;

    @Transactional(readOnly = true)
    public List<BookSummary> recommendFor(Long userId, int limit) {
        if (userId == null || orderRepository.countByUserIdAndStatusIn(userId, COMPLETED) == 0) {
            return bestsellerFallback(limit, Set.of());
        }

        // Step 2 — set S
        List<Long> s = orderRepository.findBookIdsOrderedBy(userId, COMPLETED);
        if (s.isEmpty()) {
            return bestsellerFallback(limit, Set.of());
        }
        Set<Long> sSet = new HashSet<>(s);

        // Step 3 — set U
        List<Long> u = orderRepository.findPeerUsersWhoOrdered(s, userId, COMPLETED);
        if (u.isEmpty()) {
            return bestsellerFallback(limit, sSet);
        }

        // Step 4 — per-user order counts (weight normaliser)
        Map<Long, Long> ordersPerUser = new HashMap<>();
        for (Object[] row : orderRepository.countOrdersPerUser(u, COMPLETED)) {
            ordersPerUser.put((Long) row[0], (Long) row[1]);
        }

        // Step 4 (cont.) — score each candidate book
        Map<Long, Double> score = new HashMap<>();
        for (Object[] row : orderRepository.countBooksOrderedByUsers(u, COMPLETED)) {
            Long bookId = (Long) row[0];
            if (sSet.contains(bookId)) continue;
            long orderCount = (Long) row[1];
            double weight = 0.0;
            for (Long peer : u) {
                long peerOrders = ordersPerUser.getOrDefault(peer, 1L);
                weight += 1.0 / (Math.log(2.0 + peerOrders) / Math.log(2.0));
            }
            // Simplified: weight the raw count by the average peer normaliser.
            double avgNorm = weight / Math.max(1, u.size());
            score.merge(bookId, orderCount * avgNorm, Double::sum);
        }
        if (score.isEmpty()) {
            return bestsellerFallback(limit, sSet);
        }

        // Fetch candidate books once, then apply category-overlap filter
        Set<Long> sCategoryIds = collectCategoryIds(s);
        Map<Long, BookEntity> books = new HashMap<>();
        for (BookEntity b : bookRepository.findAllById(score.keySet())) {
            books.put(b.getId(), b);
        }

        // Step 5 — category-overlap filter, step 6 — sort
        LinkedHashMap<Long, BookEntity> ranked = new LinkedHashMap<>();
        score.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Long, Double>>comparingDouble(Map.Entry::getValue).reversed()
                        .thenComparing(e -> -bookCopiesSold(books.get(e.getKey()))))
                .forEach(e -> {
                    BookEntity b = books.get(e.getKey());
                    if (b == null) return;
                    if (!sCategoryIds.isEmpty() && !categoryOverlap(b, sCategoryIds)) return;
                    ranked.put(b.getId(), b);
                });

        List<BookSummary> result = ranked.values().stream()
                .limit(limit)
                .map(bookMapper::toSummary)
                .toList();

        // Step 7 — pad with bestsellers if short
        if (result.size() < limit) {
            Set<Long> exclude = new HashSet<>(sSet);
            exclude.addAll(ranked.keySet());
            List<BookSummary> filler = bestsellerFallback(limit - result.size(), exclude);
            return concat(result, filler, limit);
        }
        return result;
    }

    // ------------------------------------------------------------------------

    private List<BookSummary> bestsellerFallback(int limit, Set<Long> exclude) {
        int fetch = Math.max(limit + exclude.size(), limit);
        return bookRepository.findAllByOrderByCopiesSoldDesc(PageRequest.of(0, fetch)).stream()
                .filter(b -> !exclude.contains(b.getId()))
                .limit(limit)
                .map(bookMapper::toSummary)
                .toList();
    }

    private Set<Long> collectCategoryIds(List<Long> bookIds) {
        Set<Long> ids = new HashSet<>();
        for (BookEntity b : bookRepository.findAllById(bookIds)) {
            for (CategoryEntity c : b.getCategories()) ids.add(c.getId());
        }
        return ids;
    }

    private static boolean categoryOverlap(BookEntity book, Set<Long> reference) {
        for (CategoryEntity c : book.getCategories()) {
            if (reference.contains(c.getId())) return true;
        }
        return false;
    }

    private static int bookCopiesSold(BookEntity b) {
        if (b == null || b.getCopiesSold() == null) return 0;
        return b.getCopiesSold();
    }

    private static List<BookSummary> concat(List<BookSummary> a, List<BookSummary> b, int limit) {
        java.util.ArrayList<BookSummary> out = new java.util.ArrayList<>(a);
        out.addAll(b);
        return out.stream().limit(limit).toList();
    }
}

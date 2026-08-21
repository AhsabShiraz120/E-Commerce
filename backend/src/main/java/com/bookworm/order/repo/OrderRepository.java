package com.bookworm.order.repo;

import com.bookworm.order.entity.OrderEntity;
import com.bookworm.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

    // --- Recommender queries -------------------------------------------------

    @Query("select count(o) from OrderEntity o where o.userId = :userId and o.status in :statuses")
    long countByUserIdAndStatusIn(Long userId, Collection<OrderStatus> statuses);

    /**
     * Distinct {@code bookId}s from the user's completed orders (set S in plan §3).
     */
    @Query("""
        select distinct oi.book.id
        from OrderEntity o join o.items oi
        where o.userId = :userId and o.status in :statuses
    """)
    List<Long> findBookIdsOrderedBy(Long userId, Collection<OrderStatus> statuses);

    /**
     * Distinct {@code userId}s (other than the current one) who ordered any book in :bookIds
     * (set U in plan §3).
     */
    @Query("""
        select distinct o.userId
        from OrderEntity o join o.items oi
        where oi.book.id in :bookIds and o.userId <> :excludeUserId and o.status in :statuses
    """)
    List<Long> findPeerUsersWhoOrdered(Collection<Long> bookIds, Long excludeUserId,
                                       Collection<OrderStatus> statuses);

    /**
     * For each book ordered by users in :userIds, count the number of orders it appears in.
     * Rows are {@code [bookId, orderCount]}. Uses join fetch so BookEntity is materialized.
     */
    @Query("""
        select oi.book.id, count(oi)
        from OrderEntity o join o.items oi
        where o.userId in :userIds and o.status in :statuses
        group by oi.book.id
    """)
    List<Object[]> countBooksOrderedByUsers(Collection<Long> userIds,
                                            Collection<OrderStatus> statuses);

    /**
     * Per user, how many completed orders — used as the {@code userOrderCount}
     * weight-normalizer in step 4 of the plan §3 algorithm.
     */
    @Query("""
        select o.userId, count(o)
        from OrderEntity o
        where o.userId in :userIds and o.status in :statuses
        group by o.userId
    """)
    List<Object[]> countOrdersPerUser(Collection<Long> userIds,
                                      Collection<OrderStatus> statuses);
}

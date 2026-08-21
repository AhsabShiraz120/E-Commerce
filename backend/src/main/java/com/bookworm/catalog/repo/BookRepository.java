package com.bookworm.catalog.repo;

import com.bookworm.catalog.entity.BookEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<BookEntity, Long>,
                                        JpaSpecificationExecutor<BookEntity> {

    Page<BookEntity> findAllByOrderByCopiesSoldDesc(Pageable pageable);

    Page<BookEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Related-reads query: same brand, sharing at least one category with the source book,
     * excluding the source itself, in-stock only, ranked by copies_sold desc.
     */
    @Query("""
        select distinct b from BookEntity b
        join b.categories c
        where b.id <> :bookId
          and b.brand.id = :brandId
          and c.id in :categoryIds
          and b.stock > 0
        order by b.copiesSold desc
        """)
    List<BookEntity> findRelated(Long bookId, Long brandId, List<Long> categoryIds, Pageable pageable);

    /**
     * Pessimistic lock on the book row for atomic stock decrement during checkout.
     * Emits {@code SELECT ... FOR UPDATE} on PostgreSQL; blocks concurrent checkouts
     * from double-selling the last copy.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookEntity b where b.id = :id")
    Optional<BookEntity> findByIdForUpdate(Long id);
}

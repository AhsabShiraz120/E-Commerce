package com.bookworm.catalog.repo;

import com.bookworm.catalog.entity.BookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
}

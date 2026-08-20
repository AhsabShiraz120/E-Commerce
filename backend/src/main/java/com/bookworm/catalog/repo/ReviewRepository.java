package com.bookworm.catalog.repo;

import com.bookworm.catalog.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    Page<ReviewEntity> findAllByBookIdOrderByCreatedAtDesc(Long bookId, Pageable pageable);

    boolean existsByBookIdAndUserId(Long bookId, Long userId);

    @Query("select coalesce(avg(r.rating), 0.0) from ReviewEntity r where r.bookId = :bookId")
    Double averageRating(Long bookId);
}

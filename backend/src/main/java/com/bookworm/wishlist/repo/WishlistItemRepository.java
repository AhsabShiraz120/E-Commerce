package com.bookworm.wishlist.repo;

import com.bookworm.wishlist.entity.WishlistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, Long> {

    Optional<WishlistItemEntity> findByWishlistIdAndBookId(Long wishlistId, Long bookId);

    Optional<WishlistItemEntity> findByIdAndWishlistId(Long id, Long wishlistId);
}

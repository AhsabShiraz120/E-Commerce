package com.bookworm.wishlist.repo;

import com.bookworm.wishlist.entity.WishlistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistEntity, Long> {

    Optional<WishlistEntity> findByUserId(Long userId);
}

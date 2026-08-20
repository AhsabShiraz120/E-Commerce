package com.bookworm.cart.repo;

import com.bookworm.cart.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    Optional<CartItemEntity> findByCartIdAndBookId(Long cartId, Long bookId);

    Optional<CartItemEntity> findByIdAndCartId(Long id, Long cartId);
}

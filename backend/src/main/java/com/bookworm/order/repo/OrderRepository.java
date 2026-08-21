package com.bookworm.order.repo;

import com.bookworm.order.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Page<OrderEntity> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);
}

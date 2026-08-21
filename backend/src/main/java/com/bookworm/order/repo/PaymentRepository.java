package com.bookworm.order.repo;

import com.bookworm.order.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findAllByOrderId(Long orderId);

    Optional<PaymentEntity> findFirstByOrderIdAndStatus(Long orderId, String status);
}

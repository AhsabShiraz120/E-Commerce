package com.bookworm.member.repo;

import com.bookworm.member.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findAllByUserIdOrderByIsDefaultDescIdAsc(Long userId);

    Optional<AddressEntity> findByIdAndUserId(Long id, Long userId);

    List<AddressEntity> findAllByUserIdAndIsDefaultTrue(Long userId);
}

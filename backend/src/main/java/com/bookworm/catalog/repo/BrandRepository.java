package com.bookworm.catalog.repo;

import com.bookworm.catalog.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {

    List<BrandEntity> findAllByOrderByNameAsc();

    Optional<BrandEntity> findBySlug(String slug);

    List<BrandEntity> findAllBySlugIn(List<String> slugs);
}

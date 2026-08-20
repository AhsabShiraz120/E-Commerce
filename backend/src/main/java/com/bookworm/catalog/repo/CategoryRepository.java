package com.bookworm.catalog.repo;

import com.bookworm.catalog.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    List<CategoryEntity> findAllByOrderByNameAsc();

    Optional<CategoryEntity> findBySlug(String slug);

    List<CategoryEntity> findAllBySlugIn(List<String> slugs);
}

package com.bookworm.catalog;

import com.bookworm.api.model.Category;
import com.bookworm.catalog.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    Category toDto(CategoryEntity entity);
}

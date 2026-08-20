package com.bookworm.catalog;

import com.bookworm.api.model.Brand;
import com.bookworm.catalog.entity.BrandEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    Brand toDto(BrandEntity entity);
}

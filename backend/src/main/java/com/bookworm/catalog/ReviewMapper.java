package com.bookworm.catalog;

import com.bookworm.api.model.Review;
import com.bookworm.catalog.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    @Mapping(target = "userName", ignore = true)
    Review toDto(ReviewEntity entity);
}

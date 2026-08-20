package com.bookworm.catalog;

import com.bookworm.api.model.Author;
import com.bookworm.catalog.entity.AuthorEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthorMapper {
    Author toDto(AuthorEntity entity);
}

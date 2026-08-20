package com.bookworm.catalog;

import com.bookworm.api.model.Book;
import com.bookworm.api.model.BookFormat;
import com.bookworm.api.model.BookSummary;
import com.bookworm.catalog.entity.BookEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = { CategoryMapper.class, BrandMapper.class, AuthorMapper.class },
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BookMapper {

    @Mapping(target = "format", source = "format", qualifiedByName = "stringToFormat")
    Book toDto(BookEntity entity);

    @Mapping(target = "authorName", source = "author.name")
    @Mapping(target = "inStock",    source = "stock",  qualifiedByName = "stockToInStock")
    @Mapping(target = "format",     source = "format", qualifiedByName = "stringToFormat")
    BookSummary toSummary(BookEntity entity);

    @Named("stringToFormat")
    default BookFormat stringToFormat(String format) {
        return format == null ? null : BookFormat.fromValue(format);
    }

    @Named("stockToInStock")
    default Boolean stockToInStock(Integer stock) {
        return stock != null && stock > 0;
    }
}

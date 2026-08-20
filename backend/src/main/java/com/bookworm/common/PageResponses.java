package com.bookworm.common;

import com.bookworm.api.model.PageMeta;
import org.springframework.data.domain.Page;

/**
 * Small helper that turns Spring's {@link Page} into the flat {@code PageMeta}
 * shape defined in the OpenAPI spec.
 */
public final class PageResponses {

    private PageResponses() {}

    public static PageMeta meta(Page<?> page) {
        return new PageMeta()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements((int) page.getTotalElements())
                .totalPages(page.getTotalPages());
    }
}

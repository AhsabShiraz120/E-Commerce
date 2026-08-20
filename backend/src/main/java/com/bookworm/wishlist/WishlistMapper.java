package com.bookworm.wishlist;

import com.bookworm.api.model.Wishlist;
import com.bookworm.api.model.WishlistItem;
import com.bookworm.catalog.BookMapper;
import com.bookworm.wishlist.entity.WishlistEntity;
import com.bookworm.wishlist.entity.WishlistItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WishlistMapper {

    private final BookMapper bookMapper;

    public Wishlist toDto(WishlistEntity entity) {
        List<WishlistItem> items = entity.getItems().stream()
                .map(this::toItemDto)
                .toList();
        return new Wishlist()
                .id(entity.getId())
                .items(items);
    }

    private WishlistItem toItemDto(WishlistItemEntity item) {
        return new WishlistItem()
                .id(item.getId())
                .book(bookMapper.toSummary(item.getBook()));
    }
}

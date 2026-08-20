package com.bookworm.wishlist;

import com.bookworm.api.model.Wishlist;
import com.bookworm.catalog.entity.BookEntity;
import com.bookworm.catalog.repo.BookRepository;
import com.bookworm.common.ApiException;
import com.bookworm.wishlist.entity.WishlistEntity;
import com.bookworm.wishlist.entity.WishlistItemEntity;
import com.bookworm.wishlist.repo.WishlistItemRepository;
import com.bookworm.wishlist.repo.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final BookRepository bookRepository;
    private final WishlistMapper wishlistMapper;

    @Transactional
    public Wishlist getWishlist(Long userId) {
        return wishlistMapper.toDto(getOrCreateWishlist(userId));
    }

    @Transactional
    public Wishlist addItem(Long userId, Long bookId) {
        WishlistEntity wl = getOrCreateWishlist(userId);
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> ApiException.notFound("Book " + bookId + " not found"));

        if (wishlistItemRepository.findByWishlistIdAndBookId(wl.getId(), bookId).isEmpty()) {
            WishlistItemEntity item = WishlistItemEntity.builder()
                    .wishlist(wl).book(book).build();
            wl.getItems().add(item);
            wishlistItemRepository.save(item);
        }
        return wishlistMapper.toDto(wl);
    }

    @Transactional
    public Wishlist removeItem(Long userId, Long itemId) {
        WishlistEntity wl = getOrCreateWishlist(userId);
        WishlistItemEntity item = wishlistItemRepository.findByIdAndWishlistId(itemId, wl.getId())
                .orElseThrow(() -> ApiException.notFound("Wishlist line " + itemId + " not found"));
        wl.getItems().remove(item);
        wishlistItemRepository.delete(item);
        return wishlistMapper.toDto(wl);
    }

    // -------------------------------------------------------------------------------

    @Transactional
    public WishlistEntity getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> wishlistRepository.save(WishlistEntity.builder().userId(userId).build()));
    }
}

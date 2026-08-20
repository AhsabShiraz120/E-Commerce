package com.bookworm.wishlist;

import com.bookworm.api.WishlistApi;
import com.bookworm.api.model.Wishlist;
import com.bookworm.api.model.WishlistItemRequest;
import com.bookworm.common.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WishlistController implements WishlistApi {

    private final WishlistService wishlistService;

    @Override
    public ResponseEntity<Wishlist> getWishlist() {
        return ResponseEntity.ok(wishlistService.getWishlist(AuthenticatedUser.requireCurrentUserId()));
    }

    @Override
    public ResponseEntity<Wishlist> addWishlistItem(WishlistItemRequest req) {
        return ResponseEntity.ok(wishlistService.addItem(
                AuthenticatedUser.requireCurrentUserId(), req.getBookId()));
    }

    @Override
    public ResponseEntity<Wishlist> removeWishlistItem(Long id) {
        return ResponseEntity.ok(wishlistService.removeItem(
                AuthenticatedUser.requireCurrentUserId(), id));
    }
}

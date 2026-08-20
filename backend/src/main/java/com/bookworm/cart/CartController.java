package com.bookworm.cart;

import com.bookworm.api.CartApi;
import com.bookworm.api.model.Cart;
import com.bookworm.api.model.CartItemRequest;
import com.bookworm.api.model.CartItemUpdateRequest;
import com.bookworm.common.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CartController implements CartApi {

    private final CartService cartService;

    @Override
    public ResponseEntity<Cart> getCart() {
        return ResponseEntity.ok(cartService.getCart(AuthenticatedUser.requireCurrentUserId()));
    }

    @Override
    public ResponseEntity<Cart> addCartItem(CartItemRequest req) {
        return ResponseEntity.ok(cartService.addItem(
                AuthenticatedUser.requireCurrentUserId(), req.getBookId(), req.getQuantity()));
    }

    @Override
    public ResponseEntity<Cart> updateCartItem(Long id, CartItemUpdateRequest req) {
        return ResponseEntity.ok(cartService.updateItem(
                AuthenticatedUser.requireCurrentUserId(), id, req.getQuantity()));
    }

    @Override
    public ResponseEntity<Cart> removeCartItem(Long id) {
        return ResponseEntity.ok(cartService.removeItem(
                AuthenticatedUser.requireCurrentUserId(), id));
    }
}

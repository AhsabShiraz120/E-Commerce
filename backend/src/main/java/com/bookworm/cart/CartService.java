package com.bookworm.cart;

import com.bookworm.api.model.ApiErrorCode;
import com.bookworm.api.model.Cart;
import com.bookworm.cart.entity.CartEntity;
import com.bookworm.cart.entity.CartItemEntity;
import com.bookworm.cart.repo.CartItemRepository;
import com.bookworm.cart.repo.CartRepository;
import com.bookworm.catalog.entity.BookEntity;
import com.bookworm.catalog.repo.BookRepository;
import com.bookworm.common.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final int MAX_QTY_PER_LINE = 99;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    @Transactional
    public Cart getCart(Long userId) {
        return cartMapper.toDto(getOrCreateCart(userId));
    }

    @Transactional
    public Cart addItem(Long userId, Long bookId, Integer requestedQty) {
        int qty = requestedQty == null ? 1 : requestedQty;
        CartEntity cart = getOrCreateCart(userId);
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> ApiException.notFound("Book " + bookId + " not found"));

        CartItemEntity item = cartItemRepository
                .findByCartIdAndBookId(cart.getId(), bookId)
                .orElse(null);

        int newQty = (item == null ? 0 : item.getQuantity()) + qty;
        if (newQty > MAX_QTY_PER_LINE) newQty = MAX_QTY_PER_LINE;
        if (book.getStock() < newQty) {
            throw new ApiException(org.springframework.http.HttpStatus.CONFLICT,
                    ApiErrorCode.STOCK_INSUFFICIENT,
                    "Only " + book.getStock() + " copies available");
        }

        if (item == null) {
            item = CartItemEntity.builder().cart(cart).book(book).quantity(newQty).build();
            cart.getItems().add(item);
        } else {
            item.setQuantity(newQty);
        }
        cartItemRepository.save(item);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public Cart updateItem(Long userId, Long itemId, Integer quantity) {
        int qty = quantity == null ? 1 : Math.min(Math.max(quantity, 1), MAX_QTY_PER_LINE);
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> ApiException.notFound("Cart line " + itemId + " not found"));
        if (item.getBook().getStock() < qty) {
            throw new ApiException(org.springframework.http.HttpStatus.CONFLICT,
                    ApiErrorCode.STOCK_INSUFFICIENT,
                    "Only " + item.getBook().getStock() + " copies available");
        }
        item.setQuantity(qty);
        return cartMapper.toDto(cart);
    }

    @Transactional
    public Cart removeItem(Long userId, Long itemId) {
        CartEntity cart = getOrCreateCart(userId);
        CartItemEntity item = cartItemRepository.findByIdAndCartId(itemId, cart.getId())
                .orElseThrow(() -> ApiException.notFound("Cart line " + itemId + " not found"));
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return cartMapper.toDto(cart);
    }

    // -------------------------------------------------------------------------------

    @Transactional
    public CartEntity getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(CartEntity.builder().userId(userId).build()));
    }
}

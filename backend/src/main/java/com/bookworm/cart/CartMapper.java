package com.bookworm.cart;

import com.bookworm.api.model.Cart;
import com.bookworm.api.model.CartItem;
import com.bookworm.cart.entity.CartEntity;
import com.bookworm.cart.entity.CartItemEntity;
import com.bookworm.catalog.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CartMapper {

    private final BookMapper bookMapper;

    public Cart toDto(CartEntity entity) {
        List<CartItem> lines = entity.getItems().stream()
                .map(this::toItemDto)
                .toList();
        int subtotal = lines.stream()
                .mapToInt(l -> l.getLinePaise() == null ? 0 : l.getLinePaise())
                .sum();
        return new Cart()
                .id(entity.getId())
                .items(lines)
                .subtotalPaise(subtotal);
    }

    private CartItem toItemDto(CartItemEntity item) {
        int line = item.getBook().getPricePaise() * item.getQuantity();
        return new CartItem()
                .id(item.getId())
                .book(bookMapper.toSummary(item.getBook()))
                .quantity(item.getQuantity())
                .linePaise(line);
    }
}

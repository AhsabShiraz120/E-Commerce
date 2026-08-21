package com.bookworm.order;

import com.bookworm.api.model.Address;
import com.bookworm.api.model.Order;
import com.bookworm.api.model.OrderItem;
import com.bookworm.api.model.OrderStatus;
import com.bookworm.api.model.Payment;
import com.bookworm.api.model.PaymentMethod;
import com.bookworm.api.model.PaymentStatus;
import com.bookworm.catalog.BookMapper;
import com.bookworm.member.AddressMapper;
import com.bookworm.member.repo.AddressRepository;
import com.bookworm.order.entity.OrderEntity;
import com.bookworm.order.entity.OrderItemEntity;
import com.bookworm.order.entity.PaymentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final BookMapper bookMapper;
    private final AddressMapper addressMapper;
    private final AddressRepository addressRepository;

    public Order toDto(OrderEntity o) {
        List<OrderItem> items = o.getItems().stream().map(this::toItem).toList();
        Address address = addressRepository.findById(o.getAddressId())
                .map(addressMapper::toDto)
                .orElse(null);
        return new Order()
                .id(o.getId())
                .items(items)
                .address(address)
                .status(OrderStatus.fromValue(o.getStatus().name()))
                .subtotalPaise(o.getSubtotalPaise())
                .taxPaise(o.getTaxPaise())
                .shippingPaise(o.getShippingPaise())
                .discountPaise(o.getDiscountPaise())
                .giftPointsUsed(o.getGiftPointsUsed())
                .totalPaise(o.getTotalPaise())
                .createdAt(o.getCreatedAt())
                .cancellableUntil(o.getCancellableUntil());
    }

    public Payment toPaymentDto(PaymentEntity p) {
        return new Payment()
                .id(p.getId())
                .orderId(p.getOrderId())
                .method(PaymentMethod.fromValue(p.getMethod()))
                .status(PaymentStatus.fromValue(p.getStatus()))
                .transactionRef(p.getTransactionRef())
                .amountPaise(p.getAmountPaise())
                .processedAt(p.getProcessedAt());
    }

    private OrderItem toItem(OrderItemEntity oi) {
        return new OrderItem()
                .id(oi.getId())
                .book(bookMapper.toSummary(oi.getBook()))
                .quantity(oi.getQuantity())
                .priceAtPurchasePaise(oi.getPriceAtPurchase());
    }
}

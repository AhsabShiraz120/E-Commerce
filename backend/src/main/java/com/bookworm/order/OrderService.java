package com.bookworm.order;

import com.bookworm.api.model.*;
import com.bookworm.cart.CartService;
import com.bookworm.cart.entity.CartEntity;
import com.bookworm.cart.entity.CartItemEntity;
import com.bookworm.catalog.entity.BookEntity;
import com.bookworm.catalog.repo.BookRepository;
import com.bookworm.common.ApiException;
import com.bookworm.common.PageResponses;
import com.bookworm.member.entity.UserEntity;
import com.bookworm.member.repo.AddressRepository;
import com.bookworm.member.repo.UserRepository;
import com.bookworm.order.entity.OrderEntity;
import com.bookworm.order.entity.OrderItemEntity;
import com.bookworm.order.entity.OrderStatus;
import com.bookworm.order.entity.PaymentEntity;
import com.bookworm.order.repo.OrderRepository;
import com.bookworm.order.repo.PaymentRepository;
import com.bookworm.payment.MockPaymentGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    public static final int CANCEL_WINDOW_HOURS = 48;
    private static final int PAGE_MAX_SIZE = 100;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final BookRepository bookRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final MockPaymentGateway paymentGateway;

    // ------------------------------------------------------ CHECKOUT

    @Transactional
    public Order checkout(Long userId, CheckoutRequest req) {
        // 1. Resolve caller + address (ownership check)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.unauthenticated("User no longer exists"));
        addressRepository.findByIdAndUserId(req.getAddressId(), userId)
                .orElseThrow(() -> ApiException.notFound("Address " + req.getAddressId() + " not found"));

        // 2. Cart must not be empty
        CartEntity cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED,
                    "Your cart is empty");
        }

        // 3. Pessimistic-lock each book, decrement stock, freeze price
        List<OrderItemEntity> orderItems = new ArrayList<>(cart.getItems().size());
        int subtotal = 0;
        Set<Long> touchedBooks = new HashSet<>();
        for (CartItemEntity line : cart.getItems()) {
            Long bookId = line.getBook().getId();
            BookEntity locked = bookRepository.findByIdForUpdate(bookId)
                    .orElseThrow(() -> ApiException.notFound("Book " + bookId + " not found"));
            if (locked.getStock() < line.getQuantity()) {
                throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.STOCK_INSUFFICIENT,
                        "\"" + locked.getTitle() + "\" only has " + locked.getStock() + " in stock");
            }
            locked.setStock(locked.getStock() - line.getQuantity());
            touchedBooks.add(bookId);

            OrderItemEntity oi = OrderItemEntity.builder()
                    .book(locked)
                    .quantity(line.getQuantity())
                    .priceAtPurchase(locked.getPricePaise())
                    .build();
            orderItems.add(oi);
            subtotal += locked.getPricePaise() * line.getQuantity();
        }

        // 4. Totals (plan §3)
        int requestedPoints = req.getUseGiftPoints() == null ? 0 : req.getUseGiftPoints();
        var breakdown = PricingCalculator.compute(subtotal, 0, requestedPoints, user.getGiftPoints());

        // 5. Persist order
        OrderEntity order = OrderEntity.builder()
                .userId(userId)
                .addressId(req.getAddressId())
                .subtotalPaise(breakdown.subtotalPaise())
                .taxPaise(breakdown.taxPaise())
                .shippingPaise(breakdown.shippingPaise())
                .discountPaise(breakdown.discountPaise())
                .giftPointsUsed(breakdown.giftPointsUsed())
                .totalPaise(breakdown.totalPaise())
                .status(OrderStatus.PENDING)
                .cancellableUntil(OffsetDateTime.now().plus(Duration.ofHours(CANCEL_WINDOW_HOURS)))
                .build();
        orderItems.forEach(oi -> { oi.setOrder(order); order.getItems().add(oi); });
        OrderEntity saved = orderRepository.save(order);

        // 6. Debit gift points and clear cart
        user.setGiftPoints(user.getGiftPoints() - breakdown.giftPointsUsed());
        cart.getItems().clear();

        Order dto = orderMapper.toDto(saved);
        dto.setGiftPointsClamped(breakdown.giftPointsClamped());
        return dto;
    }

    // ------------------------------------------------------ HISTORY

    @Transactional(readOnly = true)
    public OrderPage listMine(Long userId, Integer page, Integer size) {
        int p = page == null ? 0 : Math.max(0, page);
        int s = size == null ? 24 : Math.min(Math.max(size, 1), PAGE_MAX_SIZE);
        Page<OrderEntity> pg = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(p, s));
        List<Order> content = pg.getContent().stream().map(orderMapper::toDto).toList();
        return new OrderPage().content(content).meta(PageResponses.meta(pg));
    }

    @Transactional(readOnly = true)
    public Order getOne(Long userId, Long orderId) {
        OrderEntity o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ApiException.notFound("Order " + orderId + " not found"));
        return orderMapper.toDto(o);
    }

    // ------------------------------------------------------ CANCEL

    @Transactional
    public Order cancel(Long userId, Long orderId) {
        OrderEntity o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ApiException.notFound("Order " + orderId + " not found"));

        if (OffsetDateTime.now().isAfter(o.getCancellableUntil())) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CANCEL_WINDOW_CLOSED,
                    "The 48-hour cancel window closed at " + o.getCancellableUntil());
        }
        if (o.getStatus() != OrderStatus.PENDING && o.getStatus() != OrderStatus.PAID) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.ILLEGAL_STATUS_TRANSITION,
                    "Order in status " + o.getStatus() + " cannot be cancelled");
        }

        // Restore stock
        for (OrderItemEntity oi : o.getItems()) {
            oi.getBook().setStock(oi.getBook().getStock() + oi.getQuantity());
        }

        // Refund gift points (money refund is a payment-gateway concern; branch 7)
        if (o.getGiftPointsUsed() > 0) {
            userRepository.findById(userId).ifPresent(u ->
                    u.setGiftPoints(u.getGiftPoints() + o.getGiftPointsUsed())
            );
        }

        o.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toDto(o);
    }

    // ------------------------------------------------------ BUY AGAIN

    @Transactional
    public BuyAgainResult buyAgain(Long userId, Long orderId) {
        OrderEntity o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ApiException.notFound("Order " + orderId + " not found"));
        List<Long> skipped = new ArrayList<>();
        Cart cart = null;
        for (OrderItemEntity oi : o.getItems()) {
            Long bookId = oi.getBook().getId();
            if (oi.getBook().getStock() < 1) {
                skipped.add(bookId);
                continue;
            }
            try {
                cart = cartService.addItem(userId, bookId, 1);
            } catch (ApiException ex) {
                if (ex.getCode() == ApiErrorCode.STOCK_INSUFFICIENT) {
                    skipped.add(bookId);
                } else {
                    throw ex;
                }
            }
        }
        if (cart == null) cart = cartService.getCart(userId);
        return new BuyAgainResult().cart(cart).skippedBookIds(skipped);
    }

    // ------------------------------------------------------ PAY

    /**
     * Routes through {@link MockPaymentGateway}. Cards ending in {@code 0000}
     * (or UPI IDs starting with {@code fail@}) are declined; every other input
     * succeeds. Both success and decline paths persist a {@link PaymentEntity}
     * row so the order has an audit trail.
     */
    @Transactional
    public Payment pay(Long userId, Long orderId, PaymentRequest req) {
        OrderEntity o = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> ApiException.notFound("Order " + orderId + " not found"));
        if (o.getStatus() != OrderStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.ILLEGAL_STATUS_TRANSITION,
                    "Order in status " + o.getStatus() + " cannot be paid");
        }

        String method = req.getMethod() == null ? "CREDIT" : req.getMethod().getValue();
        MockPaymentGateway.ChargeResult result = paymentGateway.charge(req, o.getTotalPaise());

        PaymentEntity payment = PaymentEntity.builder()
                .orderId(o.getId())
                .method(method)
                .status(result.success() ? "SUCCESS" : "DECLINED")
                .transactionRef(result.transactionRef())
                .amountPaise(o.getTotalPaise())
                .build();
        paymentRepository.save(payment);

        if (!result.success()) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, ApiErrorCode.PAYMENT_DECLINED,
                    result.declineReason());
        }

        o.setStatus(OrderStatus.PAID);

        // Track copies sold (used by bestseller rail + recommender)
        for (OrderItemEntity oi : o.getItems()) {
            BookEntity book = oi.getBook();
            book.setCopiesSold((book.getCopiesSold() == null ? 0 : book.getCopiesSold()) + oi.getQuantity());
        }
        return orderMapper.toPaymentDto(payment);
    }
}

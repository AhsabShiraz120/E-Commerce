package com.bookworm.order;

import com.bookworm.api.OrdersApi;
import com.bookworm.api.model.*;
import com.bookworm.common.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrdersController implements OrdersApi {

    private final OrderService orderService;

    @Override
    public ResponseEntity<Order> checkout(CheckoutRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.checkout(AuthenticatedUser.requireCurrentUserId(), req));
    }

    @Override
    public ResponseEntity<OrderPage> listMyOrders(Integer page, Integer size) {
        return ResponseEntity.ok(orderService.listMine(
                AuthenticatedUser.requireCurrentUserId(), page, size));
    }

    @Override
    public ResponseEntity<Order> getOrder(Long id) {
        return ResponseEntity.ok(orderService.getOne(
                AuthenticatedUser.requireCurrentUserId(), id));
    }

    @Override
    public ResponseEntity<Payment> payForOrder(Long id, PaymentRequest req) {
        return ResponseEntity.ok(orderService.pay(
                AuthenticatedUser.requireCurrentUserId(), id, req));
    }

    @Override
    public ResponseEntity<Order> cancelOrder(Long id) {
        return ResponseEntity.ok(orderService.cancel(
                AuthenticatedUser.requireCurrentUserId(), id));
    }

    @Override
    public ResponseEntity<BuyAgainResult> buyAgain(Long id) {
        return ResponseEntity.ok(orderService.buyAgain(
                AuthenticatedUser.requireCurrentUserId(), id));
    }
}

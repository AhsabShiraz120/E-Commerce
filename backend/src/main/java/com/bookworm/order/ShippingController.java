package com.bookworm.order;

import com.bookworm.api.ShippingApi;
import com.bookworm.api.model.ShippingQuote;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShippingController implements ShippingApi {

    private final ShippingService shippingService;

    @Override
    public ResponseEntity<ShippingQuote> getShippingQuote(String pin, Integer subtotalPaise) {
        return ResponseEntity.ok(shippingService.quote(pin, subtotalPaise));
    }
}

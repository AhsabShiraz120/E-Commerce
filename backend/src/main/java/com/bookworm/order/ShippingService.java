package com.bookworm.order;

import com.bookworm.api.model.ShippingQuote;
import org.springframework.stereotype.Service;

/**
 * v1 mock: deterministic quote. Free shipping ≥ ₹499, else ₹49; ETA 3–7 days.
 * PIN is validated only for shape (6 digits) in the controller layer via bean
 * validation on the {@code @RequestParam}.
 */
@Service
public class ShippingService {

    public ShippingQuote quote(String pin, Integer subtotalPaise) {
        int subtotal = subtotalPaise == null ? 0 : subtotalPaise;
        boolean free = subtotal >= PricingCalculator.FREE_SHIPPING_THRESHOLD_PAISE;
        int price = free ? 0 : PricingCalculator.FLAT_SHIPPING_PAISE;
        return new ShippingQuote()
                .pricePaise(price)
                .etaDaysMin(3)
                .etaDaysMax(7)
                .freeShipping(free);
    }
}

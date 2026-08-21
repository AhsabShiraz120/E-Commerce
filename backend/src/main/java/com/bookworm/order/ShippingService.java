package com.bookworm.order;

import com.bookworm.api.model.ShippingQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Deterministic mock: free shipping ≥ ₹499, else ₹49; ETA 3–7 days.
 * PIN is validated only for shape (6 digits) via bean validation on the
 * {@code @RequestParam}. A real service would consult a courier rate table
 * keyed by PIN and warehouse.
 */
@Service
@Slf4j
public class ShippingService {

    public ShippingQuote quote(String pin, Integer subtotalPaise) {
        int subtotal = subtotalPaise == null ? 0 : subtotalPaise;
        boolean free = subtotal >= PricingCalculator.FREE_SHIPPING_THRESHOLD_PAISE;
        int price = free ? 0 : PricingCalculator.FLAT_SHIPPING_PAISE;
        log.info("MOCK SHIPPING QUOTE  pin={} subtotal={}paise price={}paise free={}",
                pin, subtotal, price, free);
        return new ShippingQuote()
                .pricePaise(price)
                .etaDaysMin(3)
                .etaDaysMax(7)
                .freeShipping(free);
    }
}

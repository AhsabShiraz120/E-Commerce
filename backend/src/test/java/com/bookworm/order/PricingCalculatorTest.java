package com.bookworm.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PricingCalculatorTest {

    // ---------------------------------- tax + shipping -----------------------------

    @Test
    @DisplayName("subtotal ≥ ₹499 → free shipping")
    void freeShippingAtThreshold() {
        var r = PricingCalculator.compute(49_900, 0, 0, 0);
        assertThat(r.shippingPaise()).isZero();
        assertThat(r.taxPaise()).isEqualTo(5_988); // floor(49900 * 0.12)
        assertThat(r.totalPaise()).isEqualTo(49_900 + 5_988);
    }

    @Test
    @DisplayName("subtotal just below ₹499 → flat ₹49 shipping")
    void flatShippingBelowThreshold() {
        var r = PricingCalculator.compute(49_899, 0, 0, 0);
        assertThat(r.shippingPaise()).isEqualTo(4_900);
        assertThat(r.taxPaise()).isEqualTo(5_987); // floor(49899 * 0.12) = 5987
    }

    // ---------------------------------- gift points --------------------------------

    @Test
    @DisplayName("gift points redeem 1:1 against the order")
    void giftPointsRedeemedOneToOne() {
        int subtotal = 100_000; // ₹1000
        var r = PricingCalculator.compute(subtotal, 0, 100, 250);
        assertThat(r.giftPointsUsed()).isEqualTo(100);
        assertThat(r.giftPointsClamped()).isFalse();
        int expectedTotal = subtotal + r.taxPaise() + r.shippingPaise() - 10_000;
        assertThat(r.totalPaise()).isEqualTo(expectedTotal);
    }

    @Test
    @DisplayName("asking for more points than balance is silently clamped and flagged")
    void giftPointsClampedToBalance() {
        var r = PricingCalculator.compute(100_000, 0, 500, 100);
        assertThat(r.giftPointsUsed()).isEqualTo(100);
        assertThat(r.giftPointsClamped()).isTrue();
    }

    @Test
    @DisplayName("gift points cannot zero out the order — leaves at least ₹1")
    void giftPointsLeaveAtLeastOneRupee() {
        // subtotal = ₹100, no tax/shipping to keep math clear (shipping applies though)
        int subtotal = 10_000; // ₹100
        // tax = 1200, shipping = 4900 (below threshold), gross = 16100
        // cap = 16100 - 100 = 16000 => max 160 points
        var r = PricingCalculator.compute(subtotal, 0, 999, 999);
        assertThat(r.giftPointsUsed()).isLessThanOrEqualTo(160);
        assertThat(r.totalPaise()).isGreaterThanOrEqualTo(100);
    }

    @Test
    @DisplayName("clamped=false when the request is 0")
    void notClampedWhenZeroRequested() {
        var r = PricingCalculator.compute(100_000, 0, 0, 999);
        assertThat(r.giftPointsUsed()).isZero();
        assertThat(r.giftPointsClamped()).isFalse();
    }

    // ---------------------------------- coupon -------------------------------------

    @Test
    @DisplayName("coupon discount cannot exceed subtotal")
    void couponClampedToSubtotal() {
        var r = PricingCalculator.compute(10_000, 999_999, 0, 0);
        assertThat(r.discountPaise()).isEqualTo(10_000);
        assertThat(r.totalPaise()).isEqualTo(r.taxPaise() + r.shippingPaise());
    }
}

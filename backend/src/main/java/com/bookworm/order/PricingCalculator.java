package com.bookworm.order;

/**
 * Pure computation of order totals, per plan §3.
 *
 * <ul>
 *   <li>subtotalPaise      = Σ (item.priceAtPurchase × qty)
 *   <li>shippingPaise      = subtotal ≥ ₹499 ? 0 : ₹49
 *   <li>taxPaise           = floor(subtotal × 0.12)
 *   <li>couponDiscount     = min(coupon.value, subtotal)               // v1: always 0
 *   <li>giftPointsUsedPaise = min(userBalancePaise,
 *                                 max(0, subtotal + tax + shipping - coupon - 100))
 *   <li>totalPaise         = subtotal + tax + shipping - coupon - giftPointsUsed
 * </ul>
 */
public final class PricingCalculator {

    public static final int TAX_RATE_BPS = 1200;                 // 12% in basis points
    public static final int FREE_SHIPPING_THRESHOLD_PAISE = 49_900; // ₹499
    public static final int FLAT_SHIPPING_PAISE = 4_900;           // ₹49
    public static final int MIN_ORDER_AFTER_DISCOUNTS_PAISE = 100; // leave at least ₹1

    private PricingCalculator() {}

    public record PriceBreakdown(
            int subtotalPaise,
            int taxPaise,
            int shippingPaise,
            int discountPaise,
            int giftPointsUsed,
            int totalPaise,
            boolean giftPointsClamped
    ) {}

    public static PriceBreakdown compute(int subtotalPaise,
                                         int couponDiscountPaise,
                                         int requestedGiftPoints,
                                         int userGiftPointsBalance) {
        int subtotal   = Math.max(0, subtotalPaise);
        int shipping   = subtotal >= FREE_SHIPPING_THRESHOLD_PAISE ? 0 : FLAT_SHIPPING_PAISE;
        int tax        = (int) Math.floorDiv((long) subtotal * TAX_RATE_BPS, 10_000);
        int coupon     = Math.max(0, Math.min(couponDiscountPaise, subtotal));

        int giftPointsRequestedPaise = Math.max(0, requestedGiftPoints) * 100;
        int userBalancePaise         = Math.max(0, userGiftPointsBalance) * 100;
        int cap = Math.max(0, subtotal + tax + shipping - coupon - MIN_ORDER_AFTER_DISCOUNTS_PAISE);
        int giftPointsUsedPaise      = Math.min(Math.min(giftPointsRequestedPaise, userBalancePaise), cap);
        int giftPointsUsed           = giftPointsUsedPaise / 100;
        boolean clamped              = giftPointsRequestedPaise > giftPointsUsedPaise
                                        && requestedGiftPoints > 0;

        int total = subtotal + tax + shipping - coupon - (giftPointsUsed * 100);

        return new PriceBreakdown(subtotal, tax, shipping, coupon, giftPointsUsed, total, clamped);
    }
}

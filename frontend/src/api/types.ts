/**
 * Convenience re-exports of the most-used DTO types from the generated
 * OpenAPI schema so components can `import type { Book, Cart, Order }`
 * instead of drilling through `components['schemas'][…]`.
 */

import type { components } from './generated/schema';

export type ApiError            = components['schemas']['ApiError'];
export type ApiErrorCode        = components['schemas']['ApiErrorCode'];
export type PageMeta            = components['schemas']['PageMeta'];

export type User                  = components['schemas']['User'];
export type UpdateProfileRequest  = components['schemas']['UpdateProfileRequest'];
export type Address               = components['schemas']['Address'];
export type AddressRequest        = components['schemas']['AddressRequest'];

export type Category            = components['schemas']['Category'];
export type Brand               = components['schemas']['Brand'];
export type Author              = components['schemas']['Author'];

export type Book                = components['schemas']['Book'];
export type BookSummary         = components['schemas']['BookSummary'];
export type BookPage            = components['schemas']['BookPage'];
export type BookFormat          = components['schemas']['BookFormat'];

export type Review              = components['schemas']['Review'];
export type ReviewPage          = components['schemas']['ReviewPage'];
export type ReviewRequest       = components['schemas']['ReviewRequest'];

export type Cart                = components['schemas']['Cart'];
export type CartItem            = components['schemas']['CartItem'];
export type CartItemRequest     = components['schemas']['CartItemRequest'];
export type CartItemUpdateRequest = components['schemas']['CartItemUpdateRequest'];

export type Wishlist            = components['schemas']['Wishlist'];
export type WishlistItem        = components['schemas']['WishlistItem'];
export type WishlistItemRequest = components['schemas']['WishlistItemRequest'];

export type Order               = components['schemas']['Order'];
export type OrderItem           = components['schemas']['OrderItem'];
export type OrderPage           = components['schemas']['OrderPage'];
export type OrderStatus         = components['schemas']['OrderStatus'];
export type CheckoutRequest     = components['schemas']['CheckoutRequest'];

export type Payment             = components['schemas']['Payment'];
export type PaymentMethod       = components['schemas']['PaymentMethod'];
export type PaymentStatus       = components['schemas']['PaymentStatus'];
export type PaymentRequest      = components['schemas']['PaymentRequest'];

export type BuyAgainResult      = components['schemas']['BuyAgainResult'];
export type ShippingQuote       = components['schemas']['ShippingQuote'];

export type AuthResponse        = components['schemas']['AuthResponse'];
export type LoginRequest        = components['schemas']['LoginRequest'];
export type RegisterRequest     = components['schemas']['RegisterRequest'];
export type RefreshRequest      = components['schemas']['RefreshRequest'];

import { api } from '../api';
import type {
  BuyAgainResult,
  CheckoutRequest,
  Order,
  OrderPage,
  Payment,
  PaymentRequest,
  ShippingQuote,
} from '@/api/types';

export const ordersApi = api.injectEndpoints({
  endpoints: (b) => ({
    checkout: b.mutation<Order, CheckoutRequest>({
      query: (body) => ({ url: '/orders/checkout', method: 'POST', body }),
      invalidatesTags: ['Cart', 'Orders', 'Me'],
    }),
    payForOrder: b.mutation<Payment, { id: number; body: PaymentRequest }>({
      query: ({ id, body }) => ({ url: `/orders/${id}/pay`, method: 'POST', body }),
      invalidatesTags: ['Orders'],
    }),
    listMyOrders: b.query<OrderPage, { page?: number; size?: number }>({
      query: ({ page, size }) => ({ url: '/orders', params: { page, size } }),
      providesTags: ['Orders'],
    }),
    getOrder: b.query<Order, number>({
      query: (id) => `/orders/${id}`,
      providesTags: (_r, _e, id) => [{ type: 'Orders', id }],
    }),
    cancelOrder: b.mutation<Order, number>({
      query: (id) => ({ url: `/orders/${id}/cancel`, method: 'POST' }),
      invalidatesTags: ['Orders'],
    }),
    buyAgain: b.mutation<BuyAgainResult, number>({
      query: (id) => ({ url: `/orders/${id}/buy-again`, method: 'POST' }),
      invalidatesTags: ['Cart'],
    }),
    getShippingQuote: b.query<ShippingQuote, { pin: string; subtotalPaise: number }>({
      query: (params) => ({ url: '/shipping/quote', params }),
    }),
  }),
  overrideExisting: false,
});

export const {
  useCheckoutMutation,
  usePayForOrderMutation,
  useListMyOrdersQuery,
  useGetOrderQuery,
  useCancelOrderMutation,
  useBuyAgainMutation,
  useGetShippingQuoteQuery,
} = ordersApi;

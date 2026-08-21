import { api } from '../api';
import type { Cart, CartItemRequest, CartItemUpdateRequest } from '@/api/types';

export const cartApi = api.injectEndpoints({
  endpoints: (b) => ({
    getCart: b.query<Cart, void>({
      query: () => '/cart',
      providesTags: ['Cart'],
    }),
    addCartItem: b.mutation<Cart, CartItemRequest>({
      query: (body) => ({ url: '/cart/items', method: 'POST', body }),
      invalidatesTags: ['Cart'],
    }),
    updateCartItem: b.mutation<Cart, { id: number; body: CartItemUpdateRequest }>({
      query: ({ id, body }) => ({ url: `/cart/items/${id}`, method: 'PUT', body }),
      invalidatesTags: ['Cart'],
    }),
    removeCartItem: b.mutation<Cart, number>({
      query: (id) => ({ url: `/cart/items/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Cart'],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetCartQuery,
  useAddCartItemMutation,
  useUpdateCartItemMutation,
  useRemoveCartItemMutation,
} = cartApi;

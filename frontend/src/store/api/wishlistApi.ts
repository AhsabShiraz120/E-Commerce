import { api } from '../api';
import type { Wishlist, WishlistItemRequest } from '@/api/types';

export const wishlistApi = api.injectEndpoints({
  endpoints: (b) => ({
    getWishlist: b.query<Wishlist, void>({
      query: () => '/wishlist',
      providesTags: ['Wishlist'],
    }),
    addWishlistItem: b.mutation<Wishlist, WishlistItemRequest>({
      query: (body) => ({ url: '/wishlist/items', method: 'POST', body }),
      invalidatesTags: ['Wishlist'],
    }),
    removeWishlistItem: b.mutation<Wishlist, number>({
      query: (id) => ({ url: `/wishlist/items/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Wishlist'],
    }),
  }),
  overrideExisting: false,
});

export const {
  useGetWishlistQuery,
  useAddWishlistItemMutation,
  useRemoveWishlistItemMutation,
} = wishlistApi;

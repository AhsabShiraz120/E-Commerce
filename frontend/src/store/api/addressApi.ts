import { api } from '../api';
import type { Address, AddressRequest } from '@/api/types';

export const addressApi = api.injectEndpoints({
  endpoints: (b) => ({
    listMyAddresses: b.query<Address[], void>({
      query: () => '/me/addresses',
      providesTags: ['Me'],
    }),
    addMyAddress: b.mutation<Address, AddressRequest>({
      query: (body) => ({ url: '/me/addresses', method: 'POST', body }),
      invalidatesTags: ['Me'],
    }),
    updateMyAddress: b.mutation<Address, { id: number; body: AddressRequest }>({
      query: ({ id, body }) => ({ url: `/me/addresses/${id}`, method: 'PUT', body }),
      invalidatesTags: ['Me'],
    }),
    deleteMyAddress: b.mutation<void, number>({
      query: (id) => ({ url: `/me/addresses/${id}`, method: 'DELETE' }),
      invalidatesTags: ['Me'],
    }),
  }),
  overrideExisting: false,
});

export const {
  useListMyAddressesQuery,
  useAddMyAddressMutation,
  useUpdateMyAddressMutation,
  useDeleteMyAddressMutation,
} = addressApi;

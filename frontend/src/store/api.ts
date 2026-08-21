import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import type { RootState } from './store';

/**
 * RTK Query root API. Individual feature files inject their endpoints via
 * `api.injectEndpoints()`. Base URL is `/api` (Vite dev-server proxies to
 * :8080). The JWT access token is pulled from state at request time.
 */
export const api = createApi({
  reducerPath: 'api',
  baseQuery: fetchBaseQuery({
    baseUrl: '/api',
    prepareHeaders: (headers, { getState }) => {
      const token = (getState() as RootState).auth.accessToken;
      if (token) headers.set('Authorization', `Bearer ${token}`);
      return headers;
    },
  }),
  tagTypes: ['Cart', 'Wishlist', 'Orders', 'Reviews', 'Me'],
  endpoints: () => ({}),
});

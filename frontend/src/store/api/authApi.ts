import { api } from '../api';
import type { AuthResponse, LoginRequest, RefreshRequest, RegisterRequest, User, UpdateProfileRequest } from '@/api/types';

export const authApi = api.injectEndpoints({
  endpoints: (b) => ({
    login: b.mutation<AuthResponse, LoginRequest>({
      query: (body) => ({ url: '/auth/login', method: 'POST', body }),
    }),
    register: b.mutation<AuthResponse, RegisterRequest>({
      query: (body) => ({ url: '/auth/register', method: 'POST', body }),
    }),
    refresh: b.mutation<AuthResponse, RefreshRequest>({
      query: (body) => ({ url: '/auth/refresh', method: 'POST', body }),
    }),
    getMe: b.query<User, void>({
      query: () => '/me',
      providesTags: ['Me'],
    }),
    updateMe: b.mutation<User, UpdateProfileRequest>({
      query: (body) => ({ url: '/me', method: 'PUT', body }),
      invalidatesTags: ['Me'],
    }),
  }),
  overrideExisting: false,
});

export const {
  useLoginMutation,
  useRegisterMutation,
  useRefreshMutation,
  useGetMeQuery,
  useUpdateMeMutation,
} = authApi;

import { api } from '../api';
import type { Review, ReviewPage, ReviewRequest } from '@/api/types';

export const reviewsApi = api.injectEndpoints({
  endpoints: (b) => ({
    listReviews: b.query<ReviewPage, { bookId: number; page?: number; size?: number }>({
      query: ({ bookId, page, size }) => ({
        url: `/books/${bookId}/reviews`,
        params: { page, size },
      }),
      providesTags: (_r, _e, { bookId }) => [{ type: 'Reviews', id: bookId }],
    }),
    createReview: b.mutation<Review, { bookId: number; body: ReviewRequest }>({
      query: ({ bookId, body }) => ({ url: `/books/${bookId}/reviews`, method: 'POST', body }),
      invalidatesTags: (_r, _e, { bookId }) => [{ type: 'Reviews', id: bookId }],
    }),
  }),
  overrideExisting: false,
});

export const { useListReviewsQuery, useCreateReviewMutation } = reviewsApi;

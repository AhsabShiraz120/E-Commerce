import { api } from '../api';
import type { Book, BookPage, BookSummary, Brand, Category } from '@/api/types';

export interface ListBooksParams {
  q?: string;
  category?: string[];
  brand?: string[];
  format?: string;
  language?: string;
  priceMinPaise?: number;
  priceMaxPaise?: number;
  minRating?: number;
  sort?: string;
  page?: number;
  size?: number;
}

export const catalogApi = api.injectEndpoints({
  endpoints: (b) => ({
    listCategories: b.query<Category[], void>({
      query: () => '/categories',
    }),
    listBrands: b.query<Brand[], void>({
      query: () => '/brands',
    }),
    listBooks: b.query<BookPage, ListBooksParams>({
      query: (params) => ({ url: '/books', params: toQueryParams(params) }),
    }),
    getBook: b.query<Book, number>({
      query: (id) => `/books/${id}`,
    }),
    getRelatedBooks: b.query<BookSummary[], number>({
      query: (id) => `/books/${id}/related`,
    }),
    getRecommendedBooks: b.query<BookSummary[], number | void>({
      query: (limit) => ({ url: '/books/recommended', params: { limit: limit ?? 10 } }),
    }),
  }),
  overrideExisting: false,
});

// Strip undefined values so RTK Query doesn't send them as "undefined".
function toQueryParams(input: ListBooksParams): Record<string, string | number | string[]> {
  const out: Record<string, string | number | string[]> = {};
  for (const [k, v] of Object.entries(input)) {
    if (v === undefined || v === null) continue;
    if (Array.isArray(v) && v.length === 0) continue;
    if (typeof v === 'string' && v.length === 0) continue;
    out[k] = v;
  }
  return out;
}

export const {
  useListCategoriesQuery,
  useListBrandsQuery,
  useListBooksQuery,
  useGetBookQuery,
  useGetRelatedBooksQuery,
  useGetRecommendedBooksQuery,
} = catalogApi;

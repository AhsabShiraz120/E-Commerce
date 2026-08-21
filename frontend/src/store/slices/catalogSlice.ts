import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import type { BookFormat } from '@/api/types';

export interface CatalogFilters {
  q: string;
  categories: string[];      // slugs
  brands: string[];          // slugs
  format?: BookFormat;
  language?: string;
  priceMinPaise?: number;
  priceMaxPaise?: number;
  minRating?: number;
  sort: 'relevance' | 'price_asc' | 'price_desc' | 'newest' | 'rating' | 'popular';
}

const initialState: CatalogFilters = {
  q: '',
  categories: [],
  brands: [],
  sort: 'relevance',
};

const catalogSlice = createSlice({
  name: 'catalog',
  initialState,
  reducers: {
    setQuery(state, action: PayloadAction<string>) { state.q = action.payload; },
    toggleCategory(state, action: PayloadAction<string>) {
      const slug = action.payload;
      state.categories = state.categories.includes(slug)
        ? state.categories.filter(s => s !== slug)
        : [...state.categories, slug];
    },
    setCategories(state, action: PayloadAction<string[]>) { state.categories = action.payload; },
    setBrands(state, action: PayloadAction<string[]>) { state.brands = action.payload; },
    setFormat(state, action: PayloadAction<BookFormat | undefined>) { state.format = action.payload; },
    setLanguage(state, action: PayloadAction<string | undefined>) { state.language = action.payload; },
    setPriceRange(state, action: PayloadAction<{ min?: number; max?: number }>) {
      state.priceMinPaise = action.payload.min;
      state.priceMaxPaise = action.payload.max;
    },
    setMinRating(state, action: PayloadAction<number | undefined>) { state.minRating = action.payload; },
    setSort(state, action: PayloadAction<CatalogFilters['sort']>) { state.sort = action.payload; },
    resetFilters() { return initialState; },
  },
});

export const {
  setQuery, toggleCategory, setCategories, setBrands, setFormat, setLanguage,
  setPriceRange, setMinRating, setSort, resetFilters,
} = catalogSlice.actions;
export default catalogSlice.reducer;

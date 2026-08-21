import { useSearchParams } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { CategorySidebar } from '@/components/Layout/CategorySidebar';
import { BookCard } from '@/components/BookCard';
import { useListBooksQuery, type ListBooksParams } from '@/store/api/catalogApi';
import { useAppDispatch, useAppSelector } from '@/store/hooks';
import { setCategories, setFormat, setPriceRange, setMinRating, setSort, resetFilters } from '@/store/slices/catalogSlice';
import type { BookFormat } from '@/api/types';

/**
 * Full catalog page with filter bar + book grid. Filter state lives in
 * catalogSlice; URL query string mirrors it so links are shareable.
 */
export function CatalogPage() {
  const dispatch = useAppDispatch();
  const filters = useAppSelector((s) => s.catalog);
  const [searchParams, setSearchParams] = useSearchParams();
  const [page, setPage] = useState(0);

  // Initial hydration: URL → store
  useEffect(() => {
    const cat = searchParams.getAll('category');
    if (cat.length > 0) dispatch(setCategories(cat));
    const sort = searchParams.get('sort') as ListBooksParams['sort'];
    if (sort) dispatch(setSort(sort as never));
  }, [dispatch, searchParams]);

  // Store → URL
  useEffect(() => {
    const p = new URLSearchParams();
    filters.categories.forEach((c) => p.append('category', c));
    if (filters.sort !== 'relevance') p.set('sort', filters.sort);
    if (filters.format) p.set('format', filters.format);
    setSearchParams(p, { replace: true });
  }, [filters, setSearchParams]);

  const params: ListBooksParams = {
    q:             filters.q || undefined,
    category:      filters.categories.length ? filters.categories : undefined,
    brand:         filters.brands.length ? filters.brands : undefined,
    format:        filters.format,
    language:      filters.language,
    priceMinPaise: filters.priceMinPaise,
    priceMaxPaise: filters.priceMaxPaise,
    minRating:     filters.minRating,
    sort:          filters.sort,
    page,
    size:          24,
  };
  const { data, isLoading, isFetching } = useListBooksQuery(params);

  return (
    <div className="flex flex-col lg:flex-row gap-6">
      <CategorySidebar className="hidden lg:block" />
      <div className="flex-1 space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-semibold">Catalog</h1>
          {(filters.categories.length > 0 || filters.format || filters.sort !== 'relevance') && (
            <button
              onClick={() => { dispatch(resetFilters()); setPage(0); }}
              className="text-sm text-muted hover:text-body underline"
            >
              Clear filters
            </button>
          )}
        </div>

        <FilterBar />

        {isLoading || isFetching ? (
          <BookGrid loading />
        ) : !data || data.content.length === 0 ? (
          <p className="text-muted py-10 text-center">No books match these filters.</p>
        ) : (
          <>
            <BookGrid books={data.content} />
            <Pagination
              page={data.meta.page}
              totalPages={data.meta.totalPages}
              onPage={(n) => setPage(n)}
            />
          </>
        )}
      </div>
    </div>
  );
}

function FilterBar() {
  const dispatch = useAppDispatch();
  const filters = useAppSelector((s) => s.catalog);

  return (
    <div className="card flex flex-wrap gap-3 items-center">
      <Select
        label="Format"
        value={filters.format ?? ''}
        onChange={(v) => dispatch(setFormat((v as BookFormat) || undefined))}
        options={[
          { value: '',           label: 'Format' },
          { value: 'Paperback',  label: 'Paperback' },
          { value: 'HardCover',  label: 'HardCover' },
          { value: 'eBook',      label: 'eBook' },
        ]}
      />
      <Select
        label="Price"
        value={priceOptionKey(filters.priceMinPaise, filters.priceMaxPaise)}
        onChange={(v) => {
          const range = PRICE_OPTIONS[v as keyof typeof PRICE_OPTIONS];
          dispatch(setPriceRange({ min: range?.[0], max: range?.[1] }));
        }}
        options={[
          { value: '',       label: 'Price' },
          { value: 'u200',   label: 'Under ₹200' },
          { value: '200-500',label: '₹200–₹500' },
          { value: '500-1000', label: '₹500–₹1000' },
          { value: 'over1000', label: 'Above ₹1000' },
        ]}
      />
      <Select
        label="Rating"
        value={filters.minRating != null ? String(filters.minRating) : ''}
        onChange={(v) => dispatch(setMinRating(v ? Number(v) : undefined))}
        options={[
          { value: '',  label: 'Rating' },
          { value: '4', label: '4★ & up' },
          { value: '3', label: '3★ & up' },
        ]}
      />
      <div className="flex-1" />
      <Select
        label="Sort by"
        value={filters.sort}
        onChange={(v) => dispatch(setSort(v as never))}
        options={[
          { value: 'relevance',  label: 'Sort: Relevance' },
          { value: 'price_asc',  label: 'Price: Low to High' },
          { value: 'price_desc', label: 'Price: High to Low' },
          { value: 'newest',     label: 'Newest' },
          { value: 'rating',     label: 'Rating' },
          { value: 'popular',    label: 'Popular' },
        ]}
      />
    </div>
  );
}

function Select({ label, value, onChange, options }: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  options: { value: string; label: string }[];
}) {
  return (
    <select
      aria-label={label}
      className="bg-surface2 border border-border rounded px-3 py-1.5 text-sm hover:border-accent transition-colors"
      value={value}
      onChange={(e) => onChange(e.target.value)}
    >
      {options.map((o) => (
        <option key={o.value} value={o.value}>{o.label}</option>
      ))}
    </select>
  );
}

const PRICE_OPTIONS = {
  u200:      [undefined, 20000] as [number | undefined, number | undefined],
  '200-500': [20000, 50000] as [number | undefined, number | undefined],
  '500-1000': [50000, 100000] as [number | undefined, number | undefined],
  over1000:  [100000, undefined] as [number | undefined, number | undefined],
};

function priceOptionKey(min: number | undefined, max: number | undefined): string {
  for (const [k, [mn, mx]] of Object.entries(PRICE_OPTIONS)) {
    if (mn === min && mx === max) return k;
  }
  return '';
}

function BookGrid({ books, loading }: { books?: { id: number }[]; loading?: boolean }) {
  if (loading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        {Array.from({ length: 10 }).map((_, i) => (
          <div key={i} className="animate-pulse">
            <div className="aspect-[3/4] rounded bg-surface2" />
            <div className="mt-2 h-3 w-3/4 bg-surface2 rounded" />
            <div className="mt-1 h-3 w-1/2 bg-surface2 rounded" />
          </div>
        ))}
      </div>
    );
  }
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
      {books?.map((b: unknown) => (
        <BookCard key={(b as { id: number }).id} book={b as never} className="w-auto" />
      ))}
    </div>
  );
}

function Pagination({ page, totalPages, onPage }: {
  page: number;
  totalPages: number;
  onPage: (n: number) => void;
}) {
  if (totalPages <= 1) return null;
  return (
    <div className="flex items-center justify-center gap-3 py-2">
      <button
        className="btn-ghost !py-1 !px-3 text-sm"
        onClick={() => onPage(Math.max(0, page - 1))}
        disabled={page === 0}
      >
        Previous
      </button>
      <span className="text-sm text-muted">Page {page + 1} of {totalPages}</span>
      <button
        className="btn-ghost !py-1 !px-3 text-sm"
        onClick={() => onPage(Math.min(totalPages - 1, page + 1))}
        disabled={page + 1 >= totalPages}
      >
        Next
      </button>
    </div>
  );
}

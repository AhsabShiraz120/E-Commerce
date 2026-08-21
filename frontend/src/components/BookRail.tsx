import type { BookSummary } from '@/api/types';
import { BookCard } from './BookCard';

interface Props {
  title: string;
  books: BookSummary[] | undefined;
  loading?: boolean;
  emptyMessage?: string;
}

/**
 * Horizontally-scrolling rail of book cards. Used for the three Home page
 * sections (Recommended / Bestsellers / New Launches) and the Related Reads
 * sidebar on the product detail page.
 */
export function BookRail({ title, books, loading, emptyMessage }: Props) {
  return (
    <section>
      <h2 className="text-lg font-semibold mb-3">{title}</h2>
      {loading ? (
        <div className="flex gap-4 overflow-x-auto pb-2">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="w-40 sm:w-44 shrink-0 animate-pulse">
              <div className="aspect-[3/4] rounded bg-surface2" />
              <div className="mt-2 h-3 w-3/4 bg-surface2 rounded" />
              <div className="mt-1 h-3 w-1/2 bg-surface2 rounded" />
            </div>
          ))}
        </div>
      ) : !books || books.length === 0 ? (
        <p className="text-muted text-sm py-4">{emptyMessage ?? 'Nothing here yet.'}</p>
      ) : (
        <div className="flex gap-4 overflow-x-auto pb-2 -mx-4 px-4 snap-x">
          {books.map((b) => (
            <div key={b.id} className="snap-start">
              <BookCard book={b} />
            </div>
          ))}
        </div>
      )}
    </section>
  );
}

import { Link } from 'react-router-dom';
import { Star } from 'lucide-react';
import type { BookSummary } from '@/api/types';
import { formatPaise } from '@/lib/format';

interface Props {
  book: BookSummary;
  className?: string;
}

/**
 * Compact book card used in home rails, related-reads sidebar, and wishlist grid.
 * Cover falls back to a coloured gradient with the title if the image URL 404s.
 */
export function BookCard({ book, className = '' }: Props) {
  return (
    <Link
      to={`/books/${book.id}`}
      className={`group block ${className}`}
    >
      <CoverArt title={book.title} coverUrl={book.coverUrl} />
      <div className="mt-2 space-y-0.5">
        <p className="text-sm font-medium line-clamp-2 group-hover:text-accent transition-colors">
          {book.title}
        </p>
        {book.authorName && (
          <p className="text-xs text-muted line-clamp-1">{book.authorName}</p>
        )}
        <div className="flex items-center justify-between pt-1">
          <span className="text-sm font-semibold tabular-nums">{formatPaise(book.pricePaise)}</span>
          {typeof book.rating === 'number' && book.rating > 0 && (
            <span className="inline-flex items-center gap-0.5 text-xs text-muted">
              <Star className="w-3 h-3 fill-star text-star" />
              {book.rating.toFixed(1)}
            </span>
          )}
        </div>
      </div>
    </Link>
  );
}

function CoverArt({ title, coverUrl }: { title: string; coverUrl?: string }) {
  return (
    <div className="aspect-[3/4] rounded overflow-hidden bg-surface2 relative">
      {coverUrl ? (
        <>
          <img
            src={coverUrl}
            alt=""
            loading="lazy"
            onError={(e) => { (e.currentTarget as HTMLImageElement).style.display = 'none'; }}
            className="absolute inset-0 w-full h-full object-cover"
          />
          <FallbackGradient title={title} />
        </>
      ) : (
        <FallbackGradient title={title} />
      )}
    </div>
  );
}

function FallbackGradient({ title }: { title: string }) {
  const hue = (hash(title) % 360);
  return (
    <div
      className="absolute inset-0 flex items-end p-3 text-white text-sm font-medium leading-tight"
      style={{
        background: `linear-gradient(135deg,
          hsl(${hue}, 55%, 45%) 0%,
          hsl(${(hue + 40) % 360}, 55%, 25%) 100%)`,
      }}
    >
      <span className="line-clamp-4 drop-shadow">{title}</span>
    </div>
  );
}

function hash(s: string): number {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  return Math.abs(h);
}

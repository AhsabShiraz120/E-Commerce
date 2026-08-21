import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Heart, ShoppingCart, Star } from 'lucide-react';
import {
  useGetBookQuery,
  useGetRelatedBooksQuery,
} from '@/store/api/catalogApi';
import { useAddCartItemMutation } from '@/store/api/cartApi';
import { useAddWishlistItemMutation } from '@/store/api/wishlistApi';
import { useAppSelector } from '@/store/hooks';
import { formatPaise } from '@/lib/format';
import { BookRail } from '@/components/BookRail';
import { ReviewsSection } from '@/components/ReviewsSection';

export function BookDetailPage() {
  const params = useParams<{ id: string }>();
  const bookId = Number(params.id);
  const { data: book, isLoading } = useGetBookQuery(bookId, { skip: !bookId });
  const { data: related } = useGetRelatedBooksQuery(bookId, { skip: !bookId });

  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));
  const [addToCart, { isLoading: adding }] = useAddCartItemMutation();
  const [addToWishlist, { isLoading: wishing }] = useAddWishlistItemMutation();
  const [qty, setQty] = useState(1);
  const [toast, setToast] = useState<string | null>(null);

  if (isLoading) return <div className="text-muted">Loading…</div>;
  if (!book) return <div className="text-muted">Book not found.</div>;

  async function onAddToCart() {
    if (!book) return;
    try {
      await addToCart({ bookId: book.id, quantity: qty }).unwrap();
      setToast(`Added ${qty} × “${book.title}” to cart`);
    } catch (e) {
      setToast(errMsg(e) ?? 'Could not add to cart');
    }
    setTimeout(() => setToast(null), 2500);
  }

  async function onAddToWishlist() {
    if (!book) return;
    try {
      await addToWishlist({ bookId: book.id }).unwrap();
      setToast(`“${book.title}” added to wishlist`);
    } catch (e) {
      setToast(errMsg(e) ?? 'Could not add to wishlist');
    }
    setTimeout(() => setToast(null), 2500);
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[1fr_18rem] gap-8">
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-[15rem_1fr] gap-6">
          {/* Cover */}
          <div className="aspect-[3/4] rounded overflow-hidden bg-surface2 relative">
            <FallbackGradient title={book.title} />
          </div>

          {/* Info */}
          <div className="space-y-3">
            <h1 className="text-2xl font-semibold">{book.title}</h1>
            {book.author && <p className="text-muted">by {book.author.name}</p>}
            <div className="flex items-center gap-3 text-sm">
              {typeof book.rating === 'number' && book.rating > 0 && (
                <span className="inline-flex items-center gap-1">
                  <Star className="w-4 h-4 fill-star text-star" />
                  {book.rating.toFixed(1)}
                </span>
              )}
              <span className="badge bg-surface2 border border-border">{book.format}</span>
              <span className="text-muted">·</span>
              <span className="text-muted">{book.language}</span>
            </div>
            <div className="text-3xl font-semibold tabular-nums">{formatPaise(book.pricePaise)}</div>
            <p className="text-sm text-muted">
              Tentative delivery in {book.tentativeDeliveryDays ?? 5} days
              {book.stock != null && book.stock > 0
                ? ` · ${book.stock} in stock`
                : ' · Out of stock'}
            </p>

            {/* Quantity + actions */}
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <div className="inline-flex items-center bg-surface2 rounded border border-border">
                <button
                  className="px-3 py-2 text-sm hover:bg-border rounded-l"
                  onClick={() => setQty((q) => Math.max(1, q - 1))}
                  aria-label="Decrease quantity"
                >−</button>
                <span className="px-4 tabular-nums">{qty}</span>
                <button
                  className="px-3 py-2 text-sm hover:bg-border rounded-r"
                  onClick={() => setQty((q) => Math.min(99, q + 1))}
                  aria-label="Increase quantity"
                >+</button>
              </div>
              <button
                onClick={onAddToCart}
                disabled={!isSignedIn || adding || (book.stock ?? 0) < 1}
                className="btn-primary"
              >
                <ShoppingCart className="w-4 h-4" />
                {adding ? 'Adding…' : 'Add to Cart'}
              </button>
              <button
                onClick={onAddToWishlist}
                disabled={!isSignedIn || wishing}
                className="btn-ghost"
              >
                <Heart className="w-4 h-4" />
                Add to Wishlist
              </button>
            </div>
            {!isSignedIn && (
              <p className="text-xs text-muted">Sign in to add to cart or wishlist.</p>
            )}
          </div>
        </div>

        {book.description && (
          <section>
            <h2 className="text-lg font-semibold mb-2">About the Book</h2>
            <p className="text-body/90 leading-relaxed">{book.description}</p>
          </section>
        )}

        {book.author?.bio && (
          <section>
            <h2 className="text-lg font-semibold mb-2">About the Writer</h2>
            <p className="text-body/90 leading-relaxed">{book.author.bio}</p>
          </section>
        )}

        <ReviewsSection bookId={book.id} />
      </div>

      {/* Related Reads sidebar */}
      <aside>
        <BookRail
          title="Related Reads"
          books={related}
          emptyMessage="Nothing similar in stock right now."
        />
      </aside>

      {toast && (
        <div className="fixed bottom-4 right-4 bg-surface2 border border-border rounded px-4 py-2 shadow-lg text-sm">
          {toast}
        </div>
      )}
    </div>
  );
}

function FallbackGradient({ title }: { title: string }) {
  const hue = hash(title) % 360;
  return (
    <div
      className="absolute inset-0 flex items-end p-4 text-white font-medium"
      style={{
        background: `linear-gradient(135deg, hsl(${hue}, 55%, 45%) 0%, hsl(${(hue + 40) % 360}, 55%, 25%) 100%)`,
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

function errMsg(e: unknown): string | undefined {
  if (e && typeof e === 'object' && 'data' in e) {
    const d = (e as { data?: { message?: string } }).data;
    return d?.message;
  }
  return undefined;
}

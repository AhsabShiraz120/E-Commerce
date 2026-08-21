import { useState } from 'react';
import { Star } from 'lucide-react';
import clsx from 'clsx';
import { useCreateReviewMutation, useListReviewsQuery } from '@/store/api/reviewsApi';
import { useAppSelector } from '@/store/hooks';
import { formatDate } from '@/lib/format';

/**
 * Reviews list + "write a review" form used on the product detail page.
 * Signed-in users see the form; anonymous users only see the list.
 */
export function ReviewsSection({ bookId }: { bookId: number }) {
  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));
  const { data, isLoading } = useListReviewsQuery({ bookId, page: 0, size: 20 });

  return (
    <section>
      <h2 className="text-lg font-semibold mb-3">Reviews</h2>

      {isSignedIn && <NewReviewForm bookId={bookId} />}

      {isLoading ? (
        <p className="text-muted text-sm">Loading reviews…</p>
      ) : !data || data.content.length === 0 ? (
        <p className="text-muted text-sm py-4">No reviews yet. Be the first to write one.</p>
      ) : (
        <ul className="space-y-4">
          {data.content.map((r) => (
            <li key={r.id} className="card">
              <div className="flex items-center justify-between mb-1">
                <span className="font-medium">{r.userName ?? 'Anonymous'}</span>
                <span className="text-xs text-muted">{formatDate(r.createdAt)}</span>
              </div>
              <div className="flex items-center gap-1 mb-2">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Star
                    key={i}
                    className={clsx('w-4 h-4', i < r.rating ? 'fill-star text-star' : 'text-muted/40')}
                  />
                ))}
              </div>
              {r.text && <p className="text-sm text-body/90 whitespace-pre-line">{r.text}</p>}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

function NewReviewForm({ bookId }: { bookId: number }) {
  const [rating, setRating] = useState(5);
  const [text, setText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [err, setErr] = useState<string | null>(null);
  const [ok, setOk] = useState(false);
  const [createReview] = useCreateReviewMutation();

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErr(null); setOk(false); setSubmitting(true);
    try {
      await createReview({ bookId, body: { rating, text: text || null as unknown as string | undefined } }).unwrap();
      setOk(true);
      setText('');
    } catch (ex: unknown) {
      const d = (ex as { data?: { message?: string } }).data;
      setErr(d?.message ?? 'Could not submit review');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="card mb-4 space-y-3">
      <div className="flex items-center gap-2">
        <span className="text-sm text-muted">Your rating:</span>
        {Array.from({ length: 5 }).map((_, i) => (
          <button
            key={i}
            type="button"
            onClick={() => setRating(i + 1)}
            aria-label={`${i + 1} stars`}
          >
            <Star className={clsx('w-5 h-5', i < rating ? 'fill-star text-star' : 'text-muted/40')} />
          </button>
        ))}
      </div>
      <textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="What did you think? (optional)"
        rows={3}
        className="w-full px-3 py-2 rounded bg-surface2 border border-border focus:border-accent outline-none text-sm"
      />
      {err && <p className="text-sm text-danger">{err}</p>}
      {ok && <p className="text-sm text-success">Thanks for the review!</p>}
      <button type="submit" className="btn-primary" disabled={submitting}>
        {submitting ? 'Submitting…' : 'Submit Review'}
      </button>
    </form>
  );
}

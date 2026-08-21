import { Link, useNavigate } from 'react-router-dom';
import clsx from 'clsx';
import {
  useBuyAgainMutation,
  useCancelOrderMutation,
  useListMyOrdersQuery,
} from '@/store/api/ordersApi';
import { useAppSelector } from '@/store/hooks';
import { formatDate, formatPaise } from '@/lib/format';
import type { OrderStatus } from '@/api/types';

export function OrdersPage() {
  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));
  const { data, isLoading } = useListMyOrdersQuery({ size: 50 }, { skip: !isSignedIn });
  const [cancel, { isLoading: cancelling }] = useCancelOrderMutation();
  const [buyAgain, { isLoading: buying }] = useBuyAgainMutation();
  const navigate = useNavigate();

  if (!isSignedIn) {
    return (
      <div className="card text-center py-10">
        <p className="text-muted mb-4">Sign in to see your orders.</p>
        <Link to="/login" className="btn-primary">Sign in</Link>
      </div>
    );
  }

  if (isLoading) return <p className="text-muted">Loading orders…</p>;

  const orders = data?.content ?? [];
  if (orders.length === 0) {
    return (
      <div className="card text-center py-12">
        <h1 className="text-2xl font-semibold mb-2">My Orders</h1>
        <p className="text-muted mb-3">You have no orders yet.</p>
        <Link to="/catalog" className="text-accent hover:underline">Start shopping →</Link>
      </div>
    );
  }

  async function onBuyAgain(id: number) {
    try {
      const res = await buyAgain(id).unwrap();
      navigate('/cart');
      // If the endpoint skipped items, we don't surface it in a toast — cart
      // page shows what actually landed. Keeping this simple.
      void res;
    } catch { /* silent */ }
  }

  return (
    <section className="space-y-4">
      <h1 className="text-2xl font-semibold">My Orders</h1>
      <ul className="space-y-3">
        {orders.map((o) => {
          const cancellable = canCancel(o.status, o.cancellableUntil);
          const total = o.items.reduce((n, x) => n + x.quantity, 0);
          const preview = o.items.slice(0, 3);
          return (
            <li key={o.id} className="card">
              <div className="flex flex-wrap items-start gap-4">
                <div className="flex -space-x-2">
                  {preview.map((line) => (
                    <div key={line.id} className="w-12 aspect-[3/4] rounded overflow-hidden bg-surface2 relative shrink-0 border-2 border-surface">
                      <div
                        className="absolute inset-0 flex items-end p-1 text-white text-[10px] font-medium"
                        style={{ background: gradient(line.book.title) }}
                      >
                        <span className="line-clamp-3 drop-shadow">{line.book.title}</span>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-medium">Order #{o.id}</span>
                    <StatusBadge status={o.status} />
                  </div>
                  <p className="text-sm text-muted mt-0.5">
                    Ordered on {formatDate(o.createdAt)} · {total} {total === 1 ? 'item' : 'items'}
                  </p>
                  <p className="text-xs text-muted mt-1">
                    {cancellable
                      ? `Cancellable until ${formatDate(o.cancellableUntil)}`
                      : 'Cancel window closed'}
                  </p>
                </div>

                <div className="text-right">
                  <p className="font-semibold tabular-nums">{formatPaise(o.totalPaise)}</p>
                </div>
              </div>

              <div className="flex flex-wrap gap-2 mt-3">
                <button
                  className="btn-ghost !py-1.5 text-sm"
                  onClick={() => onBuyAgain(o.id)}
                  disabled={buying}
                >
                  Buy Again
                </button>
                {cancellable && (
                  <button
                    className="btn-ghost !py-1.5 text-sm text-danger"
                    onClick={() => cancel(o.id)}
                    disabled={cancelling}
                  >
                    Cancel Order
                  </button>
                )}
              </div>
            </li>
          );
        })}
      </ul>
    </section>
  );
}

function StatusBadge({ status }: { status: OrderStatus | undefined }) {
  const classes: Record<string, string> = {
    PENDING:   'bg-warn/20 text-warn',
    PAID:      'bg-accent/20 text-accent',
    SHIPPED:   'bg-accent/20 text-accent',
    DELIVERED: 'bg-success/20 text-success',
    CANCELLED: 'bg-danger/20 text-danger',
    RETURNED:  'bg-muted/20 text-muted',
  };
  return (
    <span className={clsx('badge', classes[status ?? 'PENDING'])}>
      {status ?? 'PENDING'}
    </span>
  );
}

function canCancel(status: OrderStatus | undefined, cancellableUntil: string | undefined): boolean {
  if (!cancellableUntil) return false;
  if (status !== 'PENDING' && status !== 'PAID') return false;
  return new Date(cancellableUntil).getTime() > Date.now();
}

function gradient(s: string): string {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  const hue = Math.abs(h) % 360;
  return `linear-gradient(135deg, hsl(${hue}, 55%, 45%) 0%, hsl(${(hue + 40) % 360}, 55%, 25%) 100%)`;
}

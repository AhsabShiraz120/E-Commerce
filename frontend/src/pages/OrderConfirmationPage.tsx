import { Link, useParams } from 'react-router-dom';
import { CheckCircle2 } from 'lucide-react';
import { useGetOrderQuery } from '@/store/api/ordersApi';
import { useAppSelector } from '@/store/hooks';
import { formatDate, formatPaise } from '@/lib/format';

export function OrderConfirmationPage() {
  const { id } = useParams<{ id: string }>();
  const orderId = Number(id);
  const { data: order, isLoading } = useGetOrderQuery(orderId, { skip: !orderId });
  const email = useAppSelector((s) => s.auth.user?.email);

  if (isLoading || !order) return <p className="text-muted">Loading order…</p>;

  const eta = expectedDelivery(order.createdAt, order.items[0]?.book);

  return (
    <div className="max-w-2xl mx-auto text-center space-y-6 py-6">
      <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-success/20 text-success">
        <CheckCircle2 className="w-10 h-10" />
      </div>

      <div>
        <h1 className="text-3xl font-semibold">Order Placed!</h1>
        <p className="text-muted mt-2">
          Your order #{order.id} is confirmed.
          {email && <> We'll email you a receipt at <span className="text-body">{email}</span>.</>}
        </p>
        {eta && <p className="text-muted">Expected delivery: <span className="text-body">{eta}</span></p>}
      </div>

      {/* Books */}
      <ul className="space-y-2 text-left">
        {order.items.map((line) => (
          <li key={line.id} className="card flex items-center gap-3">
            <div className="w-12 aspect-[3/4] rounded overflow-hidden bg-surface2 relative shrink-0">
              <div
                className="absolute inset-0 flex items-end p-1 text-white text-[10px] font-medium"
                style={{ background: gradient(line.book.title) }}
              >
                <span className="line-clamp-3 drop-shadow">{line.book.title}</span>
              </div>
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-medium text-sm line-clamp-1">{line.book.title}</p>
              <p className="text-xs text-muted">Qty {line.quantity}</p>
            </div>
            <span className="tabular-nums text-sm font-semibold">
              {formatPaise(line.priceAtPurchasePaise * line.quantity)}
            </span>
          </li>
        ))}
      </ul>

      <div className="text-lg">
        <span className="text-muted">Order total: </span>
        <span className="font-semibold tabular-nums">{formatPaise(order.totalPaise)}</span>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 justify-center pt-2">
        <Link to="/" className="btn-primary">Continue your Shopping</Link>
        <Link to="/orders" className="btn-ghost">View Order</Link>
      </div>
    </div>
  );
}

function expectedDelivery(createdAt: string | undefined, book?: { pricePaise?: number }): string | null {
  if (!createdAt) return null;
  const start = new Date(createdAt);
  // Rough estimate: seven days from order placement — the exact per-book
  // tentativeDeliveryDays isn't projected onto OrderItem in the summary DTO.
  void book;
  const eta = new Date(start.getTime() + 7 * 24 * 60 * 60 * 1000);
  return formatDate(eta.toISOString());
}

function gradient(s: string): string {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  const hue = Math.abs(h) % 360;
  return `linear-gradient(135deg, hsl(${hue}, 55%, 45%) 0%, hsl(${(hue + 40) % 360}, 55%, 25%) 100%)`;
}

import { Link, useNavigate } from 'react-router-dom';
import { Trash2, ArrowRight } from 'lucide-react';
import { useGetCartQuery, useRemoveCartItemMutation, useUpdateCartItemMutation } from '@/store/api/cartApi';
import { useAppSelector } from '@/store/hooks';
import { formatPaise } from '@/lib/format';

const TAX_RATE = 0.12;
const FREE_SHIPPING_THRESHOLD_PAISE = 49_900;
const FLAT_SHIPPING_PAISE = 4_900;

export function CartPage() {
  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));
  const { data: cart, isLoading } = useGetCartQuery(undefined, { skip: !isSignedIn });
  const [updateItem] = useUpdateCartItemMutation();
  const [removeItem] = useRemoveCartItemMutation();
  const navigate = useNavigate();

  if (!isSignedIn) {
    return (
      <div className="card text-center py-10">
        <p className="text-muted mb-4">Sign in to see your cart.</p>
        <Link to="/login" className="btn-primary">Sign in</Link>
      </div>
    );
  }

  if (isLoading) return <p className="text-muted">Loading your cart…</p>;

  const items = cart?.items ?? [];
  const subtotal = cart?.subtotalPaise ?? 0;
  const shipping = subtotal >= FREE_SHIPPING_THRESHOLD_PAISE ? 0 : (items.length > 0 ? FLAT_SHIPPING_PAISE : 0);
  const tax      = Math.floor(subtotal * TAX_RATE);
  const total    = subtotal + shipping + tax;

  if (items.length === 0) {
    return (
      <div className="card text-center py-12">
        <h1 className="text-2xl font-semibold mb-2">Your cart is empty.</h1>
        <Link to="/catalog" className="text-accent hover:underline inline-flex items-center gap-1">
          Continue shopping <ArrowRight className="w-4 h-4" />
        </Link>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-[1fr_20rem] gap-6">
      {/* Line items */}
      <section className="space-y-3">
        <h1 className="text-2xl font-semibold">Your Cart</h1>
        <ul className="space-y-3">
          {items.map((line) => (
            <li key={line.id} className="card flex gap-4">
              <div className="w-20 aspect-[3/4] rounded overflow-hidden bg-surface2 relative shrink-0">
                <div
                  className="absolute inset-0 flex items-end p-2 text-white text-xs font-medium"
                  style={{ background: gradient(line.book.title) }}
                >
                  <span className="line-clamp-3 drop-shadow">{line.book.title}</span>
                </div>
              </div>
              <div className="flex-1 min-w-0">
                <Link to={`/books/${line.book.id}`} className="font-medium hover:text-accent line-clamp-2">
                  {line.book.title}
                </Link>
                <p className="text-xs text-muted mt-0.5">{line.book.authorName}</p>
                <div className="mt-3 flex items-center gap-3">
                  <div className="inline-flex items-center bg-surface2 rounded border border-border text-sm">
                    <button
                      className="px-2 py-1 hover:bg-border rounded-l"
                      onClick={() => updateItem({ id: line.id, body: { quantity: Math.max(1, line.quantity - 1) } })}
                      disabled={line.quantity <= 1}
                    >−</button>
                    <span className="px-3 tabular-nums">{line.quantity}</span>
                    <button
                      className="px-2 py-1 hover:bg-border rounded-r"
                      onClick={() => updateItem({ id: line.id, body: { quantity: line.quantity + 1 } })}
                    >+</button>
                  </div>
                  <button
                    className="text-muted hover:text-danger"
                    onClick={() => removeItem(line.id)}
                    aria-label="Remove line"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
              <div className="text-right">
                <p className="font-semibold tabular-nums">{formatPaise(line.linePaise)}</p>
                <p className="text-xs text-muted mt-0.5">{formatPaise(line.book.pricePaise)} each</p>
              </div>
            </li>
          ))}
        </ul>
      </section>

      {/* Order summary */}
      <aside>
        <div className="card sticky top-20 space-y-3">
          <h2 className="text-lg font-semibold">Order Summary</h2>
          <SummaryLine label="Subtotal" value={formatPaise(subtotal)} />
          <SummaryLine label="Tax (12%)" value={formatPaise(tax)} />
          <SummaryLine
            label="Delivery"
            value={shipping === 0 ? <span className="text-success">Free</span> : formatPaise(shipping)}
          />
          <div className="border-t border-border pt-2 flex items-center justify-between font-semibold">
            <span>Grand Total</span>
            <span className="tabular-nums">{formatPaise(total)}</span>
          </div>
          <button
            className="btn-primary w-full"
            onClick={() => navigate('/checkout')}
          >
            Pay Now
          </button>
          <p className="text-xs text-muted">
            Free shipping on orders ₹499 and above.
          </p>
        </div>
      </aside>
    </div>
  );
}

function SummaryLine({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-muted">{label}</span>
      <span className="tabular-nums">{value}</span>
    </div>
  );
}

function gradient(s: string): string {
  let h = 0;
  for (let i = 0; i < s.length; i++) h = (h * 31 + s.charCodeAt(i)) | 0;
  const hue = Math.abs(h) % 360;
  return `linear-gradient(135deg, hsl(${hue}, 55%, 45%) 0%, hsl(${(hue + 40) % 360}, 55%, 25%) 100%)`;
}

import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

import { useGetCartQuery } from '@/store/api/cartApi';
import { useCheckoutMutation, useGetShippingQuoteQuery } from '@/store/api/ordersApi';
import {
  useAddMyAddressMutation,
  useListMyAddressesQuery,
} from '@/store/api/addressApi';
import { useAppSelector } from '@/store/hooks';
import { formatPaise } from '@/lib/format';
import { PaymentModal } from '@/components/PaymentModal';
import type { Address, Order } from '@/api/types';

const TAX_RATE = 0.12;

const addressSchema = z.object({
  line1:   z.string().min(1, 'Required'),
  line2:   z.string().optional(),
  city:    z.string().min(1, 'Required'),
  state:   z.string().min(1, 'Required'),
  pin:     z.string().regex(/^[0-9]{6}$/, '6 digits'),
  phone:   z.string().optional(),
});
type AddressForm = z.infer<typeof addressSchema>;

export function CheckoutPage() {
  const isSignedIn = useAppSelector((s) => Boolean(s.auth.accessToken));
  const user = useAppSelector((s) => s.auth.user);
  const navigate = useNavigate();

  const { data: cart } = useGetCartQuery(undefined, { skip: !isSignedIn });
  const { data: addresses } = useListMyAddressesQuery(undefined, { skip: !isSignedIn });

  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [useGiftPoints, setUseGiftPoints] = useState(0);
  const [pendingOrder, setPendingOrder] = useState<Order | null>(null);

  const [checkout, { isLoading: checkingOut, error: checkoutErr }] = useCheckoutMutation();

  const subtotal = cart?.subtotalPaise ?? 0;
  const pinForQuote = addresses?.find((a) => a.id === selectedAddressId)?.pin
                   ?? addresses?.find((a) => a.isDefault)?.pin;
  const { data: shippingQuote } = useGetShippingQuoteQuery(
    { pin: pinForQuote ?? '', subtotalPaise: subtotal },
    { skip: !pinForQuote },
  );

  const activeAddressId = selectedAddressId
    ?? addresses?.find((a) => a.isDefault)?.id
    ?? addresses?.[0]?.id
    ?? null;

  const tax      = Math.floor(subtotal * TAX_RATE);
  const shipping = shippingQuote?.pricePaise ?? 0;
  const giftPtsPaise = Math.max(0, Math.min(useGiftPoints, user?.giftPointsBalance ?? 0)) * 100;
  const total    = Math.max(0, subtotal + tax + shipping - giftPtsPaise);

  if (!isSignedIn) {
    return (
      <div className="card text-center py-10">
        <p className="text-muted mb-4">Sign in to check out.</p>
        <Link to="/login" className="btn-primary">Sign in</Link>
      </div>
    );
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="card text-center py-12">
        <p className="text-muted mb-2">Your cart is empty.</p>
        <Link to="/catalog" className="text-accent hover:underline">Continue shopping →</Link>
      </div>
    );
  }

  async function onCheckout() {
    if (!activeAddressId) return;
    try {
      const order = await checkout({
        addressId: activeAddressId,
        useGiftPoints,
      }).unwrap();
      setPendingOrder(order);
    } catch { /* handled via checkoutErr */ }
  }

  return (
    <>
      <div className="grid grid-cols-1 lg:grid-cols-[1fr_22rem] gap-6">
        {/* Left */}
        <section className="space-y-5">
          <h1 className="text-2xl font-semibold">Checkout</h1>

          <div className="card">
            <h2 className="text-lg font-semibold mb-3">Delivery Address</h2>
            <SavedAddresses
              addresses={addresses ?? []}
              selectedId={activeAddressId}
              onSelect={setSelectedAddressId}
            />
            <NewAddressForm />
          </div>

          <div className="card">
            <h2 className="text-lg font-semibold mb-3">Order Items</h2>
            <ul className="divide-y divide-border">
              {cart.items.map((l) => (
                <li key={l.id} className="flex items-center justify-between py-2 text-sm">
                  <span className="line-clamp-1">
                    {l.book.title} <span className="text-muted">× {l.quantity}</span>
                  </span>
                  <span className="tabular-nums">{formatPaise(l.linePaise)}</span>
                </li>
              ))}
            </ul>
          </div>
        </section>

        {/* Right */}
        <aside>
          <div className="card sticky top-20 space-y-3">
            <h2 className="text-lg font-semibold">Order Summary</h2>
            <SummaryLine label="Subtotal" value={formatPaise(subtotal)} />
            <SummaryLine label="Tax (12%)" value={formatPaise(tax)} />
            <SummaryLine
              label="Delivery"
              value={shipping === 0 ? <span className="text-success">Free</span> : formatPaise(shipping)}
            />
            {(user?.giftPointsBalance ?? 0) > 0 && (
              <div className="space-y-1">
                <div className="flex items-center justify-between text-sm">
                  <label htmlFor="gp" className="text-muted">
                    Gift Points ({user?.giftPointsBalance ?? 0} available)
                  </label>
                  <span className="tabular-nums text-success">−{formatPaise(giftPtsPaise)}</span>
                </div>
                <input
                  id="gp"
                  type="range"
                  min={0}
                  max={user?.giftPointsBalance ?? 0}
                  value={useGiftPoints}
                  onChange={(e) => setUseGiftPoints(Number(e.target.value))}
                  className="w-full"
                />
              </div>
            )}
            <div className="border-t border-border pt-2 flex items-center justify-between font-semibold">
              <span>Grand Total</span>
              <span className="tabular-nums">{formatPaise(total)}</span>
            </div>
            <button
              className="btn-primary w-full"
              onClick={onCheckout}
              disabled={!activeAddressId || checkingOut}
            >
              {checkingOut ? 'Placing order…' : 'Pay Now'}
            </button>
            {checkoutErr && (
              <p className="text-sm text-danger">
                {extractMsg(checkoutErr) ?? 'Could not place order'}
              </p>
            )}
            <p className="text-xs text-muted">
              Free shipping ≥ ₹499 · 48-hour cancel window on every order.
            </p>
          </div>
        </aside>
      </div>

      {pendingOrder && (
        <PaymentModal
          orderId={pendingOrder.id}
          amountPaise={pendingOrder.totalPaise}
          onClose={() => setPendingOrder(null)}
          onSuccess={() => {
            const id = pendingOrder.id;
            setPendingOrder(null);
            navigate(`/orders/${id}/confirmation`);
          }}
        />
      )}
    </>
  );
}

function SavedAddresses({
  addresses, selectedId, onSelect,
}: { addresses: Address[]; selectedId: number | null; onSelect: (id: number) => void }) {
  if (addresses.length === 0) {
    return <p className="text-sm text-muted mb-3">You have no saved addresses. Add one below.</p>;
  }
  return (
    <div className="space-y-2 mb-4">
      {addresses.map((a) => (
        <label
          key={a.id}
          className="flex items-start gap-3 bg-surface2 border border-border rounded px-3 py-2 cursor-pointer hover:border-accent"
        >
          <input
            type="radio"
            checked={selectedId === a.id}
            onChange={() => onSelect(a.id!)}
            className="mt-1"
          />
          <div className="text-sm">
            <p>{a.line1}{a.line2 ? `, ${a.line2}` : ''}</p>
            <p className="text-muted">{a.city}, {a.state} {a.pin}, {a.country}</p>
            {a.isDefault && <p className="text-xs text-success mt-0.5">Default</p>}
          </div>
        </label>
      ))}
    </div>
  );
}

function NewAddressForm() {
  const [addAddress, { isLoading }] = useAddMyAddressMutation();
  const [open, setOpen] = useState(false);
  const { register, handleSubmit, formState: { errors }, reset } = useForm<AddressForm>({
    resolver: zodResolver(addressSchema),
  });

  async function onSubmit(v: AddressForm) {
    try {
      await addAddress({ ...v, country: 'India', isDefault: false }).unwrap();
      setOpen(false);
      reset();
    } catch { /* silent */ }
  }

  if (!open) {
    return (
      <button className="btn-ghost !py-1.5 text-sm" onClick={() => setOpen(true)}>
        + Add new address
      </button>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-3 border-t border-border pt-3">
      <Field label="Address line 1" error={errors.line1?.message}>
        <input className={inputCls} {...register('line1')} />
      </Field>
      <Field label="Address line 2 (optional)">
        <input className={inputCls} {...register('line2')} />
      </Field>
      <div className="grid grid-cols-2 gap-3">
        <Field label="City" error={errors.city?.message}>
          <input className={inputCls} {...register('city')} />
        </Field>
        <Field label="State" error={errors.state?.message}>
          <input className={inputCls} {...register('state')} />
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-3">
        <Field label="PIN" error={errors.pin?.message}>
          <input className={inputCls} maxLength={6} {...register('pin')} />
        </Field>
        <Field label="Phone (optional)">
          <input className={inputCls} {...register('phone')} />
        </Field>
      </div>
      <div className="flex gap-2">
        <button type="submit" className="btn-primary" disabled={isLoading}>
          {isLoading ? 'Saving…' : 'Save address'}
        </button>
        <button type="button" className="btn-ghost" onClick={() => setOpen(false)}>Cancel</button>
      </div>
    </form>
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

const inputCls =
  'w-full px-3 py-2 rounded bg-surface2 border border-border focus:border-accent outline-none text-sm';

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="block text-xs text-muted mb-1">{label}</span>
      {children}
      {error && <span className="block text-xs text-danger mt-1">{error}</span>}
    </label>
  );
}

function extractMsg(e: unknown): string | undefined {
  if (e && typeof e === 'object' && 'data' in e) {
    const d = (e as { data?: { message?: string } }).data;
    return d?.message;
  }
  return undefined;
}

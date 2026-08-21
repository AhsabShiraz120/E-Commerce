import { useState } from 'react';
import { X, CreditCard } from 'lucide-react';
import clsx from 'clsx';
import { usePayForOrderMutation } from '@/store/api/ordersApi';
import type { PaymentMethod, PaymentRequest } from '@/api/types';
import { formatPaise } from '@/lib/format';

interface Props {
  orderId: number;
  amountPaise: number;
  onClose: () => void;
  onSuccess: () => void;
}

const TABS: { key: PaymentMethod; label: string }[] = [
  { key: 'CREDIT', label: 'Credit Card' },
  { key: 'DEBIT',  label: 'Debit Card' },
  { key: 'UPI',    label: 'UPI' },
  { key: 'WALLET', label: 'Wallet' },
];

const WALLETS = ['Paytm', 'PhonePe', 'AmazonPay', 'BookWormWallet'] as const;

/**
 * Payment modal matching the wireframe: tabs across the top, per-method form,
 * 1.5s deterministic "Processing…" delay (plan §1) before hitting the mock
 * gateway. Cards ending in 0000 → decline; retry allowed inline.
 */
export function PaymentModal({ orderId, amountPaise, onClose, onSuccess }: Props) {
  const [method, setMethod] = useState<PaymentMethod>('CREDIT');
  const [pay] = usePayForOrderMutation();
  const [state, setState] = useState<'idle' | 'processing'>('idle');
  const [err, setErr] = useState<string | null>(null);

  // Card
  const [cardNumber, setCardNumber] = useState('');
  const [cardholderName, setCardholderName] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  // UPI
  const [upiId, setUpiId] = useState('');
  // Wallet
  const [wallet, setWallet] = useState<(typeof WALLETS)[number]>('Paytm');

  async function onSubmit() {
    setErr(null);
    setState('processing');
    // Deterministic spinner delay per plan §1
    await new Promise((r) => setTimeout(r, 1500));

    const body: PaymentRequest = {
      method,
      ...(method === 'CREDIT' || method === 'DEBIT'
        ? { cardNumber, cardholderName, expiry, cvv }
        : {}),
      ...(method === 'UPI' ? { upiId } : {}),
      ...(method === 'WALLET' ? { wallet } : {}),
    };

    try {
      await pay({ id: orderId, body }).unwrap();
      setState('idle');
      onSuccess();
    } catch (e) {
      setState('idle');
      const msg = extractMsg(e) ?? 'Payment failed';
      setErr(msg);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm px-4">
      <div className="bg-surface border border-border rounded-lg w-full max-w-lg shadow-xl">
        <header className="flex items-center justify-between p-4 border-b border-border">
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <CreditCard className="w-5 h-5 text-accent" />
            Choose Payment Method
          </h2>
          <button onClick={onClose} className="text-muted hover:text-body" aria-label="Close">
            <X className="w-5 h-5" />
          </button>
        </header>

        {/* Tabs */}
        <div className="grid grid-cols-4 gap-1 p-2 bg-surface2 border-b border-border">
          {TABS.map((t) => (
            <button
              key={t.key}
              onClick={() => { setMethod(t.key); setErr(null); }}
              className={clsx(
                'px-2 py-2 text-xs sm:text-sm rounded transition-colors',
                method === t.key
                  ? 'bg-surface text-body'
                  : 'text-muted hover:text-body',
              )}
            >
              {t.label}
            </button>
          ))}
        </div>

        {/* Body */}
        <div className="p-4 space-y-3">
          {(method === 'CREDIT' || method === 'DEBIT') && (
            <>
              <Field label="Card Number">
                <input
                  value={cardNumber}
                  onChange={(e) => setCardNumber(e.target.value)}
                  placeholder="4111 1111 1111 1111"
                  className={inputCls}
                />
              </Field>
              <Field label="Cardholder Name">
                <input
                  value={cardholderName}
                  onChange={(e) => setCardholderName(e.target.value)}
                  className={inputCls}
                />
              </Field>
              <div className="grid grid-cols-2 gap-3">
                <Field label="Expiry (MM/YY)">
                  <input
                    value={expiry}
                    onChange={(e) => setExpiry(e.target.value)}
                    placeholder="12/28"
                    className={inputCls}
                  />
                </Field>
                <Field label="CVV">
                  <input
                    value={cvv}
                    onChange={(e) => setCvv(e.target.value)}
                    placeholder="123"
                    maxLength={4}
                    className={inputCls}
                  />
                </Field>
              </div>
              <p className="text-xs text-muted">
                Try any card that doesn't end in <code className="text-body">0000</code>.
                A card ending in <code className="text-body">0000</code> triggers a decline.
              </p>
            </>
          )}

          {method === 'UPI' && (
            <Field label="UPI ID">
              <input
                value={upiId}
                onChange={(e) => setUpiId(e.target.value)}
                placeholder="you@bank"
                className={inputCls}
              />
            </Field>
          )}

          {method === 'WALLET' && (
            <div className="space-y-2">
              {WALLETS.map((w) => (
                <label key={w} className="flex items-center gap-2 bg-surface2 border border-border rounded px-3 py-2 cursor-pointer hover:border-accent">
                  <input
                    type="radio"
                    checked={wallet === w}
                    onChange={() => setWallet(w)}
                  />
                  <span className="text-sm">{w === 'BookWormWallet' ? 'Book Worm Wallet' : w === 'AmazonPay' ? 'Amazon Pay' : w}</span>
                </label>
              ))}
            </div>
          )}

          {err && <p className="text-sm text-danger">{err}</p>}
        </div>

        {/* Footer */}
        <footer className="p-4 border-t border-border">
          <button
            className="btn-primary w-full"
            onClick={onSubmit}
            disabled={state === 'processing'}
          >
            {state === 'processing' ? 'Processing…' : `Pay ${formatPaise(amountPaise)}`}
          </button>
        </footer>
      </div>
    </div>
  );
}

const inputCls =
  'w-full px-3 py-2 rounded bg-surface2 border border-border focus:border-accent outline-none text-sm';

function Field({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <label className="block">
      <span className="block text-xs text-muted mb-1">{label}</span>
      {children}
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

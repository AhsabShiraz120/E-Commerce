/**
 * Money helpers. All backend prices are integer paise (₹ × 100).
 * Frontend formats with the ₹ symbol and Indian digit grouping.
 */

const INR_FORMATTER = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  maximumFractionDigits: 0,
  minimumFractionDigits: 0,
});

export function formatPaise(paise: number | null | undefined): string {
  const p = typeof paise === 'number' ? paise : 0;
  return INR_FORMATTER.format(Math.round(p / 100));
}

/** For display alongside a "Save ₹X" ribbon on discounted lines. */
export function paiseToRupees(paise: number): number {
  return Math.round(paise / 100);
}

export function formatDate(iso: string | undefined): string {
  if (!iso) return '';
  const d = new Date(iso);
  return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

import { describe, expect, it } from 'vitest';
import { formatPaise, paiseToRupees } from './format';

describe('formatPaise', () => {
  it('formats integer paise as ₹ with Indian grouping and no decimals', () => {
    expect(formatPaise(34900)).toBe('₹349');
    expect(formatPaise(129900)).toBe('₹1,299');
    expect(formatPaise(0)).toBe('₹0');
  });

  it('handles null/undefined as ₹0', () => {
    expect(formatPaise(null)).toBe('₹0');
    expect(formatPaise(undefined)).toBe('₹0');
  });
});

describe('paiseToRupees', () => {
  it('converts paise to rupees rounding half up', () => {
    expect(paiseToRupees(34900)).toBe(349);
    expect(paiseToRupees(34950)).toBe(350);
    expect(paiseToRupees(34949)).toBe(349);
  });
});

import { describe, it, expect } from 'vitest';
import { formatMmSs } from '../format';

describe('formatMmSs (speaking, мм:сс)', () => {
  it('0 → «0:00»', () => {
    expect(formatMmSs(0)).toBe('0:00');
  });

  it('65 → «1:05»', () => {
    expect(formatMmSs(65)).toBe('1:05');
  });

  it('3600 → «60:00»', () => {
    expect(formatMmSs(3600)).toBe('60:00');
  });

  it('9 → «0:09» (паддинг секунд)', () => {
    expect(formatMmSs(9)).toBe('0:09');
  });
});

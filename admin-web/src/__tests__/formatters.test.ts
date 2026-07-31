import { describe, it, expect } from 'vitest';
import {
  formatDate,
  formatDateTime,
  formatRelativeTime,
  formatNumber,
  formatPercent,
  formatFileSize,
  truncateString,
  capitalizeFirst,
  slugify,
  formatDuration,
  formatCurrency,
} from '../utils/formatters';

describe('formatters', () => {
  describe('formatDate', () => {
    it('should format date string correctly', () => {
      const result = formatDate('2024-01-15');
      expect(result).toBe('Jan 15, 2024');
    });

    it('should format Date object correctly', () => {
      const result = formatDate(new Date('2024-01-15'));
      expect(result).toBe('Jan 15, 2024');
    });

    it('should return dash for empty date', () => {
      expect(formatDate('')).toBe('-');
      expect(formatDate(null as any)).toBe('-');
      expect(formatDate(undefined as any)).toBe('-');
    });

    it('should use custom format string', () => {
      const result = formatDate('2024-01-15', 'yyyy-MM-dd');
      expect(result).toBe('2024-01-15');
    });
  });

  describe('formatDateTime', () => {
    it('should format date with time', () => {
      const result = formatDateTime('2024-01-15T14:30:00');
      expect(result).toContain('Jan 15, 2024');
      expect(result).toContain('14:30');
    });

    it('should return dash for empty date', () => {
      expect(formatDateTime('')).toBe('-');
    });
  });

  describe('formatRelativeTime', () => {
    it('should return relative time for past date', () => {
      const pastDate = new Date();
      pastDate.setDate(pastDate.getDate() - 1);
      const result = formatRelativeTime(pastDate);
      expect(result).toContain('ago');
    });

    it('should return dash for empty date', () => {
      expect(formatRelativeTime('')).toBe('-');
    });
  });

  describe('formatNumber', () => {
    it('should format number with commas', () => {
      expect(formatNumber(1000)).toBe('1,000');
      expect(formatNumber(1000000)).toBe('1,000,000');
    });

    it('should format with decimal places', () => {
      expect(formatNumber(1234.56, 2)).toBe('1,234.56');
    });

    it('should return dash for null/undefined', () => {
      expect(formatNumber(null as any)).toBe('-');
      expect(formatNumber(undefined as any)).toBe('-');
    });
  });

  describe('formatPercent', () => {
    it('should format as percentage', () => {
      expect(formatPercent(50)).toBe('50.0%');
      expect(formatPercent(75.5)).toBe('75.5%');
    });

    it('should respect decimal places', () => {
      expect(formatPercent(50, 0)).toBe('50%');
      expect(formatPercent(50.123, 2)).toBe('50.12%');
    });

    it('should return dash for null/undefined', () => {
      expect(formatPercent(null as any)).toBe('-');
      expect(formatPercent(undefined as any)).toBe('-');
    });
  });

  describe('formatFileSize', () => {
    it('should format bytes', () => {
      expect(formatFileSize(0)).toBe('0 B');
      expect(formatFileSize(512)).toBe('512 B');
    });

    it('should format kilobytes', () => {
      expect(formatFileSize(1024)).toBe('1 KB');
      expect(formatFileSize(1536)).toBe('1.5 KB');
    });

    it('should format megabytes', () => {
      expect(formatFileSize(1024 * 1024)).toBe('1 MB');
      expect(formatFileSize(5 * 1024 * 1024)).toBe('5 MB');
    });

    it('should format gigabytes', () => {
      expect(formatFileSize(1024 * 1024 * 1024)).toBe('1 GB');
    });
  });

  describe('truncateString', () => {
    it('should truncate long strings', () => {
      expect(truncateString('Hello World', 5)).toBe('Hello...');
    });

    it('should not truncate short strings', () => {
      expect(truncateString('Hi', 10)).toBe('Hi');
    });

    it('should handle empty strings', () => {
      expect(truncateString('', 5)).toBe('');
    });
  });

  describe('capitalizeFirst', () => {
    it('should capitalize first letter', () => {
      expect(capitalizeFirst('hello')).toBe('Hello');
      expect(capitalizeFirst('world')).toBe('World');
    });

    it('should handle empty strings', () => {
      expect(capitalizeFirst('')).toBe('');
    });

    it('should handle already capitalized', () => {
      expect(capitalizeFirst('Hello')).toBe('Hello');
    });
  });

  describe('slugify', () => {
    it('should convert to slug', () => {
      expect(slugify('Hello World')).toBe('hello-world');
      expect(slugify('Test Title Here')).toBe('test-title-here');
    });

    it('should remove special characters', () => {
      expect(slugify('Hello@World!')).toBe('helloworld');
    });

    it('should handle multiple spaces', () => {
      expect(slugify('Hello   World')).toBe('hello-world');
    });

    it('should trim hyphens', () => {
      expect(slugify('-hello-world-')).toBe('hello-world');
    });
  });

  describe('formatDuration', () => {
    it('should format minutes only', () => {
      expect(formatDuration(30)).toBe('30m');
    });

    it('should format hours only', () => {
      expect(formatDuration(120)).toBe('2h');
    });

    it('should format hours and minutes', () => {
      expect(formatDuration(90)).toBe('1h 30m');
    });
  });

  describe('formatCurrency', () => {
    it('should format USD', () => {
      expect(formatCurrency(100)).toBe('$100.00');
      expect(formatCurrency(50.5)).toBe('$50.50');
    });

    it('should format other currencies', () => {
      expect(formatCurrency(100, 'EUR')).toContain('100');
      expect(formatCurrency(100, 'GBP')).toContain('100');
    });
  });
});

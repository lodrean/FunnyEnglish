import { describe, it, expect } from 'vitest';
import {
  formatDuration,
  formatDate,
  formatFileSize,
} from '../utils/format';

describe('format utilities', () => {
  describe('formatDuration', () => {
    it('should format seconds only', () => {
      expect(formatDuration(45)).toBe('45s');
      expect(formatDuration(0)).toBe('0s');
    });

    it('should format minutes and seconds', () => {
      expect(formatDuration(90)).toBe('1:30');
      expect(formatDuration(125)).toBe('2:05');
    });

    it('should pad seconds with zero', () => {
      expect(formatDuration(61)).toBe('1:01');
      expect(formatDuration(600)).toBe('10:00');
    });
  });

  describe('formatDate', () => {
    it('should format date string', () => {
      const result = formatDate('2024-01-15');
      expect(result).toContain('Jan');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });

    it('should format ISO date string', () => {
      const result = formatDate('2024-12-25T10:30:00.000Z');
      expect(result).toContain('Dec');
      expect(result).toContain('25');
      expect(result).toContain('2024');
    });
  });

  describe('formatFileSize', () => {
    it('should format zero bytes', () => {
      expect(formatFileSize(0)).toBe('0 Bytes');
    });

    it('should format bytes', () => {
      expect(formatFileSize(512)).toBe('512 Bytes');
      expect(formatFileSize(1023)).toBe('1023 Bytes');
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

    it('should handle large files', () => {
      expect(formatFileSize(1024 * 1024 * 1024 * 2)).toContain('GB');
    });
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useToast } from '../useToast';
import { ToastContext } from '../../components/feedback/ToastProvider';
import React from 'react';

describe('useToast', () => {
  const mockToastContext = {
    success: vi.fn(),
    error: vi.fn(),
    warning: vi.fn(),
    info: vi.fn(),
    show: vi.fn(),
    dismiss: vi.fn(),
    dismissAll: vi.fn(),
  };

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    React.createElement(ToastContext.Provider, { value: mockToastContext }, children)
  );

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('returns toast context when used within provider', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    expect(result.current).toBe(mockToastContext);
    expect(result.current.success).toBeDefined();
    expect(result.current.dismiss).toBeDefined();
  });

  it('throws error when used outside provider', () => {
    // Suppress console.error for this test
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

    expect(() => {
      renderHook(() => useToast());
    }).toThrow('useToast must be used within a ToastProvider');

    consoleSpy.mockRestore();
  });

  it('can call success from context', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    result.current.success('Test message');

    expect(mockToastContext.success).toHaveBeenCalledWith('Test message');
  });

  it('can call dismiss from context', () => {
    const { result } = renderHook(() => useToast(), { wrapper });

    result.current.dismiss('toast-1');

    expect(mockToastContext.dismiss).toHaveBeenCalledWith('toast-1');
  });
});

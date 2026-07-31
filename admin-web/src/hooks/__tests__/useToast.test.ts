import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useToast } from '../useToast';
import { ToastContext } from '../../components/feedback/ToastProvider';
import React from 'react';

describe('useToast', () => {
  const mockToastContext = {
    showToast: vi.fn(),
    hideToast: vi.fn(),
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
    expect(result.current.showToast).toBeDefined();
    expect(result.current.hideToast).toBeDefined();
  });

  it('throws error when used outside provider', () => {
    // Suppress console.error for this test
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    
    expect(() => {
      renderHook(() => useToast());
    }).toThrow('useToast must be used within a ToastProvider');
    
    consoleSpy.mockRestore();
  });

  it('can call showToast from context', () => {
    const { result } = renderHook(() => useToast(), { wrapper });
    
    result.current.showToast({ message: 'Test message', severity: 'success' });
    
    expect(mockToastContext.showToast).toHaveBeenCalledWith({ 
      message: 'Test message', 
      severity: 'success' 
    });
  });

  it('can call hideToast from context', () => {
    const { result } = renderHook(() => useToast(), { wrapper });
    
    result.current.hideToast();
    
    expect(mockToastContext.hideToast).toHaveBeenCalled();
  });
});

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useConfirm } from '../useConfirm';

describe('useConfirm', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('initializes with isOpen as false', () => {
    const { result } = renderHook(() => useConfirm());
    
    expect(result.current.confirmState.isOpen).toBe(false);
  });

  it('opens confirmation dialog when confirm is called', async () => {
    const { result } = renderHook(() => useConfirm());
    
    let confirmPromise: Promise<boolean>;
    
    act(() => {
      confirmPromise = result.current.confirm({
        title: 'Delete Item?',
        message: 'Are you sure?',
      });
    });
    
    expect(result.current.confirmState.isOpen).toBe(true);
    expect(result.current.confirmState.title).toBe('Delete Item?');
    expect(result.current.confirmState.message).toBe('Are you sure?');
  });

  it('handles confirm action', async () => {
    const { result } = renderHook(() => useConfirm());
    
    let confirmPromise: Promise<boolean>;
    
    act(() => {
      confirmPromise = result.current.confirm({
        title: 'Delete?',
        message: 'Confirm delete',
      });
    });
    
    act(() => {
      result.current.handleConfirm();
    });
    
    expect(result.current.confirmState.isOpen).toBe(false);
    
    const resolvedValue = await confirmPromise!;
    expect(resolvedValue).toBe(true);
  });

  it('handles cancel action', async () => {
    const { result } = renderHook(() => useConfirm());
    
    let confirmPromise: Promise<boolean>;
    
    act(() => {
      confirmPromise = result.current.confirm({
        title: 'Delete?',
        message: 'Confirm delete',
      });
    });
    
    act(() => {
      result.current.handleCancel();
    });
    
    expect(result.current.confirmState.isOpen).toBe(false);
    
    const resolvedValue = await confirmPromise!;
    expect(resolvedValue).toBe(false);
  });

  it('uses default button texts', () => {
    const { result } = renderHook(() => useConfirm());
    
    act(() => {
      result.current.confirm({
        title: 'Test',
        message: 'Test message',
      });
    });
    
    expect(result.current.confirmState.confirmText).toBe('Confirm');
    expect(result.current.confirmState.cancelText).toBe('Cancel');
  });

  it('accepts custom button texts', () => {
    const { result } = renderHook(() => useConfirm());
    
    act(() => {
      result.current.confirm({
        title: 'Delete?',
        message: 'Confirm',
        confirmText: 'Yes, Delete',
        cancelText: 'No, Keep',
      });
    });
    
    expect(result.current.confirmState.confirmText).toBe('Yes, Delete');
    expect(result.current.confirmState.cancelText).toBe('No, Keep');
  });

  it('handles danger option', () => {
    const { result } = renderHook(() => useConfirm());
    
    act(() => {
      result.current.confirm({
        title: 'Delete?',
        message: 'Confirm',
        danger: true,
      });
    });
    
    expect(result.current.confirmState.danger).toBe(true);
  });
});

/**
 * ToastProvider Component
 * 
 * Global toast context provider that manages toast notifications
 * across the application. Provides helper methods for showing
 * different types of toasts.
 * 
 * @module components/feedback/ToastProvider
 */

import React, { createContext, useContext, useCallback, useState, useMemo } from 'react';
import { Box } from '@mui/material';
import { Toast, ToastItem, ToastType, ToastAction } from './Toast';

/** Maximum number of toasts displayed simultaneously */
const MAX_TOASTS = 5;

/** Default duration for auto-dismiss (5 seconds) */
const DEFAULT_DURATION = 5000;

/** Toast context interface */
interface ToastContextValue {
  /** Show a success toast */
  success: (message: string, duration?: number, action?: ToastAction) => void;
  /** Show an error toast */
  error: (message: string, duration?: number, action?: ToastAction) => void;
  /** Show a warning toast */
  warning: (message: string, duration?: number, action?: ToastAction) => void;
  /** Show an info toast */
  info: (message: string, duration?: number, action?: ToastAction) => void;
  /** Show a toast with custom type */
  show: (type: ToastType, message: string, duration?: number, action?: ToastAction) => void;
  /** Dismiss a specific toast by ID */
  dismiss: (id: string) => void;
  /** Dismiss all toasts */
  dismissAll: () => void;
}

/** Props for ToastProvider */
interface ToastProviderProps {
  children: React.ReactNode;
  /** Maximum number of toasts (default: 5) */
  maxToasts?: number;
  /** Default duration in milliseconds (default: 5000) */
  defaultDuration?: number;
  /** Position of toast container */
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left' | 'top-center' | 'bottom-center';
}

/**
 * Toast Context
 * 
 * Provides global access to toast methods throughout the application.
 */
// eslint-disable-next-line react-refresh/only-export-components
export const ToastContext = createContext<ToastContextValue | undefined>(undefined);

/**
 * Generate unique ID for each toast
 */
const generateId = (): string => {
  return `toast-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

/**
 * Get position styles based on position prop
 */
const getPositionStyles = (position: string): React.CSSProperties => {
  const styles: React.CSSProperties = {
    position: 'fixed',
    zIndex: 9999,
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
    padding: '16px',
  };

  switch (position) {
    case 'top-right':
      styles.top = 0;
      styles.right = 0;
      styles.alignItems = 'flex-end';
      break;
    case 'top-left':
      styles.top = 0;
      styles.left = 0;
      styles.alignItems = 'flex-start';
      break;
    case 'bottom-right':
      styles.bottom = 0;
      styles.right = 0;
      styles.alignItems = 'flex-end';
      styles.flexDirection = 'column-reverse';
      break;
    case 'bottom-left':
      styles.bottom = 0;
      styles.left = 0;
      styles.alignItems = 'flex-start';
      styles.flexDirection = 'column-reverse';
      break;
    case 'top-center':
      styles.top = 0;
      styles.left = '50%';
      styles.transform = 'translateX(-50%)';
      styles.alignItems = 'center';
      break;
    case 'bottom-center':
      styles.bottom = 0;
      styles.left = '50%';
      styles.transform = 'translateX(-50%)';
      styles.alignItems = 'center';
      styles.flexDirection = 'column-reverse';
      break;
    default:
      styles.top = 0;
      styles.right = 0;
      styles.alignItems = 'flex-end';
  }

  return styles;
};

/**
 * Toast Provider Component
 * 
 * Wraps the application and provides global toast functionality.
 * Renders a fixed-position container for toast notifications.
 * 
 * @example
 * ```tsx
 * <ToastProvider>
 *   <App />
 * </ToastProvider>
 * ```
 * 
 * @example
 * ```tsx
 * const { success, error } = useToast();
 * 
 * // Show success toast
 * success('Operation completed successfully');
 * 
 * // Show error toast with custom duration
 * error('Something went wrong', 10000);
 * 
 * // Show toast with action
 * success('Item saved', 5000, {
 *   label: 'View',
 *   onClick: () => navigate('/item/123')
 * });
 * ```
 */
export const ToastProvider: React.FC<ToastProviderProps> = ({
  children,
  maxToasts = MAX_TOASTS,
  defaultDuration = DEFAULT_DURATION,
  position = 'top-right',
}) => {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  /**
   * Dismiss a specific toast by ID
   */
  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((toast) => toast.id !== id));
  }, []);

  /**
   * Dismiss all toasts
   */
  const dismissAll = useCallback(() => {
    setToasts([]);
  }, []);

  /**
   * Show a toast with specified type
   */
  const show = useCallback(
    (type: ToastType, message: string, duration?: number, action?: ToastAction) => {
      const id = generateId();
      const newToast: ToastItem = {
        id,
        type,
        message,
        duration: duration ?? defaultDuration,
        action,
      };

      setToasts((prev) => {
        // Add new toast to the beginning
        const updated = [newToast, ...prev];
        // Remove oldest toasts if exceeding max
        if (updated.length > maxToasts) {
          return updated.slice(0, maxToasts);
        }
        return updated;
      });
    },
    [maxToasts, defaultDuration]
  );

  /**
   * Show success toast
   */
  const success = useCallback(
    (message: string, duration?: number, action?: ToastAction) => {
      show('success', message, duration, action);
    },
    [show]
  );

  /**
   * Show error toast
   */
  const error = useCallback(
    (message: string, duration?: number, action?: ToastAction) => {
      show('error', message, duration, action);
    },
    [show]
  );

  /**
   * Show warning toast
   */
  const warning = useCallback(
    (message: string, duration?: number, action?: ToastAction) => {
      show('warning', message, duration, action);
    },
    [show]
  );

  /**
   * Show info toast
   */
  const info = useCallback(
    (message: string, duration?: number, action?: ToastAction) => {
      show('info', message, duration, action);
    },
    [show]
  );

  // Memoize context value to prevent unnecessary re-renders
  const contextValue = useMemo(
    () => ({
      success,
      error,
      warning,
      info,
      show,
      dismiss,
      dismissAll,
    }),
    [success, error, warning, info, show, dismiss, dismissAll]
  );

  const positionStyles = getPositionStyles(position);

  return (
    <ToastContext.Provider value={contextValue}>
      {children}
      
      {/* Toast Container */}
      <Box
        role="region"
        aria-label="Notifications"
        sx={positionStyles}
      >
        {toasts.map((toast) => (
          <Toast
            key={toast.id}
            {...toast}
            onClose={dismiss}
          />
        ))}
      </Box>
    </ToastContext.Provider>
  );
};

/**
 * useToast Hook
 * 
 * Custom hook to access toast methods from any component.
 * Must be used within a ToastProvider.
 * 
 * @returns ToastContextValue object with toast methods
 * @throws Error if used outside of ToastProvider
 * 
 * @example
 * ```tsx
 * const { success, error, warning, info, dismissAll } = useToast();
 * 
 * success('Saved successfully!');
 * error('Something went wrong');
 * warning('Please check your input');
 * info('New update available');
 * ```
 */
// eslint-disable-next-line react-refresh/only-export-components
export const useToast = (): ToastContextValue => {
  const context = useContext(ToastContext);
  
  if (context === undefined) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  
  return context;
};

export default ToastProvider;

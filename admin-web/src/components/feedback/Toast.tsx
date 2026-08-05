/**
 * Toast Component - Notification system with auto-dismiss
 * Design System 2.0
 */

import React, { useEffect, useState, useCallback } from 'react';
import {
  Alert,
  AlertTitle,
  IconButton,
  Box,
  LinearProgress,
  Slide,
  alpha,
  useTheme,
} from '@mui/material';
import {
  Close as CloseIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
} from '@mui/icons-material';

// =============================================================================
// TYPES
// =============================================================================

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastAction {
  label: string;
  onClick: () => void;
}

export interface ToastItem {
  id: string;
  type: ToastType;
  message: string;
  duration?: number;
  action?: ToastAction;
}

export interface ToastProps {
  id: string;
  message: string;
  title?: string;
  type: ToastType;
  duration?: number;
  onClose: (id: string) => void;
  action?: ToastAction;
  persistent?: boolean;
}

// =============================================================================
// TOAST ITEM COMPONENT
// =============================================================================

const ToastItem: React.FC<ToastProps> = ({
  id,
  message,
  title,
  type,
  duration = 5000,
  onClose,
  action,
  persistent = false,
}) => {
  const theme = useTheme();
  const [progress, setProgress] = useState(100);
  const [isPaused, setIsPaused] = useState(false);

  const icons = {
    success: <CheckCircleIcon fontSize="small" />,
    error: <ErrorIcon fontSize="small" />,
    warning: <WarningIcon fontSize="small" />,
    info: <InfoIcon fontSize="small" />,
  };

  const titles = {
    success: 'Success',
    error: 'Error',
    warning: 'Warning',
    info: 'Information',
  };

  useEffect(() => {
    if (persistent || isPaused) return;

    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev <= 0) {
          clearInterval(interval);
          onClose(id);
          return 0;
        }
        return prev - (100 / (duration / 100));
      });
    }, 100);

    return () => clearInterval(interval);
  }, [id, duration, onClose, persistent, isPaused]);

  const handleClose = () => {
    onClose(id);
  };

  return (
    <Slide direction="left" in={true} mountOnEnter unmountOnExit>
      <Box
        onMouseEnter={() => setIsPaused(true)}
        onMouseLeave={() => setIsPaused(false)}
        sx={{
          minWidth: 320,
          maxWidth: 480,
          mb: 1.5,
          borderRadius: 2,
          overflow: 'hidden',
          boxShadow: theme.shadows[8],
          backgroundColor: theme.palette.background.paper,
          border: `1px solid ${alpha(theme.palette.divider, 0.5)}`,
        }}
      >
        <Alert
          severity={type}
          icon={icons[type]}
          action={
            <IconButton
              size="small"
              onClick={handleClose}
              sx={{
                color: 'inherit',
                opacity: 0.7,
                '&:hover': { opacity: 1 },
              }}
            >
              <CloseIcon fontSize="small" />
            </IconButton>
          }
          sx={{
            alignItems: 'flex-start',
            py: 1.5,
            px: 2,
            '& .MuiAlert-message': {
              flex: 1,
            },
          }}
        >
          {title && (
            <AlertTitle sx={{ fontWeight: 600, mb: 0.5 }}>
              {title}
            </AlertTitle>
          )}
          {!title && (
            <AlertTitle sx={{ fontWeight: 600, mb: 0.5 }}>
              {titles[type]}
            </AlertTitle>
          )}
          <Box sx={{ pr: 2 }}>{message}</Box>

          {action && (
            <Box sx={{ mt: 1 }}>
              <IconButton
                size="small"
                onClick={action.onClick}
                sx={{
                  color: `${type}.main`,
                  fontWeight: 600,
                  textTransform: 'uppercase',
                  fontSize: '0.75rem',
                  p: 0.5,
                  '&:hover': {
                    backgroundColor: alpha(theme.palette[type].main, 0.1),
                  },
                }}
              >
                {action.label}
              </IconButton>
            </Box>
          )}
        </Alert>

        {/* Progress bar */}
        {!persistent && (
          <LinearProgress
            variant="determinate"
            value={progress}
            color={type}
            sx={{
              height: 3,
              '& .MuiLinearProgress-bar': {
                transition: isPaused ? 'none' : 'transform 0.1s linear',
              },
            }}
          />
        )}
      </Box>
    </Slide>
  );
};

// =============================================================================
// TOAST CONTAINER
// =============================================================================

export interface ToastContainerProps {
  toasts: ToastProps[];
  onClose: (id: string) => void;
  position?: 'top-right' | 'top-left' | 'bottom-right' | 'bottom-left';
}

export const ToastContainer: React.FC<ToastContainerProps> = ({
  toasts,
  onClose,
  position = 'top-right',
}) => {
  const positionStyles = {
    'top-right': { top: 24, right: 24 },
    'top-left': { top: 24, left: 24 },
    'bottom-right': { bottom: 24, right: 24 },
    'bottom-left': { bottom: 24, left: 24 },
  };

  if (toasts.length === 0) return null;

  return (
    <Box
      sx={{
        position: 'fixed',
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: 1,
        ...positionStyles[position],
      }}
    >
      {toasts.map((toast) => (
        <ToastItem key={toast.id} {...toast} onClose={onClose} />
      ))}
    </Box>
  );
};

// =============================================================================
// HOOK FOR TOAST MANAGEMENT
// =============================================================================

export interface UseToastReturn {
  toasts: ToastProps[];
  showToast: (toast: Omit<ToastProps, 'id' | 'onClose'>) => void;
  hideToast: (id: string) => void;
  clearAll: () => void;
}

// eslint-disable-next-line react-refresh/only-export-components
export const useToast = (): UseToastReturn => {
  const [toasts, setToasts] = useState<ToastProps[]>([]);

  const showToast = useCallback((toast: Omit<ToastProps, 'id' | 'onClose'>) => {
    const id = Math.random().toString(36).substr(2, 9);
    setToasts((prev) => [
      ...prev,
      {
        ...toast,
        id,
        onClose: (toastId: string) => {
          setToasts((prev) => prev.filter((t) => t.id !== toastId));
        },
      },
    ]);
  }, []);

  const hideToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const clearAll = useCallback(() => {
    setToasts([]);
  }, []);

  return { toasts, showToast, hideToast, clearAll };
};

export { ToastItem as Toast };
export default ToastItem;

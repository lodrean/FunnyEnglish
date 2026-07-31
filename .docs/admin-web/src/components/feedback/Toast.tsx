/**
 * Toast Component
 * 
 * Individual toast notification with slide-in animation,
 * progress bar, and auto-dismiss functionality.
 * 
 * @module components/feedback/Toast
 */

import React, { useEffect, useCallback, useRef, useState } from 'react';
import { Box, IconButton, Typography, Button, Slide, Paper } from '@mui/material';
import {
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Close as CloseIcon,
} from '@mui/icons-material';

/** Toast notification types */
export type ToastType = 'success' | 'error' | 'warning' | 'info';

/** Toast action button configuration */
export interface ToastAction {
  label: string;
  onClick: () => void;
}

/** Toast item data structure */
export interface ToastItem {
  id: string;
  type: ToastType;
  message: string;
  duration?: number;
  action?: ToastAction;
}

/** Props for individual Toast component */
interface ToastProps {
  toast: ToastItem;
  onDismiss: (id: string) => void;
  index: number;
}

/** Color configuration for each toast type */
const toastConfig: Record<ToastType, { icon: React.ElementType; color: string; bgColor: string }> = {
  success: {
    icon: CheckCircleIcon,
    color: '#43A047',
    bgColor: '#E8F5E9',
  },
  error: {
    icon: ErrorIcon,
    color: '#E53935',
    bgColor: '#FFEBEE',
  },
  warning: {
    icon: WarningIcon,
    color: '#FB8C00',
    bgColor: '#FFF3E0',
  },
  info: {
    icon: InfoIcon,
    color: '#2196F3',
    bgColor: '#E3F2FD',
  },
};

/**
 * Individual Toast Component
 * 
 * Displays a single toast notification with:
 * - Slide-in animation
 * - Progress bar for auto-dismiss
 * - Close button
 * - Optional action button
 * - Hover to pause timer
 */
export const Toast: React.FC<ToastProps> = ({ toast, onDismiss, index }) => {
  const { id, type, message, duration = 5000, action } = toast;
  const config = toastConfig[type];
  const Icon = config.icon;
  
  const [progress, setProgress] = useState(100);
  const [isPaused, setIsPaused] = useState(false);
  const [isExiting, setIsExiting] = useState(false);
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const progressRef = useRef<NodeJS.Timeout | null>(null);
  const startTimeRef = useRef<number>(Date.now());
  const remainingTimeRef = useRef<number>(duration);

  /**
   * Handle toast dismissal with exit animation
   */
  const handleDismiss = useCallback(() => {
    setIsExiting(true);
    // Wait for exit animation before removing from state
    setTimeout(() => {
      onDismiss(id);
    }, 300);
  }, [id, onDismiss]);

  /**
   * Handle action button click
   */
  const handleActionClick = useCallback(() => {
    if (action) {
      action.onClick();
      handleDismiss();
    }
  }, [action, handleDismiss]);

  /**
   * Setup auto-dismiss timer and progress bar
   */
  useEffect(() => {
    if (duration <= 0) return;

    const updateProgress = () => {
      if (!isPaused) {
        const elapsed = Date.now() - startTimeRef.current;
        const remaining = Math.max(0, remainingTimeRef.current - elapsed);
        const newProgress = (remaining / duration) * 100;
        setProgress(newProgress);

        if (remaining <= 0) {
          handleDismiss();
        }
      }
    };

    // Update progress every 50ms for smooth animation
    progressRef.current = setInterval(updateProgress, 50);

    // Main dismiss timer
    timerRef.current = setTimeout(() => {
      handleDismiss();
    }, remainingTimeRef.current);

    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
      if (progressRef.current) {
        clearInterval(progressRef.current);
      }
    };
  }, [duration, isPaused, handleDismiss]);

  /**
   * Handle mouse enter - pause timer
   */
  const handleMouseEnter = () => {
    setIsPaused(true);
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }
    // Calculate remaining time
    const elapsed = Date.now() - startTimeRef.current;
    remainingTimeRef.current = Math.max(0, remainingTimeRef.current - elapsed);
  };

  /**
   * Handle mouse leave - resume timer
   */
  const handleMouseLeave = () => {
    setIsPaused(false);
    startTimeRef.current = Date.now();
    if (remainingTimeRef.current > 0) {
      timerRef.current = setTimeout(() => {
        handleDismiss();
      }, remainingTimeRef.current);
    }
  };

  return (
    <Slide
      direction="left"
      in={!isExiting}
      mountOnEnter
      unmountOnExit
      timeout={300}
    >
      <Paper
        elevation={4}
        role="alert"
        aria-live="polite"
        onMouseEnter={handleMouseEnter}
        onMouseLeave={handleMouseLeave}
        sx={{
          display: 'flex',
          alignItems: 'flex-start',
          minWidth: 320,
          maxWidth: 480,
          mb: 1,
          overflow: 'hidden',
          position: 'relative',
          borderRadius: 2,
          borderLeft: 4,
          borderColor: config.color,
          backgroundColor: '#FFFFFF',
          transition: 'transform 0.2s ease, box-shadow 0.2s ease',
          '&:hover': {
            transform: 'translateY(-2px)',
            boxShadow: 6,
          },
        }}
      >
        {/* Icon */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            p: 1.5,
            backgroundColor: config.bgColor,
          }}
        >
          <Icon sx={{ color: config.color, fontSize: 24 }} />
        </Box>

        {/* Content */}
        <Box sx={{ flex: 1, p: 1.5, pr: 4 }}>
          <Typography
            variant="body2"
            sx={{
              color: '#212121',
              fontWeight: 500,
              lineHeight: 1.5,
            }}
          >
            {message}
          </Typography>

          {/* Action Button */}
          {action && (
            <Button
              size="small"
              onClick={handleActionClick}
              sx={{
                mt: 1,
                color: config.color,
                fontWeight: 600,
                textTransform: 'none',
                '&:hover': {
                  backgroundColor: config.bgColor,
                },
              }}
            >
              {action.label}
            </Button>
          )}
        </Box>

        {/* Close Button */}
        <IconButton
          size="small"
          onClick={handleDismiss}
          aria-label="Close notification"
          sx={{
            position: 'absolute',
            top: 8,
            right: 8,
            color: '#757575',
            '&:hover': {
              color: '#212121',
              backgroundColor: 'rgba(0, 0, 0, 0.04)',
            },
          }}
        >
          <CloseIcon fontSize="small" />
        </IconButton>

        {/* Progress Bar */}
        {duration > 0 && (
          <Box
            sx={{
              position: 'absolute',
              bottom: 0,
              left: 0,
              height: 3,
              width: `${progress}%`,
              backgroundColor: config.color,
              transition: isPaused ? 'none' : 'width 0.05s linear',
              opacity: 0.7,
            }}
          />
        )}
      </Paper>
    </Slide>
  );
};

export default Toast;

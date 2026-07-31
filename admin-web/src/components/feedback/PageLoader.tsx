/**
 * PageLoader Component
 * 
 * Full-page loading overlay with centered spinner and optional text.
 * Used for blocking operations like initial page load or form submission.
 * 
 * @module components/feedback/PageLoader
 */

import React from 'react';
import {
  Box,
  CircularProgress,
  Typography,
  Fade,
  Backdrop,
} from '@mui/material';

/** Props for PageLoader component */
export interface PageLoaderProps {
  /** Whether the loader is visible */
  loading: boolean;
  /** Optional loading text to display */
  text?: string;
  /** Size of the spinner (default: 56) */
  size?: number;
  /** Thickness of the spinner (default: 4) */
  thickness?: number;
  /** Color of the spinner (default: primary) */
  color?: 'primary' | 'secondary' | 'error' | 'info' | 'success' | 'warning' | 'inherit';
  /** Whether to show backdrop overlay (default: true) */
  backdrop?: boolean;
  /** Z-index for the loader (default: 9999) */
  zIndex?: number;
  /** Minimum height when not fullscreen (default: 400) */
  minHeight?: number | string;
  /** Whether to take full viewport height (default: true) */
  fullscreen?: boolean;
  /** Custom spinner component (optional) */
  customSpinner?: React.ReactNode;
}

/**
 * PageLoader Component
 * 
 * Displays a full-page loading state with:
 * - Centered circular progress spinner
 * - Optional loading text
 * - Backdrop overlay to block interactions
 * - Fade animation for smooth appearance
 * 
 * @example
 * ```tsx
 * // Basic usage
 * <PageLoader loading={isLoading} />
 * 
 * // With loading text
 * <PageLoader 
 *   loading={isSubmitting} 
 *   text="Saving your changes..." 
 * />
 * 
 * // Custom size and color
 * <PageLoader 
 *   loading={isLoading}
 *   size={80}
 *   color="success"
 *   text="Loading data..."
 * />
 * 
 * // Without backdrop (for inline loading)
 * <PageLoader 
 *   loading={isLoading}
 *   backdrop={false}
 *   fullscreen={false}
 *   minHeight={200}
 * />
 * ```
 */
export const PageLoader: React.FC<PageLoaderProps> = ({
  loading,
  text,
  size = 56,
  thickness = 4,
  color = 'primary',
  backdrop = true,
  zIndex = 9999,
  minHeight = 400,
  fullscreen = true,
  customSpinner,
}) => {
  const content = (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        ...(fullscreen
          ? {
              position: 'fixed',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
            }
          : {
              minHeight,
              width: '100%',
            }),
      }}
    >
      {/* Spinner */}
      {customSpinner || (
        <CircularProgress
          size={size}
          thickness={thickness}
          color={color}
          sx={{
            color: color === 'primary' ? '#4A90D9' : undefined,
          }}
          aria-label="Loading"
        />
      )}

      {/* Loading Text */}
      {text && (
        <Typography
          variant="body1"
          sx={{
            mt: 3,
            color: '#616161',
            fontWeight: 500,
            textAlign: 'center',
          }}
        >
          {text}
        </Typography>
      )}
    </Box>
  );

  // Wrap with backdrop if enabled
  if (backdrop) {
    return (
      <Backdrop
        open={loading}
        sx={{
          zIndex,
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          flexDirection: 'column',
        }}
        transitionDuration={300}
      >
        <Fade in={loading} timeout={300}>
          {content}
        </Fade>
      </Backdrop>
    );
  }

  // Inline loader without backdrop
  return (
    <Fade in={loading} timeout={300}>
      <Box
        sx={{
          display: loading ? 'flex' : 'none',
        }}
      >
        {content}
      </Box>
    </Fade>
  );
};

/**
 * InlineLoader Component
 * 
 * Simplified loader for inline use within components.
 * Does not include backdrop and has smaller default size.
 * 
 * @example
 * ```tsx
 * <InlineLoader loading={isLoading} text="Loading..." />
 * ```
 */
export const InlineLoader: React.FC<Omit<PageLoaderProps, 'backdrop' | 'fullscreen'>> = ({
  loading,
  text,
  size = 32,
  thickness = 3,
  color = 'primary',
  minHeight = 100,
  customSpinner,
}) => {
  return (
    <PageLoader
      loading={loading}
      text={text}
      size={size}
      thickness={thickness}
      color={color}
      backdrop={false}
      fullscreen={false}
      minHeight={minHeight}
      customSpinner={customSpinner}
    />
  );
};

/**
 * SkeletonLoader Component
 * 
 * Skeleton placeholder for content that is loading.
 * Shows a pulsing placeholder shape.
 * 
 * @example
 * ```tsx
 * <SkeletonLoader loading={isLoading} height={200}>
 *   <ActualContent />
 * </SkeletonLoader>
 * ```
 */
import { Skeleton } from '@mui/material';

export interface SkeletonLoaderProps {
  loading: boolean;
  children: React.ReactNode;
  height?: number | string;
  width?: number | string;
  variant?: 'text' | 'rectangular' | 'rounded' | 'circular';
  animation?: 'pulse' | 'wave' | false;
}

export const SkeletonLoader: React.FC<SkeletonLoaderProps> = ({
  loading,
  children,
  height = 200,
  width = '100%',
  variant = 'rectangular',
  animation = 'pulse',
}) => {
  if (loading) {
    return (
      <Skeleton
        variant={variant}
        width={width}
        height={height}
        animation={animation}
        sx={{
          borderRadius: variant === 'rounded' ? 2 : undefined,
          backgroundColor: 'rgba(0, 0, 0, 0.08)',
        }}
      />
    );
  }

  return <>{children}</>;
};

export default PageLoader;

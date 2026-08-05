/**
 * ErrorBoundary Component
 * 
 * React error boundary that catches JavaScript errors in child components
 * and displays a fallback UI instead of crashing the entire application.
 * 
 * @module components/feedback/ErrorBoundary
 */

import React, { Component, ErrorInfo, ReactNode } from 'react';
import { logger } from '../../utils/logger';
import {
  Box,
  Button,
  Typography,
  Paper,
  Collapse,
  IconButton,
  Alert,
  AlertTitle,
} from '@mui/material';
import {
  Error as ErrorIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  BugReport as BugReportIcon,
} from '@mui/icons-material';

/** Props for ErrorBoundary component */
interface ErrorBoundaryProps {
  /** Child components to render */
  children: ReactNode;
  /** Custom fallback UI (optional) */
  fallback?: ReactNode;
  /** Callback when error is caught (optional) */
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  /** Callback when retry is clicked (optional) */
  onRetry?: () => void;
  /** Whether to show error details in development mode (default: true) */
  showDetails?: boolean;
  /** Custom error message (optional) */
  errorMessage?: string;
  /** Custom retry button text (default: 'Try Again') */
  retryText?: string;
}

/** State for ErrorBoundary component */
interface ErrorBoundaryState {
  /** Whether an error has been caught */
  hasError: boolean;
  /** The caught error */
  error: Error | null;
  /** React error info */
  errorInfo: ErrorInfo | null;
  /** Whether error details are expanded */
  detailsExpanded: boolean;
  /** Error occurrence timestamp */
  errorTimestamp: number | null;
}

/**
 * ErrorBoundary Class Component
 * 
 * Catches JavaScript errors anywhere in the child component tree,
 * logs those errors, and displays a fallback UI.
 * 
 * @example
 * ```tsx
 * // Basic usage
 * <ErrorBoundary>
 *   <MyComponent />
 * </ErrorBoundary>
 * 
 * // With custom fallback
 * <ErrorBoundary fallback={<CustomErrorPage />}>
 *   <MyComponent />
 * </ErrorBoundary>
 * 
 * // With error handler and retry
 * <ErrorBoundary
 *   onError={(error, errorInfo) => {
 *     logErrorToService(error, errorInfo);
 *   }}
 *   onRetry={() => {
 *     // Reset state or refetch data
 *   }}
 * >
 *   <MyComponent />
 * </ErrorBoundary>
 * ```
 */
export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      detailsExpanded: false,
      errorTimestamp: null,
    };
  }

  /**
   * Update state when an error is caught
   */
  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
      errorTimestamp: Date.now(),
    };
  }

  /**
   * Log error details when caught
   */
  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    this.setState({ errorInfo });

    // Log to console + remote (OpenSpec add-client-logging)
    logger.error('ErrorBoundary', `Caught: ${error.message}`, error);
    console.error('Component stack:', errorInfo.componentStack);

    // Call optional error handler
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  /**
   * Handle retry button click
   */
  handleRetry = (): void => {
    // Reset error state
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      detailsExpanded: false,
      errorTimestamp: null,
    });

    // Call optional retry handler
    if (this.props.onRetry) {
      this.props.onRetry();
    }
  };

  /**
   * Toggle error details visibility
   */
  toggleDetails = (): void => {
    this.setState((prevState) => ({
      detailsExpanded: !prevState.detailsExpanded,
    }));
  };

  render(): ReactNode {
    const { hasError, error, errorInfo, detailsExpanded, errorTimestamp } = this.state;
    const {
      children,
      fallback,
      showDetails = true,
      errorMessage = 'Something went wrong. Please try again.',
      retryText = 'Try Again',
    } = this.props;

    // Render children if no error
    if (!hasError) {
      return children;
    }

    // Render custom fallback if provided
    if (fallback) {
      return fallback;
    }

    // Determine if we're in development mode
    const isDevelopment = process.env.NODE_ENV === 'development';
    const shouldShowDetails = showDetails && isDevelopment;

    return (
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          p: 3,
          backgroundColor: '#F5F5F5',
        }}
      >
        <Paper
          elevation={3}
          sx={{
            maxWidth: 600,
            width: '100%',
            p: 4,
            borderRadius: 3,
            textAlign: 'center',
          }}
        >
          {/* Error Icon */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              mb: 3,
            }}
          >
            <Box
              sx={{
                width: 80,
                height: 80,
                borderRadius: '50%',
                backgroundColor: 'rgba(229, 57, 53, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <ErrorIcon sx={{ fontSize: 40, color: '#E53935' }} />
            </Box>
          </Box>

          {/* Error Title */}
          <Typography
            variant="h5"
            component="h1"
            sx={{
              fontWeight: 600,
              color: '#212121',
              mb: 2,
            }}
          >
            Oops! Something Went Wrong
          </Typography>

          {/* Error Message */}
          <Typography
            variant="body1"
            sx={{
              color: '#616161',
              mb: 3,
              lineHeight: 1.6,
            }}
          >
            {errorMessage}
          </Typography>

          {/* Retry Button */}
          <Button
            variant="contained"
            size="large"
            startIcon={<RefreshIcon />}
            onClick={this.handleRetry}
            sx={{
              mb: 3,
              textTransform: 'none',
              fontWeight: 600,
              backgroundColor: '#4A90D9',
              '&:hover': {
                backgroundColor: '#357ABD',
              },
            }}
          >
            {retryText}
          </Button>

          {/* Error Details (Development Only) */}
          {shouldShowDetails && error && (
            <Box sx={{ mt: 2, textAlign: 'left' }}>
              <Alert
                severity="error"
                icon={<BugReportIcon />}
                action={
                  <IconButton
                    size="small"
                    onClick={this.toggleDetails}
                    aria-label={detailsExpanded ? 'Hide details' : 'Show details'}
                  >
                    {detailsExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                  </IconButton>
                }
                sx={{
                  backgroundColor: 'rgba(229, 57, 53, 0.08)',
                  '& .MuiAlert-icon': {
                    color: '#E53935',
                  },
                }}
              >
                <AlertTitle>Development Error Details</AlertTitle>
                <Typography variant="body2" sx={{ fontFamily: 'monospace', wordBreak: 'break-word' }}>
                  <strong>Error:</strong> {error.name}: {error.message}
                </Typography>
                {errorTimestamp && (
                  <Typography variant="caption" display="block" sx={{ mt: 1, color: '#757575' }}>
                    Occurred at: {new Date(errorTimestamp).toLocaleString()}
                  </Typography>
                )}
              </Alert>

              <Collapse in={detailsExpanded}>
                <Box
                  sx={{
                    mt: 2,
                    p: 2,
                    backgroundColor: '#263238',
                    borderRadius: 1,
                    overflow: 'auto',
                  }}
                >
                  <Typography
                    variant="caption"
                    component="pre"
                    sx={{
                      color: '#FFCDD2',
                      fontFamily: 'monospace',
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      margin: 0,
                    }}
                  >
                    {error.stack}
                  </Typography>
                  {errorInfo && (
                    <>
                      <Typography
                        variant="caption"
                        component="pre"
                        sx={{
                          color: '#B0BEC5',
                          fontFamily: 'monospace',
                          whiteSpace: 'pre-wrap',
                          wordBreak: 'break-word',
                          margin: 0,
                          mt: 2,
                        }}
                      >
                        {errorInfo.componentStack}
                      </Typography>
                    </>
                  )}
                </Box>
              </Collapse>
            </Box>
          )}

          {/* Support Message */}
          <Typography
            variant="caption"
            sx={{
              display: 'block',
              mt: 3,
              color: '#9E9E9E',
            }}
          >
            If the problem persists, please contact support.
          </Typography>
        </Paper>
      </Box>
    );
  }
}

/**
 * ErrorFallback Component
 * 
 * Standalone error fallback UI that can be used outside of ErrorBoundary.
 * 
 * @example
 * ```tsx
 * <ErrorFallback 
 *   error={error}
 *   onRetry={() => refetch()}
 * />
 * ```
 */
export interface ErrorFallbackProps {
  error: Error;
  onRetry?: () => void;
  retryText?: string;
  errorMessage?: string;
}

export const ErrorFallback: React.FC<ErrorFallbackProps> = ({
  error,
  onRetry,
  retryText = 'Try Again',
  errorMessage = 'An error occurred while loading this content.',
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        p: 4,
        textAlign: 'center',
      }}
    >
      <ErrorIcon sx={{ fontSize: 48, color: '#E53935', mb: 2 }} />
      <Typography variant="h6" sx={{ color: '#212121', mb: 1 }}>
        Error
      </Typography>
      <Typography variant="body2" sx={{ color: '#616161', mb: 3 }}>
        {errorMessage}
      </Typography>
      {onRetry && (
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={onRetry}
          sx={{ textTransform: 'none' }}
        >
          {retryText}
        </Button>
      )}
      {process.env.NODE_ENV === 'development' && (
        <Typography
          variant="caption"
          sx={{
            mt: 2,
            p: 2,
            backgroundColor: '#FFEBEE',
            borderRadius: 1,
            color: '#C62828',
            fontFamily: 'monospace',
            textAlign: 'left',
            maxWidth: '100%',
            overflow: 'auto',
          }}
        >
          {error.message}
        </Typography>
      )}
    </Box>
  );
};

/**
 * withErrorBoundary HOC
 * 
 * Higher-order component that wraps a component with ErrorBoundary.
 * 
 * @example
 * ```tsx
 * const SafeComponent = withErrorBoundary(MyComponent, {
 *   onError: (error) => console.error(error),
 * });
 * ```
 */
// eslint-disable-next-line react-refresh/only-export-components
export function withErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<ErrorBoundaryProps, 'children'>
): React.FC<P> {
  const WithErrorBoundary: React.FC<P> = (props) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WithErrorBoundary.displayName = `WithErrorBoundary(${Component.displayName || Component.name || 'Component'})`;

  return WithErrorBoundary;
}

export default ErrorBoundary;

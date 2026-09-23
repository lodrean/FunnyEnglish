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
  alpha,
  useTheme,
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

    return (
      <ErrorBoundaryView
        error={error}
        errorInfo={errorInfo}
        detailsExpanded={detailsExpanded}
        errorTimestamp={errorTimestamp}
        showDetails={showDetails}
        errorMessage={errorMessage}
        retryText={retryText}
        onRetryClick={this.handleRetry}
        onToggleDetails={this.toggleDetails}
      />
    );
  }
}

/** Props для тематизированного фолбэка (класс не может использовать хуки темы) */
interface ErrorBoundaryViewProps {
  error: Error | null;
  errorInfo: ErrorInfo | null;
  detailsExpanded: boolean;
  errorTimestamp: number | null;
  showDetails: boolean;
  errorMessage: string;
  retryText: string;
  onRetryClick: () => void;
  onToggleDetails: () => void;
}

/**
 * ErrorBoundaryView — рендер фолбэка; цвета только из темы
 * (bd ku2: ранее хардкоды #4A90D9/#357ABD/MUI-grey/Material-легаси).
 */
const ErrorBoundaryView: React.FC<ErrorBoundaryViewProps> = ({
  error,
  errorInfo,
  detailsExpanded,
  errorTimestamp,
  showDetails,
  errorMessage,
  retryText,
  onRetryClick,
  onToggleDetails,
}) => {
  const theme = useTheme();
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
        backgroundColor: 'background.default',
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
              backgroundColor: alpha(theme.palette.error.main, 0.1),
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <ErrorIcon sx={{ fontSize: 40, color: 'error.main' }} />
          </Box>
        </Box>

        {/* Error Title */}
        <Typography
          variant="h5"
          component="h1"
          sx={{
            fontWeight: 600,
            color: 'text.primary',
            mb: 2,
          }}
        >
          Oops! Something Went Wrong
        </Typography>

        {/* Error Message */}
        <Typography
          variant="body1"
          sx={{
            color: 'text.secondary',
            mb: 3,
            lineHeight: 1.6,
          }}
        >
          {errorMessage}
        </Typography>

        {/* Retry Button (containedPrimary: primaryStrong + hover state layer из темы) */}
        <Button
          variant="contained"
          color="primary"
          size="large"
          startIcon={<RefreshIcon />}
          onClick={onRetryClick}
          sx={{
            mb: 3,
            textTransform: 'none',
            fontWeight: 600,
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
                  onClick={onToggleDetails}
                  aria-label={detailsExpanded ? 'Hide details' : 'Show details'}
                >
                  {detailsExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                </IconButton>
              }
              sx={{
                backgroundColor: alpha(theme.palette.error.main, 0.08),
                '& .MuiAlert-icon': {
                  color: 'error.main',
                },
              }}
            >
              <AlertTitle>Development Error Details</AlertTitle>
              <Typography variant="body2" sx={{ fontFamily: 'monospace', wordBreak: 'break-word' }}>
                <strong>Error:</strong> {error.name}: {error.message}
              </Typography>
              {errorTimestamp && (
                <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                  Occurred at: {new Date(errorTimestamp).toLocaleString()}
                </Typography>
              )}
            </Alert>

            <Collapse in={detailsExpanded}>
              <Box
                sx={{
                  mt: 2,
                  p: 2,
                  backgroundColor: 'text.primary',
                  borderRadius: 1,
                  overflow: 'auto',
                }}
              >
                <Typography
                  variant="caption"
                  component="pre"
                  sx={{
                    color: 'background.paper',
                    fontFamily: 'monospace',
                    whiteSpace: 'pre-wrap',
                    wordBreak: 'break-word',
                    margin: 0,
                  }}
                >
                  {error.stack}
                </Typography>
                {errorInfo && (
                  <Typography
                    variant="caption"
                    component="pre"
                    sx={{
                      color: 'background.default',
                      fontFamily: 'monospace',
                      whiteSpace: 'pre-wrap',
                      wordBreak: 'break-word',
                      margin: 0,
                      mt: 2,
                    }}
                  >
                    {errorInfo.componentStack}
                  </Typography>
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
            color: 'text.disabled',
          }}
        >
          If the problem persists, please contact support.
        </Typography>
      </Paper>
    </Box>
  );
};

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
      <ErrorIcon sx={{ fontSize: 48, color: 'error.main', mb: 2 }} />
      <Typography variant="h6" sx={{ color: 'text.primary', mb: 1 }}>
        Error
      </Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mb: 3 }}>
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
            backgroundColor: (theme) => alpha(theme.palette.error.main, 0.08),
            borderRadius: 1,
            color: 'error.dark',
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

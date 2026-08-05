import React from 'react';
import {
  Alert,
  AlertTitle,
  Box,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Typography,
  IconButton,
} from '@mui/material';
import {
  Error as ErrorIcon,
  Warning as WarningIcon,
  Info as InfoIcon,
  Close as CloseIcon,
} from '@mui/icons-material';

export type ErrorSeverity = 'error' | 'warning' | 'info' | 'success';

export interface ValidationError {
  field: string;
  message: string;
}

export interface ErrorDetails {
  title?: string;
  message: string;
  severity?: ErrorSeverity;
  validationErrors?: ValidationError[];
  fieldErrors?: Record<string, string>;
  code?: string;
  suggestion?: string;
}

interface ErrorDisplayProps {
  error: ErrorDetails | null;
  onClose?: () => void;
  showIcon?: boolean;
  dense?: boolean;
  maxWidth?: number | string;
}

// Map backend field names to user-friendly labels
const FIELD_LABELS: Record<string, string> = {
  categoryId: 'Category',
  title: 'Test Title',
  description: 'Description',
  thumbnailUrl: 'Thumbnail Image',
  difficulty: 'Difficulty Level',
  pointsReward: 'Points Reward',
  timeLimitSeconds: 'Time Limit',
  isPublished: 'Publication Status',
  questions: 'Questions',
  'questions[].title': 'Question Title',
  'questions[].content': 'Question Content',
  'questions[].points': 'Question Points',
  instruction: 'Instruction',
  imageUrl: 'Image',
  words: 'Words',
  hotspots: 'Hotspots',
  text: 'Text',
  translation: 'Translation',
  wordId: 'Word',
  x: 'X Coordinate',
  y: 'Y Coordinate',
  width: 'Width',
  height: 'Height',
};

// Get user-friendly label for field
const getFieldLabel = (field: string): string => {
  return FIELD_LABELS[field] || field.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase());
};

export const ErrorDisplay: React.FC<ErrorDisplayProps> = ({
  error,
  onClose,
  showIcon = true,
  dense = false,
  maxWidth = '100%',
}) => {
  if (!error) return null;

  const severity = error.severity || 'error';
  const title = error.title || (severity === 'error' ? 'Error' : severity === 'warning' ? 'Warning' : 'Information');
  
  // Convert fieldErrors to validationErrors format
  const validationErrors: ValidationError[] = error.validationErrors || [];
  if (error.fieldErrors) {
    Object.entries(error.fieldErrors).forEach(([field, message]) => {
      validationErrors.push({ field, message });
    });
  }

  const hasMultipleErrors = validationErrors.length > 1;

  return (
    <Box sx={{ maxWidth, mb: 2 }}>
      <Alert
        severity={severity}
        icon={showIcon ? undefined : false}
        action={
          onClose && (
            <IconButton size="small" onClick={onClose} color="inherit">
              <CloseIcon fontSize="small" />
            </IconButton>
          )
        }
        sx={{
          '& .MuiAlert-message': {
            width: '100%',
          },
        }}
      >
        <AlertTitle sx={{ fontWeight: 600 }}>{title}</AlertTitle>
        
        {/* Main message */}
        <Typography variant="body2" sx={{ mb: validationErrors.length > 0 ? 1.5 : 0 }}>
          {error.message}
        </Typography>

        {/* Suggestion if provided */}
        {error.suggestion && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1, fontStyle: 'italic' }}>
            💡 {error.suggestion}
          </Typography>
        )}

        {/* Validation errors list */}
        {validationErrors.length > 0 && (
          <Box sx={{ mt: 1.5 }}>
            <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
              {hasMultipleErrors ? `${validationErrors.length} issues found:` : 'Issue details:'}
            </Typography>
            <List dense={dense} sx={{ mt: 0.5 }}>
              {validationErrors.map((err, index) => (
                <ListItem key={index} sx={{ py: 0.25, px: 1 }}>
                  <ListItemIcon sx={{ minWidth: 28 }}>
                    {severity === 'error' ? (
                      <ErrorIcon color="error" fontSize="small" />
                    ) : severity === 'warning' ? (
                      <WarningIcon color="warning" fontSize="small" />
                    ) : (
                      <InfoIcon color="info" fontSize="small" />
                    )}
                  </ListItemIcon>
                  <ListItemText
                    primary={
                      <Typography variant="body2" component="span">
                        <strong>{getFieldLabel(err.field)}:</strong>{' '}
                        <span style={{ color: 'inherit' }}>{err.message}</span>
                      </Typography>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Box>
        )}

        {/* Error code for debugging */}
        {error.code && (
          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
            Error code: {error.code}
          </Typography>
        )}
      </Alert>
    </Box>
  );
};

// Hook to parse API errors
// eslint-disable-next-line react-refresh/only-export-components
export const useApiError = () => {
  const parseError = (error: any): ErrorDetails | null => {
    if (!error) return null;

    // Axios error with response
    if (error.response?.data) {
      const { data, status } = error.response;
      
      // Validation error (400)
      if (status === 400 && data.details) {
        const fieldErrors: Record<string, string> = data.details;
        
        // Count required field errors
        const requiredErrors = Object.entries(fieldErrors).filter(
          ([_, msg]) => msg.toLowerCase().includes('required') || msg.toLowerCase().includes('must not be blank')
        );
        
        return {
          title: 'Validation Failed',
          message: requiredErrors.length > 0 
            ? `Please fill in all required fields (${requiredErrors.length} missing)`
            : data.message || 'Please correct the following issues:',
          severity: 'error',
          fieldErrors,
          suggestion: requiredErrors.length > 0 
            ? 'Required fields are marked with *'
            : undefined,
        };
      }

      // Not found (404)
      if (status === 404) {
        return {
          title: 'Not Found',
          message: data.message || 'The requested resource was not found.',
          severity: 'warning',
          code: '404',
        };
      }

      // Forbidden (403)
      if (status === 403) {
        return {
          title: 'Access Denied',
          message: data.message || 'You do not have permission to perform this action.',
          severity: 'warning',
          code: '403',
          suggestion: 'Contact your administrator if you need access.',
        };
      }

      // Server error (500)
      if (status >= 500) {
        return {
          title: 'Server Error',
          message: data.message || 'An unexpected error occurred on the server.',
          severity: 'error',
          code: status.toString(),
          suggestion: 'Please try again later or contact support if the problem persists.',
        };
      }

      // Generic error
      return {
        title: data.error || 'Error',
        message: data.message || 'An error occurred.',
        severity: 'error',
        code: status?.toString(),
      };
    }

    // Network error
    if (error.message?.includes('Network Error')) {
      return {
        title: 'Connection Error',
        message: 'Unable to connect to the server.',
        severity: 'error',
        suggestion: 'Please check your internet connection and try again.',
      };
    }

    // Generic error
    return {
      title: 'Error',
      message: error.message || 'An unexpected error occurred.',
      severity: 'error',
    };
  };

  return { parseError };
};

// Simple error message component for inline use
export const InlineError: React.FC<{ message: string | null; field?: string }> = ({ message, field }) => {
  if (!message) return null;
  
  return (
    <Typography 
      variant="caption" 
      color="error" 
      sx={{ 
        display: 'flex', 
        alignItems: 'center', 
        mt: 0.5,
        gap: 0.5 
      }}
    >
      <ErrorIcon fontSize="inherit" />
      {field && <strong>{getFieldLabel(field)}: </strong>}
      {message}
    </Typography>
  );
};

export default ErrorDisplay;

/**
 * ConfirmDialog Component - Confirmation modal for destructive actions
 * Design System 2.0
 */

import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  IconButton,
  Box,
  Typography,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Close as CloseIcon,
  Warning as WarningIcon,
  Delete as DeleteIcon,
  Help as HelpIcon,
} from '@mui/icons-material';

// =============================================================================
// TYPES
// =============================================================================

export type ConfirmDialogVariant = 'danger' | 'warning' | 'info';

export interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: ConfirmDialogVariant;
  loading?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  onClose?: () => void;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'info',
  loading = false,
  onConfirm,
  onCancel,
  onClose,
}) => {
  const theme = useTheme();

  const icons = {
    danger: <DeleteIcon fontSize="large" />,
    warning: <WarningIcon fontSize="large" />,
    info: <HelpIcon fontSize="large" />,
  };

  const colors = {
    danger: {
      icon: theme.palette.error.main,
      bg: alpha(theme.palette.error.main, 0.1),
      button: 'error',
    },
    warning: {
      icon: theme.palette.warning.main,
      bg: alpha(theme.palette.warning.main, 0.1),
      button: 'warning',
    },
    info: {
      icon: theme.palette.primary.main,
      bg: alpha(theme.palette.primary.main, 0.1),
      button: 'primary',
    },
  };

  const color = colors[variant];

  const handleClose = () => {
    onClose?.() || onCancel();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 3,
          overflow: 'hidden',
        },
      }}
    >
      {/* Header */}
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          pb: 2,
          pt: 3,
          px: 3,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Box
            sx={{
              p: 1.5,
              borderRadius: 2,
              backgroundColor: color.bg,
              color: color.icon,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            {icons[variant]}
          </Box>
          <Typography variant="h6" component="span" fontWeight={600}>
            {title}
          </Typography>
        </Box>
        <IconButton
          onClick={handleClose}
          size="small"
          sx={{
            color: 'text.secondary',
            '&:hover': { color: 'text.primary' },
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      {/* Content */}
      <DialogContent sx={{ px: 3, pb: 3 }}>
        <DialogContentText
          sx={{
            color: 'text.primary',
            fontSize: '1rem',
            lineHeight: 1.6,
            ml: variant === 'info' ? 0 : 7, // Align with title if icon present
          }}
        >
          {message}
        </DialogContentText>
      </DialogContent>

      {/* Actions */}
      <DialogActions
        sx={{
          px: 3,
          pb: 3,
          gap: 1,
          ml: variant === 'info' ? 0 : 7,
        }}
      >
        <Button
          onClick={onCancel}
          variant="outlined"
          disabled={loading}
          sx={{
            textTransform: 'none',
            fontWeight: 500,
            px: 3,
          }}
        >
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          variant="contained"
          color={color.button as 'error' | 'warning' | 'primary'}
          disabled={loading}
          autoFocus
          sx={{
            textTransform: 'none',
            fontWeight: 500,
            px: 3,
            boxShadow: 'none',
            '&:hover': {
              boxShadow: 'none',
            },
          }}
        >
          {loading ? 'Processing...' : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

// Convenience exports for common use cases
export const DeleteConfirmDialog: React.FC<
  Omit<ConfirmDialogProps, 'variant' | 'confirmText'>
> = (props) => (
  <ConfirmDialog
    {...props}
    variant="danger"
    confirmText="Delete"
    title={props.title || 'Confirm Deletion'}
    message={
      props.message ||
      'Are you sure you want to delete this item? This action cannot be undone.'
    }
  />
);

export const WarningConfirmDialog: React.FC<
  Omit<ConfirmDialogProps, 'variant'>
> = (props) => (
  <ConfirmDialog {...props} variant="warning" />
);

export default ConfirmDialog;

/**
 * ConfirmDialog Component
 * 
 * Modal dialog for confirming user actions, especially destructive ones.
 * Features keyboard navigation, focus trap, and accessibility support.
 * 
 * @module components/feedback/ConfirmDialog
 */

import React, { useEffect, useRef, useCallback } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  IconButton,
  Fade,
} from '@mui/material';
import {
  Warning as WarningIcon,
  Delete as DeleteIcon,
  Close as CloseIcon,
} from '@mui/icons-material';

/** Props for ConfirmDialog component */
export interface ConfirmDialogProps {
  /** Whether the dialog is open */
  open: boolean;
  /** Dialog title */
  title: string;
  /** Dialog message/content */
  message: string;
  /** Text for confirm button (default: 'Confirm') */
  confirmText?: string;
  /** Text for cancel button (default: 'Cancel') */
  cancelText?: string;
  /** Whether this is a dangerous action (red confirm button) */
  danger?: boolean;
  /** Icon to display (default: Warning for danger, none otherwise) */
  icon?: React.ReactNode;
  /** Callback when confirm is clicked */
  onConfirm: () => void;
  /** Callback when cancel is clicked or dialog is dismissed */
  onCancel: () => void;
  /** Whether to disable buttons during processing */
  loading?: boolean;
  /** Whether the dialog can be dismissed by clicking backdrop */
  disableBackdropClick?: boolean;
}

/**
 * ConfirmDialog Component
 * 
 * A confirmation modal that:
 * - Traps focus within the dialog
 * - Supports keyboard navigation (Enter to confirm, Escape to cancel)
 * - Shows warning styling for destructive actions
 * - Prevents accidental deletions with visual cues
 * 
 * @example
 * ```tsx
 * // Basic usage
 * <ConfirmDialog
 *   open={showDialog}
 *   title="Delete Item"
 *   message="Are you sure you want to delete this item?"
 *   onConfirm={handleDelete}
 *   onCancel={() => setShowDialog(false)}
 * />
 * 
 * // Danger mode for destructive actions
 * <ConfirmDialog
 *   open={showDeleteDialog}
 *   title="Delete User"
 *   message="This action cannot be undone. The user and all their data will be permanently deleted."
 *   confirmText="Delete"
 *   cancelText="Keep"
 *   danger
 *   onConfirm={handleDeleteUser}
 *   onCancel={() => setShowDeleteDialog(false)}
 * />
 * 
 * // With loading state
 * <ConfirmDialog
 *   open={showDialog}
 *   title="Save Changes"
 *   message="Do you want to save your changes?"
 *   confirmText="Save"
 *   loading={isSaving}
 *   onConfirm={handleSave}
 *   onCancel={handleCancel}
 * />
 * ```
 */
export const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  danger = false,
  icon,
  onConfirm,
  onCancel,
  loading = false,
  disableBackdropClick = false,
}) => {
  const confirmButtonRef = useRef<HTMLButtonElement>(null);
  const cancelButtonRef = useRef<HTMLButtonElement>(null);
  const previousActiveElement = useRef<Element | null>(null);

  /**
   * Store the previously focused element when dialog opens
   */
  useEffect(() => {
    if (open) {
      previousActiveElement.current = document.activeElement;
      // Focus confirm button after a short delay for animation
      setTimeout(() => {
        if (danger && cancelButtonRef.current) {
          cancelButtonRef.current.focus();
        } else if (confirmButtonRef.current) {
          confirmButtonRef.current.focus();
        }
      }, 100);
    } else if (previousActiveElement.current instanceof HTMLElement) {
      // Restore focus when dialog closes
      previousActiveElement.current.focus();
    }
  }, [open, danger]);

  /**
   * Handle keyboard events for accessibility
   */
  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.preventDefault();
        onCancel();
      } else if (event.key === 'Enter' && !loading) {
        event.preventDefault();
        onConfirm();
      }
    },
    [onCancel, onConfirm, loading]
  );

  /**
   * Handle backdrop click
   */
  const handleBackdropClick = useCallback(
    (event: React.MouseEvent<HTMLDivElement>) => {
      if (event.target === event.currentTarget && !disableBackdropClick) {
        onCancel();
      }
    },
    [onCancel, disableBackdropClick]
  );

  /**
   * Handle confirm button click
   */
  const handleConfirm = useCallback(() => {
    if (!loading) {
      onConfirm();
    }
  }, [onConfirm, loading]);

  /**
   * Handle cancel button click
   */
  const handleCancel = useCallback(() => {
    if (!loading) {
      onCancel();
    }
  }, [onCancel, loading]);

  // Default icon based on danger prop
  const defaultIcon = danger ? (
    <WarningIcon sx={{ fontSize: 48, color: '#E53935' }} />
  ) : null;

  const displayIcon = icon !== undefined ? icon : defaultIcon;

  return (
    <Dialog
      open={open}
      onClose={loading ? undefined : handleCancel}
      onClick={handleBackdropClick}
      onKeyDown={handleKeyDown}
      maxWidth="sm"
      fullWidth
      TransitionComponent={Fade}
      transitionDuration={200}
      PaperProps={{
        role: 'alertdialog',
        'aria-modal': true,
        'aria-labelledby': 'confirm-dialog-title',
        'aria-describedby': 'confirm-dialog-description',
        sx: {
          borderRadius: 3,
          overflow: 'hidden',
        },
      }}
      sx={{
        '& .MuiBackdrop-root': {
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
        },
      }}
    >
      {/* Header with icon for danger mode */}
      {danger && displayIcon && (
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'center',
            pt: 3,
            pb: 1,
            backgroundColor: 'rgba(229, 57, 53, 0.08)',
          }}
        >
          {displayIcon}
        </Box>
      )}

      {/* Close button for non-danger dialogs */}
      {!danger && (
        <IconButton
          onClick={handleCancel}
          disabled={loading}
          sx={{
            position: 'absolute',
            right: 8,
            top: 8,
            color: '#757575',
          }}
          aria-label="Close dialog"
        >
          <CloseIcon />
        </IconButton>
      )}

      {/* Title */}
      <DialogTitle
        id="confirm-dialog-title"
        sx={{
          pt: danger ? 2 : 3,
          pb: 1,
          textAlign: danger ? 'center' : 'left',
          fontWeight: 600,
          color: danger ? '#E53935' : '#212121',
        }}
      >
        {title}
      </DialogTitle>

      {/* Content */}
      <DialogContent sx={{ pt: 1, pb: 2 }}>
        <Typography
          id="confirm-dialog-description"
          variant="body1"
          sx={{
            color: '#616161',
            textAlign: danger ? 'center' : 'left',
            lineHeight: 1.6,
          }}
        >
          {message}
        </Typography>
      </DialogContent>

      {/* Actions */}
      <DialogActions
        sx={{
          px: 3,
          pb: 3,
          gap: 1,
          justifyContent: danger ? 'center' : 'flex-end',
        }}
      >
        <Button
          ref={cancelButtonRef}
          onClick={handleCancel}
          disabled={loading}
          variant="outlined"
          sx={{
            minWidth: 100,
            textTransform: 'none',
            fontWeight: 500,
            borderColor: '#E0E0E0',
            color: '#616161',
            '&:hover': {
              borderColor: '#BDBDBD',
              backgroundColor: '#F5F5F5',
            },
          }}
        >
          {cancelText}
        </Button>
        
        <Button
          ref={confirmButtonRef}
          onClick={handleConfirm}
          disabled={loading}
          variant="contained"
          startIcon={danger ? <DeleteIcon /> : undefined}
          sx={{
            minWidth: 100,
            textTransform: 'none',
            fontWeight: 600,
            backgroundColor: danger ? '#E53935' : '#4A90D9',
            '&:hover': {
              backgroundColor: danger ? '#C62828' : '#357ABD',
            },
            '&:disabled': {
              backgroundColor: danger ? '#EF9A9A' : '#90CAF9',
            },
          }}
        >
          {loading ? 'Processing...' : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

/**
 * useConfirmDialog Hook
 * 
 * Custom hook for managing confirm dialog state.
 * 
 * @example
 * ```tsx
 * const { isOpen, open, close, config } = useConfirmDialog();
 * 
 * const handleDelete = () => {
 *   open({
 *     title: 'Delete Item',
 *     message: 'Are you sure?',
 *     danger: true,
 *     onConfirm: () => {
 *       deleteItem();
 *       close();
 *     },
 *   });
 * };
 * 
 * return (
 *   <>
 *     <Button onClick={handleDelete}>Delete</Button>
 *     <ConfirmDialog {...config} open={isOpen} onCancel={close} />
 *   </>
 * );
 * ```
 */
export const useConfirmDialog = () => {
  const [isOpen, setIsOpen] = React.useState(false);
  const [config, setConfig] = React.useState<Omit<ConfirmDialogProps, 'open' | 'onCancel'>>({
    title: '',
    message: '',
    onConfirm: () => {},
  });

  const open = React.useCallback(
    (newConfig: Omit<ConfirmDialogProps, 'open' | 'onCancel' | 'onConfirm'> & { onConfirm: () => void }) => {
      setConfig(newConfig);
      setIsOpen(true);
    },
    []
  );

  const close = React.useCallback(() => {
    setIsOpen(false);
  }, []);

  return {
    isOpen,
    open,
    close,
    config: {
      ...config,
      onCancel: close,
    },
  };
};

export default ConfirmDialog;

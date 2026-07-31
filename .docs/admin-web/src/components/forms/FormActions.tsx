import React from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Divider,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  Save,
  Cancel,
  Check,
  ArrowBack,
} from '@mui/icons-material';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  textPrimary: '#212121',
  textSecondary: '#757575',
  background: '#F5F5F5',
  card: '#FFFFFF',
};

// Form action button variants
export type ActionVariant = 'save' | 'cancel' | 'submit' | 'reset' | 'back';

// Individual action button config
export interface ActionButton {
  /** Button type */
  type?: 'button' | 'submit' | 'reset';
  /** Button variant */
  variant?: 'text' | 'outlined' | 'contained';
  /** Button color */
  color?: 'primary' | 'secondary' | 'success' | 'error' | 'info' | 'warning';
  /** Button label */
  label: string;
  /** Button icon */
  icon?: React.ReactNode;
  /** Whether button is loading */
  loading?: boolean;
  /** Whether button is disabled */
  disabled?: boolean;
  /** Click handler */
  onClick?: () => void;
  /** Button position (left or right) */
  position?: 'left' | 'right';
}

// Form actions props
export interface FormActionsProps {
  /** Save button loading state */
  isSaving?: boolean;
  /** Cancel button loading state */
  isCanceling?: boolean;
  /** Whether save button is disabled */
  saveDisabled?: boolean;
  /** Whether cancel button is disabled */
  cancelDisabled?: boolean;
  /** Save button text */
  saveText?: string;
  /** Cancel button text */
  cancelText?: string;
  /** Whether to show save button */
  showSave?: boolean;
  /** Whether to show cancel button */
  showCancel?: boolean;
  /** Whether to show back button */
  showBack?: boolean;
  /** Back button text */
  backText?: string;
  /** Custom save button icon */
  saveIcon?: React.ReactNode;
  /** Custom cancel button icon */
  cancelIcon?: React.ReactNode;
  /** Custom back button icon */
  backIcon?: React.ReactNode;
  /** Save button click handler */
  onSave?: () => void;
  /** Cancel button click handler */
  onCancel?: () => void;
  /** Back button click handler */
  onBack?: () => void;
  /** Custom action buttons */
  customActions?: ActionButton[];
  /** Whether to show divider above actions */
  showDivider?: boolean;
  /** Custom class name */
  className?: string;
  /** Alignment of buttons */
  align?: 'left' | 'center' | 'right';
  /** Spacing between buttons */
  spacing?: number;
  /** Whether to stack buttons on mobile */
  stackOnMobile?: boolean;
  /** Full width buttons on mobile */
  fullWidthMobile?: boolean;
}

/**
 * Form Actions Component
 * 
 * A reusable component for form action buttons (Save, Cancel, Back, Custom).
 * Handles loading states, disabled states, and responsive layout.
 * 
 * @example
 * ```tsx
 * // Basic usage
 * <FormActions
 *   onSave={handleSave}
 *   onCancel={handleCancel}
 *   isSaving={isSubmitting}
 * />
 * 
 * // With custom actions
 * <FormActions
 *   customActions={[
 *     { label: 'Draft', variant: 'outlined', onClick: saveDraft },
 *     { label: 'Publish', variant: 'contained', onClick: publish, loading: isPublishing },
 *   ]}
 * />
 * ```
 */
export function FormActions({
  isSaving = false,
  isCanceling = false,
  saveDisabled = false,
  cancelDisabled = false,
  saveText = 'Save',
  cancelText = 'Cancel',
  showSave = true,
  showCancel = true,
  showBack = false,
  backText = 'Back',
  saveIcon = <Save />,
  cancelIcon = <Cancel />,
  backIcon = <ArrowBack />,
  onSave,
  onCancel,
  onBack,
  customActions = [],
  showDivider = true,
  className,
  align = 'right',
  spacing = 2,
  stackOnMobile = false,
  fullWidthMobile = false,
}: FormActionsProps): React.ReactElement {
  
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  // Determine flex direction and alignment
  const flexDirection = isMobile && stackOnMobile ? 'column-reverse' : 'row';
  const justifyContent = align === 'left' 
    ? 'flex-start' 
    : align === 'center' 
    ? 'center' 
    : 'flex-end';

  // Render default save button
  const renderSaveButton = () => (
    <Button
      type="submit"
      variant="contained"
      color="primary"
      disabled={saveDisabled || isSaving}
      onClick={onSave}
      startIcon={isSaving ? <CircularProgress size={18} color="inherit" /> : saveIcon}
      fullWidth={isMobile && fullWidthMobile}
      sx={{
        backgroundColor: colors.primary,
        '&:hover': {
          backgroundColor: '#3A7BC8',
        },
        '&.Mui-disabled': {
          backgroundColor: 'rgba(74, 144, 217, 0.5)',
          color: 'white',
        },
        minWidth: 100,
      }}
    >
      {isSaving ? 'Saving...' : saveText}
    </Button>
  );

  // Render default cancel button
  const renderCancelButton = () => (
    <Button
      type="button"
      variant="outlined"
      color="inherit"
      disabled={cancelDisabled || isCanceling}
      onClick={onCancel}
      startIcon={isCanceling ? <CircularProgress size={18} /> : cancelIcon}
      fullWidth={isMobile && fullWidthMobile}
      sx={{
        borderColor: colors.textSecondary,
        color: colors.textPrimary,
        '&:hover': {
          borderColor: colors.primary,
          color: colors.primary,
          backgroundColor: 'rgba(74, 144, 217, 0.04)',
        },
        minWidth: 100,
      }}
    >
      {cancelText}
    </Button>
  );

  // Render back button
  const renderBackButton = () => (
    <Button
      type="button"
      variant="text"
      color="inherit"
      onClick={onBack}
      startIcon={backIcon}
      fullWidth={isMobile && fullWidthMobile}
      sx={{
        color: colors.textSecondary,
        '&:hover': {
          color: colors.primary,
          backgroundColor: 'rgba(74, 144, 217, 0.04)',
        },
      }}
    >
      {backText}
    </Button>
  );

  // Render custom action button
  const renderCustomButton = (action: ActionButton, index: number) => {
    const buttonColors: Record<string, string> = {
      primary: colors.primary,
      success: colors.success,
      error: colors.error,
    };

    return (
      <Button
        key={index}
        type={action.type || 'button'}
        variant={action.variant || 'contained'}
        color={action.color || 'primary'}
        disabled={action.disabled || action.loading}
        onClick={action.onClick}
        startIcon={action.loading ? <CircularProgress size={18} color="inherit" /> : action.icon}
        fullWidth={isMobile && fullWidthMobile}
        sx={{
          minWidth: 100,
          ...(action.variant === 'contained' && {
            backgroundColor: buttonColors[action.color || 'primary'],
          }),
        }}
      >
        {action.loading ? `${action.label}...` : action.label}
      </Button>
    );
  };

  // Separate left and right positioned actions
  const leftActions = customActions.filter((a) => a.position === 'left');
  const rightActions = customActions.filter((a) => a.position !== 'left');

  return (
    <Box className={className}>
      {showDivider && (
        <Divider sx={{ mb: spacing }} />
      )}
      
      <Box
        sx={{
          display: 'flex',
          flexDirection,
          justifyContent,
          alignItems: 'center',
          gap: spacing,
          flexWrap: 'wrap',
        }}
      >
        {/* Left side actions */}
        {(showBack || leftActions.length > 0) && (
          <Box
            sx={{
              display: 'flex',
              flexDirection,
              gap: spacing,
              mr: align === 'right' ? 'auto' : undefined,
            }}
          >
            {showBack && renderBackButton()}
            {leftActions.map((action, index) => renderCustomButton(action, index))}
          </Box>
        )}

        {/* Right side actions */}
        <Box
          sx={{
            display: 'flex',
            flexDirection,
            gap: spacing,
          }}
        >
          {showCancel && renderCancelButton()}
          {rightActions.map((action, index) => renderCustomButton(action, index))}
          {showSave && renderSaveButton()}
        </Box>
      </Box>
    </Box>
  );
}

// Preset configurations for common use cases
export const FormActionPresets = {
  /** Standard save/cancel actions */
  standard: (onSave: () => void, onCancel: () => void, isSaving = false): FormActionsProps => ({
    onSave,
    onCancel,
    isSaving,
  }),

  /** Create mode actions */
  create: (onSave: () => void, onCancel: () => void, isSaving = false): FormActionsProps => ({
    onSave,
    onCancel,
    isSaving,
    saveText: 'Create',
    saveIcon: <Check />,
  }),

  /** Edit mode actions */
  edit: (onSave: () => void, onCancel: () => void, onBack: () => void, isSaving = false): FormActionsProps => ({
    onSave,
    onCancel,
    onBack,
    isSaving,
    showBack: true,
    saveText: 'Update',
  }),

  /** Delete confirmation actions */
  delete: (onDelete: () => void, onCancel: () => void, isDeleting = false): FormActionsProps => ({
    onSave: onDelete,
    onCancel,
    isSaving: isDeleting,
    saveText: 'Delete',
    saveIcon: <Check />,
    customActions: [
      {
        label: 'Delete',
        variant: 'contained',
        color: 'error',
        loading: isDeleting,
        onClick: onDelete,
        position: 'right',
      },
    ],
    showSave: false,
  }),
};

export default FormActions;

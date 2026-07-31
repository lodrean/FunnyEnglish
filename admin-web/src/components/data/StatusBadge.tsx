/**
 * StatusBadge Component - Status indicator with customizable variants
 * Design System 2.0
 */

import React from 'react';
import { Chip, alpha, useTheme, ChipProps } from '@mui/material';

// =============================================================================
// TYPES
// =============================================================================

export type StatusVariant =
  | 'success'
  | 'error'
  | 'warning'
  | 'info'
  | 'primary'
  | 'secondary'
  | 'default'
  | 'draft'
  | 'published'
  | 'archived'
  | 'active'
  | 'inactive'
  | 'pending';

export interface StatusBadgeProps extends Omit<ChipProps, 'color'> {
  status: StatusVariant;
  label?: string;
  size?: 'small' | 'medium';
  variant?: 'filled' | 'outlined' | 'light';
  dot?: boolean;
}

// =============================================================================
// STATUS CONFIGURATION
// =============================================================================

const statusConfig: Record<
  StatusVariant,
  { label: string; color: string; lightColor: string }
> = {
  // Semantic statuses
  success: { label: 'Success', color: '#43A047', lightColor: '#E8F5E9' },
  error: { label: 'Error', color: '#E53935', lightColor: '#FFEBEE' },
  warning: { label: 'Warning', color: '#FB8C00', lightColor: '#FFF3E0' },
  info: { label: 'Info', color: '#2196F3', lightColor: '#E3F2FD' },
  primary: { label: 'Primary', color: '#4A90D9', lightColor: '#E3F2FD' },
  secondary: { label: 'Secondary', color: '#9C27B0', lightColor: '#F3E5F5' },
  default: { label: 'Default', color: '#757575', lightColor: '#F5F5F5' },

  // Content statuses
  draft: { label: 'Draft', color: '#757575', lightColor: '#F5F5F5' },
  published: { label: 'Published', color: '#43A047', lightColor: '#E8F5E9' },
  archived: { label: 'Archived', color: '#9E9E9E', lightColor: '#EEEEEE' },

  // User statuses
  active: { label: 'Active', color: '#43A047', lightColor: '#E8F5E9' },
  inactive: { label: 'Inactive', color: '#757575', lightColor: '#F5F5F5' },
  pending: { label: 'Pending', color: '#FB8C00', lightColor: '#FFF3E0' },
};

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  label,
  size = 'small',
  variant = 'light',
  dot = false,
  sx,
  ...chipProps
}) => {
  const theme = useTheme();
  const config = statusConfig[status];
  const displayLabel = label || config.label;

  const getStyles = () => {
    const baseStyles = {
      fontWeight: 600,
      borderRadius: size === 'small' ? 1 : 1.5,
      height: size === 'small' ? 24 : 32,
      fontSize: size === 'small' ? '0.75rem' : '0.875rem',
    };

    switch (variant) {
      case 'filled':
        return {
          ...baseStyles,
          backgroundColor: config.color,
          color: '#FFFFFF',
          '&:hover': {
            backgroundColor: config.color,
          },
        };

      case 'outlined':
        return {
          ...baseStyles,
          backgroundColor: 'transparent',
          color: config.color,
          border: `1.5px solid ${config.color}`,
          '&:hover': {
            backgroundColor: alpha(config.color, 0.08),
          },
        };

      case 'light':
      default:
        return {
          ...baseStyles,
          backgroundColor:
            theme.palette.mode === 'dark'
              ? alpha(config.color, 0.2)
              : config.lightColor,
          color:
            theme.palette.mode === 'dark' ? config.color : config.color,
          '&:hover': {
            backgroundColor:
              theme.palette.mode === 'dark'
                ? alpha(config.color, 0.3)
                : alpha(config.color, 0.15),
          },
        };
    }
  };

  const icon = dot ? (
    <span
      style={{
        width: size === 'small' ? 6 : 8,
        height: size === 'small' ? 6 : 8,
        borderRadius: '50%',
        backgroundColor: variant === 'filled' ? '#FFFFFF' : config.color,
        display: 'inline-block',
        marginRight: 4,
      }}
    />
  ) : undefined;

  return (
    <Chip
      label={displayLabel}
      size={size}
      icon={icon}
      sx={{
        ...getStyles(),
        ...sx,
      }}
      {...chipProps}
    />
  );
};

// Convenience exports for common use cases
export const PublishedBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (props) => (
  <StatusBadge status="published" {...props} />
);

export const DraftBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (props) => (
  <StatusBadge status="draft" {...props} />
);

export const ActiveBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (props) => (
  <StatusBadge status="active" {...props} />
);

export const InactiveBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (props) => (
  <StatusBadge status="inactive" {...props} />
);

export default StatusBadge;

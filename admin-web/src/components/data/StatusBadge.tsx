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

export interface StatusBadgeProps extends Omit<ChipProps, 'color' | 'variant'> {
  status: StatusVariant;
  label?: string;
  size?: 'small' | 'medium';
  variant?: 'filled' | 'outlined' | 'light';
  dot?: boolean;
}

// =============================================================================
// STATUS CONFIGURATION
// Цвета — ТОЛЬКО theme.palette.speaking.status (bd ku2, tokens.json v1.3.2):
// tone — основной ключ статуса, контейнер = `<tone>Container`.
// =============================================================================

type StatusTone = 'new' | 'reviewed' | 'error' | 'info' | 'secondary' | 'neutral' | 'archived';

const statusConfig: Record<StatusVariant, { label: string; tone: StatusTone }> = {
  // Semantic statuses
  success: { label: 'Success', tone: 'reviewed' },
  error: { label: 'Error', tone: 'error' },
  warning: { label: 'Warning', tone: 'new' },
  info: { label: 'Info', tone: 'info' },
  primary: { label: 'Primary', tone: 'info' },
  secondary: { label: 'Secondary', tone: 'secondary' },
  default: { label: 'Default', tone: 'neutral' },

  // Content statuses
  draft: { label: 'Draft', tone: 'neutral' },
  published: { label: 'Published', tone: 'reviewed' },
  archived: { label: 'Archived', tone: 'archived' },

  // User statuses
  active: { label: 'Active', tone: 'reviewed' },
  inactive: { label: 'Inactive', tone: 'neutral' },
  pending: { label: 'Pending', tone: 'new' },
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
  const statusColors = theme.palette.speaking.status;
  const color = statusColors[config.tone];
  const containerColor = statusColors[`${config.tone}Container` as `${StatusTone}Container`];

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
          backgroundColor: color,
          color: theme.palette.getContrastText(color),
          '&:hover': {
            backgroundColor: color,
          },
        };

      case 'outlined':
        return {
          ...baseStyles,
          backgroundColor: 'transparent',
          color,
          border: `1.5px solid ${color}`,
          '&:hover': {
            backgroundColor: alpha(color, 0.08),
          },
        };

      case 'light':
      default:
        return {
          ...baseStyles,
          backgroundColor:
            theme.palette.mode === 'dark' ? alpha(color, 0.2) : containerColor,
          color,
          '&:hover': {
            backgroundColor:
              theme.palette.mode === 'dark' ? alpha(color, 0.3) : alpha(color, 0.15),
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
        backgroundColor: variant === 'filled' ? theme.palette.getContrastText(color) : color,
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

/**
 * StatusBadge Component
 * Status indicators using MUI Chip with custom colors per status
 */
import React from 'react';
import { Chip, styled, ChipProps } from '@mui/material';

// Design System Colors
const COLORS = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  textPrimary: '#212121',
  textSecondary: '#757575',
};

// Type Definitions
export type StatusVariant =
  | 'active'
  | 'inactive'
  | 'pending'
  | 'error'
  | 'success'
  | 'warning'
  | 'info'
  | 'default';

export interface StatusBadgeProps extends Omit<ChipProps, 'color'> {
  status: StatusVariant;
  label?: string;
  size?: 'small' | 'medium';
  variant?: 'filled' | 'outlined';
  showDot?: boolean;
}

// Status configuration with colors and default labels
interface StatusConfig {
  color: string;
  backgroundColor: string;
  borderColor: string;
  defaultLabel: string;
}

const statusConfig: Record<StatusVariant, StatusConfig> = {
  active: {
    color: COLORS.success,
    backgroundColor: 'rgba(67, 160, 71, 0.12)',
    borderColor: 'rgba(67, 160, 71, 0.3)',
    defaultLabel: 'Active',
  },
  inactive: {
    color: COLORS.textSecondary,
    backgroundColor: 'rgba(117, 117, 117, 0.12)',
    borderColor: 'rgba(117, 117, 117, 0.3)',
    defaultLabel: 'Inactive',
  },
  pending: {
    color: COLORS.warning,
    backgroundColor: 'rgba(251, 140, 0, 0.12)',
    borderColor: 'rgba(251, 140, 0, 0.3)',
    defaultLabel: 'Pending',
  },
  error: {
    color: COLORS.error,
    backgroundColor: 'rgba(229, 57, 53, 0.12)',
    borderColor: 'rgba(229, 57, 53, 0.3)',
    defaultLabel: 'Error',
  },
  success: {
    color: COLORS.success,
    backgroundColor: 'rgba(67, 160, 71, 0.12)',
    borderColor: 'rgba(67, 160, 71, 0.3)',
    defaultLabel: 'Success',
  },
  warning: {
    color: COLORS.warning,
    backgroundColor: 'rgba(251, 140, 0, 0.12)',
    borderColor: 'rgba(251, 140, 0, 0.3)',
    defaultLabel: 'Warning',
  },
  info: {
    color: COLORS.info,
    backgroundColor: 'rgba(33, 150, 243, 0.12)',
    borderColor: 'rgba(33, 150, 243, 0.3)',
    defaultLabel: 'Info',
  },
  default: {
    color: COLORS.textSecondary,
    backgroundColor: 'rgba(117, 117, 117, 0.12)',
    borderColor: 'rgba(117, 117, 117, 0.3)',
    defaultLabel: 'Default',
  },
};

// Status Dot Component
const StatusDot = styled('span')<{ color: string }>(({ color }) => ({
  width: 8,
  height: 8,
  borderRadius: '50%',
  backgroundColor: color,
  display: 'inline-block',
  marginRight: 6,
  flexShrink: 0,
}));

// Styled Chip Component
const StyledChip = styled(Chip)<{
  statuscolor: string;
  statusbgcolor: string;
  statusbordercolor: string;
  variantType: 'filled' | 'outlined';
}>(({ statuscolor, statusbgcolor, statusbordercolor, variantType, theme }) => ({
  fontWeight: 600,
  fontSize: '0.75rem',
  height: 28,
  borderRadius: 14,
  ...(variantType === 'filled' && {
    backgroundColor: statusbgcolor,
    color: statuscolor,
    '&:hover': {
      backgroundColor: statusbgcolor,
    },
  }),
  ...(variantType === 'outlined' && {
    backgroundColor: 'transparent',
    color: statuscolor,
    border: `1px solid ${statusbordercolor}`,
    '&:hover': {
      backgroundColor: statusbgcolor,
    },
  }),
  '& .MuiChip-label': {
    paddingLeft: 10,
    paddingRight: 10,
  },
  '& .MuiChip-icon': {
    color: statuscolor,
  },
}));

// Main Component
const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  label,
  size = 'small',
  variant = 'filled',
  showDot = true,
  ...chipProps
}) => {
  const config = statusConfig[status];
  const displayLabel = label || config.defaultLabel;

  // Create icon with dot if showDot is true
  const icon = showDot ? (
    <StatusDot color={config.color} />
  ) : undefined;

  return (
    <StyledChip
      label={displayLabel}
      size={size}
      icon={icon}
      statuscolor={config.color}
      statusbgcolor={config.backgroundColor}
      statusbordercolor={config.borderColor}
      variantType={variant}
      {...chipProps}
    />
  );
};

// Preset components for common use cases
export const ActiveBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (
  props
) => <StatusBadge status="active" {...props} />;

export const InactiveBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (
  props
) => <StatusBadge status="inactive" {...props} />;

export const PendingBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (
  props
) => <StatusBadge status="pending" {...props} />;

export const ErrorBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (
  props
) => <StatusBadge status="error" {...props} />;

export const SuccessBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (
  props
) => <StatusBadge status="success" {...props} />;

export const WarningBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (
  props
) => <StatusBadge status="warning" {...props} />;

export const InfoBadge: React.FC<Omit<StatusBadgeProps, 'status'>> = (props) => (
  <StatusBadge status="info" {...props} />
);

export default StatusBadge;

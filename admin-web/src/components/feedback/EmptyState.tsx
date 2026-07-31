/**
 * EmptyState Component - Empty list placeholder with call-to-action
 * Design System 2.0
 */

import React from 'react';
import {
  Box,
  Typography,
  Button,
  alpha,
  useTheme,
} from '@mui/material';
import {
  Inbox as InboxIcon,
  Search as SearchIcon,
  FolderOpen as FolderOpenIcon,
  Add as AddIcon,
} from '@mui/icons-material';

// =============================================================================
// TYPES
// =============================================================================

export type EmptyStateVariant = 'default' | 'search' | 'folder';

export interface EmptyStateProps {
  title?: string;
  message?: string;
  variant?: EmptyStateVariant;
  icon?: React.ReactNode;
  action?: {
    label: string;
    onClick: () => void;
    icon?: React.ReactNode;
  };
  secondaryAction?: {
    label: string;
    onClick: () => void;
  };
  size?: 'small' | 'medium' | 'large';
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const EmptyState: React.FC<EmptyStateProps> = ({
  title,
  message,
  variant = 'default',
  icon,
  action,
  secondaryAction,
  size = 'medium',
}) => {
  const theme = useTheme();

  const icons = {
    default: <InboxIcon />,
    search: <SearchIcon />,
    folder: <FolderOpenIcon />,
  };

  const defaultTitles = {
    default: 'No data available',
    search: 'No results found',
    folder: 'Folder is empty',
  };

  const defaultMessages = {
    default: 'There are no records to display at this time.',
    search: 'Try adjusting your search or filters to find what you\'re looking for.',
    folder: 'This folder doesn\'t contain any items yet.',
  };

  const displayTitle = title || defaultTitles[variant];
  const displayMessage = message || defaultMessages[variant];
  const IconComponent = icon || icons[variant];

  const sizes = {
    small: {
      icon: 40,
      title: 'h6',
      message: 'body2',
      py: 4,
    },
    medium: {
      icon: 64,
      title: 'h5',
      message: 'body1',
      py: 6,
    },
    large: {
      icon: 96,
      title: 'h4',
      message: 'h6',
      py: 8,
    },
  };

  const currentSize = sizes[size];

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        py: currentSize.py,
        px: 4,
        textAlign: 'center',
      }}
    >
      {/* Icon */}
      <Box
        sx={{
          width: currentSize.icon,
          height: currentSize.icon,
          borderRadius: '50%',
          backgroundColor: alpha(theme.palette.action.hover, 0.5),
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          mb: 3,
          color: 'text.secondary',
          '& > svg': {
            fontSize: currentSize.icon * 0.5,
          },
        }}
      >
        {IconComponent}
      </Box>

      {/* Title */}
      <Typography
        variant={currentSize.title as 'h4' | 'h5' | 'h6'}
        fontWeight={600}
        color="text.primary"
        gutterBottom
      >
        {displayTitle}
      </Typography>

      {/* Message */}
      <Typography
        variant={currentSize.message as 'body1' | 'body2' | 'h6'}
        color="text.secondary"
        sx={{
          maxWidth: 400,
          mb: action || secondaryAction ? 3 : 0,
          lineHeight: 1.6,
        }}
      >
        {displayMessage}
      </Typography>

      {/* Actions */}
      {(action || secondaryAction) && (
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            flexWrap: 'wrap',
            justifyContent: 'center',
          }}
        >
          {action && (
            <Button
              variant="contained"
              onClick={action.onClick}
              startIcon={action.icon || <AddIcon />}
              sx={{
                textTransform: 'none',
                fontWeight: 500,
              }}
            >
              {action.label}
            </Button>
          )}
          {secondaryAction && (
            <Button
              variant="outlined"
              onClick={secondaryAction.onClick}
              sx={{
                textTransform: 'none',
                fontWeight: 500,
              }}
            >
              {secondaryAction.label}
            </Button>
          )}
        </Box>
      )}
    </Box>
  );
};

// Convenience exports for common use cases
export const SearchEmptyState: React.FC<Omit<EmptyStateProps, 'variant'>> = (props) => (
  <EmptyState {...props} variant="search" />
);

export const FolderEmptyState: React.FC<Omit<EmptyStateProps, 'variant'>> = (props) => (
  <EmptyState {...props} variant="folder" />
);

export default EmptyState;

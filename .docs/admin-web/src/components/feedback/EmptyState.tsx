/**
 * EmptyState Component
 * 
 * Displays a friendly empty state for lists, search results, or error states.
 * Includes customizable icon, title, description, and optional action button.
 * 
 * @module components/feedback/EmptyState
 */

import React from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  SxProps,
  Theme,
} from '@mui/material';
import {
  Search as SearchIcon,
  Inbox as InboxIcon,
  ErrorOutline as ErrorIcon,
  FolderOpen as FolderOpenIcon,
  Add as AddIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';

/** Empty state variant types */
export type EmptyStateVariant = 'search' | 'data' | 'error' | 'custom';

/** Props for EmptyState component */
export interface EmptyStateProps {
  /** Predefined variant or custom configuration */
  variant?: EmptyStateVariant;
  /** Custom icon (overrides variant icon) */
  icon?: React.ReactNode;
  /** Title text (overrides variant title) */
  title?: string;
  /** Description text (overrides variant description) */
  description?: string;
  /** Action button text */
  actionText?: string;
  /** Action button click handler */
  onAction?: () => void;
  /** Secondary action button text */
  secondaryActionText?: string;
  /** Secondary action button click handler */
  onSecondaryAction?: () => void;
  /** Custom icon color */
  iconColor?: string;
  /** Custom icon background color */
  iconBgColor?: string;
  /** Whether to show in a Paper container */
  paper?: boolean;
  /** Minimum height */
  minHeight?: number | string;
  /** Custom styles */
  sx?: SxProps<Theme>;
  /** Size variant */
  size?: 'small' | 'medium' | 'large';
}

/** Default configurations for each variant */
const variantConfigs: Record<
  EmptyStateVariant,
  { icon: React.ElementType; title: string; description: string; iconColor: string; iconBgColor: string }
> = {
  search: {
    icon: SearchIcon,
    title: 'No Results Found',
    description: 'We couldn\'t find any items matching your search. Try different keywords or filters.',
    iconColor: '#757575',
    iconBgColor: '#F5F5F5',
  },
  data: {
    icon: InboxIcon,
    title: 'No Data Available',
    description: 'There are no items to display yet. Create your first item to get started.',
    iconColor: '#4A90D9',
    iconBgColor: 'rgba(74, 144, 217, 0.1)',
  },
  error: {
    icon: ErrorIcon,
    title: 'Something Went Wrong',
    description: 'We encountered an error while loading the data. Please try again.',
    iconColor: '#E53935',
    iconBgColor: 'rgba(229, 57, 53, 0.1)',
  },
  custom: {
    icon: FolderOpenIcon,
    title: 'Nothing to Show',
    description: 'There is no content to display at this time.',
    iconColor: '#757575',
    iconBgColor: '#F5F5F5',
  },
};

/** Size configurations */
const sizeConfigs = {
  small: {
    iconSize: 48,
    iconBoxSize: 80,
    titleVariant: 'h6' as const,
    descVariant: 'body2' as const,
    spacing: 2,
  },
  medium: {
    iconSize: 64,
    iconBoxSize: 100,
    titleVariant: 'h5' as const,
    descVariant: 'body1' as const,
    spacing: 3,
  },
  large: {
    iconSize: 80,
    iconBoxSize: 120,
    titleVariant: 'h4' as const,
    descVariant: 'body1' as const,
    spacing: 4,
  },
};

/**
 * EmptyState Component
 * 
 * Displays an empty state with customizable content for various scenarios:
 * - Search: No search results
 * - Data: Empty list, no items yet
 * - Error: Failed to load data
 * - Custom: Fully customizable
 * 
 * @example
 * ```tsx
 * // Search empty state
 * <EmptyState variant="search" />
 * 
 * // Data empty state with action
 * <EmptyState
 *   variant="data"
 *   actionText="Create Item"
 *   onAction={() => navigate('/create')}
 * />
 * 
 * // Error state with retry
 * <EmptyState
 *   variant="error"
 *   actionText="Try Again"
 *   onAction={refetch}
 * />
 * 
 * // Custom empty state
 * <EmptyState
 *   icon={<CustomIcon />}
 *   title="Custom Title"
 *   description="Custom description text"
 *   actionText="Do Something"
 *   onAction={handleAction}
 * />
 * ```
 */
export const EmptyState: React.FC<EmptyStateProps> = ({
  variant = 'data',
  icon: customIcon,
  title: customTitle,
  description: customDescription,
  actionText,
  onAction,
  secondaryActionText,
  onSecondaryAction,
  iconColor: customIconColor,
  iconBgColor: customIconBgColor,
  paper = false,
  minHeight = 400,
  sx,
  size = 'medium',
}) => {
  const config = variantConfigs[variant];
  const sizeConfig = sizeConfigs[size];

  // Use custom values or fall back to variant defaults
  const Icon = config.icon;
  const title = customTitle ?? config.title;
  const description = customDescription ?? config.description;
  const iconColor = customIconColor ?? config.iconColor;
  const iconBgColor = customIconBgColor ?? config.iconBgColor;

  // Determine action icon based on variant
  const getActionIcon = () => {
    if (variant === 'error') return <RefreshIcon />;
    if (variant === 'data') return <AddIcon />;
    return undefined;
  };

  const content = (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign: 'center',
        minHeight,
        p: sizeConfig.spacing * 2,
        ...sx,
      }}
    >
      {/* Icon Container */}
      <Box
        sx={{
          width: sizeConfig.iconBoxSize,
          height: sizeConfig.iconBoxSize,
          borderRadius: '50%',
          backgroundColor: iconBgColor,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          mb: sizeConfig.spacing,
        }}
      >
        {customIcon || (
          <Icon
            sx={{
              fontSize: sizeConfig.iconSize,
              color: iconColor,
            }}
          />
        )}
      </Box>

      {/* Title */}
      <Typography
        variant={sizeConfig.titleVariant}
        component="h2"
        sx={{
          fontWeight: 600,
          color: '#212121',
          mb: 1,
        }}
      >
        {title}
      </Typography>

      {/* Description */}
      <Typography
        variant={sizeConfig.descVariant}
        sx={{
          color: '#757575',
          maxWidth: 400,
          mb: actionText || secondaryActionText ? sizeConfig.spacing : 0,
          lineHeight: 1.6,
        }}
      >
        {description}
      </Typography>

      {/* Action Buttons */}
      {(actionText || secondaryActionText) && (
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            flexWrap: 'wrap',
            justifyContent: 'center',
          }}
        >
          {secondaryActionText && onSecondaryAction && (
            <Button
              variant="outlined"
              onClick={onSecondaryAction}
              sx={{
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
              {secondaryActionText}
            </Button>
          )}
          {actionText && onAction && (
            <Button
              variant="contained"
              startIcon={getActionIcon()}
              onClick={onAction}
              sx={{
                textTransform: 'none',
                fontWeight: 600,
                backgroundColor: variant === 'error' ? '#E53935' : '#4A90D9',
                '&:hover': {
                  backgroundColor: variant === 'error' ? '#C62828' : '#357ABD',
                },
              }}
            >
              {actionText}
            </Button>
          )}
        </Box>
      )}
    </Box>
  );

  // Wrap in Paper if requested
  if (paper) {
    return (
      <Paper
        elevation={1}
        sx={{
          borderRadius: 2,
          overflow: 'hidden',
        }}
      >
        {content}
      </Paper>
    );
  }

  return content;
};

/**
 * SearchEmptyState Component
 * 
 * Pre-configured empty state for search results.
 * 
 * @example
 * ```tsx
 * <SearchEmptyState query={searchQuery} onClear={() => setQuery('')} />
 * ```
 */
export interface SearchEmptyStateProps {
  query: string;
  onClear: () => void;
  sx?: SxProps<Theme>;
}

export const SearchEmptyState: React.FC<SearchEmptyStateProps> = ({
  query,
  onClear,
  sx,
}) => {
  return (
    <EmptyState
      variant="search"
      title={`No results for "${query}"`}
      description="Try checking for typos or using different keywords."
      actionText="Clear Search"
      onAction={onClear}
      sx={sx}
    />
  );
};

/**
 * ErrorEmptyState Component
 * 
 * Pre-configured empty state for error scenarios.
 * 
 * @example
 * ```tsx
 * <ErrorEmptyState onRetry={refetch} />
 * ```
 */
export interface ErrorEmptyStateProps {
  onRetry: () => void;
  message?: string;
  sx?: SxProps<Theme>;
}

export const ErrorEmptyState: React.FC<ErrorEmptyStateProps> = ({
  onRetry,
  message,
  sx,
}) => {
  return (
    <EmptyState
      variant="error"
      description={message}
      actionText="Try Again"
      onAction={onRetry}
      sx={sx}
    />
  );
};

/**
 * CreateEmptyState Component
 * 
 * Pre-configured empty state for creating first item.
 * 
 * @example
 * ```tsx
 * <CreateEmptyState
 *   itemName="Lesson"
 *   onCreate={() => navigate('/lessons/create')}
 * />
 * ```
 */
export interface CreateEmptyStateProps {
  itemName: string;
  onCreate: () => void;
  sx?: SxProps<Theme>;
}

export const CreateEmptyState: React.FC<CreateEmptyStateProps> = ({
  itemName,
  onCreate,
  sx,
}) => {
  return (
    <EmptyState
      variant="data"
      title={`No ${itemName}s Yet`}
      description={`Get started by creating your first ${itemName.toLowerCase()}.`}
      actionText={`Create ${itemName}`}
      onAction={onCreate}
      sx={sx}
    />
  );
};

export default EmptyState;

/**
 * SkeletonCard Component
 * 
 * Loading skeleton placeholders for cards, lists, and other content.
 * Provides visual feedback while content is loading.
 * 
 * @module components/feedback/SkeletonCard
 */

import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Skeleton,
  Grid,
  Paper,
  SxProps,
  Theme,
} from '@mui/material';

/** Skeleton animation type */
export type SkeletonAnimation = 'pulse' | 'wave' | false;

/** Props for SkeletonCard component */
export interface SkeletonCardProps {
  /** Number of skeleton cards to render */
  count?: number;
  /** Whether to show avatar/image section */
  hasImage?: boolean;
  /** Height of image section */
  imageHeight?: number;
  /** Number of text lines */
  lines?: number;
  /** Whether to show action buttons */
  hasActions?: boolean;
  /** Animation type */
  animation?: SkeletonAnimation;
  /** Custom styles */
  sx?: SxProps<Theme>;
  /** Card variant */
  variant?: 'elevation' | 'outlined';
  /** Grid column configuration */
  columns?: { xs?: number; sm?: number; md?: number; lg?: number; xl?: number };
  /** Spacing between cards */
  spacing?: number;
}

/** Props for SkeletonList component */
export interface SkeletonListProps {
  /** Number of list items */
  count?: number;
  /** Whether to show avatar */
  hasAvatar?: boolean;
  /** Avatar size */
  avatarSize?: number;
  /** Number of text lines per item */
  lines?: number;
  /** Animation type */
  animation?: SkeletonAnimation;
  /** Custom styles */
  sx?: SxProps<Theme>;
  /** Whether to show dividers */
  dividers?: boolean;
  /** Item height */
  itemHeight?: number;
}

/** Props for SkeletonTable component */
export interface SkeletonTableProps {
  /** Number of rows */
  rows?: number;
  /** Number of columns */
  columns?: number;
  /** Whether to show header */
  hasHeader?: boolean;
  /** Row height */
  rowHeight?: number;
  /** Animation type */
  animation?: SkeletonAnimation;
  /** Custom styles */
  sx?: SxProps<Theme>;
}

/** Props for SkeletonText component */
export interface SkeletonTextProps {
  /** Number of lines */
  lines?: number;
  /** Width of each line (can be array for different widths) */
  widths?: string[] | string;
  /** Animation type */
  animation?: SkeletonAnimation;
  /** Custom styles */
  sx?: SxProps<Theme>;
}

/**
 * SkeletonCard Component
 * 
 * Card-shaped loading skeleton with configurable sections.
 * 
 * @example
 * ```tsx
 * // Single skeleton card
 * <SkeletonCard />
 * 
 * // Multiple cards in grid
 * <SkeletonCard count={6} columns={{ xs: 1, sm: 2, md: 3 }} />
 * 
 * // Card with image and actions
 * <SkeletonCard hasImage imageHeight={200} hasActions />
 * ```
 */
export const SkeletonCard: React.FC<SkeletonCardProps> = ({
  count = 1,
  hasImage = false,
  imageHeight = 140,
  lines = 3,
  hasActions = false,
  animation = 'pulse',
  sx,
  variant = 'elevation',
  columns = { xs: 1, sm: 2, md: 3 },
  spacing = 3,
}) => {
  const renderSkeletonCard = (index: number) => (
    <Grid item {...columns} key={index}>
      <Card
        variant={variant}
        sx={{
          height: '100%',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {/* Image Section */}
        {hasImage && (
          <Skeleton
            variant="rectangular"
            height={imageHeight}
            animation={animation}
            sx={{
              backgroundColor: 'rgba(0, 0, 0, 0.08)',
            }}
          />
        )}

        <CardContent sx={{ flexGrow: 1 }}>
          {/* Title Line */}
          <Skeleton
            variant="text"
            height={28}
            width="70%"
            animation={animation}
            sx={{ mb: 1, backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
          />

          {/* Content Lines */}
          {Array.from({ length: lines }).map((_, lineIndex) => (
            <Skeleton
              key={lineIndex}
              variant="text"
              height={16}
              width={lineIndex === lines - 1 ? '60%' : '100%'}
              animation={animation}
              sx={{
                mb: lineIndex < lines - 1 ? 0.5 : 0,
                backgroundColor: 'rgba(0, 0, 0, 0.08)',
              }}
            />
          ))}
        </CardContent>

        {/* Actions Section */}
        {hasActions && (
          <Box
            sx={{
              p: 2,
              pt: 0,
              display: 'flex',
              gap: 1,
              justifyContent: 'flex-end',
            }}
          >
            <Skeleton
              variant="rounded"
              width={80}
              height={32}
              animation={animation}
              sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
            />
            <Skeleton
              variant="rounded"
              width={80}
              height={32}
              animation={animation}
              sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
            />
          </Box>
        )}
      </Card>
    </Grid>
  );

  return (
    <Box sx={sx}>
      <Grid container spacing={spacing}>
        {Array.from({ length: count }).map((_, index) => renderSkeletonCard(index))}
      </Grid>
    </Box>
  );
};

/**
 * SkeletonList Component
 * 
 * List-shaped loading skeleton with avatar and text lines.
 * 
 * @example
 * ```tsx
 * // Basic list
 * <SkeletonList count={5} />
 * 
 * // List with avatars
 * <SkeletonList count={5} hasAvatar avatarSize={40} />
 * 
 * // List with dividers
 * <SkeletonList count={5} hasAvatar dividers />
 * ```
 */
export const SkeletonList: React.FC<SkeletonListProps> = ({
  count = 3,
  hasAvatar = true,
  avatarSize = 40,
  lines = 2,
  animation = 'pulse',
  sx,
  dividers = false,
  itemHeight,
}) => {
  return (
    <Box sx={sx}>
      {Array.from({ length: count }).map((_, index) => (
        <React.Fragment key={index}>
          <Box
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 2,
              py: 1.5,
              height: itemHeight,
            }}
          >
            {/* Avatar */}
            {hasAvatar && (
              <Skeleton
                variant="circular"
                width={avatarSize}
                height={avatarSize}
                animation={animation}
                sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)', flexShrink: 0 }}
              />
            )}

            {/* Text Lines */}
            <Box sx={{ flexGrow: 1 }}>
              {Array.from({ length: lines }).map((_, lineIndex) => (
                <Skeleton
                  key={lineIndex}
                  variant="text"
                  height={lineIndex === 0 ? 20 : 16}
                  width={lineIndex === 0 ? '40%' : lineIndex === lines - 1 ? '70%' : '100%'}
                  animation={animation}
                  sx={{
                    mb: lineIndex < lines - 1 ? 0.5 : 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.08)',
                  }}
                />
              ))}
            </Box>

            {/* Action placeholder */}
            <Skeleton
              variant="rounded"
              width={24}
              height={24}
              animation={animation}
              sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)', flexShrink: 0 }}
            />
          </Box>

          {/* Divider */}
          {dividers && index < count - 1 && (
            <Box
              sx={{
                height: 1,
                backgroundColor: 'rgba(0, 0, 0, 0.08)',
                my: 0.5,
              }}
            />
          )}
        </React.Fragment>
      ))}
    </Box>
  );
};

/**
 * SkeletonTable Component
 * 
 * Table-shaped loading skeleton with rows and columns.
 * 
 * @example
 * ```tsx
 * // Basic table skeleton
 * <SkeletonTable rows={5} columns={4} />
 * 
 * // Table without header
 * <SkeletonTable rows={10} columns={3} hasHeader={false} />
 * ```
 */
export const SkeletonTable: React.FC<SkeletonTableProps> = ({
  rows = 5,
  columns = 4,
  hasHeader = true,
  rowHeight = 52,
  animation = 'pulse',
  sx,
}) => {
  return (
    <Paper elevation={0} sx={{ overflow: 'hidden', ...sx }}>
      {/* Header */}
      {hasHeader && (
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            height: rowHeight,
            backgroundColor: 'rgba(0, 0, 0, 0.02)',
            borderBottom: 1,
            borderColor: 'rgba(0, 0, 0, 0.08)',
            px: 2,
          }}
        >
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Box
              key={colIndex}
              sx={{
                flex: colIndex === 0 ? 2 : 1,
                pr: 2,
              }}
            >
              <Skeleton
                variant="text"
                height={20}
                width={colIndex === 0 ? '60%' : '80%'}
                animation={animation}
                sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
              />
            </Box>
          ))}
        </Box>
      )}

      {/* Rows */}
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <Box
          key={rowIndex}
          sx={{
            display: 'flex',
            alignItems: 'center',
            height: rowHeight,
            borderBottom:
              rowIndex < rows - 1 ? '1px solid rgba(0, 0, 0, 0.08)' : undefined,
            px: 2,
          }}
        >
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Box
              key={colIndex}
              sx={{
                flex: colIndex === 0 ? 2 : 1,
                pr: 2,
              }}
            >
              <Skeleton
                variant="text"
                height={16}
                width={colIndex === 0 ? '80%' : colIndex === columns - 1 ? '40%' : '60%'}
                animation={animation}
                sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
              />
            </Box>
          ))}
        </Box>
      ))}
    </Paper>
  );
};

/**
 * SkeletonText Component
 * 
 * Simple text skeleton with multiple lines.
 * 
 * @example
 * ```tsx
 * // Basic text skeleton
 * <SkeletonText lines={4} />
 * 
 * // With custom widths
 * <SkeletonText lines={3} widths={['100%', '80%', '60%']} />
 * ```
 */
export const SkeletonText: React.FC<SkeletonTextProps> = ({
  lines = 3,
  widths,
  animation = 'pulse',
  sx,
}) => {
  const getWidth = (index: number): string => {
    if (typeof widths === 'string') {
      return widths;
    }
    if (Array.isArray(widths) && widths[index]) {
      return widths[index];
    }
    // Default alternating widths
    const defaultWidths = ['100%', '95%', '85%', '90%', '70%'];
    return defaultWidths[index % defaultWidths.length];
  };

  return (
    <Box sx={sx}>
      {Array.from({ length: lines }).map((_, index) => (
        <Skeleton
          key={index}
          variant="text"
          height={index === 0 ? 24 : 16}
          width={getWidth(index)}
          animation={animation}
          sx={{
            mb: index < lines - 1 ? 1 : 0,
            backgroundColor: 'rgba(0, 0, 0, 0.08)',
          }}
        />
      ))}
    </Box>
  );
};

/**
 * SkeletonForm Component
 * 
 * Form-shaped loading skeleton with labels and inputs.
 * 
 * @example
 * ```tsx
 * <SkeletonForm fields={4} />
 * ```
 */
export interface SkeletonFormProps {
  /** Number of form fields */
  fields?: number;
  /** Animation type */
  animation?: SkeletonAnimation;
  /** Custom styles */
  sx?: SxProps<Theme>;
}

export const SkeletonForm: React.FC<SkeletonFormProps> = ({
  fields = 4,
  animation = 'pulse',
  sx,
}) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, ...sx }}>
      {Array.from({ length: fields }).map((_, index) => (
        <Box key={index}>
          {/* Label */}
          <Skeleton
            variant="text"
            height={16}
            width={80 + (index % 3) * 20}
            animation={animation}
            sx={{ mb: 0.5, backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
          />
          {/* Input */}
          <Skeleton
            variant="rounded"
            height={48}
            width="100%"
            animation={animation}
            sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
          />
        </Box>
      ))}

      {/* Submit Button */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
        <Skeleton
          variant="rounded"
          height={40}
          width={120}
          animation={animation}
          sx={{ backgroundColor: 'rgba(0, 0, 0, 0.08)' }}
        />
      </Box>
    </Box>
  );
};

export default SkeletonCard;

/**
 * SkeletonCard Component - Loading placeholder for cards
 * Design System 2.0
 */

import React from 'react';
import {
  Card,
  CardContent,
  Skeleton,
  Box,
  alpha,
  useTheme,
} from '@mui/material';

// =============================================================================
// TYPES
// =============================================================================

export interface SkeletonCardProps {
  rows?: number;
  hasHeader?: boolean;
  hasActions?: boolean;
  height?: number | string;
}

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const SkeletonCard: React.FC<SkeletonCardProps> = ({
  rows = 3,
  hasHeader = true,
  hasActions = false,
  height,
}) => {
  const theme = useTheme();

  return (
    <Card
      sx={{
        height: height || '100%',
        borderRadius: 3,
        boxShadow: theme.shadows[1],
      }}
    >
      <CardContent sx={{ p: 3 }}>
        {/* Header */}
        {hasHeader && (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              mb: 3,
            }}
          >
            <Skeleton variant="text" width={120} height={28} />
            {hasActions && <Skeleton variant="circular" width={32} height={32} />}
          </Box>
        )}

        {/* Content rows */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          {Array.from({ length: rows }).map((_, index) => (
            <Box key={index}>
              <Skeleton
                variant="text"
                width={`${Math.random() * 40 + 40}%`}
                height={20}
                sx={{ mb: 0.5 }}
              />
              <Skeleton
                variant="rectangular"
                height={index === 0 ? 60 : 40}
                sx={{ borderRadius: 1 }}
              />
            </Box>
          ))}
        </Box>
      </CardContent>
    </Card>
  );
};

// =============================================================================
// TABLE SKELETON
// =============================================================================

export interface SkeletonTableProps {
  rows?: number;
  columns?: number;
  hasHeader?: boolean;
}

export const SkeletonTable: React.FC<SkeletonTableProps> = ({
  rows = 5,
  columns = 4,
  hasHeader = true,
}) => {
  const theme = useTheme();

  return (
    <Box sx={{ width: '100%' }}>
      {/* Header */}
      {hasHeader && (
        <Box
          sx={{
            display: 'flex',
            gap: 2,
            mb: 2,
            p: 2,
            backgroundColor: alpha(theme.palette.action.hover, 0.5),
            borderRadius: 1,
          }}
        >
          {Array.from({ length: columns }).map((_, i) => (
            <Skeleton
              key={i}
              variant="text"
              width={`${100 / columns}%`}
              height={24}
            />
          ))}
        </Box>
      )}

      {/* Rows */}
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <Box
          key={rowIndex}
          sx={{
            display: 'flex',
            gap: 2,
            p: 2,
            borderBottom: `1px solid ${theme.palette.divider}`,
            '&:last-child': { borderBottom: 'none' },
          }}
        >
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Skeleton
              key={colIndex}
              variant="text"
              width={`${100 / columns}%`}
              height={20}
            />
          ))}
        </Box>
      ))}
    </Box>
  );
};

// =============================================================================
// STATS SKELETON
// =============================================================================

export const SkeletonStats: React.FC<{ count?: number }> = ({ count = 4 }) => {
  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: {
          xs: '1fr',
          sm: 'repeat(2, 1fr)',
          lg: `repeat(${count}, 1fr)`,
        },
        gap: 3,
      }}
    >
      {Array.from({ length: count }).map((_, i) => (
        <SkeletonCard key={i} hasHeader hasActions />
      ))}
    </Box>
  );
};

export default SkeletonCard;

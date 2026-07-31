/**
 * SkeletonCard Component
 * Loading skeleton for cards with various layouts
 */
import React from 'react';
import {
  Card,
  CardContent,
  Skeleton,
  Box,
  styled,
} from '@mui/material';

// Type Definitions
export type SkeletonCardVariant = 'stats' | 'list' | 'chart' | 'profile' | 'default';

export interface SkeletonCardProps {
  variant?: SkeletonCardVariant;
  count?: number;
  height?: number;
  showAvatar?: boolean;
  lines?: number;
}

// Styled Components
const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  borderRadius: theme.shape.borderRadius * 1.5,
  boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
  overflow: 'hidden',
}));

// Stats Card Skeleton Layout
const StatsSkeleton: React.FC = () => (
  <CardContent sx={{ p: 3 }}>
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      <Skeleton variant="circular" width={56} height={56} />
      <Box sx={{ flex: 1 }}>
        <Skeleton variant="text" width={100} height={20} sx={{ mb: 0.5 }} />
        <Skeleton variant="text" width={80} height={40} />
      </Box>
    </Box>
    <Box sx={{ mt: 2 }}>
      <Skeleton variant="text" width={120} height={20} />
    </Box>
    <Box sx={{ mt: 2, height: 60 }}>
      <Skeleton variant="rectangular" height={60} sx={{ borderRadius: 1 }} />
    </Box>
  </CardContent>
);

// List Card Skeleton Layout
const ListSkeleton: React.FC<{ lines?: number }> = ({ lines = 5 }) => (
  <CardContent sx={{ p: 2 }}>
    <Skeleton variant="text" width={150} height={28} sx={{ mb: 2 }} />
    {Array.from({ length: lines }).map((_, index) => (
      <Box
        key={index}
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 2,
          py: 1.5,
          borderBottom:
            index < lines - 1 ? '1px solid rgba(0,0,0,0.06)' : 'none',
        }}
      >
        <Skeleton variant="circular" width={40} height={40} />
        <Box sx={{ flex: 1 }}>
          <Skeleton variant="text" width="60%" height={20} />
          <Skeleton variant="text" width="40%" height={16} />
        </Box>
        <Skeleton variant="text" width={60} height={20} />
      </Box>
    ))}
  </CardContent>
);

// Chart Card Skeleton Layout
const ChartSkeleton: React.FC = () => (
  <CardContent sx={{ p: 3 }}>
    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
      <Skeleton variant="text" width={150} height={28} />
      <Skeleton variant="text" width={80} height={24} />
    </Box>
    <Skeleton variant="rectangular" height={250} sx={{ borderRadius: 1 }} />
    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 3, mt: 2 }}>
      <Skeleton variant="text" width={60} height={20} />
      <Skeleton variant="text" width={60} height={20} />
      <Skeleton variant="text" width={60} height={20} />
    </Box>
  </CardContent>
);

// Profile Card Skeleton Layout
const ProfileSkeleton: React.FC = () => (
  <CardContent sx={{ p: 3, textAlign: 'center' }}>
    <Skeleton
      variant="circular"
      width={100}
      height={100}
      sx={{ mx: 'auto', mb: 2 }}
    />
    <Skeleton variant="text" width={180} height={28} sx={{ mx: 'auto' }} />
    <Skeleton variant="text" width={120} height={20} sx={{ mx: 'auto', mb: 2 }} />
    <Box sx={{ display: 'flex', justifyContent: 'center', gap: 4, mt: 2 }}>
      <Box sx={{ textAlign: 'center' }}>
        <Skeleton variant="text" width={50} height={28} sx={{ mx: 'auto' }} />
        <Skeleton variant="text" width={60} height={16} sx={{ mx: 'auto' }} />
      </Box>
      <Box sx={{ textAlign: 'center' }}>
        <Skeleton variant="text" width={50} height={28} sx={{ mx: 'auto' }} />
        <Skeleton variant="text" width={60} height={16} sx={{ mx: 'auto' }} />
      </Box>
      <Box sx={{ textAlign: 'center' }}>
        <Skeleton variant="text" width={50} height={28} sx={{ mx: 'auto' }} />
        <Skeleton variant="text" width={60} height={16} sx={{ mx: 'auto' }} />
      </Box>
    </Box>
  </CardContent>
);

// Default Card Skeleton Layout
const DefaultSkeleton: React.FC<{ showAvatar?: boolean; lines?: number }> = ({
  showAvatar = false,
  lines = 3,
}) => (
  <CardContent sx={{ p: 3 }}>
    <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2, mb: 2 }}>
      {showAvatar && <Skeleton variant="circular" width={48} height={48} />}
      <Box sx={{ flex: 1 }}>
        <Skeleton variant="text" width="70%" height={24} />
        <Skeleton variant="text" width="40%" height={16} />
      </Box>
    </Box>
    {Array.from({ length: lines }).map((_, index) => (
      <Skeleton
        key={index}
        variant="text"
        width={index === lines - 1 ? '60%' : '100%'}
        height={18}
        sx={{ mb: 0.5 }}
      />
    ))}
    <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
      <Skeleton variant="rectangular" width={80} height={32} sx={{ borderRadius: 1 }} />
      <Skeleton variant="rectangular" width={80} height={32} sx={{ borderRadius: 1 }} />
    </Box>
  </CardContent>
);

// Main Component
const SkeletonCard: React.FC<SkeletonCardProps> = ({
  variant = 'default',
  count = 1,
  height,
  showAvatar = false,
  lines = 3,
}) => {
  const renderSkeleton = () => {
    switch (variant) {
      case 'stats':
        return <StatsSkeleton />;
      case 'list':
        return <ListSkeleton lines={lines} />;
      case 'chart':
        return <ChartSkeleton />;
      case 'profile':
        return <ProfileSkeleton />;
      case 'default':
      default:
        return <DefaultSkeleton showAvatar={showAvatar} lines={lines} />;
    }
  };

  return (
    <>
      {Array.from({ length: count }).map((_, index) => (
        <StyledCard key={index} sx={{ height }}>
          {renderSkeleton()}
        </StyledCard>
      ))}
    </>
  );
};

// Table Row Skeleton for DataTable loading states
export interface TableRowSkeletonProps {
  columns: number;
  rows?: number;
  showCheckbox?: boolean;
}

export const TableRowSkeleton: React.FC<TableRowSkeletonProps> = ({
  columns,
  rows = 5,
  showCheckbox = false,
}) => {
  return (
    <>
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <Box
          key={rowIndex}
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 2,
            py: 1.5,
            px: 2,
            borderBottom: '1px solid rgba(0,0,0,0.06)',
          }}
        >
          {showCheckbox && (
            <Skeleton variant="rectangular" width={40} height={40} sx={{ flexShrink: 0 }} />
          )}
          {Array.from({ length: columns }).map((_, colIndex) => (
            <Skeleton
              key={colIndex}
              variant="text"
              width={colIndex === 0 ? '25%' : `${60 + Math.random() * 30}%`}
              height={24}
              sx={{ flex: colIndex === 0 ? 2 : 1 }}
            />
          ))}
        </Box>
      ))}
    </>
  );
};

// Dashboard Stats Grid Skeleton
export interface StatsGridSkeletonProps {
  count?: number;
}

export const StatsGridSkeleton: React.FC<StatsGridSkeletonProps> = ({
  count = 4,
}) => {
  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: {
          xs: '1fr',
          sm: 'repeat(2, 1fr)',
          lg: `repeat(${Math.min(count, 4)}, 1fr)`,
        },
        gap: 3,
      }}
    >
      <SkeletonCard variant="stats" count={count} />
    </Box>
  );
};

export default SkeletonCard;

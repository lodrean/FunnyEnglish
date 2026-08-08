/**
 * StatsCard Component - Metric display card with trend indicator
 * Design System 2.0
 */

import React from 'react';
import {
  Card,
  CardContent,
  Box,
  Typography,
  Skeleton,
  alpha,
  useTheme,
} from '@mui/material';
import type { Theme } from '@mui/material/styles';
import {
  TrendingUp,
  TrendingDown,
  TrendingFlat,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  ResponsiveContainer,
  AreaChart,
  Area,
} from 'recharts';

// =============================================================================
// TYPES
// =============================================================================

export type StatsCardVariant = 'primary' | 'success' | 'warning' | 'error' | 'info';
export type StatsCardChartType = 'sparkline' | 'area' | 'none';

export interface StatsCardProps {
  title: string;
  value: string | number;
  change?: {
    value: number;
    isPositive: boolean;
    label?: string;
  };
  icon: React.ComponentType<{ sx?: object }>;
  variant?: StatsCardVariant;
  chartType?: StatsCardChartType;
  chartData?: number[];
  loading?: boolean;
  onClick?: () => void;
}

// =============================================================================
// COLOR MAPS
// =============================================================================

const getColors = (variant: StatsCardVariant, theme: Theme) => {
  const colors = {
    primary: {
      main: theme.palette.primary.main,
      light: alpha(theme.palette.primary.main, 0.15),
      gradient: `linear-gradient(135deg, ${theme.palette.primary.main} 0%, ${theme.palette.primary.dark} 100%)`,
    },
    success: {
      main: theme.palette.success.main,
      light: alpha(theme.palette.success.main, 0.15),
      gradient: `linear-gradient(135deg, ${theme.palette.success.main} 0%, ${theme.palette.success.dark} 100%)`,
    },
    warning: {
      main: theme.palette.warning.main,
      light: alpha(theme.palette.warning.main, 0.15),
      gradient: `linear-gradient(135deg, ${theme.palette.warning.main} 0%, ${theme.palette.warning.dark} 100%)`,
    },
    error: {
      main: theme.palette.error.main,
      light: alpha(theme.palette.error.main, 0.15),
      gradient: `linear-gradient(135deg, ${theme.palette.error.main} 0%, ${theme.palette.error.dark} 100%)`,
    },
    info: {
      main: theme.palette.info.main,
      light: alpha(theme.palette.info.main, 0.15),
      gradient: `linear-gradient(135deg, ${theme.palette.info.main} 0%, ${theme.palette.info.dark} 100%)`,
    },
  };

  return colors[variant];
};

// =============================================================================
// CHART COMPONENTS
// =============================================================================

const SparklineChart: React.FC<{ data: number[]; color: string }> = ({ data, color }) => {
  const chartData = data.map((val, idx) => ({ idx, val }));

  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={chartData} margin={{ top: 0, right: 0, bottom: 0, left: 0 }}>
        <Line
          type="monotone"
          dataKey="val"
          stroke={color}
          strokeWidth={2}
          dot={false}
          animationDuration={1000}
        />
      </LineChart>
    </ResponsiveContainer>
  );
};

const AreaChartComponent: React.FC<{ data: number[]; color: string }> = ({ data, color }) => {
  const chartData = data.map((val, idx) => ({ idx, val }));

  return (
    <ResponsiveContainer width="100%" height="100%">
      <AreaChart data={chartData} margin={{ top: 0, right: 0, bottom: 0, left: 0 }}>
        <defs>
          <linearGradient id={`gradient-${color.replace('#', '')}`} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={color} stopOpacity={0.3} />
            <stop offset="100%" stopColor={color} stopOpacity={0} />
          </linearGradient>
        </defs>
        <Area
          type="monotone"
          dataKey="val"
          stroke={color}
          strokeWidth={2}
          fill={`url(#gradient-${color.replace('#', '')})`}
          animationDuration={1000}
        />
      </AreaChart>
    </ResponsiveContainer>
  );
};

// =============================================================================
// TREND INDICATOR
// =============================================================================

const TrendIndicator: React.FC<{
  change: StatsCardProps['change'];
  variant: StatsCardVariant;
}> = ({ change }) => {

  if (!change) return null;

  const TrendIcon = change.isPositive ? TrendingUp : change.value === 0 ? TrendingFlat : TrendingDown;
  const trendColor = change.isPositive
    ? 'success.main'
    : change.value === 0
    ? 'text.disabled'
    : 'error.main';

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        mt: 1,
        gap: 0.5,
      }}
    >
      <TrendIcon
        fontSize="small"
        sx={{ color: trendColor }}
      />
      <Typography
        variant="body2"
        sx={{
          color: trendColor,
          fontWeight: 600,
        }}
      >
        {change.isPositive ? '+' : ''}{change.value}%
      </Typography>
      {change.label && (
        <Typography variant="body2" color="text.secondary">
          {change.label}
        </Typography>
      )}
    </Box>
  );
};

// =============================================================================
// MAIN COMPONENT
// =============================================================================

export const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  change,
  icon: Icon,
  variant = 'primary',
  chartType = 'none',
  chartData,
  loading = false,
  onClick,
}) => {
  const theme = useTheme();
  const colors = getColors(variant, theme);

  if (loading) {
    return (
      <Card
        sx={{
          height: '100%',
          borderRadius: 3,
          boxShadow: theme.shadows[1],
        }}
      >
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
            <Skeleton variant="text" width={100} />
            <Skeleton variant="circular" width={48} height={48} />
          </Box>
          <Skeleton variant="text" width={80} height={48} />
          <Skeleton variant="text" width={120} />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card
      onClick={onClick}
      sx={{
        height: '100%',
        borderRadius: 3,
        boxShadow: theme.shadows[1],
        transition: 'all 0.2s ease-in-out',
        cursor: onClick ? 'pointer' : 'default',
        position: 'relative',
        overflow: 'hidden',
        '&:hover': onClick
          ? {
              transform: 'translateY(-2px)',
              boxShadow: theme.shadows[4],
            }
          : {},
      }}
    >
      <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
        <Box
          sx={{
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'space-between',
            mb: chartType !== 'none' && chartData ? 1 : 2,
          }}
        >
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                fontWeight: 500,
                mb: 0.5,
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                fontSize: '0.75rem',
              }}
            >
              {title}
            </Typography>
            <Typography
              variant="h4"
              component="div"
              sx={{
                fontWeight: 700,
                color: 'text.primary',
                lineHeight: 1.2,
              }}
            >
              {value}
            </Typography>
            {change && <TrendIndicator change={change} variant={variant} />}
          </Box>

          <Box
            sx={{
              p: 1.5,
              borderRadius: 2,
              backgroundColor: colors.light,
              color: colors.main,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <Icon sx={{ fontSize: 28 }} />
          </Box>
        </Box>

        {/* Chart */}
        {chartType !== 'none' && chartData && chartData.length > 0 && (
          <Box
            sx={{
              height: 60,
              mt: 2,
              mx: -3,
              mb: -1,
            }}
          >
            {chartType === 'sparkline' && <SparklineChart data={chartData} color={colors.main} />}
            {chartType === 'area' && <AreaChartComponent data={chartData} color={colors.main} />}
          </Box>
        )}
      </CardContent>
    </Card>
  );
};

export default StatsCard;

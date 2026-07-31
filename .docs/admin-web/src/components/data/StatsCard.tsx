/**
 * StatsCard Component
 * Metric display cards with sparkline charts and trend indicators
 */
import React from 'react';
import {
  Card,
  CardContent,
  Box,
  Typography,
  Avatar,
  styled,
  SvgIconProps,
} from '@mui/material';
import {
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
} from '@mui/icons-material';
import {
  LineChart,
  Line,
  ResponsiveContainer,
  YAxis,
} from 'recharts';

// Design System Colors
const COLORS = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
};

// Type Definitions
export type StatsCardVariant = 'primary' | 'success' | 'warning' | 'error' | 'info';

export interface SparklineDataPoint {
  value: number;
}

export interface StatsCardProps {
  title: string;
  value: string | number;
  change?: number;
  changeLabel?: string;
  icon: React.ComponentType<SvgIconProps>;
  variant?: StatsCardVariant;
  sparklineData?: SparklineDataPoint[];
  showSparkline?: boolean;
  loading?: boolean;
}

// Color configurations for each variant
const variantColors: Record<StatsCardVariant, { main: string; light: string }> = {
  primary: { main: COLORS.primary, light: 'rgba(74, 144, 217, 0.15)' },
  success: { main: COLORS.success, light: 'rgba(67, 160, 71, 0.15)' },
  warning: { main: COLORS.warning, light: 'rgba(251, 140, 0, 0.15)' },
  error: { main: COLORS.error, light: 'rgba(229, 57, 53, 0.15)' },
  info: { main: COLORS.info, light: 'rgba(33, 150, 243, 0.15)' },
};

// Styled Components
const StyledCard = styled(Card)(({ theme }) => ({
  height: '100%',
  backgroundColor: COLORS.card,
  borderRadius: theme.shape.borderRadius * 1.5,
  boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
  transition: 'transform 0.2s ease, box-shadow 0.2s ease',
  overflow: 'hidden',
  '&:hover': {
    transform: 'translateY(-2px)',
    boxShadow: '0 4px 16px rgba(0,0,0,0.12)',
  },
}));

const IconContainer = styled(Avatar)<{ variant: StatsCardVariant }>(
  ({ variant }) => ({
    width: 56,
    height: 56,
    backgroundColor: variantColors[variant].light,
    color: variantColors[variant].main,
  })
);

const TrendContainer = styled(Box)<{ isPositive: boolean }>(
  ({ isPositive }) => ({
    display: 'flex',
    alignItems: 'center',
    gap: '4px',
    padding: '4px 8px',
    borderRadius: '12px',
    backgroundColor: isPositive
      ? 'rgba(67, 160, 71, 0.12)'
      : 'rgba(229, 57, 53, 0.12)',
    color: isPositive ? COLORS.success : COLORS.error,
  })
);

const SparklineContainer = styled(Box)({
  height: 60,
  marginTop: 16,
  marginLeft: -16,
  marginRight: -16,
});

// Sparkline Component
const SparklineChart: React.FC<{
  data: SparklineDataPoint[];
  color: string;
}> = ({ data, color }) => {
  if (!data || data.length === 0) return null;

  // Calculate min and max for Y-axis scaling
  const values = data.map((d) => d.value);
  const minValue = Math.min(...values);
  const maxValue = Math.max(...values);
  const padding = (maxValue - minValue) * 0.1;

  return (
    <ResponsiveContainer width="100%" height="100%">
      <LineChart data={data} margin={{ top: 5, right: 0, left: 0, bottom: 5 }}>
        <YAxis domain={[minValue - padding, maxValue + padding]} hide />
        <Line
          type="monotone"
          dataKey="value"
          stroke={color}
          strokeWidth={2}
          dot={false}
          activeDot={{ r: 4, fill: color }}
          animationDuration={1000}
        />
      </LineChart>
    </ResponsiveContainer>
  );
};

// Main Component
const StatsCard: React.FC<StatsCardProps> = ({
  title,
  value,
  change,
  changeLabel = 'vs last period',
  icon: Icon,
  variant = 'primary',
  sparklineData,
  showSparkline = false,
  loading = false,
}) => {
  const colors = variantColors[variant];
  const isPositive = change !== undefined ? change >= 0 : true;
  const hasChange = change !== undefined;

  if (loading) {
    return (
      <StyledCard>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <IconContainer variant={variant}>
              <Icon />
            </IconContainer>
            <Box sx={{ flex: 1 }}>
              <Typography
                variant="body2"
                color="textSecondary"
                sx={{ mb: 0.5 }}
              >
                {title}
              </Typography>
              <Typography variant="h4" fontWeight="bold">
                --
              </Typography>
            </Box>
          </Box>
        </CardContent>
      </StyledCard>
    );
  }

  return (
    <StyledCard>
      <CardContent sx={{ p: 3, '&:last-child': { pb: 3 } }}>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
          <IconContainer variant={variant}>
            <Icon />
          </IconContainer>
          <Box sx={{ flex: 1 }}>
            <Typography
              variant="body2"
              color="textSecondary"
              sx={{ mb: 0.5, fontWeight: 500 }}
            >
              {title}
            </Typography>
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1.5,
                flexWrap: 'wrap',
              }}
            >
              <Typography
                variant="h4"
                fontWeight="bold"
                color="textPrimary"
                sx={{ lineHeight: 1.2 }}
              >
                {value}
              </Typography>
              {hasChange && (
                <TrendContainer isPositive={isPositive}>
                  {isPositive ? (
                    <TrendingUpIcon fontSize="small" />
                  ) : (
                    <TrendingDownIcon fontSize="small" />
                  )}
                  <Typography variant="caption" fontWeight="600">
                    {isPositive ? '+' : ''}
                    {change.toFixed(1)}%
                  </Typography>
                </TrendContainer>
              )}
            </Box>
            {hasChange && (
              <Typography
                variant="caption"
                color="textSecondary"
                sx={{ mt: 0.5, display: 'block' }}
              >
                {changeLabel}
              </Typography>
            )}
          </Box>
        </Box>

        {showSparkline && sparklineData && sparklineData.length > 0 && (
          <SparklineContainer>
            <SparklineChart data={sparklineData} color={colors.main} />
          </SparklineContainer>
        )}
      </CardContent>
    </StyledCard>
  );
};

export default StatsCard;

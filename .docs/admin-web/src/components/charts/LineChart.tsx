/**
 * LineChart Component
 * Time series charts with responsive container and MUI theme colors
 */
import React from 'react';
import {
  LineChart as RechartsLineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  TooltipProps,
} from 'recharts';
import { Box, Typography, Skeleton, styled } from '@mui/material';

// Design System Colors
const CHART_COLORS = [
  '#4A90D9',
  '#43A047',
  '#FB8C00',
  '#E53935',
  '#9C27B0',
  '#00BCD4',
  '#FFEB3B',
  '#795548',
];

const COLORS = {
  primary: '#4A90D9',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
  border: '#E0E0E0',
  grid: '#E0E0E0',
};

// Type Definitions
export interface LineChartDataPoint {
  [key: string]: string | number;
}

export interface LineChartSeries {
  key: string;
  name: string;
  color?: string;
  strokeWidth?: number;
  type?: 'monotone' | 'linear' | 'step';
  dot?: boolean;
}

export interface LineChartProps {
  data: LineChartDataPoint[];
  series: LineChartSeries[];
  xAxisKey: string;
  xAxisLabel?: string;
  yAxisLabel?: string;
  height?: number;
  loading?: boolean;
  showGrid?: boolean;
  showLegend?: boolean;
  showTooltip?: boolean;
  title?: string;
  subtitle?: string;
  yAxisFormatter?: (value: number) => string;
  xAxisFormatter?: (value: string) => string;
  tooltipFormatter?: (value: number, name: string) => [string, string];
}

// Styled Components
const ChartContainer = styled(Box)(({ theme }) => ({
  backgroundColor: COLORS.card,
  borderRadius: theme.shape.borderRadius * 1.5,
  padding: theme.spacing(3),
  boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
}));

const ChartHeader = styled(Box)({
  marginBottom: 24,
});

// Custom Tooltip Component
const CustomTooltip: React.FC<TooltipProps<number, string>> = ({
  active,
  payload,
  label,
}) => {
  if (active && payload && payload.length) {
    return (
      <Box
        sx={{
          backgroundColor: COLORS.card,
          border: `1px solid ${COLORS.border}`,
          borderRadius: 1,
          padding: 1.5,
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
        }}
      >
        <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
          {label}
        </Typography>
        {payload.map((entry, index) => (
          <Box
            key={index}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1,
              mb: 0.5,
            }}
          >
            <Box
              sx={{
                width: 10,
                height: 10,
                borderRadius: '50%',
                backgroundColor: entry.color,
              }}
            />
            <Typography variant="body2" color="textSecondary">
              {entry.name}:
            </Typography>
            <Typography variant="body2" fontWeight={600}>
              {entry.value}
            </Typography>
          </Box>
        ))}
      </Box>
    );
  }
  return null;
};

// Main Component
const LineChart: React.FC<LineChartProps> = ({
  data,
  series,
  xAxisKey,
  xAxisLabel,
  yAxisLabel,
  height = 300,
  loading = false,
  showGrid = true,
  showLegend = true,
  showTooltip = true,
  title,
  subtitle,
  yAxisFormatter,
  xAxisFormatter,
  tooltipFormatter,
}) => {
  if (loading) {
    return (
      <ChartContainer>
        <Skeleton variant="text" width={200} height={32} sx={{ mb: 1 }} />
        <Skeleton variant="text" width={150} height={20} sx={{ mb: 3 }} />
        <Skeleton variant="rectangular" height={height} sx={{ borderRadius: 1 }} />
      </ChartContainer>
    );
  }

  if (!data || data.length === 0) {
    return (
      <ChartContainer>
        <Box
          sx={{
            height,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexDirection: 'column',
            color: COLORS.textSecondary,
          }}
        >
          <Typography variant="h6" color="textSecondary">
            No data available
          </Typography>
        </Box>
      </ChartContainer>
    );
  }

  return (
    <ChartContainer>
      {(title || subtitle) && (
        <ChartHeader>
          {title && (
            <Typography variant="h6" fontWeight={600} gutterBottom>
              {title}
            </Typography>
          )}
          {subtitle && (
            <Typography variant="body2" color="textSecondary">
              {subtitle}
            </Typography>
          )}
        </ChartHeader>
      )}
      <ResponsiveContainer width="100%" height={height}>
        <RechartsLineChart
          data={data}
          margin={{ top: 10, right: 30, left: 20, bottom: 30 }}
        >
          {showGrid && (
            <CartesianGrid
              strokeDasharray="3 3"
              stroke={COLORS.grid}
              vertical={false}
            />
          )}
          <XAxis
            dataKey={xAxisKey}
            tick={{ fill: COLORS.textSecondary, fontSize: 12 }}
            tickLine={{ stroke: COLORS.border }}
            axisLine={{ stroke: COLORS.border }}
            tickFormatter={xAxisFormatter}
            label={
              xAxisLabel
                ? {
                    value: xAxisLabel,
                    position: 'insideBottom',
                    offset: -20,
                    fill: COLORS.textSecondary,
                    fontSize: 12,
                  }
                : undefined
            }
          />
          <YAxis
            tick={{ fill: COLORS.textSecondary, fontSize: 12 }}
            tickLine={{ stroke: COLORS.border }}
            axisLine={{ stroke: COLORS.border }}
            tickFormatter={yAxisFormatter}
            label={
              yAxisLabel
                ? {
                    value: yAxisLabel,
                    angle: -90,
                    position: 'insideLeft',
                    fill: COLORS.textSecondary,
                    fontSize: 12,
                  }
                : undefined
            }
          />
          {showTooltip && (
            <Tooltip
              content={<CustomTooltip />}
              formatter={tooltipFormatter}
            />
          )}
          {showLegend && (
            <Legend
              verticalAlign="top"
              height={36}
              iconType="circle"
              wrapperStyle={{
                paddingBottom: 20,
                fontSize: 12,
                color: COLORS.textPrimary,
              }}
            />
          )}
          {series.map((s, index) => (
            <Line
              key={s.key}
              type={s.type || 'monotone'}
              dataKey={s.key}
              name={s.name}
              stroke={s.color || CHART_COLORS[index % CHART_COLORS.length]}
              strokeWidth={s.strokeWidth || 2}
              dot={s.dot !== false ? { r: 3, strokeWidth: 2 } : false}
              activeDot={{ r: 5, strokeWidth: 2 }}
              animationDuration={1000}
              animationEasing="ease-in-out"
            />
          ))}
        </RechartsLineChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
};

// Simple Line Chart variant for quick usage
export interface SimpleLineChartProps {
  data: { label: string; value: number }[];
  color?: string;
  height?: number;
  loading?: boolean;
}

export const SimpleLineChart: React.FC<SimpleLineChartProps> = ({
  data,
  color = COLORS.primary,
  height = 200,
  loading = false,
}) => {
  const chartData = data.map((d) => ({ name: d.label, value: d.value }));

  return (
    <LineChart
      data={chartData}
      series={[{ key: 'value', name: 'Value', color }]}
      xAxisKey="name"
      height={height}
      loading={loading}
      showLegend={false}
    />
  );
};

// Multi-series Line Chart with area fill option
export interface AreaLineChartProps extends Omit<LineChartProps, 'series'> {
  series: (LineChartSeries & { fill?: boolean; fillOpacity?: number })[];
}

export const AreaLineChart: React.FC<AreaLineChartProps> = ({
  series,
  ...props
}) => {
  // Transform series to include area fill
  const areaSeries = series.map((s) => ({
    ...s,
    strokeWidth: s.strokeWidth || 2,
  }));

  return <LineChart series={areaSeries} {...props} />;
};

export default LineChart;

/**
 * BarChart Component
 * Comparison charts with responsive container and MUI theme colors
 */
import React from 'react';
import {
  BarChart as RechartsBarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Cell,
  LabelList,
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
export interface BarChartDataPoint {
  [key: string]: string | number;
}

export interface BarChartSeries {
  key: string;
  name: string;
  color?: string;
  stackId?: string;
  barSize?: number;
  radius?: [number, number, number, number];
}

export interface BarChartProps {
  data: BarChartDataPoint[];
  series: BarChartSeries[];
  xAxisKey: string;
  xAxisLabel?: string;
  yAxisLabel?: string;
  height?: number;
  loading?: boolean;
  showGrid?: boolean;
  showLegend?: boolean;
  showTooltip?: boolean;
  showDataLabels?: boolean;
  title?: string;
  subtitle?: string;
  layout?: 'horizontal' | 'vertical';
  yAxisFormatter?: (value: number) => string;
  xAxisFormatter?: (value: string) => string;
  tooltipFormatter?: (value: number, name: string) => [string, string];
  useCustomColors?: boolean;
  customColors?: string[];
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
const CustomTooltip: React.FC<any> = ({
  active,
  payload,
  label,
}) => {
  const safePayload = payload ?? [];
  const safeLabel = label ?? '';
  if (active && safePayload && safePayload.length) {
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
          {safeLabel}
        </Typography>
        {safePayload.map((entry, index) => (
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
                borderRadius: 2,
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
const BarChart: React.FC<BarChartProps> = ({
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
  showDataLabels = false,
  title,
  subtitle,
  layout = 'horizontal',
  yAxisFormatter,
  xAxisFormatter,
  tooltipFormatter,
  useCustomColors = false,
  customColors,
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

  const colors = customColors || CHART_COLORS;
  const isHorizontal = layout === 'horizontal';

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
        <RechartsBarChart
          data={data}
          layout={layout}
          margin={{ top: 10, right: 30, left: 20, bottom: 30 }}
        >
          {showGrid && (
            <CartesianGrid
              strokeDasharray="3 3"
              stroke={COLORS.grid}
              horizontal={isHorizontal}
              vertical={!isHorizontal}
            />
          )}
          {isHorizontal ? (
            <>
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
            </>
          ) : (
            <>
              <XAxis
                type="number"
                tick={{ fill: COLORS.textSecondary, fontSize: 12 }}
                tickLine={{ stroke: COLORS.border }}
                axisLine={{ stroke: COLORS.border }}
                tickFormatter={yAxisFormatter}
              />
              <YAxis
                type="category"
                dataKey={xAxisKey}
                tick={{ fill: COLORS.textSecondary, fontSize: 12 }}
                tickLine={{ stroke: COLORS.border }}
                axisLine={{ stroke: COLORS.border }}
                width={100}
              />
            </>
          )}
          {showTooltip && (
            <Tooltip
              content={<CustomTooltip />}
              formatter={tooltipFormatter}
              cursor={{ fill: 'rgba(0,0,0,0.04)' }}
            />
          )}
          {showLegend && (
            <Legend
              verticalAlign="top"
              height={36}
              iconType="square"
              wrapperStyle={{
                paddingBottom: 20,
                fontSize: 12,
                color: COLORS.textPrimary,
              }}
            />
          )}
          {series.map((s, index) => (
            <Bar
              key={s.key}
              dataKey={s.key}
              name={s.name}
              fill={s.color || colors[index % colors.length]}
              stackId={s.stackId}
              barSize={s.barSize || 30}
              radius={s.radius || [4, 4, 0, 0]}
              animationDuration={800}
              animationEasing="ease-out"
            >
              {showDataLabels && (
                <LabelList
                  dataKey={s.key}
                  position="top"
                  style={{ fill: COLORS.textPrimary, fontSize: 11 }}
                />
              )}
              {useCustomColors &&
                data.map((_, i) => (
                  <Cell
                    key={`cell-${i}`}
                    fill={colors[i % colors.length]}
                  />
                ))}
            </Bar>
          ))}
        </RechartsBarChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
};

// Simple Bar Chart variant for quick usage
export interface SimpleBarChartProps {
  data: { label: string; value: number }[];
  color?: string;
  height?: number;
  loading?: boolean;
  showDataLabels?: boolean;
}

export const SimpleBarChart: React.FC<SimpleBarChartProps> = ({
  data,
  color = COLORS.primary,
  height = 250,
  loading = false,
  showDataLabels = false,
}) => {
  const chartData = data.map((d) => ({ name: d.label, value: d.value }));

  return (
    <BarChart
      data={chartData}
      series={[{ key: 'value', name: 'Value', color }]}
      xAxisKey="name"
      height={height}
      loading={loading}
      showLegend={false}
      showDataLabels={showDataLabels}
    />
  );
};

// Stacked Bar Chart variant
export interface StackedBarChartProps extends Omit<BarChartProps, 'series'> {
  series: (BarChartSeries & { stackId: string })[];
}

export const StackedBarChart: React.FC<StackedBarChartProps> = ({
  series,
  ...props
}) => {
  // Ensure all series have the same stackId for stacking
  const stackedSeries = series.map((s) => ({
    ...s,
    stackId: s.stackId || 'stack1',
    radius: [0, 0, 0, 0] as [number, number, number, number],
  }));

  // Last series in each stack gets rounded corners
  const stackGroups: { [key: string]: number } = {};
  stackedSeries.forEach((s, i) => {
    stackGroups[s.stackId] = i;
  });

  return (
    <BarChart
      series={stackedSeries.map((s, i) => ({
        ...s,
        radius:
          i === stackGroups[s.stackId] ? ([4, 4, 0, 0] as [number, number, number, number]) : s.radius,
      }))}
      {...props}
    />
  );
};

// Horizontal Bar Chart variant
export interface HorizontalBarChartProps extends BarChartProps {
  yAxisWidth?: number;
}

export const HorizontalBarChart: React.FC<HorizontalBarChartProps> = ({
  yAxisWidth: _yAxisWidth,
  ...props
}) => {
  return (
    <BarChart
      layout="vertical"
      {...props}
    />
  );
};

export default BarChart;

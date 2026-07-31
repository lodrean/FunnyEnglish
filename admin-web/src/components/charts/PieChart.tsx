/**
 * PieChart Component
 * Distribution charts with responsive container and MUI theme colors
 */
import React, { useState } from 'react';
import {
  PieChart as RechartsPieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Sector,
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
};

// Type Definitions
export interface PieChartDataPoint {
  name: string;
  value: number;
  color?: string;
}

export interface PieChartProps {
  data: PieChartDataPoint[];
  height?: number;
  loading?: boolean;
  showTooltip?: boolean;
  showLegend?: boolean;
  title?: string;
  subtitle?: string;
  donut?: boolean;
  donutInnerRadius?: number;
  donutOuterRadius?: number;
  innerRadius?: number;
  outerRadius?: number;
  legendPosition?: 'top' | 'bottom' | 'left' | 'right';
  legendLayout?: 'horizontal' | 'vertical';
  showPercentage?: boolean;
  onSliceClick?: (data: PieChartDataPoint, index: number) => void;
  customColors?: string[];
  activeSliceIndex?: number;
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

const CenterLabel = styled(Box)({
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  textAlign: 'center',
});

// Active Shape for hover effect
const renderActiveShape = (props: any) => {
  const {
    cx,
    cy,
    innerRadius,
    outerRadius,
    startAngle,
    endAngle,
    fill,
    payload,
    percent,
    value,
  } = props as {
    cx: number;
    cy: number;
    innerRadius: number;
    outerRadius: number;
    startAngle: number;
    endAngle: number;
    fill: string;
    payload: { name: string };
    percent: number;
    value: number;
  };

  return (
    <g>
      <Sector
        cx={cx}
        cy={cy}
        innerRadius={innerRadius}
        outerRadius={outerRadius + 8}
        startAngle={startAngle}
        endAngle={endAngle}
        fill={fill}
      />
      <Sector
        cx={cx}
        cy={cy}
        startAngle={startAngle}
        endAngle={endAngle}
        innerRadius={outerRadius + 10}
        outerRadius={outerRadius + 14}
        fill={fill}
        opacity={0.3}
      />
      <text
        x={cx}
        y={cy}
        dy={-10}
        textAnchor="middle"
        fill={COLORS.textPrimary}
        style={{ fontSize: 14, fontWeight: 600 }}
      >
        {payload.name}
      </text>
      <text
        x={cx}
        y={cy}
        dy={15}
        textAnchor="middle"
        fill={COLORS.textSecondary}
        style={{ fontSize: 12 }}
      >
        {value} ({(percent * 100).toFixed(1)}%)
      </text>
    </g>
  );
};

// Custom Tooltip Component
const CustomTooltip: React.FC<
  TooltipProps<number, string> & { showPercentage?: boolean }
> = (props) => {
  const { active, payload, showPercentage = true } = props as any;
  if (active && payload && payload.length) {
    const data = payload[0] as any;
    const total = data.payload?.total || 0;
    const percentage = total > 0 ? ((data.value || 0) / total) * 100 : 0;

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
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
          <Box
            sx={{
              width: 10,
              height: 10,
              borderRadius: '50%',
              backgroundColor: data.color,
            }}
          />
          <Typography variant="subtitle2" fontWeight={600}>
            {data.name}
          </Typography>
        </Box>
        <Typography variant="body2" color="textSecondary">
          Value: <strong>{data.value}</strong>
        </Typography>
        {showPercentage && (
          <Typography variant="body2" color="textSecondary">
            Percentage: <strong>{percentage.toFixed(1)}%</strong>
          </Typography>
        )}
      </Box>
    );
  }
  return null;
};

// Main Component
const PieChart: React.FC<PieChartProps> = ({
  data,
  height = 300,
  loading = false,
  showTooltip = true,
  showLegend = true,
  title,
  subtitle,
  donut = false,
  donutInnerRadius = 60,
  donutOuterRadius = 100,
  innerRadius = 0,
  outerRadius = 100,
  legendPosition = 'bottom',
  legendLayout = 'horizontal',
  showPercentage = true,
  onSliceClick,
  customColors,
  activeSliceIndex,
}) => {
  const [activeIndex, setActiveIndex] = useState<number | null>(
    activeSliceIndex ?? null
  );

  const colors = customColors || CHART_COLORS;

  // Calculate total for percentage calculations
  const total = data.reduce((sum, item) => sum + item.value, 0);
  const dataWithTotal = data.map((item) => ({ ...item, total }));

  const handleMouseEnter = (_: any, index: number) => {
    setActiveIndex(index);
  };

  const handleMouseLeave = () => {
    if (activeSliceIndex === undefined) {
      setActiveIndex(null);
    }
  };

  const handleClick = (data: PieChartDataPoint, index: number) => {
    onSliceClick?.(data, index);
  };

  if (loading) {
    return (
      <ChartContainer>
        <Skeleton variant="text" width={200} height={32} sx={{ mb: 1 }} />
        <Skeleton variant="text" width={150} height={20} sx={{ mb: 3 }} />
        <Skeleton variant="circular" height={height} sx={{ mx: 'auto' }} />
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

  const chartInnerRadius = donut ? donutInnerRadius : innerRadius;
  const chartOuterRadius = donut ? donutOuterRadius : outerRadius;

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
      <Box sx={{ position: 'relative' }}>
        <ResponsiveContainer width="100%" height={height}>
          <RechartsPieChart>
            {showTooltip && (
              <Tooltip
                content={<CustomTooltip showPercentage={showPercentage} />}
              />
            )}
            {showLegend && (
              <Legend
                verticalAlign={
                  legendPosition === 'top' || legendPosition === 'bottom'
                    ? legendPosition
                    : 'bottom'
                }
                align={
                  legendPosition === 'left' || legendPosition === 'right'
                    ? legendPosition
                    : 'center'
                }
                layout={legendLayout}
                iconType="circle"
                wrapperStyle={{
                  paddingTop: legendPosition === 'bottom' ? 20 : 0,
                  paddingBottom: legendPosition === 'top' ? 20 : 0,
                  fontSize: 12,
                  color: COLORS.textPrimary,
                }}
              />
            )}
            <Pie
              data={dataWithTotal}
              cx="50%"
              cy="50%"
              innerRadius={chartInnerRadius}
              outerRadius={chartOuterRadius}
              paddingAngle={2}
              dataKey="value"
              nameKey="name"
              animationBegin={0}
              animationDuration={800}
              animationEasing="ease-out"
              {...(activeIndex !== null ? { activeIndex } : {})}
              activeShape={renderActiveShape}
              onMouseEnter={handleMouseEnter}
              onMouseLeave={handleMouseLeave}
              onClick={handleClick}
              labelLine={false}
              label={
                showPercentage
                  ? (entry: any) =>
                      `${entry.name}: ${((entry.percent || 0) * 100).toFixed(
                        0
                      )}%`
                  : undefined
              }
            >
              {dataWithTotal.map((entry, index) => (
                <Cell
                  key={`cell-${index}`}
                  fill={entry.color || colors[index % colors.length]}
                  stroke={COLORS.card}
                  strokeWidth={2}
                />
              ))}
            </Pie>
          </RechartsPieChart>
        </ResponsiveContainer>
        {donut && !showPercentage && (
          <CenterLabel>
            <Typography variant="h4" fontWeight="bold" color="textPrimary">
              {total.toLocaleString()}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              Total
            </Typography>
          </CenterLabel>
        )}
      </Box>
    </ChartContainer>
  );
};

// Simple Pie Chart variant for quick usage
export interface SimplePieChartProps {
  data: { label: string; value: number }[];
  height?: number;
  loading?: boolean;
  donut?: boolean;
}

export const SimplePieChart: React.FC<SimplePieChartProps> = ({
  data,
  height = 250,
  loading = false,
  donut = false,
}) => {
  const chartData = data.map((d) => ({ name: d.label, value: d.value }));

  return (
    <PieChart
      data={chartData}
      height={height}
      loading={loading}
      donut={donut}
      showLegend={false}
      showPercentage={false}
    />
  );
};

// Half Pie Chart variant (semicircle)
export interface HalfPieChartProps extends Omit<PieChartProps, 'data'> {
  data: PieChartDataPoint[];
  startAngle?: number;
  endAngle?: number;
}

export const HalfPieChart: React.FC<HalfPieChartProps> = ({
  data,
  startAngle = 180,
  endAngle = 0,
  height = 200,
  ..._props
}) => {
  return (
    <Box>
      <ResponsiveContainer width="100%" height={height}>
        <RechartsPieChart>
          <Pie
            data={data}
            cx="50%"
            cy="100%"
            startAngle={startAngle}
            endAngle={endAngle}
            innerRadius={60}
            outerRadius={100}
            paddingAngle={2}
            dataKey="value"
            nameKey="name"
          >
            {data.map((entry, index) => (
              <Cell
                key={`cell-${index}`}
                fill={entry.color || CHART_COLORS[index % CHART_COLORS.length]}
              />
            ))}
          </Pie>
          <Tooltip />
        </RechartsPieChart>
      </ResponsiveContainer>
    </Box>
  );
};

// Mini Pie Chart for stat cards
export interface MiniPieChartProps {
  data: { label: string; value: number; color?: string }[];
  size?: number;
}

export const MiniPieChart: React.FC<MiniPieChartProps> = ({
  data,
  size = 60,
}) => {
  return (
    <Box sx={{ width: size, height: size }}>
      <ResponsiveContainer width="100%" height="100%">
        <RechartsPieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={size * 0.3}
            outerRadius={size * 0.5}
            paddingAngle={2}
            dataKey="value"
            nameKey="label"
          >
            {data.map((entry, index) => (
              <Cell
                key={`cell-${index}`}
                fill={entry.color || CHART_COLORS[index % CHART_COLORS.length]}
              />
            ))}
          </Pie>
        </RechartsPieChart>
      </ResponsiveContainer>
    </Box>
  );
};

export default PieChart;

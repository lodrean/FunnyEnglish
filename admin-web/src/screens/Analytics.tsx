import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  Paper,
  Grid,
  Card,
  CardContent,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Skeleton,
  Alert,
  Menu,
  MenuItem,
} from '@mui/material';
import {
  Download as DownloadIcon,
  TrendingUp as TrendingUpIcon,
  People as PeopleIcon,
  TableChart as CsvIcon,
  PersonOutline as GuestIcon,
  TaskAlt as CompletionIcon,
  SwapHoriz as ConversionIcon,
  OnlinePrediction as ActiveIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import {
  getAdminAnalytics,
  getAdminDailyActivity,
  getAdminLevelDistribution,
  getPopularTests,
  getRecentActivity,
  getGuestAnalytics,
} from '../api/client';
import type { DailyActivity, LevelDistribution, PopularTest, RecentActivityItem } from '../types';
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Legend,
  PieChart,
  Pie,
  Cell,
  AreaChart,
  Area,
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

const CHART_COLORS = [COLORS.primary, COLORS.success, COLORS.warning, COLORS.error, COLORS.info];

// Types
interface DateRange {
  start: string;
  end: string;
}

// Только реальные данные backend (аудит К1: моки запрещены)
interface AnalyticsData {
  metrics: {
    totalUsers: number;
    totalCompletions: number;
    publishedTests: number;
    totalTests: number;
  };
  dailyActivity: DailyActivity[];
  levels: LevelDistribution[];
  popularTests: PopularTest[];
  recentActivity: RecentActivityItem[];
}

const DAY_MS = 24 * 60 * 60 * 1000;
const toIsoDate = (d: Date): string => d.toISOString().slice(0, 10);

// Backend daily-activity принимает только "последние N дней" — запрашиваем покрывающий
// диапазон, точный [start, end] отфильтровываем на клиенте.
const fetchAnalytics = async (days: number): Promise<AnalyticsData> => {
  const [overview, dailyActivity, levels, popularTests, recentActivity] = await Promise.all([
    getAdminAnalytics(),
    getAdminDailyActivity(days),
    getAdminLevelDistribution(),
    getPopularTests(5),
    getRecentActivity(10),
  ]);

  return {
    metrics: {
      totalUsers: Number(overview.totalUsers),
      totalCompletions: Number(overview.totalCompletions),
      publishedTests: Number(overview.publishedTests),
      totalTests: Number(overview.totalTests),
    },
    dailyActivity,
    levels,
    popularTests,
    recentActivity,
  };
};

// Metric Card Component
interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  color: string;
  loading?: boolean;
}

const MetricCard: React.FC<MetricCardProps> = ({ title, value, subtitle, icon, color, loading }) => {
  if (loading) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Skeleton variant="text" width="60%" height={24} />
          <Skeleton variant="text" width="40%" height={48} sx={{ mt: 1 }} />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4" fontWeight="bold">
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              width: 48,
              height: 48,
              borderRadius: 2,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: `${color}20`,
              color: color,
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

// Empty state вместо моков, когда backend вернул пустые данные
const EmptyState: React.FC<{ message: string; height?: number }> = ({ message, height = 320 }) => (
  <Box display="flex" alignItems="center" justifyContent="center" height={height} data-testid="analytics-empty-state">
    <Typography color="text.secondary">{message}</Typography>
  </Box>
);

const formatDateLabel = (iso: string): string =>
  new Date(`${iso}T00:00:00`).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });

const formatTimestamp = (iso: string): string => new Date(iso).toLocaleString();

const ACTIVITY_TYPE_LABELS: Record<string, string> = {
  NEW_USER: 'New user',
  TEST_COMPLETED: 'Test completed',
  ACHIEVEMENT: 'Achievement',
};

const activityTypeColor = (type: string): string => {
  switch (type) {
    case 'NEW_USER':
      return COLORS.primary;
    case 'TEST_COMPLETED':
      return COLORS.success;
    case 'ACHIEVEMENT':
      return COLORS.warning;
    default:
      return COLORS.info;
  }
};

const csvEscape = (value: string | number): string => {
  const s = String(value);
  return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
};

const Analytics: React.FC = () => {
  const [dateRange, setDateRange] = useState<DateRange>(() => {
    const now = Date.now();
    return { start: toIsoDate(new Date(now - 13 * DAY_MS)), end: toIsoDate(new Date(now)) };
  });
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  // Сколько дней запросить у backend, чтобы покрыть выбранный start
  const daysNeeded = Math.min(
    365,
    Math.max(1, Math.floor((Date.now() - new Date(`${dateRange.start}T00:00:00`).getTime()) / DAY_MS) + 1)
  );

  const { data, isLoading, error } = useQuery({
    queryKey: ['analytics', daysNeeded],
    queryFn: () => fetchAnalytics(daysNeeded),
  });

  // Гостевые (обезличенные) пользователи — реальные данные с backend
  const { data: guestData, isLoading: guestLoading } = useQuery({
    queryKey: ['guestAnalytics'],
    queryFn: getGuestAnalytics,
  });

  // Точный диапазон [start, end] — фильтрация на клиенте (ISO-даты сравниваются строками)
  const filteredActivity = (data?.dailyActivity ?? []).filter(
    (d) => d.date >= dateRange.start && d.date <= dateRange.end
  );

  const handleExportCsv = () => {
    if (!data) return;
    const lines: string[] = [];
    lines.push('Metric,Value');
    lines.push(`Total Users,${data.metrics.totalUsers}`);
    lines.push(`Tests Completed,${data.metrics.totalCompletions}`);
    lines.push(`Published Tests,${data.metrics.publishedTests}`);
    lines.push(`Total Tests,${data.metrics.totalTests}`);
    lines.push('');
    lines.push('Date,New Users,Tests Completed,Achievements Earned');
    filteredActivity.forEach((d) => {
      lines.push([d.date, d.newUsers, d.testsCompleted, d.achievementsEarned].map(csvEscape).join(','));
    });
    lines.push('');
    lines.push('Popular Test,Category,Completions');
    data.popularTests.forEach((t) => {
      lines.push([t.name, t.category, t.completions].map(csvEscape).join(','));
    });
    lines.push('');
    lines.push('Level,Users');
    data.levels.forEach((l) => {
      lines.push([`Level ${l.level}`, l.users].map(csvEscape).join(','));
    });

    const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `analytics_${dateRange.start}_${dateRange.end}.csv`;
    link.click();
    URL.revokeObjectURL(url);
    setExportMenuAnchor(null);
  };

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">Failed to load analytics data. Please try again.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 3, md: 4 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={3}>
        <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
          Analytics
        </Typography>
        <Box display="flex" gap={1} flexWrap="wrap">
          <TextField
            type="date"
            label="Start Date"
            size="small"
            value={dateRange.start}
            onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            type="date"
            label="End Date"
            size="small"
            value={dateRange.end}
            onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
            InputLabelProps={{ shrink: true }}
          />
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={(e) => setExportMenuAnchor(e.currentTarget)}
            disabled={!data}
          >
            Export
          </Button>
          <Menu
            anchorEl={exportMenuAnchor}
            open={Boolean(exportMenuAnchor)}
            onClose={() => setExportMenuAnchor(null)}
          >
            <MenuItem onClick={handleExportCsv}>
              <CsvIcon fontSize="small" sx={{ mr: 1 }} />
              Export as CSV
            </MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* Metrics */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12} sm={6} lg={4}>
          <MetricCard
            title="Total Users"
            value={data?.metrics.totalUsers.toLocaleString() || 0}
            subtitle="registered users"
            icon={<PeopleIcon />}
            color={COLORS.primary}
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={4}>
          <MetricCard
            title="Tests Completed"
            value={data?.metrics.totalCompletions.toLocaleString() || 0}
            subtitle="all time"
            icon={<CompletionIcon />}
            color={COLORS.success}
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={4}>
          <MetricCard
            title="Published Tests"
            value={data?.metrics.publishedTests.toLocaleString() || 0}
            subtitle={`${data?.metrics.totalTests ?? 0} total`}
            icon={<TrendingUpIcon />}
            color={COLORS.info}
            loading={isLoading}
          />
        </Grid>
      </Grid>

      {/* Guest (anonymous) analytics — обезличенные данные гостевого режима */}
      <Box mb={1} display="flex" alignItems="center" gap={1}>
        <Typography variant="h6" fontWeight="bold" color={COLORS.textPrimary} data-testid="guest-analytics-title">
          Guest Users (anonymous)
        </Typography>
        <Chip size="small" label="обезличенные данные" variant="outlined" />
      </Box>
      <Typography variant="body2" color={COLORS.textSecondary} mb={2}>
        Незарегистрированные пользователи. Собираются только анонимные события (без имён и email).
      </Typography>
      <Grid container spacing={3} mb={3} data-testid="guest-analytics-section">
        <Grid xs={12} sm={6} lg={3}>
          <MetricCard
            title="Total Guests"
            value={guestData?.totalGuests.toLocaleString() || 0}
            subtitle="уникальных устройств"
            icon={<GuestIcon />}
            color={COLORS.info}
            loading={guestLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <MetricCard
            title="Active (7d)"
            value={guestData?.activeGuests7d.toLocaleString() || 0}
            subtitle="активных за неделю"
            icon={<ActiveIcon />}
            color={COLORS.primary}
            loading={guestLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <MetricCard
            title="Tests Completed"
            value={guestData?.guestTestCompletions.toLocaleString() || 0}
            subtitle="прохождений гостями"
            icon={<CompletionIcon />}
            color={COLORS.success}
            loading={guestLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <MetricCard
            title="Conversion"
            value={`${Math.round((guestData?.conversionRate || 0) * 100)}%`}
            subtitle={`${guestData?.convertedGuests || 0} зарегистрировались`}
            icon={<ConversionIcon />}
            color={COLORS.warning}
            loading={guestLoading}
          />
        </Grid>
      </Grid>

      {/* Charts Row 1 */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12} lg={8}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              User Activity
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : filteredActivity.length === 0 ? (
              <EmptyState message="No activity data for the selected period" />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <AreaChart data={filteredActivity}>
                  <defs>
                    <linearGradient id="colorNewUsers" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={COLORS.primary} stopOpacity={0.8} />
                      <stop offset="95%" stopColor={COLORS.primary} stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="colorCompleted" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={COLORS.success} stopOpacity={0.8} />
                      <stop offset="95%" stopColor={COLORS.success} stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E0E0E0" />
                  <XAxis dataKey="date" tickFormatter={formatDateLabel} stroke={COLORS.textSecondary} />
                  <YAxis stroke={COLORS.textSecondary} allowDecimals={false} />
                  <Tooltip
                    labelFormatter={(label) => formatDateLabel(String(label))}
                    contentStyle={{
                      backgroundColor: COLORS.card,
                      border: `1px solid ${COLORS.primary}`,
                      borderRadius: 8,
                    }}
                  />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="newUsers"
                    stroke={COLORS.primary}
                    fillOpacity={1}
                    fill="url(#colorNewUsers)"
                    name="New Users"
                  />
                  <Area
                    type="monotone"
                    dataKey="testsCompleted"
                    stroke={COLORS.success}
                    fillOpacity={1}
                    fill="url(#colorCompleted)"
                    name="Tests Completed"
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
        <Grid xs={12} lg={4}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              User Level Distribution
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : !data || data.levels.length === 0 ? (
              <EmptyState message="No user level data yet" />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <PieChart>
                  <Pie
                    data={data.levels}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="users"
                    nameKey="level"
                  >
                    {data.levels.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value, name) => [value, `Level ${name}`]} />
                  <Legend formatter={(value) => `Level ${value}`} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Charts Row 2 */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Popular Tests
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : !data || data.popularTests.length === 0 ? (
              <EmptyState message="No completed tests yet" />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={data.popularTests}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E0E0E0" />
                  <XAxis dataKey="name" stroke={COLORS.textSecondary} />
                  <YAxis stroke={COLORS.textSecondary} allowDecimals={false} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: COLORS.card,
                      border: `1px solid ${COLORS.primary}`,
                      borderRadius: 8,
                    }}
                  />
                  <Legend />
                  <Bar dataKey="completions" fill={COLORS.primary} name="Completions" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Tables Row */}
      <Grid container spacing={3}>
        {/* Top Performing Tests */}
        <Grid xs={12} lg={6}>
          <Paper sx={{ p: 4 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Top Performing Tests
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ backgroundColor: '#FAFAFA' }}>
                    <TableCell>Test Name</TableCell>
                    <TableCell>Category</TableCell>
                    <TableCell align="center">Completions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {isLoading ? (
                    [...Array(5)].map((_, i) => (
                      <TableRow key={i}>
                        {[...Array(3)].map((_, j) => (
                          <TableCell key={j}>
                            <Skeleton variant="text" />
                          </TableCell>
                        ))}
                      </TableRow>
                    ))
                  ) : !data || data.popularTests.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={3} align="center">
                        <Typography variant="body2" color="text.secondary" py={2}>
                          No completed tests yet
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    data.popularTests.map((test) => (
                      <TableRow key={test.id} hover>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500}>
                            {test.name}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={test.category} size="small" />
                        </TableCell>
                        <TableCell align="center">{test.completions.toLocaleString()}</TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* Recent Activity */}
        <Grid xs={12} lg={6}>
          <Paper sx={{ p: 4 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Recent Activity
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ backgroundColor: '#FAFAFA' }}>
                    <TableCell>User</TableCell>
                    <TableCell align="center">Type</TableCell>
                    <TableCell>Details</TableCell>
                    <TableCell align="right">When</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {isLoading ? (
                    [...Array(5)].map((_, i) => (
                      <TableRow key={i}>
                        {[...Array(4)].map((_, j) => (
                          <TableCell key={j}>
                            <Skeleton variant="text" />
                          </TableCell>
                        ))}
                      </TableRow>
                    ))
                  ) : !data || data.recentActivity.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center">
                        <Typography variant="body2" color="text.secondary" py={2}>
                          No recent activity yet
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    data.recentActivity.map((item, index) => (
                      <TableRow key={`${item.timestamp}-${index}`} hover>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500}>
                            {item.userName}
                          </Typography>
                        </TableCell>
                        <TableCell align="center">
                          <Chip
                            label={ACTIVITY_TYPE_LABELS[item.type] ?? item.type}
                            size="small"
                            sx={{
                              backgroundColor: `${activityTypeColor(item.type)}20`,
                              color: activityTypeColor(item.type),
                            }}
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body2" color="text.secondary">
                            {item.details || '—'}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="caption" color="text.secondary">
                            {formatTimestamp(item.timestamp)}
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Analytics;

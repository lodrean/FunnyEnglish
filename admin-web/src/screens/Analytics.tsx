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
  Assignment as AssignmentIcon,
  PictureAsPdf as PdfIcon,
  TableChart as CsvIcon,
  PersonOutline as GuestIcon,
  TaskAlt as CompletionIcon,
  SwapHoriz as ConversionIcon,
  OnlinePrediction as ActiveIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getGuestAnalytics } from '../api/client';
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

interface AnalyticsData {
  metrics: {
    totalUsers: number;
    activeUsers: number;
    testsTaken: number;
    avgCompletionTime: number;
    completionRate: number;
    avgScore: number;
  };
  userActivity: { date: string; newUsers: number; activeUsers: number; sessions: number }[];
  testPerformance: { testName: string; attempts: number; completions: number; avgScore: number }[];
  topTests: { id: string; name: string; category: string; attempts: number; completionRate: number; avgScore: number }[];
  topUsers: { id: string; name: string; email: string; testsCompleted: number; avgScore: number; totalTime: number }[];
  userDistribution: { name: string; value: number }[];
  scoreDistribution: { range: string; count: number }[];
}

// Mock API
const fetchAnalytics = async (_dateRange: DateRange): Promise<AnalyticsData> => {
  await new Promise((resolve) => setTimeout(resolve, 800));
  
  return {
    metrics: {
      totalUsers: 12458,
      activeUsers: 3421,
      testsTaken: 15234,
      avgCompletionTime: 24.5,
      completionRate: 78.5,
      avgScore: 74.2,
    },
    userActivity: [
      { date: 'Jan 1', newUsers: 45, activeUsers: 1200, sessions: 3200 },
      { date: 'Jan 2', newUsers: 52, activeUsers: 1350, sessions: 3600 },
      { date: 'Jan 3', newUsers: 38, activeUsers: 1180, sessions: 3100 },
      { date: 'Jan 4', newUsers: 65, activeUsers: 1420, sessions: 4100 },
      { date: 'Jan 5', newUsers: 48, activeUsers: 1380, sessions: 3800 },
      { date: 'Jan 6', newUsers: 72, activeUsers: 1560, sessions: 4500 },
      { date: 'Jan 7', newUsers: 58, activeUsers: 1490, sessions: 4200 },
      { date: 'Jan 8', newUsers: 41, activeUsers: 1320, sessions: 3500 },
      { date: 'Jan 9', newUsers: 55, activeUsers: 1450, sessions: 4000 },
      { date: 'Jan 10', newUsers: 63, activeUsers: 1580, sessions: 4600 },
      { date: 'Jan 11', newUsers: 49, activeUsers: 1410, sessions: 3800 },
      { date: 'Jan 12', newUsers: 67, activeUsers: 1620, sessions: 4800 },
      { date: 'Jan 13', newUsers: 54, activeUsers: 1480, sessions: 4200 },
      { date: 'Jan 14', newUsers: 71, activeUsers: 1690, sessions: 5100 },
      { date: 'Jan 15', newUsers: 62, activeUsers: 1550, sessions: 4700 },
    ],
    testPerformance: [
      { testName: 'Basic Grammar', attempts: 1250, completions: 980, avgScore: 78.5 },
      { testName: 'Vocabulary A1', attempts: 980, completions: 820, avgScore: 82.1 },
      { testName: 'Listening B1', attempts: 750, completions: 580, avgScore: 71.3 },
      { testName: 'Reading B2', attempts: 620, completions: 480, avgScore: 75.8 },
      { testName: 'Writing C1', attempts: 420, completions: 310, avgScore: 68.9 },
      { testName: 'Business English', attempts: 890, completions: 720, avgScore: 79.2 },
      { testName: 'Academic Words', attempts: 560, completions: 430, avgScore: 73.5 },
    ],
    topTests: [
      { id: '1', name: 'Basic Grammar - Present Simple', category: 'Grammar', attempts: 1250, completionRate: 78.4, avgScore: 78.5 },
      { id: '2', name: 'Business English Vocabulary', category: 'Vocabulary', attempts: 890, completionRate: 80.9, avgScore: 79.2 },
      { id: '3', name: 'Everyday Conversations', category: 'Listening', attempts: 1450, completionRate: 88.3, avgScore: 82.3 },
      { id: '4', name: 'Vocabulary A1', category: 'Vocabulary', attempts: 980, completionRate: 83.7, avgScore: 82.1 },
      { id: '5', name: 'Reading Comprehension B2', category: 'Reading', attempts: 670, completionRate: 80.6, avgScore: 74.2 },
    ],
    topUsers: [
      { id: '1', name: 'Chris Miller', email: 'chris.miller@example.com', testsCompleted: 67, avgScore: 91.2, totalTime: 4560 },
      { id: '2', name: 'John Doe', email: 'john.doe@example.com', testsCompleted: 45, avgScore: 78.5, totalTime: 3240 },
      { id: '3', name: 'Mike Wilson', email: 'mike.wilson@example.com', testsCompleted: 32, avgScore: 65.2, totalTime: 2150 },
      { id: '4', name: 'Emma Davis', email: 'emma.davis@example.com', testsCompleted: 25, avgScore: 88.5, totalTime: 1680 },
      { id: '5', name: 'Lisa Wang', email: 'lisa.wang@example.com', testsCompleted: 28, avgScore: 74.8, totalTime: 1890 },
    ],
    userDistribution: [
      { name: 'Active', value: 3421 },
      { name: 'Inactive', value: 5234 },
      { name: 'New', value: 1245 },
      { name: 'Returning', value: 2558 },
    ],
    scoreDistribution: [
      { range: '90-100%', count: 1245 },
      { range: '80-89%', count: 2890 },
      { range: '70-79%', count: 3567 },
      { range: '60-69%', count: 2341 },
      { range: 'Below 60%', count: 4191 },
    ],
  };
};

// Metric Card Component
interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: React.ReactNode;
  color: string;
  trend?: string;
  trendUp?: boolean;
  loading?: boolean;
}

const MetricCard: React.FC<MetricCardProps> = ({ title, value, subtitle, icon, color, trend, trendUp, loading }) => {
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
            {trend && (
              <Typography variant="body2" color={trendUp ? COLORS.success : COLORS.error} sx={{ mt: 0.5 }}>
                {trendUp ? '↑' : '↓'} {trend} vs last period
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

// Format time
const formatTime = (minutes: number): string => {
  if (minutes >= 60) {
    return `${Math.floor(minutes / 60)}h ${minutes % 60}m`;
  }
  return `${minutes}m`;
};

const Analytics: React.FC = () => {
  const [dateRange, setDateRange] = useState<DateRange>({
    start: '2024-01-01',
    end: '2024-01-15',
  });
  const [exportMenuAnchor, setExportMenuAnchor] = useState<null | HTMLElement>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['analytics', dateRange],
    queryFn: () => fetchAnalytics(dateRange),
  });

  // Гостевые (обезличенные) пользователи — реальные данные с backend
  const { data: guestData, isLoading: guestLoading } = useQuery({
    queryKey: ['guestAnalytics'],
    queryFn: getGuestAnalytics,
  });

  const handleExport = (format: 'pdf' | 'csv') => {
    // Simulate export
    console.log(`Exporting as ${format}...`);
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
          >
            Export
          </Button>
          <Menu
            anchorEl={exportMenuAnchor}
            open={Boolean(exportMenuAnchor)}
            onClose={() => setExportMenuAnchor(null)}
          >
            <MenuItem onClick={() => handleExport('pdf')}>
              <PdfIcon fontSize="small" sx={{ mr: 1 }} />
              Export as PDF
            </MenuItem>
            <MenuItem onClick={() => handleExport('csv')}>
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
            subtitle={`${data?.metrics.activeUsers.toLocaleString() || 0} active`}
            icon={<PeopleIcon />}
            color={COLORS.primary}
            trend="12.5%"
            trendUp={true}
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={4}>
          <MetricCard
            title="Tests Taken"
            value={data?.metrics.testsTaken.toLocaleString() || 0}
            subtitle={`${data?.metrics.completionRate || 0}% completion rate`}
            icon={<AssignmentIcon />}
            color={COLORS.success}
            trend="8.2%"
            trendUp={true}
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={4}>
          <MetricCard
            title="Average Score"
            value={`${data?.metrics.avgScore || 0}%`}
            subtitle={`${formatTime(data?.metrics.avgCompletionTime || 0)} avg time`}
            icon={<TrendingUpIcon />}
            color={COLORS.info}
            trend="3.1%"
            trendUp={true}
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
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <AreaChart data={data?.userActivity}>
                  <defs>
                    <linearGradient id="colorNewUsers" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={COLORS.primary} stopOpacity={0.8} />
                      <stop offset="95%" stopColor={COLORS.primary} stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="colorActiveUsers" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={COLORS.success} stopOpacity={0.8} />
                      <stop offset="95%" stopColor={COLORS.success} stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E0E0E0" />
                  <XAxis dataKey="date" stroke={COLORS.textSecondary} />
                  <YAxis stroke={COLORS.textSecondary} />
                  <Tooltip
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
                    dataKey="activeUsers"
                    stroke={COLORS.success}
                    fillOpacity={1}
                    fill="url(#colorActiveUsers)"
                    name="Active Users"
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
        <Grid xs={12} lg={4}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Score Distribution
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <PieChart>
                  <Pie
                    data={data?.scoreDistribution}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="count"
                    nameKey="range"
                  >
                    {data?.scoreDistribution.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
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
              Test Performance
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={data?.testPerformance}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E0E0E0" />
                  <XAxis dataKey="testName" stroke={COLORS.textSecondary} />
                  <YAxis stroke={COLORS.textSecondary} />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: COLORS.card,
                      border: `1px solid ${COLORS.primary}`,
                      borderRadius: 8,
                    }}
                  />
                  <Legend />
                  <Bar dataKey="attempts" fill={COLORS.primary} name="Attempts" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="completions" fill={COLORS.success} name="Completions" radius={[4, 4, 0, 0]} />
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
                    <TableCell align="center">Attempts</TableCell>
                    <TableCell align="center">Completion</TableCell>
                    <TableCell align="center">Avg Score</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {isLoading ? (
                    [...Array(5)].map((_, i) => (
                      <TableRow key={i}>
                        {[...Array(5)].map((_, j) => (
                          <TableCell key={j}>
                            <Skeleton variant="text" />
                          </TableCell>
                        ))}
                      </TableRow>
                    ))
                  ) : (
                    data?.topTests.map((test) => (
                      <TableRow key={test.id} hover>
                        <TableCell>
                          <Typography variant="body2" fontWeight={500}>
                            {test.name}
                          </Typography>
                        </TableCell>
                        <TableCell>
                          <Chip label={test.category} size="small" />
                        </TableCell>
                        <TableCell align="center">{test.attempts.toLocaleString()}</TableCell>
                        <TableCell align="center">{test.completionRate}%</TableCell>
                        <TableCell align="center">
                          <Chip
                            label={`${test.avgScore}%`}
                            size="small"
                            sx={{
                              backgroundColor:
                                test.avgScore >= 80
                                  ? `${COLORS.success}20`
                                  : test.avgScore >= 60
                                  ? `${COLORS.warning}20`
                                  : `${COLORS.error}20`,
                              color:
                                test.avgScore >= 80
                                  ? COLORS.success
                                  : test.avgScore >= 60
                                  ? COLORS.warning
                                  : COLORS.error,
                            }}
                          />
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>

        {/* Most Active Users */}
        <Grid xs={12} lg={6}>
          <Paper sx={{ p: 4 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Most Active Users
            </Typography>
            <TableContainer>
              <Table size="small">
                <TableHead>
                  <TableRow sx={{ backgroundColor: '#FAFAFA' }}>
                    <TableCell>User</TableCell>
                    <TableCell align="center">Tests</TableCell>
                    <TableCell align="center">Avg Score</TableCell>
                    <TableCell align="center">Time Spent</TableCell>
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
                  ) : (
                    data?.topUsers.map((user) => (
                      <TableRow key={user.id} hover>
                        <TableCell>
                          <Box display="flex" alignItems="center" gap={1.5}>
                            <Box
                              sx={{
                                width: 32,
                                height: 32,
                                borderRadius: '50%',
                                backgroundColor: `${COLORS.primary}20`,
                                color: COLORS.primary,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '0.875rem',
                                fontWeight: 500,
                              }}
                            >
                              {user.name.charAt(0)}
                            </Box>
                            <Box>
                              <Typography variant="body2" fontWeight={500}>
                                {user.name}
                              </Typography>
                              <Typography variant="caption" color="text.secondary">
                                {user.email}
                              </Typography>
                            </Box>
                          </Box>
                        </TableCell>
                        <TableCell align="center">{user.testsCompleted}</TableCell>
                        <TableCell align="center">
                          <Chip
                            label={`${user.avgScore}%`}
                            size="small"
                            sx={{
                              backgroundColor:
                                user.avgScore >= 80
                                  ? `${COLORS.success}20`
                                  : user.avgScore >= 60
                                  ? `${COLORS.warning}20`
                                  : `${COLORS.error}20`,
                              color:
                                user.avgScore >= 80
                                  ? COLORS.success
                                  : user.avgScore >= 60
                                  ? COLORS.warning
                                  : COLORS.error,
                            }}
                          />
                        </TableCell>
                        <TableCell align="center">{formatTime(user.totalTime)}</TableCell>
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

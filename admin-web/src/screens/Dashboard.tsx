import React from 'react';
import {
  Box,
  Grid,
  Typography,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Alert,
  Paper,
  useTheme,
  useMediaQuery,
  alpha,
} from '@mui/material';
import {
  People as PeopleIcon,
  Assignment as TestIcon,
  TrendingUp as TrendingUpIcon,
  AccessTime as AccessTimeIcon,
  Add as AddIcon,
  PersonAdd as PersonAddIcon,
  CheckCircle as CheckCircleIcon,
  PlayArrow as PlayArrowIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Legend,
} from 'recharts';
import { StatsCard } from '../components/data';
import { getAdminAnalytics, getAdminDailyActivity, getRecentActivity } from '../api/client';
import type { DailyActivity, RecentActivityItem } from '../types';

// Types
interface DashboardStats {
  totalUsers: number;
  totalTests: number;
  completionRate: number;
  avgSessionTime: number;
  userGrowth: { date: string; users: number }[];
  testCompletions: { testName: string; completions: number; attempts: number }[];
}

interface Activity {
  id: string;
  type: 'user_registered' | 'test_completed' | 'test_created' | 'test_edited' | 'test_deleted';
  description: string;
  timestamp: string;
  user?: string;
}

// Transform API activity to UI activity
const transformActivity = (item: RecentActivityItem): Activity => {
  const typeMap: Record<string, Activity['type']> = {
    'NEW_USER': 'user_registered',
    'TEST_COMPLETED': 'test_completed',
    'TEST_CREATED': 'test_created',
    'TEST_EDITED': 'test_edited',
    'ACHIEVEMENT': 'test_completed',
  };
  
  return {
    id: `${item.timestamp}-${item.userName}`,
    type: typeMap[item.type] || 'test_completed',
    description: item.details || `${item.type} by ${item.userName}`,
    timestamp: item.timestamp,
    user: item.userName,
  };
};

// Fetch real dashboard data
const fetchDashboardData = async (): Promise<{ stats: DashboardStats; activities: Activity[] }> => {
  try {
    const [analytics, dailyActivity, recentActivity] = await Promise.all([
      getAdminAnalytics(),
      getAdminDailyActivity(7),
      getRecentActivity(10),
    ]);

    // Transform daily activity to user growth format
    const userGrowth = dailyActivity.map((day: DailyActivity, index: number) => ({
      date: new Date(day.date).toLocaleDateString('en-US', { weekday: 'short' }),
      users: analytics.totalUsers - (dailyActivity.length - index - 1) * 50, // Approximation
    }));

    // Mock test completions (not available in API yet)
    const testCompletions = [
      { testName: 'Basic Grammar', completions: 856, attempts: 1000 },
      { testName: 'Vocabulary A1', completions: 742, attempts: 900 },
      { testName: 'Listening B1', completions: 634, attempts: 800 },
      { testName: 'Reading B2', completions: 521, attempts: 700 },
      { testName: 'Writing C1', completions: 412, attempts: 600 },
    ];

    return {
      stats: {
        totalUsers: Number(analytics.totalUsers),
        totalTests: Number(analytics.totalTests),
        completionRate: 78.5, // Mock - not in API yet
        avgSessionTime: 24.3, // Mock - not in API yet
        userGrowth,
        testCompletions,
      },
      activities: recentActivity.map(transformActivity),
    };
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error);
    // Return fallback data
    return {
      stats: {
        totalUsers: 0,
        totalTests: 0,
        completionRate: 0,
        avgSessionTime: 0,
        userGrowth: [],
        testCompletions: [],
      },
      activities: [],
    };
  }
};

// Activity Icon Component
const ActivityIcon: React.FC<{ type: Activity['type'] }> = ({ type }) => {
  const theme = useTheme();
  
  const iconProps = { fontSize: 'small' as const };
  
  switch (type) {
    case 'user_registered':
      return <PersonAddIcon {...iconProps} sx={{ color: theme.palette.info.main }} />;
    case 'test_completed':
      return <CheckCircleIcon {...iconProps} sx={{ color: theme.palette.success.main }} />;
    case 'test_created':
      return <AddIcon {...iconProps} sx={{ color: theme.palette.primary.main }} />;
    case 'test_edited':
      return <EditIcon {...iconProps} sx={{ color: theme.palette.warning.main }} />;
    case 'test_deleted':
      return <DeleteIcon {...iconProps} sx={{ color: theme.palette.error.main }} />;
    default:
      return <PlayArrowIcon {...iconProps} />;
  }
};

// Activity Type Label
const getActivityTypeLabel = (type: Activity['type']): string => {
  const labels: Record<Activity['type'], string> = {
    user_registered: 'User',
    test_completed: 'Completion',
    test_created: 'Created',
    test_edited: 'Edited',
    test_deleted: 'Deleted',
  };
  return labels[type];
};

// Format relative time
const formatRelativeTime = (timestamp: string): string => {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMs / 3600000);
  const diffDays = Math.floor(diffMs / 86400000);

  if (diffMins < 1) return 'Just now';
  if (diffMins < 60) return `${diffMins}m ago`;
  if (diffHours < 24) return `${diffHours}h ago`;
  if (diffDays < 7) return `${diffDays}d ago`;
  return date.toLocaleDateString();
};

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  
  const { data, isLoading, error } = useQuery({
    queryKey: ['dashboard'],
    queryFn: fetchDashboardData,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  const stats = data?.stats;
  const activities = data?.activities || [];

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error" sx={{ mb: 2 }}>
          Failed to load dashboard data. Please try again later.
        </Alert>
        <Button variant="contained" onClick={() => window.location.reload()}>
          Retry
        </Button>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 3, md: 4 }}>
      {/* Header */}
      <Box 
        display="flex" 
        flexDirection={{ xs: 'column', sm: 'row' }} 
        justifyContent="space-between" 
        alignItems={{ xs: 'flex-start', sm: 'center' }} 
        mb={3} 
        gap={2}
      >
        <Typography variant="h4" fontWeight="bold" color="text.primary" data-testid="page-title">
          Dashboard
        </Typography>
        <Box display="flex" gap={1} flexWrap="wrap">
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/speaking/libraries/new')}
            data-testid="dashboard-new-library-button"
          >
            Add Library
          </Button>
          <Button
            variant="outlined"
            startIcon={<PersonAddIcon />}
            onClick={() => navigate('/users')}
            data-testid="dashboard-new-user-button"
          >
            Add User
          </Button>
        </Box>
      </Box>

      {/* Stats Cards - Using new Design System StatsCard */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Total Users"
            value={stats?.totalUsers.toLocaleString() || 0}
            icon={PeopleIcon}
            change={{ value: 12.5, isPositive: true, label: 'vs last week' }}
            variant="primary"
            chartType="sparkline"
            chartData={[65, 78, 90, 81, 96, 105, 120]}
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Total Tests"
            value={stats?.totalTests.toLocaleString() || 0}
            icon={TestIcon}
            change={{ value: 8.2, isPositive: true, label: 'vs last week' }}
            variant="info"
            chartType="sparkline"
            chartData={[45, 52, 49, 60, 55, 65, 68]}
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Completion Rate"
            value={`${stats?.completionRate || 0}%`}
            icon={TrendingUpIcon}
            change={{ value: 3.1, isPositive: true, label: 'vs last week' }}
            variant="success"
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Avg Session"
            value={`${stats?.avgSessionTime || 0}m`}
            icon={AccessTimeIcon}
            change={{ value: 5.4, isPositive: false, label: 'vs last week' }}
            variant="warning"
            loading={isLoading}
          />
        </Grid>
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12} lg={8}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              User Growth
            </Typography>
            {isLoading ? (
              <Box height={320} display="flex" alignItems="center" justifyContent="center">
                <Typography color="text.secondary">Loading chart...</Typography>
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <LineChart data={stats?.userGrowth}>
                  <CartesianGrid strokeDasharray="3 3" stroke={alpha(theme.palette.divider, 0.5)} />
                  <XAxis 
                    dataKey="date" 
                    stroke={theme.palette.text.secondary}
                    tick={{ fill: theme.palette.text.secondary }}
                  />
                  <YAxis 
                    stroke={theme.palette.text.secondary}
                    tick={{ fill: theme.palette.text.secondary }}
                  />
                  <Tooltip
                    contentStyle={{
                      backgroundColor: theme.palette.background.paper,
                      border: `1px solid ${theme.palette.divider}`,
                      borderRadius: 8,
                      boxShadow: theme.shadows[4],
                    }}
                  />
                  <Line
                    type="monotone"
                    dataKey="users"
                    stroke={theme.palette.primary.main}
                    strokeWidth={3}
                    dot={{ fill: theme.palette.primary.main, strokeWidth: 2, r: 4 }}
                    activeDot={{ r: 6, fill: theme.palette.primary.main }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
        <Grid xs={12} lg={4}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Test Completions
            </Typography>
            {isLoading ? (
              <Box height={320} display="flex" alignItems="center" justifyContent="center">
                <Typography color="text.secondary">Loading chart...</Typography>
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart 
                  data={stats?.testCompletions} 
                  layout={isMobile ? 'vertical' : 'horizontal'}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke={alpha(theme.palette.divider, 0.5)} />
                  {isMobile ? (
                    <>
                      <XAxis type="number" stroke={theme.palette.text.secondary} />
                      <YAxis 
                        dataKey="testName" 
                        type="category" 
                        width={80} 
                        stroke={theme.palette.text.secondary}
                        tick={{ fill: theme.palette.text.secondary, fontSize: 11 }}
                      />
                    </>
                  ) : (
                    <>
                      <XAxis 
                        dataKey="testName" 
                        stroke={theme.palette.text.secondary}
                        tick={{ fill: theme.palette.text.secondary, fontSize: 11 }}
                      />
                      <YAxis stroke={theme.palette.text.secondary} />
                    </>
                  )}
                  <Tooltip
                    contentStyle={{
                      backgroundColor: theme.palette.background.paper,
                      border: `1px solid ${theme.palette.divider}`,
                      borderRadius: 8,
                      boxShadow: theme.shadows[4],
                    }}
                  />
                  <Legend />
                  <Bar 
                    dataKey="completions" 
                    fill={theme.palette.success.main} 
                    name="Completed" 
                    radius={[4, 4, 0, 0]} 
                  />
                  <Bar 
                    dataKey="attempts" 
                    fill={theme.palette.primary.main} 
                    name="Attempts" 
                    radius={[4, 4, 0, 0]} 
                  />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Recent Activity */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" fontWeight="bold" mb={2}>
          Recent Activity
        </Typography>
        {isLoading ? (
          <Box py={4} textAlign="center">
            <Typography color="text.secondary">Loading activity...</Typography>
          </Box>
        ) : activities.length === 0 ? (
          <Box py={4} textAlign="center">
            <Typography color="text.secondary">No recent activity</Typography>
          </Box>
        ) : (
          <List sx={{ maxHeight: 400, overflow: 'auto' }}>
            {activities.map((activity, index) => (
              <ListItem
                key={activity.id}
                divider={index < activities.length - 1}
                sx={{
                  '&:hover': { 
                    backgroundColor: alpha(theme.palette.primary.main, 0.04),
                  },
                  borderRadius: 1,
                  transition: 'background-color 0.2s',
                }}
              >
                <ListItemIcon>
                  <ActivityIcon type={activity.type} />
                </ListItemIcon>
                <ListItemText
                  primary={
                    <Box display="flex" alignItems="center" gap={1}>
                      <Typography variant="body1" fontWeight={500}>
                        {activity.description}
                      </Typography>
                      <Chip
                        label={getActivityTypeLabel(activity.type)}
                        size="small"
                        sx={{
                          backgroundColor: alpha(
                            activity.type === 'user_registered'
                              ? theme.palette.info.main
                              : activity.type === 'test_completed'
                              ? theme.palette.success.main
                              : activity.type === 'test_created'
                              ? theme.palette.primary.main
                              : activity.type === 'test_edited'
                              ? theme.palette.warning.main
                              : theme.palette.error.main,
                            0.1
                          ),
                          color:
                            activity.type === 'user_registered'
                              ? theme.palette.info.main
                              : activity.type === 'test_completed'
                              ? theme.palette.success.main
                              : activity.type === 'test_created'
                              ? theme.palette.primary.main
                              : activity.type === 'test_edited'
                              ? theme.palette.warning.main
                              : theme.palette.error.main,
                          fontSize: '0.7rem',
                          height: 20,
                          fontWeight: 600,
                        }}
                      />
                    </Box>
                  }
                  secondary={
                    <Typography variant="body2" color="text.secondary">
                      {activity.user && `${activity.user} • `}
                      {formatRelativeTime(activity.timestamp)}
                    </Typography>
                  }
                />
              </ListItem>
            ))}
          </List>
        )}
      </Paper>
    </Box>
  );
};

export default Dashboard;

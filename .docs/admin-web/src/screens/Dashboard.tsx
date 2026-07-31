import React from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  Chip,
  Skeleton,
  Alert,
  Paper,
  useTheme,
  useMediaQuery,
} from '@mui/material';
import {
  People as PeopleIcon,
  Assignment as TestIcon,
  TrendingUp as TrendingUpIcon,
  AccessTime as AccessTimeIcon,
  Add as AddIcon,
  Category as CategoryIcon,
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

// Mock API
const fetchDashboardData = async (): Promise<{ stats: DashboardStats; activities: Activity[] }> => {
  // Simulate API delay
  await new Promise((resolve) => setTimeout(resolve, 800));
  
  return {
    stats: {
      totalUsers: 12458,
      totalTests: 342,
      completionRate: 78.5,
      avgSessionTime: 24.3,
      userGrowth: [
        { date: 'Mon', users: 11000 },
        { date: 'Tue', users: 11200 },
        { date: 'Wed', users: 11500 },
        { date: 'Thu', users: 11800 },
        { date: 'Fri', users: 12000 },
        { date: 'Sat', users: 12200 },
        { date: 'Sun', users: 12458 },
      ],
      testCompletions: [
        { testName: 'Basic Grammar', completions: 856, attempts: 1000 },
        { testName: 'Vocabulary A1', completions: 742, attempts: 900 },
        { testName: 'Listening B1', completions: 634, attempts: 800 },
        { testName: 'Reading B2', completions: 521, attempts: 700 },
        { testName: 'Writing C1', completions: 412, attempts: 600 },
      ],
    },
    activities: [
      { id: '1', type: 'user_registered', description: 'New user registered', timestamp: '2024-01-15T10:30:00Z', user: 'john.doe@example.com' },
      { id: '2', type: 'test_completed', description: 'Completed "Basic Grammar" test', timestamp: '2024-01-15T10:25:00Z', user: 'jane.smith@example.com' },
      { id: '3', type: 'test_created', description: 'Created new test "Advanced Idioms"', timestamp: '2024-01-15T09:45:00Z', user: 'admin@funnyenglish.com' },
      { id: '4', type: 'test_completed', description: 'Completed "Vocabulary A1" test', timestamp: '2024-01-15T09:30:00Z', user: 'mike.wilson@example.com' },
      { id: '5', type: 'test_edited', description: 'Updated "Listening B1" questions', timestamp: '2024-01-15T09:15:00Z', user: 'admin@funnyenglish.com' },
      { id: '6', type: 'user_registered', description: 'New user registered', timestamp: '2024-01-15T08:50:00Z', user: 'sarah.jones@example.com' },
      { id: '7', type: 'test_completed', description: 'Completed "Reading B2" test', timestamp: '2024-01-15T08:30:00Z', user: 'tom.brown@example.com' },
      { id: '8', type: 'test_deleted', description: 'Deleted "Old Practice Test"', timestamp: '2024-01-15T08:00:00Z', user: 'admin@funnyenglish.com' },
      { id: '9', type: 'test_completed', description: 'Completed "Writing C1" test', timestamp: '2024-01-15T07:45:00Z', user: 'emma.davis@example.com' },
      { id: '10', type: 'user_registered', description: 'New user registered', timestamp: '2024-01-15T07:30:00Z', user: 'chris.miller@example.com' },
    ],
  };
};

// Stats Card Component
interface StatsCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  trend?: string;
  trendUp?: boolean;
  color: string;
  loading?: boolean;
}

const StatsCard: React.FC<StatsCardProps> = ({ title, value, icon, trend, trendUp, color, loading }) => {
  if (loading) {
    return (
      <Card sx={{ height: '100%' }}>
        <CardContent>
          <Skeleton variant="text" width="60%" height={24} />
          <Skeleton variant="text" width="40%" height={48} sx={{ mt: 1 }} />
          <Skeleton variant="text" width="50%" height={20} sx={{ mt: 1 }} />
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ height: '100%', transition: 'transform 0.2s', '&:hover': { transform: 'translateY(-4px)', boxShadow: 4 } }}>
      <CardContent>
        <Box display="flex" alignItems="center" justifyContent="space-between">
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              {title}
            </Typography>
            <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
              {value}
            </Typography>
            {trend && (
              <Typography variant="body2" color={trendUp ? COLORS.success : COLORS.error} sx={{ mt: 0.5 }}>
                {trendUp ? '+' : ''}{trend} from last week
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              width: 56,
              height: 56,
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

// Activity Icon Component
const ActivityIcon: React.FC<{ type: Activity['type'] }> = ({ type }) => {
  const iconProps = { fontSize: 'small' as const };
  
  switch (type) {
    case 'user_registered':
      return <PersonAddIcon {...iconProps} sx={{ color: COLORS.info }} />;
    case 'test_completed':
      return <CheckCircleIcon {...iconProps} sx={{ color: COLORS.success }} />;
    case 'test_created':
      return <AddIcon {...iconProps} sx={{ color: COLORS.primary }} />;
    case 'test_edited':
      return <EditIcon {...iconProps} sx={{ color: COLORS.warning }} />;
    case 'test_deleted':
      return <DeleteIcon {...iconProps} sx={{ color: COLORS.error }} />;
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
    <Box p={{ xs: 2, md: 3 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
          Dashboard
        </Typography>
        <Box display="flex" gap={1} flexWrap="wrap">
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/tests/new')}
            sx={{ backgroundColor: COLORS.primary }}
          >
            New Test
          </Button>
          <Button
            variant="outlined"
            startIcon={<CategoryIcon />}
            onClick={() => navigate('/categories')}
            sx={{ borderColor: COLORS.primary, color: COLORS.primary }}
          >
            New Category
          </Button>
          <Button
            variant="outlined"
            startIcon={<PersonAddIcon />}
            onClick={() => navigate('/users')}
            sx={{ borderColor: COLORS.primary, color: COLORS.primary }}
          >
            New User
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3} mb={3}>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatsCard
            title="Total Users"
            value={stats?.totalUsers.toLocaleString() || 0}
            icon={<PeopleIcon fontSize="large" />}
            trend="12.5%"
            trendUp={true}
            color={COLORS.primary}
            loading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatsCard
            title="Total Tests"
            value={stats?.totalTests.toLocaleString() || 0}
            icon={<TestIcon fontSize="large" />}
            trend="8.2%"
            trendUp={true}
            color={COLORS.info}
            loading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatsCard
            title="Completion Rate"
            value={`${stats?.completionRate || 0}%`}
            icon={<TrendingUpIcon fontSize="large" />}
            trend="3.1%"
            trendUp={true}
            color={COLORS.success}
            loading={isLoading}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6, lg: 3 }}>
          <StatsCard
            title="Avg Session"
            value={`${stats?.avgSessionTime || 0}m`}
            icon={<AccessTimeIcon fontSize="large" />}
            trend="5.4%"
            trendUp={false}
            color={COLORS.warning}
            loading={isLoading}
          />
        </Grid>
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} mb={3}>
        <Grid size={{ xs: 12, lg: 8 }}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              User Growth
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <LineChart data={stats?.userGrowth}>
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
                  <Line
                    type="monotone"
                    dataKey="users"
                    stroke={COLORS.primary}
                    strokeWidth={3}
                    dot={{ fill: COLORS.primary, strokeWidth: 2, r: 4 }}
                    activeDot={{ r: 6, fill: COLORS.primary }}
                  />
                </LineChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, lg: 4 }}>
          <Paper sx={{ p: 3, height: 400 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Test Completions
            </Typography>
            {isLoading ? (
              <Skeleton variant="rectangular" height={320} />
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={stats?.testCompletions} layout={isMobile ? 'vertical' : 'horizontal'}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#E0E0E0" />
                  {isMobile ? (
                    <>
                      <XAxis type="number" stroke={COLORS.textSecondary} />
                      <YAxis dataKey="testName" type="category" width={80} stroke={COLORS.textSecondary} fontSize={11} />
                    </>
                  ) : (
                    <>
                      <XAxis dataKey="testName" stroke={COLORS.textSecondary} fontSize={11} />
                      <YAxis stroke={COLORS.textSecondary} />
                    </>
                  )}
                  <Tooltip
                    contentStyle={{
                      backgroundColor: COLORS.card,
                      border: `1px solid ${COLORS.primary}`,
                      borderRadius: 8,
                    }}
                  />
                  <Legend />
                  <Bar dataKey="completions" fill={COLORS.success} name="Completed" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="attempts" fill={COLORS.primary} name="Attempts" radius={[4, 4, 0, 0]} />
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
          <>
            {[1, 2, 3, 4, 5].map((i) => (
              <Skeleton key={i} variant="rectangular" height={60} sx={{ mb: 1 }} />
            ))}
          </>
        ) : (
          <List sx={{ maxHeight: 400, overflow: 'auto' }}>
            {activities.map((activity, index) => (
              <ListItem
                key={activity.id}
                divider={index < activities.length - 1}
                sx={{
                  '&:hover': { backgroundColor: 'rgba(74, 144, 217, 0.05)' },
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
                          backgroundColor:
                            activity.type === 'user_registered'
                              ? `${COLORS.info}20`
                              : activity.type === 'test_completed'
                              ? `${COLORS.success}20`
                              : activity.type === 'test_created'
                              ? `${COLORS.primary}20`
                              : activity.type === 'test_edited'
                              ? `${COLORS.warning}20`
                              : `${COLORS.error}20`,
                          color:
                            activity.type === 'user_registered'
                              ? COLORS.info
                              : activity.type === 'test_completed'
                              ? COLORS.success
                              : activity.type === 'test_created'
                              ? COLORS.primary
                              : activity.type === 'test_edited'
                              ? COLORS.warning
                              : COLORS.error,
                          fontSize: '0.7rem',
                          height: 20,
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

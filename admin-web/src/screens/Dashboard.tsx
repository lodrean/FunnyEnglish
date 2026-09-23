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
  alpha,
} from '@mui/material';
import {
  People as PeopleIcon,
  Mic as MicIcon,
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
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  Cell,
} from 'recharts';
import { StatsCard } from '../components/data';
import { getAdminAnalytics, getRecentActivity } from '../api/client';
import { getSubmissions } from '../api/speakingApi';
import type { RecentActivityItem } from '../types';
import { formatRelativeTime } from '../utils/format';

// Types — только реальные speaking-метрики (аудит D-1: моки запрещены)
interface DashboardStats {
  totalStudents: number;
  activeStudents7d: number;
  totalSubmissions: number;
  pendingReview: number;
  reviewedCount: number;
  submissionsPerDay: { date: string; submissions: number }[];
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
    description: item.details || `${item.userName}: ${item.type}`,
    timestamp: item.timestamp,
    user: item.userName,
  };
};

const DAY_MS = 24 * 60 * 60 * 1000;
// Backend ждёт LocalDate 'yyyy-MM-dd' (границы дней — UTC)
const toIsoDate = (d: Date): string => d.toISOString().slice(0, 10);

// Fetch real dashboard data. Ошибки НЕ проглатываем — их показывает error-state useQuery.
const fetchDashboardData = async (): Promise<{ stats: DashboardStats; activities: Activity[] }> => {
  const now = Date.now();
  const days = Array.from({ length: 7 }, (_, i) => toIsoDate(new Date(now - (6 - i) * DAY_MS)));

  const [analytics, recentActivity, allSubmissions, newSubmissions, reviewedSubmissions, weekSubmissions, ...dailyCounts] =
    await Promise.all([
      getAdminAnalytics(),
      getRecentActivity(10),
      getSubmissions({ size: 1 }),
      getSubmissions({ status: 'NEW', size: 1 }),
      getSubmissions({ status: 'REVIEWED', size: 1 }),
      // Последние ≤100 отправок за 7 дней — для оценки активных учеников (backend cap size=100)
      getSubmissions({ from: days[0], size: 100 }),
      // Точный count отправок на каждый день через totalElements (size=1 — минимальный ответ)
      ...days.map((day) => getSubmissions({ from: day, to: day, size: 1 })),
    ]);

  const activeStudents7d = new Set(weekSubmissions.content.map((s) => s.student.id)).size;

  return {
    stats: {
      totalStudents: Number(analytics.totalUsers),
      activeStudents7d,
      totalSubmissions: allSubmissions.totalElements,
      pendingReview: newSubmissions.totalElements,
      reviewedCount: reviewedSubmissions.totalElements,
      submissionsPerDay: days.map((day, i) => ({
        date: new Date(day).toLocaleDateString('ru-RU', { weekday: 'short' }),
        submissions: dailyCounts[i].totalElements,
      })),
    },
    activities: recentActivity.map(transformActivity),
  };
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
    user_registered: 'Пользователь',
    test_completed: 'Завершение',
    test_created: 'Создание',
    test_edited: 'Изменение',
    test_deleted: 'Удаление',
  };
  return labels[type];
};


const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const theme = useTheme();
  
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
          Не удалось загрузить данные дашборда. Попробуйте позже.
        </Alert>
        <Button variant="contained" onClick={() => window.location.reload()}>
          Повторить
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
          Дашборд
        </Typography>
        <Box display="flex" gap={1} flexWrap="wrap">
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate('/speaking/libraries/new')}
            data-testid="dashboard-new-library-button"
          >
            Добавить тему
          </Button>
          <Button
            variant="outlined"
            startIcon={<PersonAddIcon />}
            onClick={() => navigate('/users')}
            data-testid="dashboard-new-user-button"
          >
            Добавить пользователя
          </Button>
        </Box>
      </Box>

      {/* Stats Cards - реальные метрики, без вымышленных дельт */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Всего учеников"
            value={(stats?.totalStudents ?? 0).toLocaleString()}
            icon={PeopleIcon}
            variant="primary"
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Активные ученики (7 дн.)"
            value={(stats?.activeStudents7d ?? 0).toLocaleString()}
            icon={TrendingUpIcon}
            variant="success"
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Отправки практики"
            value={(stats?.totalSubmissions ?? 0).toLocaleString()}
            icon={MicIcon}
            variant="info"
            loading={isLoading}
          />
        </Grid>
        <Grid xs={12} sm={6} lg={3}>
          <StatsCard
            title="Ожидают проверки"
            value={(stats?.pendingReview ?? 0).toLocaleString()}
            icon={AccessTimeIcon}
            variant="warning"
            loading={isLoading}
          />
        </Grid>
      </Grid>

      {/* Charts Row — реальные данные по speaking-отправкам */}
      <Grid container spacing={3} mb={3}>
        <Grid xs={12} lg={8}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Отправки по дням (последние 7 дней)
            </Typography>
            {isLoading ? (
              <Box height={320} display="flex" alignItems="center" justifyContent="center">
                <Typography color="text.secondary">Загрузка графика...</Typography>
              </Box>
            ) : !stats || stats.totalSubmissions === 0 ? (
              <Box height={320} display="flex" alignItems="center" justifyContent="center">
                <Typography color="text.secondary">
                  Практики ещё нет — график появится после первой записи ученика
                </Typography>
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart data={stats.submissionsPerDay}>
                  <CartesianGrid strokeDasharray="3 3" stroke={alpha(theme.palette.divider, 0.5)} />
                  <XAxis
                    dataKey="date"
                    stroke={theme.palette.text.secondary}
                    tick={{ fill: theme.palette.text.secondary }}
                  />
                  <YAxis
                    allowDecimals={false}
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
                  <Bar
                    dataKey="submissions"
                    fill={theme.palette.primary.main}
                    name="Отправки"
                    radius={[4, 4, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
        <Grid xs={12} lg={4}>
          <Paper sx={{ p: 4, height: 420 }}>
            <Typography variant="h6" fontWeight="bold" mb={2}>
              Статус проверки
            </Typography>
            {isLoading ? (
              <Box height={320} display="flex" alignItems="center" justifyContent="center">
                <Typography color="text.secondary">Загрузка графика...</Typography>
              </Box>
            ) : !stats || stats.totalSubmissions === 0 ? (
              <Box height={320} display="flex" alignItems="center" justifyContent="center">
                <Typography color="text.secondary">Нет отправок на проверку</Typography>
              </Box>
            ) : (
              <ResponsiveContainer width="100%" height={320}>
                <BarChart
                  data={[
                    { name: 'Ожидают', count: stats.pendingReview },
                    { name: 'Проверены', count: stats.reviewedCount },
                  ]}
                >
                  <CartesianGrid strokeDasharray="3 3" stroke={alpha(theme.palette.divider, 0.5)} />
                  <XAxis
                    dataKey="name"
                    stroke={theme.palette.text.secondary}
                    tick={{ fill: theme.palette.text.secondary, fontSize: 12 }}
                  />
                  <YAxis
                    allowDecimals={false}
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
                  <Bar dataKey="count" name="Отправки" radius={[4, 4, 0, 0]}>
                    <Cell fill={theme.palette.warning.main} />
                    <Cell fill={theme.palette.success.main} />
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </Paper>
        </Grid>
      </Grid>

      {/* Recent Activity */}
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" fontWeight="bold" mb={2}>
          Последняя активность
        </Typography>
        {isLoading ? (
          <Box py={4} textAlign="center">
            <Typography color="text.secondary">Загрузка активности...</Typography>
          </Box>
        ) : activities.length === 0 ? (
          <Box py={4} textAlign="center">
            <Typography color="text.secondary">Нет недавней активности</Typography>
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

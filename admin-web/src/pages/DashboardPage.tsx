import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  CircularProgress,
} from '@mui/material';
import {
  Quiz,
  People,
  TrendingUp,
  EmojiEvents,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import {
  getAdminAnalytics,
  getPopularTests,
  getRecentActivity,
} from '../api/client';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
  change?: string;
}

function StatCard({ title, value, icon, color, change }: StatCardProps) {
  return (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
          <Box>
            <Typography color="text.secondary" variant="body2">
              {title}
            </Typography>
            <Typography variant="h4" fontWeight="bold" sx={{ my: 1 }}>
              {value}
            </Typography>
            {change && (
              <Typography variant="body2" color="success.main">
                {change}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              p: 1.5,
              borderRadius: 2,
              bgcolor: `${color}15`,
              color,
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
}

function formatTimeAgo(value?: string) {
  if (!value) {
    return '—';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  const diffMs = Date.now() - date.getTime();
  if (diffMs < 0) {
    return date.toLocaleString('ru-RU');
  }

  const minutes = Math.floor(diffMs / 60000);
  if (minutes < 1) {
    return 'только что';
  }
  if (minutes < 60) {
    return `${minutes} мин назад`;
  }

  const hours = Math.floor(minutes / 60);
  if (hours < 24) {
    return `${hours} ч назад`;
  }

  const days = Math.floor(hours / 24);
  if (days < 30) {
    return `${days} дн назад`;
  }

  return date.toLocaleString('ru-RU');
}

export default function DashboardPage() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['dashboard'],
    queryFn: getAdminAnalytics,
  });

  const {
    data: popularTests,
    isLoading: isPopularLoading,
    error: popularError,
  } = useQuery({
    queryKey: ['popular-tests'],
    queryFn: () => getPopularTests(5),
  });

  const {
    data: recentActivity,
    isLoading: isActivityLoading,
    error: activityError,
  } = useQuery({
    queryKey: ['recent-activity'],
    queryFn: () => getRecentActivity(10),
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError || !data) {
    return (
      <Box>
        <Typography variant="h5" fontWeight="bold" mb={3}>
          Дашборд
        </Typography>
        <Typography color="text.secondary">Не удалось загрузить данные</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" mb={3}>
        Дашборд
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Всего тестов"
            value={data.totalTests}
            icon={<Quiz />}
            color="#4FC3F7"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Пользователей"
            value={data.totalUsers}
            icon={<People />}
            color="#FF9800"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Прохождений"
            value={data.totalCompletions}
            icon={<TrendingUp />}
            color="#4CAF50"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Достижений выдано"
            value={data.totalAchievements}
            icon={<EmojiEvents />}
            color="#9C27B0"
          />
        </Grid>
      </Grid>

      <Grid container spacing={3} mt={2}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Популярные тесты
              </Typography>
              {isPopularLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                  <CircularProgress size={24} />
                </Box>
              ) : popularError ? (
                <Typography variant="body2" color="text.secondary">
                  Не удалось загрузить популярные тесты
                </Typography>
              ) : popularTests && popularTests.length > 0 ? (
                <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                  {popularTests.map((test) => (
                    <Box
                      key={test.id}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        py: 1,
                        '&:not(:last-of-type)': {
                          borderBottom: '1px solid',
                          borderColor: 'divider',
                        },
                      }}
                    >
                      <Typography fontWeight="medium">
                        {test.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {test.completions} прохождений
                      </Typography>
                    </Box>
                  ))}
                </Box>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  Нет данных
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Последняя активность
              </Typography>
              {isActivityLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                  <CircularProgress size={24} />
                </Box>
              ) : activityError ? (
                <Typography variant="body2" color="text.secondary">
                  Не удалось загрузить активность
                </Typography>
              ) : recentActivity && recentActivity.length > 0 ? (
                <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                  {recentActivity.map((item, index) => (
                    <Box
                      key={index}
                      sx={{
                        py: 1,
                        '&:not(:last-of-type)': {
                          borderBottom: '1px solid',
                          borderColor: 'divider',
                        },
                      }}
                    >
                      <Typography fontWeight="medium">
                        {item.userName}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {item.type}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatTimeAgo(item.timestamp)}
                      </Typography>
                    </Box>
                  ))}
                </Box>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  Нет данных
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

import { Box, Card, CardContent, CircularProgress, Grid, Typography } from '@mui/material';
import {
  People,
  Quiz,
  HelpOutline,
  CheckCircle,
  TrendingUp,
  Category,
  EmojiEvents,
} from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getAdminAnalytics } from '../api/client';

interface StatCardProps {
  title: string;
  value: string | number;
  icon: React.ReactNode;
  color: string;
  subtitle?: string;
}

function StatCard({ title, value, icon, color, subtitle }: StatCardProps) {
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
            {subtitle && (
              <Typography variant="body2" color="text.secondary">
                {subtitle}
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

export default function AnalyticsPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['admin-analytics'],
    queryFn: getAdminAnalytics,
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!data) {
    return (
      <Box>
        <Typography variant="h5" fontWeight="bold" mb={3}>
          Аналитика
        </Typography>
        <Typography color="text.secondary">Нет данных для отображения</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" mb={3}>
        Аналитика
      </Typography>

      <Grid container spacing={3}>
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
            title="Тестов"
            value={data.totalTests}
            icon={<Quiz />}
            color="#4FC3F7"
            subtitle={`Опубликовано: ${data.publishedTests}`}
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Вопросов"
            value={data.totalQuestions}
            icon={<HelpOutline />}
            color="#7E57C2"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Ответов"
            value={data.totalAnswers}
            icon={<CheckCircle />}
            color="#26A69A"
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
            title="Категорий"
            value={data.totalCategories}
            icon={<Category />}
            color="#FB8C00"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Достижений"
            value={data.totalAchievements}
            icon={<EmojiEvents />}
            color="#EC407A"
          />
        </Grid>
      </Grid>
    </Box>
  );
}

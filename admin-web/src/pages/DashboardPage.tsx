import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  LinearProgress,
} from '@mui/material';
import {
  Quiz,
  People,
  TrendingUp,
  EmojiEvents,
} from '@mui/icons-material';

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

export default function DashboardPage() {
  return (
    <Box>
      <Typography variant="h5" fontWeight="bold" mb={3}>
        Дашборд
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Всего тестов"
            value={24}
            icon={<Quiz />}
            color="#4FC3F7"
            change="+3 за неделю"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Пользователей"
            value={1250}
            icon={<People />}
            color="#FF9800"
            change="+120 за неделю"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Прохождений"
            value={8450}
            icon={<TrendingUp />}
            color="#4CAF50"
            change="+15% к прошлой неделе"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            title="Достижений выдано"
            value={3200}
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
              {[
                { name: 'Животные - уровень 1', completions: 450, progress: 90 },
                { name: 'Цвета для начинающих', completions: 380, progress: 76 },
                { name: 'Числа от 1 до 10', completions: 320, progress: 64 },
                { name: 'Моя семья', completions: 250, progress: 50 },
                { name: 'Еда и напитки', completions: 200, progress: 40 },
              ].map((test, index) => (
                <Box key={index} mb={2}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                    <Typography variant="body2">{test.name}</Typography>
                    <Typography variant="body2" color="text.secondary">
                      {test.completions} прохождений
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={test.progress}
                    sx={{ height: 8, borderRadius: 4 }}
                  />
                </Box>
              ))}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Последняя активность
              </Typography>
              {[
                { action: 'Новый пользователь', user: 'Маша К.', time: '5 мин назад' },
                { action: 'Тест пройден', user: 'Петя С.', time: '12 мин назад' },
                { action: 'Достижение получено', user: 'Аня В.', time: '25 мин назад' },
                { action: 'Новый пользователь', user: 'Дима Л.', time: '1 час назад' },
                { action: 'Тест пройден', user: 'Катя М.', time: '2 часа назад' },
              ].map((item, index) => (
                <Box
                  key={index}
                  sx={{
                    py: 1.5,
                    borderBottom: index < 4 ? '1px solid' : 'none',
                    borderColor: 'divider',
                  }}
                >
                  <Typography variant="body2">{item.action}</Typography>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                    <Typography variant="caption" color="primary">
                      {item.user}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {item.time}
                    </Typography>
                  </Box>
                </Box>
              ))}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}

import {
  Avatar,
  Box,
  Card,
  CardContent,
  CircularProgress,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
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
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import {
  getAdminAnalytics,
  getAdminDailyActivity,
  getAdminLevelDistribution,
  getAdminUsers,
} from '../api/client';
import type { AdminUserSummary } from '../types';

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

const LEVEL_COLORS = ['#FFB74D', '#4FC3F7', '#9575CD', '#4CAF50', '#EC407A', '#26A69A', '#FB8C00'];

const formatDateLabel = (isoDate: string) => {
  const [year, month, day] = isoDate.split('-');
  if (!year || !month || !day) {
    return isoDate;
  }
  return `${day}.${month}`;
};

export default function AnalyticsPage() {
  const activityDays = 7;

  const { data, isLoading } = useQuery({
    queryKey: ['admin-analytics'],
    queryFn: getAdminAnalytics,
  });

  const {
    data: users,
    isLoading: isUsersLoading,
    error: usersError,
  } = useQuery({
    queryKey: ['admin-users-analytics'],
    queryFn: () => getAdminUsers(),
  });

  const {
    data: dailyActivity,
    isLoading: isActivityLoading,
    error: activityError,
  } = useQuery({
    queryKey: ['admin-analytics-daily-activity', activityDays],
    queryFn: () => getAdminDailyActivity(activityDays),
  });

  const {
    data: levelDistribution,
    isLoading: isLevelsLoading,
    error: levelsError,
  } = useQuery({
    queryKey: ['admin-analytics-levels'],
    queryFn: getAdminLevelDistribution,
  });

  const topUsers = (users ?? [])
    .slice()
    .sort((a, b) => b.totalPoints - a.totalPoints)
    .slice(0, 10);

  const activityChartData = (dailyActivity ?? []).map((entry) => ({
    date: entry.date,
    label: formatDateLabel(entry.date),
    newUsers: entry.newUsers,
    testsCompleted: entry.testsCompleted,
    achievementsEarned: entry.achievementsEarned,
  }));

  const levelChartData = (levelDistribution ?? []).map((entry) => ({
    name: `Ур. ${entry.level}`,
    value: entry.users,
  }));

  const categoryChartData = (data?.topCategories ?? []).map((category) => ({
    name: category.categoryName,
    completions: category.completions,
  }));

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

      <Grid container spacing={3} sx={{ mt: 1 }}>
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Активность за последние {activityDays} дней
              </Typography>
              {isActivityLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                  <CircularProgress size={24} />
                </Box>
              ) : activityError ? (
                <Typography color="text.secondary">
                  Не удалось загрузить активность
                </Typography>
              ) : activityChartData.length === 0 ? (
                <Typography color="text.secondary">Нет данных</Typography>
              ) : (
                <Box sx={{ width: '100%', height: 280 }}>
                  <ResponsiveContainer>
                    <LineChart
                      data={activityChartData}
                      margin={{ top: 10, right: 20, left: 0, bottom: 0 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="label" />
                      <YAxis allowDecimals={false} />
                      <Tooltip
                        labelFormatter={(label, payload) =>
                          payload?.[0]?.payload?.date || label
                        }
                      />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="newUsers"
                        name="Новые пользователи"
                        stroke="#FF9800"
                        strokeWidth={2}
                        dot={{ r: 3 }}
                      />
                      <Line
                        type="monotone"
                        dataKey="testsCompleted"
                        name="Пройденные тесты"
                        stroke="#4FC3F7"
                        strokeWidth={2}
                        dot={{ r: 3 }}
                      />
                      <Line
                        type="monotone"
                        dataKey="achievementsEarned"
                        name="Полученные достижения"
                        stroke="#EC407A"
                        strokeWidth={2}
                        dot={{ r: 3 }}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Уровни пользователей
              </Typography>
              {isLevelsLoading ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                  <CircularProgress size={24} />
                </Box>
              ) : levelsError ? (
                <Typography color="text.secondary">
                  Не удалось загрузить уровни
                </Typography>
              ) : levelChartData.length === 0 ? (
                <Typography color="text.secondary">Нет данных</Typography>
              ) : (
                <Box sx={{ width: '100%', height: 280 }}>
                  <ResponsiveContainer>
                    <PieChart>
                      <Pie
                        data={levelChartData}
                        dataKey="value"
                        nameKey="name"
                        innerRadius={50}
                        outerRadius={90}
                        paddingAngle={2}
                      >
                        {levelChartData.map((entry, index) => (
                          <Cell
                            key={`level-${entry.name}`}
                            fill={LEVEL_COLORS[index % LEVEL_COLORS.length]}
                          />
                        ))}
                      </Pie>
                      <Tooltip />
                      <Legend />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" mb={2}>
                Топ категорий по прохождениям
              </Typography>
              {categoryChartData.length === 0 ? (
                <Typography color="text.secondary">Нет данных</Typography>
              ) : (
                <Box sx={{ width: '100%', height: 260 }}>
                  <ResponsiveContainer>
                    <BarChart
                      data={categoryChartData}
                      margin={{ top: 10, right: 20, left: 0, bottom: 10 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis
                        dataKey="name"
                        interval={0}
                        angle={-20}
                        textAnchor="end"
                        height={60}
                      />
                      <YAxis allowDecimals={false} />
                      <Tooltip />
                      <Bar
                        dataKey="completions"
                        name="Прохождения"
                        fill="#7E57C2"
                        radius={[6, 6, 0, 0]}
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Box sx={{ mt: 4 }}>
        <Card>
          <CardContent>
            <Typography variant="h6" mb={2}>
              Топ пользователей
            </Typography>
            {isUsersLoading ? (
              <Box sx={{ display: 'flex', justifyContent: 'center', py: 2 }}>
                <CircularProgress size={24} />
              </Box>
            ) : usersError ? (
              <Typography color="text.secondary">
                Не удалось загрузить данные пользователей
              </Typography>
            ) : topUsers.length === 0 ? (
              <Typography color="text.secondary">Нет данных</Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Пользователь</TableCell>
                      <TableCell align="right">Уровень</TableCell>
                      <TableCell align="right">Тесты</TableCell>
                      <TableCell align="right">Звёзды</TableCell>
                      <TableCell align="right">Очки</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {topUsers.map((user: AdminUserSummary) => (
                      <TableRow key={user.id} hover>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <Avatar src={user.avatarUrl}>
                              {user.displayName?.charAt(0) || 'U'}
                            </Avatar>
                            <Box>
                              <Typography fontWeight="medium">
                                {user.displayName}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                {user.email}
                              </Typography>
                            </Box>
                          </Box>
                        </TableCell>
                        <TableCell align="right">{user.level}</TableCell>
                        <TableCell align="right">{user.stats.testsCompleted}</TableCell>
                        <TableCell align="right">{user.stats.totalStars}</TableCell>
                        <TableCell align="right">{user.totalPoints}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </CardContent>
        </Card>
      </Box>
    </Box>
  );
}

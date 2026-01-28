import { useNavigate, useParams } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  CircularProgress,
  Divider,
  Grid,
  IconButton,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Chip,
} from '@mui/material';
import { ArrowBack } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getAdminUser } from '../api/client';
import type { UserProgress } from '../types';

function formatDate(value?: string) {
  if (!value) return '—';
  return new Date(value).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  });
}

function formatDateTime(value?: string) {
  if (!value) return '—';
  return new Date(value).toLocaleString('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function UserDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();

  const { data, isLoading } = useQuery({
    queryKey: ['admin-user', id],
    queryFn: () => getAdminUser(id!),
    enabled: !!id,
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (!data) {
    return null;
  }

  const { user, stats, progressSummary, progress, achievements } = data;

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate('/users')} sx={{ mr: 2 }}>
          <ArrowBack />
        </IconButton>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            {user.displayName}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            {user.email}
          </Typography>
        </Box>
      </Box>

      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Профиль
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Typography variant="body2" color="text.secondary">
                Роль
              </Typography>
              <Typography fontWeight="medium" mb={2}>
                {user.role}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Уровень
              </Typography>
              <Typography fontWeight="medium" mb={2}>
                {user.level}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Очки
              </Typography>
              <Typography fontWeight="medium" mb={2}>
                {user.totalPoints}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Серия
              </Typography>
              <Typography fontWeight="medium" mb={2}>
                {user.currentStreak}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Регистрация
              </Typography>
              <Typography fontWeight="medium">
                {formatDate(user.createdAt)}
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={8}>
          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Статистика
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={6} sm={4}>
                  <Typography variant="body2" color="text.secondary">
                    Тесты
                  </Typography>
                  <Typography fontWeight="bold" fontSize={20}>
                    {stats.testsCompleted}
                  </Typography>
                </Grid>
                <Grid item xs={6} sm={4}>
                  <Typography variant="body2" color="text.secondary">
                    Звёзды
                  </Typography>
                  <Typography fontWeight="bold" fontSize={20}>
                    {stats.totalStars}
                  </Typography>
                </Grid>
                <Grid item xs={6} sm={4}>
                  <Typography variant="body2" color="text.secondary">
                    Идеальные
                  </Typography>
                  <Typography fontWeight="bold" fontSize={20}>
                    {stats.perfectScores}
                  </Typography>
                </Grid>
                <Grid item xs={6} sm={4}>
                  <Typography variant="body2" color="text.secondary">
                    Текущий уровень
                  </Typography>
                  <Typography fontWeight="bold" fontSize={20}>
                    {stats.currentLevel}
                  </Typography>
                </Grid>
                <Grid item xs={6} sm={4}>
                  <Typography variant="body2" color="text.secondary">
                    До уровня
                  </Typography>
                  <Typography fontWeight="bold" fontSize={20}>
                    {stats.pointsToNextLevel}
                  </Typography>
                </Grid>
              </Grid>
            </CardContent>
          </Card>

          <Card sx={{ mb: 2 }}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Прогресс по категориям
              </Typography>
              {progressSummary.categoriesProgress.length === 0 ? (
                <Typography color="text.secondary">Нет данных</Typography>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {progressSummary.categoriesProgress.map((category) => {
                    const progressValue =
                      category.testsCount > 0
                        ? (category.completedCount / category.testsCount) * 100
                        : 0;
                    return (
                      <Box key={category.categoryId}>
                        <Box
                          sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            mb: 0.5,
                          }}
                        >
                          <Typography fontWeight="medium">
                            {category.categoryName}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {category.completedCount}/{category.testsCount}
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={progressValue}
                          sx={{ height: 8, borderRadius: 4 }}
                        />
                        <Typography variant="body2" color="text.secondary" mt={0.5}>
                          Звёзды: {category.totalStars}/{category.maxStars}
                        </Typography>
                      </Box>
                    );
                  })}
                </Box>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Прогресс по тестам
              </Typography>
              <TableContainer>
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>Тест</TableCell>
                      <TableCell align="right">Счёт</TableCell>
                      <TableCell align="right">Лучший</TableCell>
                      <TableCell align="right">Попытки</TableCell>
                      <TableCell align="right">Звёзды</TableCell>
                      <TableCell>Последняя попытка</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {progress.map((item: UserProgress) => (
                      <TableRow key={item.testId}>
                        <TableCell>{item.testTitle}</TableCell>
                        <TableCell align="right">
                          {item.score}/{item.maxScore}
                        </TableCell>
                        <TableCell align="right">
                          {item.bestScore}/{item.maxScore}
                        </TableCell>
                        <TableCell align="right">{item.attemptsCount}</TableCell>
                        <TableCell align="right">
                          <Chip
                            label={`${item.stars} / 3`}
                            size="small"
                            color={item.stars >= 2 ? 'success' : 'default'}
                          />
                        </TableCell>
                        <TableCell>{formatDateTime(item.lastAttemptAt)}</TableCell>
                      </TableRow>
                    ))}
                    {progress.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={6} align="center">
                          <Typography color="text.secondary" py={2}>
                            Прогресса пока нет
                          </Typography>
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {achievements.length > 0 && (
        <Card sx={{ mt: 2 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Достижения
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {achievements.map((achievement) => (
                <Chip key={achievement.id} label={achievement.name} />
              ))}
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
}

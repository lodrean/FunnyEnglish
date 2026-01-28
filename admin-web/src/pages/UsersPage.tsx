import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Avatar,
  Box,
  Card,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
  Chip,
  IconButton,
} from '@mui/material';
import { Visibility } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { getAdminUsers } from '../api/client';
import type { AdminUserSummary } from '../types';

const roleLabels: Record<string, string> = {
  ADMIN: 'Администратор',
  USER: 'Пользователь',
};

const roleOptions = [
  { value: 'ALL', label: 'Все роли' },
  { value: 'ADMIN', label: 'Администратор' },
  { value: 'USER', label: 'Пользователь' },
];

function formatDate(value: string) {
  if (!value) return '—';
  return new Date(value).toLocaleDateString('ru-RU', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  });
}

export default function UsersPage() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [role, setRole] = useState('ALL');

  const trimmedQuery = query.trim();

  const { data: users, isLoading } = useQuery({
    queryKey: ['admin-users', trimmedQuery, role],
    queryFn: () =>
      getAdminUsers({
        query: trimmedQuery || undefined,
        role: role === 'ALL' ? undefined : role,
      }),
  });

  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
        <Box>
          <Typography variant="h5" fontWeight="bold">
            Пользователи
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Всего: {users?.length ?? 0}
          </Typography>
        </Box>
      </Box>

      <Card sx={{ mb: 2, p: 2 }}>
        <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
          <TextField
            label="Поиск по имени или email"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            sx={{ minWidth: 260 }}
          />
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Роль</InputLabel>
            <Select
              value={role}
              label="Роль"
              onChange={(event) => setRole(event.target.value)}
            >
              {roleOptions.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Card>

      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Пользователь</TableCell>
                <TableCell>Роль</TableCell>
                <TableCell align="right">Уровень</TableCell>
                <TableCell align="right">Тесты</TableCell>
                <TableCell align="right">Звёзды</TableCell>
                <TableCell align="right">Очки</TableCell>
                <TableCell>Дата регистрации</TableCell>
                <TableCell align="right">Действия</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users?.map((user: AdminUserSummary) => (
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
                  <TableCell>
                    <Chip
                      label={roleLabels[user.role] || user.role}
                      color={user.role === 'ADMIN' ? 'secondary' : 'default'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="right">{user.level}</TableCell>
                  <TableCell align="right">{user.stats.testsCompleted}</TableCell>
                  <TableCell align="right">{user.stats.totalStars}</TableCell>
                  <TableCell align="right">{user.totalPoints}</TableCell>
                  <TableCell>{formatDate(user.createdAt)}</TableCell>
                  <TableCell align="right">
                    <IconButton
                      onClick={() => navigate(`/users/${user.id}`)}
                      title="Открыть профиль"
                      size="small"
                    >
                      <Visibility fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {users?.length === 0 && (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    <Typography color="text.secondary" py={4}>
                      Пользователи не найдены
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>
    </Box>
  );
}

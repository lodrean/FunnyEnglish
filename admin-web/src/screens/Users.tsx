import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  IconButton,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Drawer,
  FormControl,
  InputLabel,
  Select,
  Alert,
  InputAdornment,
  Avatar,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Search as SearchIcon,
  Download as DownloadIcon,
  Close as CloseIcon,
  MailOutline as MessageIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DataTable, Column, StatusBadge } from '../components/data';
import { getAdminUsers, getAdminUser, sendMessageToUser, getUserMessages } from '../api/client';
import type { AdminUserSummary } from '../types';
import { formatRelativeTime } from '../utils/format';

// Types for User List (mapped from API)
interface UserListItem {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  displayName: string;
  role: 'admin' | 'instructor' | 'student';
  status: 'active' | 'inactive' | 'suspended';
  testsCompleted: number;
  avgScore: number;
  totalTimeSpent: number;
  lastActive: string;
  createdAt: string;
  avatarUrl?: string;
  level: number;
  totalPoints: number;
}

// Debounce для серверного поиска (паттерн как в GradingInbox)
const useDebouncedValue = (value: string, delayMs = 300) => {
  const [debounced, setDebounced] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(t);
  }, [value, delayMs]);
  return debounced;
};

// Маппинг UI-ролей в роли backend (GET /admin/users?role=)
const roleToApi: Record<string, string> = {
  student: 'USER',
  instructor: 'TEACHER',
  admin: 'ADMIN',
};

// Fetch users from API (серверные поиск q, фильтр role и пагинация — bd wy7.6)
const fetchUsers = async (
  search: string,
  role: string,
  page: number,
  size: number
): Promise<{ rows: UserListItem[]; total: number }> => {
  const usersPage = await getAdminUsers({
    query: search || undefined,
    role: roleToApi[role],
    page,
    size,
  });
  const users = usersPage.content;
  
  const rows: UserListItem[] = users.map((user: AdminUserSummary) => {
    // Split displayName into first/last name (best effort)
    const nameParts = user.displayName.split(' ');
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';
    
    // Map API role to UI role
    const roleMap: Record<string, UserListItem['role']> = {
      'USER': 'student',
      'ADMIN': 'admin',
      'TEACHER': 'instructor',
    };
    
    return {
      id: user.id,
      email: user.email,
      firstName,
      lastName,
      displayName: user.displayName,
      role: roleMap[user.role] || 'student',
      status: 'active', // Default - API doesn't have status field
      testsCompleted: user.stats?.testsCompleted || 0,
      avgScore: 0, // Not in API yet
      totalTimeSpent: 0, // Not in API yet
      lastActive: user.createdAt, // Using createdAt as fallback
      createdAt: user.createdAt,
      avatarUrl: user.avatarUrl,
      level: user.level,
      totalPoints: user.totalPoints,
    };
  });
  return { rows, total: usersPage.totalElements };
};

// Создание/редактирование/удаление пользователей backend не поддерживает
// (нет POST/PUT/DELETE /admin/users) — действия убраны из UI, чтобы не было
// тихого ложного успеха (bd FunnyEnglish-9bo.4, аудит §2.3 К4).

// Role mapping for StatusBadge
const getRoleVariant = (role: UserListItem['role']) => {
  switch (role) {
    case 'admin':
      return 'error';
    case 'instructor':
      return 'info';
    case 'student':
      return 'success';
    default:
      return 'default';
  }
};

// Status mapping for StatusBadge
const getStatusVariant = (status: UserListItem['status']) => {
  switch (status) {
    case 'active':
      return 'success';
    case 'inactive':
      return 'warning';
    case 'suspended':
      return 'error';
    default:
      return 'default';
  }
};

// RU-подписи ролей/статусов (bd b85.6 — RU-унификация)
const ROLE_LABELS: Record<UserListItem['role'], string> = {
  admin: 'Админ',
  instructor: 'Инструктор',
  student: 'Ученик',
};

const STATUS_LABELS: Record<UserListItem['status'], string> = {
  active: 'Активен',
  inactive: 'Неактивен',
  suspended: 'Заблокирован',
};

const MESSAGE_TYPE_LABELS: Record<'MESSAGE' | 'COMMENT', string> = {
  MESSAGE: 'Сообщение',
  COMMENT: 'Комментарий',
};

// Score variant
const getScoreVariant = (score: number) => {
  if (score >= 80) return 'success';
  if (score >= 60) return 'warning';
  return 'error';
};

// Format time
const formatTime = (minutes: number): string => {
  const hours = Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours > 0) {
    return `${hours} ч ${mins} мин`;
  }
  return `${mins} мин`;
};


const Users: React.FC = () => {
  const theme = useTheme();
  const queryClient = useQueryClient();
  
  const [filters, setFilters] = useState({
    search: '',
    role: '',
    status: '',
  });
  // Drawer read-only с деталями пользователя (редактирование API не поддерживает)
  const [viewingUser, setViewingUser] = useState<UserListItem | null>(null);
  // Диалог «Сообщение ученику» (с его результатами тестов)
  const [messageDialogUser, setMessageDialogUser] = useState<UserListItem | null>(null);
  const [messageText, setMessageText] = useState('');
  const [messageType, setMessageType] = useState<'MESSAGE' | 'COMMENT'>('MESSAGE');
  const [messageTestId, setMessageTestId] = useState<string>('');
  
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const debouncedSearch = useDebouncedValue(filters.search);
  // смена фильтров — на первую страницу
  useEffect(() => {
    setPage(0);
  }, [debouncedSearch, filters.role]);
  const { data: usersPage, isLoading, error } = useQuery({
    queryKey: ['users', debouncedSearch, filters.role, page, pageSize],
    queryFn: () => fetchUsers(debouncedSearch, filters.role, page, pageSize),
  });
  const users = usersPage?.rows ?? [];
  const totalUsers = usersPage?.total ?? 0;

  // Детали выбранного ученика (результаты тестов) для диалога сообщения
  const { data: messageUserDetail } = useQuery({
    queryKey: ['adminUser', messageDialogUser?.id],
    queryFn: () => getAdminUser(messageDialogUser!.id),
    enabled: !!messageDialogUser,
  });

  // История сообщений ученику
  const { data: userMessages = [] } = useQuery({
    queryKey: ['userMessages', messageDialogUser?.id],
    queryFn: () => getUserMessages(messageDialogUser!.id),
    enabled: !!messageDialogUser,
  });

  const sendMessageMutation = useMutation({
    mutationFn: () =>
      sendMessageToUser(messageDialogUser!.id, {
        text: messageText.trim(),
        type: messageType,
        ...(messageType === 'COMMENT' && messageTestId ? { testId: messageTestId } : {}),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userMessages', messageDialogUser?.id] });
      setMessageText('');
    },
  });

  const handleOpenMessageDialog = (user: UserListItem) => {
    setMessageDialogUser(user);
    setMessageText('');
    setMessageType('MESSAGE');
    setMessageTestId('');
  };

  const handleCloseMessageDialog = () => {
    setMessageDialogUser(null);
    sendMessageMutation.reset();
  };

  // Поиск, фильтр роли и пагинация — серверные (queryFn выше); статус — клиентский в пределах страницы
  const filteredUsers = users.filter((user) => !filters.status || user.status === filters.status);

  const handleOpenDrawer = (user: UserListItem) => {
    setViewingUser(user);
  };

  const handleCloseDrawer = () => {
    setViewingUser(null);
  };

  const handleExportCSV = () => {
    const csv = [
      ['ID', 'Email', 'Имя', 'Фамилия', 'Роль', 'Статус', 'Тестов', 'Средний балл'].join(','),
      ...filteredUsers.map((u) =>
        [u.id, u.email, u.firstName, u.lastName, u.role, u.status, u.testsCompleted, u.avgScore].join(',')
      ),
    ].join('\n');
    
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'users.csv';
    a.click();
    URL.revokeObjectURL(url);
  };

  // Table columns definition with Design System
  const columns: Column<UserListItem>[] = [
    {
      key: 'user',
      header: 'Пользователь',
      accessor: (user: UserListItem) => (
        <Box display="flex" alignItems="center" gap={1.5}>
          <Avatar
            sx={{
              width: 32,
              height: 32,
              backgroundColor: alpha(theme.palette.primary.main, 0.12),
              color: theme.palette.primary.main,
              fontSize: '0.875rem',
            }}
          >
            {user.firstName.charAt(0)}{user.lastName.charAt(0)}
          </Avatar>
          <Box>
            <Typography variant="body2" fontWeight={500}>
              {user.firstName} {user.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {user.email}
            </Typography>
          </Box>
        </Box>
      ),
      sortable: true,
      width: '25%',
    },
    {
      key: 'role',
      header: 'Роль',
      accessor: (user: UserListItem) => (
        <StatusBadge
          status={getRoleVariant(user.role) as any}
          label={ROLE_LABELS[user.role]}
          size="small"
        />
      ),
      sortable: true,
      width: '12%',
    },
    {
      key: 'status',
      header: 'Статус',
      accessor: (user: UserListItem) => (
        <StatusBadge
          status={getStatusVariant(user.status) as any}
          label={STATUS_LABELS[user.status]}
          size="small"
        />
      ),
      sortable: true,
      width: '12%',
    },
    {
      key: 'testsCompleted',
      header: 'Тесты',
      accessor: (user: UserListItem) => user.testsCompleted,
      align: 'center',
      width: '10%',
    },
    {
      key: 'avgScore',
      header: 'Средний балл',
      accessor: (user: UserListItem) =>
        user.avgScore > 0 ? (
          <StatusBadge
            status={getScoreVariant(user.avgScore) as any}
            label={`${user.avgScore}%`}
            size="small"
          />
        ) : (
          '-'
        ),
      align: 'center',
      width: '12%',
    },
    {
      key: 'totalTimeSpent',
      header: 'Время',
      accessor: (user: UserListItem) => formatTime(user.totalTimeSpent),
      align: 'center',
      width: '12%',
    },
    {
      key: 'lastActive',
      header: 'Был активен',
      accessor: (user: UserListItem) => formatRelativeTime(user.lastActive),
      width: '15%',
    },
    {
      key: 'message',
      header: 'Сообщение',
      accessor: (user: UserListItem) => (
        <IconButton
          size="small"
          color="primary"
          data-testid={`message-user-${user.id}`}
          onClick={(e) => {
            e.stopPropagation();
            handleOpenMessageDialog(user);
          }}
        >
          <MessageIcon fontSize="small" />
        </IconButton>
      ),
      align: 'center',
      width: '8%',
    },
  ];

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">Не удалось загрузить пользователей. Попробуйте ещё раз.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 3, md: 4 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={theme.palette.text.primary} data-testid="page-title">
          Пользователи
        </Typography>
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={handleExportCSV}
          >
            Экспорт CSV
          </Button>
        </Box>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box display="flex" flexDirection={{ xs: 'column', md: 'row' }} gap={2} alignItems={{ xs: 'stretch', md: 'center' }}>
          <TextField
            placeholder="Поиск пользователей..."
            value={filters.search}
            onChange={(e) => setFilters({ ...filters, search: e.target.value })}
            size="small"
            data-testid="search-users"
            sx={{ flex: 1 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon fontSize="small" />
                </InputAdornment>
              ),
            }}
          />
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Роль</InputLabel>
            <Select
              value={filters.role}
              label="Роль"
              onChange={(e) => setFilters({ ...filters, role: e.target.value })}
            >
              <MenuItem value="">Все роли</MenuItem>
              <MenuItem value="admin">Админ</MenuItem>
              <MenuItem value="instructor">Инструктор</MenuItem>
              <MenuItem value="student">Ученик</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Статус</InputLabel>
            <Select
              value={filters.status}
              label="Статус"
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <MenuItem value="">Все статусы</MenuItem>
              <MenuItem value="active">Активен</MenuItem>
              <MenuItem value="inactive">Неактивен</MenuItem>
              <MenuItem value="suspended">Заблокирован</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Paper>

      {/* Data Table with Design System */}
      <DataTable
        columns={columns}
        data={filteredUsers}
        keyExtractor={(user) => user.id}
        loading={isLoading}
        onRowClick={(user) => handleOpenDrawer(user)}
        emptyState={<Typography color="text.secondary">Пользователи не найдены</Typography>}
        stickyHeader
        pagination={{
          page,
          pageSize,
          total: totalUsers,
          onPageChange: setPage,
          onPageSizeChange: (ps) => {
            setPageSize(ps);
            setPage(0);
          },
        }}
      />

      {/* User Details Drawer (read-only: API не поддерживает редактирование) */}
      <Drawer
        anchor="right"
        open={!!viewingUser}
        onClose={handleCloseDrawer}
        PaperProps={{ sx: { width: { xs: '100%', sm: 400 } } }}
      >
        {viewingUser && (
          <Box sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
              <Typography variant="h6" data-testid="user-details-title">
                Данные пользователя
              </Typography>
              <IconButton onClick={handleCloseDrawer}>
                <CloseIcon />
              </IconButton>
            </Box>

            <Box flex={1}>
              <TextField
                label="Email"
                fullWidth
                margin="normal"
                value={viewingUser.email}
                disabled
              />
              <TextField
                label="Имя"
                fullWidth
                margin="normal"
                value={viewingUser.firstName}
                disabled
              />
              <TextField
                label="Фамилия"
                fullWidth
                margin="normal"
                value={viewingUser.lastName}
                disabled
              />
              <TextField
                label="Роль"
                fullWidth
                margin="normal"
                value={ROLE_LABELS[viewingUser.role]}
                disabled
              />
              <TextField
                label="Статус"
                fullWidth
                margin="normal"
                value={STATUS_LABELS[viewingUser.status]}
                disabled
              />
              <Alert severity="info" sx={{ mt: 2 }}>
                Редактирование пользователей API пока не поддерживает.
              </Alert>
            </Box>

            <Box display="flex" gap={1} mt={2}>
              <Button variant="outlined" fullWidth onClick={handleCloseDrawer}>
                Закрыть
              </Button>
            </Box>
          </Box>
        )}
      </Drawer>

      {/* Message to Student Dialog (результаты + отправка сообщения/комментария) */}
      <Dialog
        open={!!messageDialogUser}
        onClose={handleCloseMessageDialog}
        maxWidth="sm"
        fullWidth
        data-testid="message-dialog"
      >
        <DialogTitle>
          Сообщение для {messageDialogUser?.displayName}
        </DialogTitle>
        <DialogContent>
          {/* Результаты тестов ученика */}
          <Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>
            Результаты тестов
          </Typography>
          {messageUserDetail?.progress?.length ? (
            <Box sx={{ maxHeight: 160, overflow: 'auto', mb: 2 }} data-testid="user-progress-list">
              {messageUserDetail.progress.map((p) => (
                <Box
                  key={p.testId}
                  display="flex"
                  justifyContent="space-between"
                  alignItems="center"
                  sx={{ py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}
                >
                  <Typography variant="body2">{p.testTitle}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {p.score}/{p.maxScore} {'⭐'.repeat(p.stars)}
                  </Typography>
                </Box>
              ))}
            </Box>
          ) : (
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Завершённых тестов пока нет
            </Typography>
          )}

          {/* Форма сообщения */}
          <Box display="flex" gap={1} mb={1}>
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel>Тип</InputLabel>
              <Select
                value={messageType}
                label="Тип"
                data-testid="message-type-select"
                onChange={(e) => setMessageType(e.target.value as 'MESSAGE' | 'COMMENT')}
              >
                <MenuItem value="MESSAGE">Сообщение</MenuItem>
                <MenuItem value="COMMENT">Комментарий к результату</MenuItem>
              </Select>
            </FormControl>
            {messageType === 'COMMENT' && (
              <FormControl size="small" sx={{ flex: 1 }}>
                <InputLabel>Тест</InputLabel>
                <Select
                  value={messageTestId}
                  label="Тест"
                  data-testid="message-test-select"
                  onChange={(e) => setMessageTestId(e.target.value)}
                >
                  {(messageUserDetail?.progress ?? []).map((p) => (
                    <MenuItem key={p.testId} value={p.testId}>
                      {p.testTitle}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
          </Box>
          <TextField
            label="Текст сообщения"
            fullWidth
            multiline
            rows={3}
            value={messageText}
            onChange={(e) => setMessageText(e.target.value)}
            inputProps={{ 'data-testid': 'message-text-input' }}
          />
          {sendMessageMutation.isSuccess && (
            <Alert severity="success" sx={{ mt: 1 }} data-testid="message-sent-alert">
              Сообщение отправлено!
            </Alert>
          )}
          {sendMessageMutation.isError && (
            <Alert severity="error" sx={{ mt: 1 }}>
              Не удалось отправить сообщение
            </Alert>
          )}

          {/* История сообщений */}
          {userMessages.length > 0 && (
            <>
              <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
                Отправленные сообщения
              </Typography>
              <Box sx={{ maxHeight: 140, overflow: 'auto' }} data-testid="messages-history">
                {userMessages.map((m) => (
                  <Box key={m.id} sx={{ py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}>
                    <Typography variant="body2">{m.text}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {MESSAGE_TYPE_LABELS[m.type]} · {new Date(m.createdAt).toLocaleString()} ·{' '}
                      {m.readAt ? 'прочитано' : 'не прочитано'}
                    </Typography>
                  </Box>
                ))}
              </Box>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseMessageDialog}>Закрыть</Button>
          <Button
            variant="contained"
            data-testid="send-message-button"
            disabled={!messageText.trim() || sendMessageMutation.isPending}
            onClick={() => sendMessageMutation.mutate()}
          >
            {sendMessageMutation.isPending ? 'Отправка...' : 'Отправить'}
          </Button>
        </DialogActions>
      </Dialog>

    </Box>
  );
};

export default Users;

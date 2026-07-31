import React, { useState } from 'react';
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
  Toolbar,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,

  Download as DownloadIcon,
  Delete as DeleteIcon,
  Close as CloseIcon,
  MailOutline as MessageIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { DataTable, ColumnDef, StatusBadge } from '../components/data';
import { getAdminUsers, getAdminUser, sendMessageToUser, getUserMessages } from '../api/client';
import type { AdminUserSummary } from '../types';

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

interface UserFormData {
  email: string;
  firstName: string;
  lastName: string;
  role: UserListItem['role'];
  status: UserListItem['status'];
}

// Fetch users from API
const fetchUsers = async (): Promise<UserListItem[]> => {
  const users = await getAdminUsers();
  
  return users.map((user: AdminUserSummary) => {
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
};

const createUser = async (_data: UserFormData): Promise<UserListItem> => {
  // TODO: Implement when API supports user creation
  throw new Error('User creation not implemented in API yet');
};

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const _updateUser = async (_id: string, _data: UserFormData): Promise<UserListItem> => {
  // TODO: Implement when API supports user update
  throw new Error('User update not implemented in API yet');
};

const deleteUser = async (id: string): Promise<void> => {
  // TODO: Implement when API supports user deletion
  console.log('Delete user:', id);
};

const bulkDeleteUsers = async (ids: string[]): Promise<void> => {
  // TODO: Implement when API supports bulk deletion
  console.log('Bulk delete users:', ids);
};

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
    return `${hours}h ${mins}m`;
  }
  return `${mins}m`;
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

const Users: React.FC = () => {
  const theme = useTheme();
  const queryClient = useQueryClient();
  
  const [filters, setFilters] = useState({
    search: '',
    role: '',
    status: '',
  });
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserListItem | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<UserListItem | null>(null);
  const [selectedUsers, setSelectedUsers] = useState<string[]>([]);
  const [bulkDeleteDialogOpen, setBulkDeleteDialogOpen] = useState(false);
  // Диалог «Сообщение ученику» (с его результатами тестов)
  const [messageDialogUser, setMessageDialogUser] = useState<UserListItem | null>(null);
  const [messageText, setMessageText] = useState('');
  const [messageType, setMessageType] = useState<'MESSAGE' | 'COMMENT'>('MESSAGE');
  const [messageTestId, setMessageTestId] = useState<string>('');
  
  const [formData, setFormData] = useState<UserFormData>({
    email: '',
    firstName: '',
    lastName: '',
    role: 'student',
    status: 'active',
  });

  const { data: users = [], isLoading, error } = useQuery({
    queryKey: ['users'],
    queryFn: fetchUsers,
  });

  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      handleCloseDrawer();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setDeleteDialogOpen(false);
      setUserToDelete(null);
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: bulkDeleteUsers,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      setSelectedUsers([]);
      setBulkDeleteDialogOpen(false);
    },
  });

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

  // Filter users
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      !filters.search ||
      user.email.toLowerCase().includes(filters.search.toLowerCase()) ||
      user.firstName.toLowerCase().includes(filters.search.toLowerCase()) ||
      user.lastName.toLowerCase().includes(filters.search.toLowerCase());
    const matchesRole = !filters.role || user.role === filters.role;
    const matchesStatus = !filters.status || user.status === filters.status;
    return matchesSearch && matchesRole && matchesStatus;
  });

  const handleOpenDrawer = (user?: UserListItem) => {
    if (user) {
      setEditingUser(user);
      setFormData({
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        role: user.role,
        status: user.status,
      });
    } else {
      setEditingUser(null);
      setFormData({
        email: '',
        firstName: '',
        lastName: '',
        role: 'student',
        status: 'active',
      });
    }
    setDrawerOpen(true);
  };

  const handleCloseDrawer = () => {
    setDrawerOpen(false);
    setEditingUser(null);
    setFormData({ email: '', firstName: '', lastName: '', role: 'student', status: 'active' });
  };

  const handleSubmit = () => {
    if (!formData.email.trim() || !formData.firstName.trim() || !formData.lastName.trim()) {
      return;
    }

    if (editingUser) {
      // updateMutation.mutate({ id: editingUser.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (user: UserListItem) => {
    setUserToDelete(user);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (userToDelete) {
      deleteMutation.mutate(userToDelete.id);
    }
  };

  const handleExportCSV = () => {
    const csv = [
      ['ID', 'Email', 'First Name', 'Last Name', 'Role', 'Status', 'Tests Completed', 'Avg Score'].join(','),
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
  const columns: ColumnDef<UserListItem>[] = [
    {
      key: 'user',
      header: 'User',
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
      header: 'Role',
      accessor: (user: UserListItem) => (
        <StatusBadge
          status={getRoleVariant(user.role) as any}
          label={user.role.charAt(0).toUpperCase() + user.role.slice(1)}
          size="small"
        />
      ),
      sortable: true,
      width: '12%',
    },
    {
      key: 'status',
      header: 'Status',
      accessor: (user: UserListItem) => (
        <StatusBadge
          status={getStatusVariant(user.status) as any}
          label={user.status.charAt(0).toUpperCase() + user.status.slice(1)}
          size="small"
        />
      ),
      sortable: true,
      width: '12%',
    },
    {
      key: 'testsCompleted',
      header: 'Tests',
      accessor: (user: UserListItem) => user.testsCompleted,
      align: 'center',
      width: '10%',
    },
    {
      key: 'avgScore',
      header: 'Avg Score',
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
      header: 'Time Spent',
      accessor: (user: UserListItem) => formatTime(user.totalTimeSpent),
      align: 'center',
      width: '12%',
    },
    {
      key: 'lastActive',
      header: 'Last Active',
      accessor: (user: UserListItem) => formatRelativeTime(user.lastActive),
      width: '15%',
    },
    {
      key: 'message',
      header: 'Message',
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
        <Alert severity="error">Failed to load users. Please try again.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 3, md: 4 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={theme.palette.text.primary} data-testid="page-title">
          Users
        </Typography>
        <Box display="flex" gap={1}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={handleExportCSV}
          >
            Export CSV
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDrawer()}
            data-testid="add-user-button"
            sx={{ backgroundColor: theme.palette.primary.main }}
          >
            Add User
          </Button>
        </Box>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box display="flex" flexDirection={{ xs: 'column', md: 'row' }} gap={2} alignItems={{ xs: 'stretch', md: 'center' }}>
          <TextField
            placeholder="Search users..."
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
            <InputLabel>Role</InputLabel>
            <Select
              value={filters.role}
              label="Role"
              onChange={(e) => setFilters({ ...filters, role: e.target.value })}
            >
              <MenuItem value="">All Roles</MenuItem>
              <MenuItem value="admin">Admin</MenuItem>
              <MenuItem value="instructor">Instructor</MenuItem>
              <MenuItem value="student">Student</MenuItem>
            </Select>
          </FormControl>
          <FormControl size="small" sx={{ minWidth: 120 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={filters.status}
              label="Status"
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <MenuItem value="">All Status</MenuItem>
              <MenuItem value="active">Active</MenuItem>
              <MenuItem value="inactive">Inactive</MenuItem>
              <MenuItem value="suspended">Suspended</MenuItem>
            </Select>
          </FormControl>
        </Box>
      </Paper>

      {/* Bulk Actions */}
      {selectedUsers.length > 0 && (
        <Paper sx={{ p: 1, mb: 2, backgroundColor: alpha(theme.palette.primary.main, 0.1) }}>
          <Toolbar variant="dense" sx={{ minHeight: 48 }}>
            <Typography variant="body2" sx={{ flex: 1 }}>
              {selectedUsers.length} user(s) selected
            </Typography>
            <Button
              size="small"
              color="error"
              startIcon={<DeleteIcon />}
              onClick={() => setBulkDeleteDialogOpen(true)}
            >
              Delete Selected
            </Button>
          </Toolbar>
        </Paper>
      )}

      {/* Data Table with Design System */}
      <DataTable
        columns={columns}
        data={filteredUsers}
        keyExtractor={(user) => user.id}
        loading={isLoading}
        selectable
        onSelectionChange={setSelectedUsers}
        onRowClick={(user) => handleOpenDrawer(user)}
        onEdit={(user) => handleOpenDrawer(user)}
        onDelete={(user) => handleDelete(user)}
        emptyMessage="No users found"
        defaultRowsPerPage={10}
        rowsPerPageOptions={[5, 10, 25, 50]}
        stickyHeader
      />

      {/* Add/Edit Drawer */}
      <Drawer
        anchor="right"
        open={drawerOpen}
        onClose={handleCloseDrawer}
        PaperProps={{ sx: { width: { xs: '100%', sm: 400 } } }}
      >
        <Box sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
          <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
            <Typography variant="h6">
              {editingUser ? 'Edit User' : 'Add User'}
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
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
              error={!formData.email.trim()}
            />
            <TextField
              label="First Name"
              fullWidth
              margin="normal"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              required
              error={!formData.firstName.trim()}
            />
            <TextField
              label="Last Name"
              fullWidth
              margin="normal"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              required
              error={!formData.lastName.trim()}
            />
            <FormControl fullWidth margin="normal">
              <InputLabel>Role</InputLabel>
              <Select
                value={formData.role}
                label="Role"
                onChange={(e) => setFormData({ ...formData, role: e.target.value as UserListItem['role'] })}
              >
                <MenuItem value="student">Student</MenuItem>
                <MenuItem value="instructor">Instructor</MenuItem>
                <MenuItem value="admin">Admin</MenuItem>
              </Select>
            </FormControl>
            <FormControl fullWidth margin="normal">
              <InputLabel>Status</InputLabel>
              <Select
                value={formData.status}
                label="Status"
                onChange={(e) => setFormData({ ...formData, status: e.target.value as UserListItem['status'] })}
              >
                <MenuItem value="active">Active</MenuItem>
                <MenuItem value="inactive">Inactive</MenuItem>
                <MenuItem value="suspended">Suspended</MenuItem>
              </Select>
            </FormControl>
          </Box>

          <Box display="flex" gap={1} mt={2}>
            <Button variant="outlined" fullWidth onClick={handleCloseDrawer}>
              Cancel
            </Button>
            <Button
              variant="contained"
              fullWidth
              onClick={handleSubmit}
              disabled={
                !formData.email.trim() ||
                !formData.firstName.trim() ||
                !formData.lastName.trim() ||
                createMutation.isPending
              }
              sx={{ backgroundColor: theme.palette.primary.main }}
            >
              {createMutation.isPending ? 'Saving...' : editingUser ? 'Update' : 'Create'}
            </Button>
          </Box>
        </Box>
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
          Message to {messageDialogUser?.displayName}
        </DialogTitle>
        <DialogContent>
          {/* Результаты тестов ученика */}
          <Typography variant="subtitle2" sx={{ mt: 1, mb: 1 }}>
            Test Results
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
              No completed tests yet
            </Typography>
          )}

          {/* Форма сообщения */}
          <Box display="flex" gap={1} mb={1}>
            <FormControl size="small" sx={{ minWidth: 140 }}>
              <InputLabel>Type</InputLabel>
              <Select
                value={messageType}
                label="Type"
                data-testid="message-type-select"
                onChange={(e) => setMessageType(e.target.value as 'MESSAGE' | 'COMMENT')}
              >
                <MenuItem value="MESSAGE">Message</MenuItem>
                <MenuItem value="COMMENT">Comment to result</MenuItem>
              </Select>
            </FormControl>
            {messageType === 'COMMENT' && (
              <FormControl size="small" sx={{ flex: 1 }}>
                <InputLabel>Test</InputLabel>
                <Select
                  value={messageTestId}
                  label="Test"
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
            label="Message text"
            fullWidth
            multiline
            rows={3}
            value={messageText}
            onChange={(e) => setMessageText(e.target.value)}
            inputProps={{ 'data-testid': 'message-text-input' }}
          />
          {sendMessageMutation.isSuccess && (
            <Alert severity="success" sx={{ mt: 1 }} data-testid="message-sent-alert">
              Message sent!
            </Alert>
          )}
          {sendMessageMutation.isError && (
            <Alert severity="error" sx={{ mt: 1 }}>
              Failed to send message
            </Alert>
          )}

          {/* История сообщений */}
          {userMessages.length > 0 && (
            <>
              <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
                Sent messages
              </Typography>
              <Box sx={{ maxHeight: 140, overflow: 'auto' }} data-testid="messages-history">
                {userMessages.map((m) => (
                  <Box key={m.id} sx={{ py: 0.5, borderBottom: '1px solid', borderColor: 'divider' }}>
                    <Typography variant="body2">{m.text}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {m.type} · {new Date(m.createdAt).toLocaleString()} ·{' '}
                      {m.readAt ? 'read' : 'unread'}
                    </Typography>
                  </Box>
                ))}
              </Box>
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseMessageDialog}>Close</Button>
          <Button
            variant="contained"
            data-testid="send-message-button"
            disabled={!messageText.trim() || sendMessageMutation.isPending}
            onClick={() => sendMessageMutation.mutate()}
          >
            {sendMessageMutation.isPending ? 'Sending...' : 'Send'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete User</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete {userToDelete?.firstName} {userToDelete?.lastName}?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            This action cannot be undone. All user data will be permanently removed.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={confirmDelete}
            variant="contained"
            color="error"
            disabled={deleteMutation.isPending}
          >
            {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Bulk Delete Dialog */}
      <Dialog open={bulkDeleteDialogOpen} onClose={() => setBulkDeleteDialogOpen(false)}>
        <DialogTitle>Delete Multiple Users</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete {selectedUsers.length} users?
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBulkDeleteDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={() => bulkDeleteMutation.mutate(selectedUsers)}
            variant="contained"
            color="error"
            disabled={bulkDeleteMutation.isPending}
          >
            {bulkDeleteMutation.isPending ? 'Deleting...' : 'Delete All'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Users;

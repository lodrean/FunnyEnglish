import React, { useState } from 'react';
import {
  Box,
  Typography,
  Button,
  TextField,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Drawer,
  FormControl,
  InputLabel,
  Select,
  Skeleton,
  Alert,
  Tooltip,
  InputAdornment,
  Avatar,
  Checkbox,
  Toolbar,
  Divider,
  FormControlLabel,
  Switch,
} from '@mui/material';
import {
  Add as AddIcon,
  Search as SearchIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  MoreVert as MoreVertIcon,
  FilterList as FilterListIcon,
  Download as DownloadIcon,
  Block as BlockIcon,
  CheckCircle as CheckCircleIcon,
  Email as EmailIcon,
  Close as CloseIcon,
} from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

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
interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: 'admin' | 'instructor' | 'student';
  status: 'active' | 'inactive' | 'suspended';
  testsCompleted: number;
  avgScore: number;
  totalTimeSpent: number;
  lastActive: string;
  createdAt: string;
  avatarUrl?: string;
}

interface UserFormData {
  email: string;
  firstName: string;
  lastName: string;
  role: User['role'];
  status: User['status'];
}

// Mock API
const fetchUsers = async (): Promise<User[]> => {
  await new Promise((resolve) => setTimeout(resolve, 700));
  
  return [
    {
      id: '1',
      email: 'john.doe@example.com',
      firstName: 'John',
      lastName: 'Doe',
      role: 'student',
      status: 'active',
      testsCompleted: 45,
      avgScore: 78.5,
      totalTimeSpent: 3240,
      lastActive: '2024-01-15T10:30:00Z',
      createdAt: '2023-06-01',
    },
    {
      id: '2',
      email: 'jane.smith@example.com',
      firstName: 'Jane',
      lastName: 'Smith',
      role: 'instructor',
      status: 'active',
      testsCompleted: 12,
      avgScore: 92.0,
      totalTimeSpent: 890,
      lastActive: '2024-01-15T09:45:00Z',
      createdAt: '2023-05-15',
    },
    {
      id: '3',
      email: 'admin@funnyenglish.com',
      firstName: 'Admin',
      lastName: 'User',
      role: 'admin',
      status: 'active',
      testsCompleted: 0,
      avgScore: 0,
      totalTimeSpent: 0,
      lastActive: '2024-01-15T11:00:00Z',
      createdAt: '2023-01-01',
    },
    {
      id: '4',
      email: 'mike.wilson@example.com',
      firstName: 'Mike',
      lastName: 'Wilson',
      role: 'student',
      status: 'active',
      testsCompleted: 32,
      avgScore: 65.2,
      totalTimeSpent: 2150,
      lastActive: '2024-01-14T16:20:00Z',
      createdAt: '2023-07-10',
    },
    {
      id: '5',
      email: 'sarah.jones@example.com',
      firstName: 'Sarah',
      lastName: 'Jones',
      role: 'student',
      status: 'inactive',
      testsCompleted: 8,
      avgScore: 82.1,
      totalTimeSpent: 540,
      lastActive: '2024-01-10T14:15:00Z',
      createdAt: '2023-08-20',
    },
    {
      id: '6',
      email: 'tom.brown@example.com',
      firstName: 'Tom',
      lastName: 'Brown',
      role: 'student',
      status: 'suspended',
      testsCompleted: 15,
      avgScore: 45.0,
      totalTimeSpent: 980,
      lastActive: '2024-01-05T09:30:00Z',
      createdAt: '2023-09-01',
    },
    {
      id: '7',
      email: 'emma.davis@example.com',
      firstName: 'Emma',
      lastName: 'Davis',
      role: 'instructor',
      status: 'active',
      testsCompleted: 25,
      avgScore: 88.5,
      totalTimeSpent: 1680,
      lastActive: '2024-01-15T08:45:00Z',
      createdAt: '2023-04-15',
    },
    {
      id: '8',
      email: 'chris.miller@example.com',
      firstName: 'Chris',
      lastName: 'Miller',
      role: 'student',
      status: 'active',
      testsCompleted: 67,
      avgScore: 91.2,
      totalTimeSpent: 4560,
      lastActive: '2024-01-15T07:30:00Z',
      createdAt: '2023-03-20',
    },
    {
      id: '9',
      email: 'lisa.wang@example.com',
      firstName: 'Lisa',
      lastName: 'Wang',
      role: 'student',
      status: 'active',
      testsCompleted: 28,
      avgScore: 74.8,
      totalTimeSpent: 1890,
      lastActive: '2024-01-14T20:00:00Z',
      createdAt: '2023-10-05',
    },
    {
      id: '10',
      email: 'david.garcia@example.com',
      firstName: 'David',
      lastName: 'Garcia',
      role: 'instructor',
      status: 'active',
      testsCompleted: 18,
      avgScore: 85.3,
      totalTimeSpent: 1240,
      lastActive: '2024-01-15T06:15:00Z',
      createdAt: '2023-02-28',
    },
  ];
};

const createUser = async (data: UserFormData): Promise<User> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  return {
    id: Math.random().toString(36).substr(2, 9),
    ...data,
    testsCompleted: 0,
    avgScore: 0,
    totalTimeSpent: 0,
    lastActive: new Date().toISOString(),
    createdAt: new Date().toISOString(),
  };
};

const updateUser = async (id: string, data: UserFormData): Promise<User> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
  throw new Error('Not implemented');
};

const deleteUser = async (id: string): Promise<void> => {
  await new Promise((resolve) => setTimeout(resolve, 500));
};

const bulkDeleteUsers = async (ids: string[]): Promise<void> => {
  await new Promise((resolve) => setTimeout(resolve, 800));
};

// Role colors
const getRoleColor = (role: User['role']) => {
  switch (role) {
    case 'admin':
      return COLORS.error;
    case 'instructor':
      return COLORS.primary;
    case 'student':
      return COLORS.success;
    default:
      return COLORS.textSecondary;
  }
};

// Status colors
const getStatusColor = (status: User['status']) => {
  switch (status) {
    case 'active':
      return COLORS.success;
    case 'inactive':
      return COLORS.warning;
    case 'suspended':
      return COLORS.error;
    default:
      return COLORS.textSecondary;
  }
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
  const queryClient = useQueryClient();
  
  const [filters, setFilters] = useState({
    search: '',
    role: '',
    status: '',
  });
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [selectedUsers, setSelectedUsers] = useState<string[]>([]);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<User | null>(null);
  const [bulkDeleteDialogOpen, setBulkDeleteDialogOpen] = useState(false);
  
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

  const paginatedUsers = filteredUsers.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelectedUsers(paginatedUsers.map((u) => u.id));
    } else {
      setSelectedUsers([]);
    }
  };

  const handleSelectUser = (userId: string) => {
    if (selectedUsers.includes(userId)) {
      setSelectedUsers(selectedUsers.filter((id) => id !== userId));
    } else {
      setSelectedUsers([...selectedUsers, userId]);
    }
  };

  const handleOpenDrawer = (user?: User) => {
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

  const handleDelete = (user: User) => {
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

  if (error) {
    return (
      <Box p={3}>
        <Alert severity="error">Failed to load users. Please try again.</Alert>
      </Box>
    );
  }

  return (
    <Box p={{ xs: 2, md: 3 }}>
      {/* Header */}
      <Box display="flex" flexDirection={{ xs: 'column', sm: 'row' }} justifyContent="space-between" alignItems={{ xs: 'flex-start', sm: 'center' }} mb={3} gap={2}>
        <Typography variant="h4" fontWeight="bold" color={COLORS.textPrimary}>
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
            sx={{ backgroundColor: COLORS.primary }}
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
        <Paper sx={{ p: 1, mb: 2, backgroundColor: `${COLORS.primary}10` }}>
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

      {/* Table */}
      <Paper>
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ backgroundColor: '#FAFAFA' }}>
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={paginatedUsers.length > 0 && selectedUsers.length === paginatedUsers.length}
                    indeterminate={selectedUsers.length > 0 && selectedUsers.length < paginatedUsers.length}
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell>User</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="center">Tests</TableCell>
                <TableCell align="center">Avg Score</TableCell>
                <TableCell align="center">Time Spent</TableCell>
                <TableCell>Last Active</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading ? (
                [...Array(5)].map((_, i) => (
                  <TableRow key={i}>
                    {[...Array(9)].map((_, j) => (
                      <TableCell key={j}>
                        <Skeleton variant="text" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))
              ) : paginatedUsers.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={9} align="center" sx={{ py: 4 }}>
                    <Typography color="text.secondary">No users found</Typography>
                  </TableCell>
                </TableRow>
              ) : (
                paginatedUsers.map((user) => (
                  <TableRow
                    key={user.id}
                    hover
                    selected={selectedUsers.includes(user.id)}
                  >
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selectedUsers.includes(user.id)}
                        onChange={() => handleSelectUser(user.id)}
                      />
                    </TableCell>
                    <TableCell>
                      <Box display="flex" alignItems="center" gap={1.5}>
                        <Avatar
                          sx={{
                            width: 32,
                            height: 32,
                            backgroundColor: `${getRoleColor(user.role)}20`,
                            color: getRoleColor(user.role),
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
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={user.role.charAt(0).toUpperCase() + user.role.slice(1)}
                        size="small"
                        sx={{
                          backgroundColor: `${getRoleColor(user.role)}20`,
                          color: getRoleColor(user.role),
                          textTransform: 'capitalize',
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={user.status.charAt(0).toUpperCase() + user.status.slice(1)}
                        size="small"
                        sx={{
                          backgroundColor: `${getStatusColor(user.status)}20`,
                          color: getStatusColor(user.status),
                          textTransform: 'capitalize',
                        }}
                      />
                    </TableCell>
                    <TableCell align="center">{user.testsCompleted}</TableCell>
                    <TableCell align="center">
                      {user.avgScore > 0 ? (
                        <Chip
                          label={`${user.avgScore}%`}
                          size="small"
                          sx={{
                            backgroundColor:
                              user.avgScore >= 80
                                ? `${COLORS.success}20`
                                : user.avgScore >= 60
                                ? `${COLORS.warning}20`
                                : `${COLORS.error}20`,
                            color:
                              user.avgScore >= 80
                                ? COLORS.success
                                : user.avgScore >= 60
                                ? COLORS.warning
                                : COLORS.error,
                          }}
                        />
                      ) : (
                        '-'
                      )}
                    </TableCell>
                    <TableCell align="center">{formatTime(user.totalTimeSpent)}</TableCell>
                    <TableCell>{formatRelativeTime(user.lastActive)}</TableCell>
                    <TableCell align="right">
                      <Box display="flex" justifyContent="flex-end" gap={0.5}>
                        <Tooltip title="Edit">
                          <IconButton size="small" onClick={() => handleOpenDrawer(user)}>
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete">
                          <IconButton size="small" onClick={() => handleDelete(user)} sx={{ color: COLORS.error }}>
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination */}
        <TablePagination
          component="div"
          count={filteredUsers.length}
          page={page}
          onPageChange={(_, newPage) => setPage(newPage)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10));
            setPage(0);
          }}
          rowsPerPageOptions={[5, 10, 25, 50]}
        />
      </Paper>

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
                onChange={(e) => setFormData({ ...formData, role: e.target.value as User['role'] })}
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
                onChange={(e) => setFormData({ ...formData, status: e.target.value as User['status'] })}
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
              sx={{ backgroundColor: COLORS.primary }}
            >
              {createMutation.isPending ? 'Saving...' : editingUser ? 'Update' : 'Create'}
            </Button>
          </Box>
        </Box>
      </Drawer>

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

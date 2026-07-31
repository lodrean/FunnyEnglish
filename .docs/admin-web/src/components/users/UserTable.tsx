/**
 * UserTable Component
 * 
 * A comprehensive user list table with:
 * - Columns: Avatar, Name, Email, Role, Status, Last Active, Actions
 * - Filters: Role dropdown, Status toggle, Date range
 * - Bulk actions: Delete, Change role, Export CSV
 * - Status toggle with optimistic UI updates
 * - Row click to open user detail drawer
 * - Client-side filtering
 */

import React, { useState, useMemo, useCallback } from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Avatar,
  Chip,
  IconButton,
  Switch,
  Checkbox,
  Toolbar,
  Typography,
  Button,
  Box,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Menu,
  MenuItem,
  Drawer,
  FormControl,
  InputLabel,
  Select,
  TextField,
  Stack,
  Badge,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  MoreVert as MoreVertIcon,
  FileDownload as FileDownloadIcon,
  Person as PersonIcon,
  FilterList as FilterListIcon,
  Clear as ClearIcon,
} from '@mui/icons-material';
import { format } from 'date-fns';

// Design System Colors
const colors = {
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
export type UserRole = 'admin' | 'editor' | 'viewer';
export type UserStatus = 'active' | 'inactive';

export interface User {
  id: string;
  name: string;
  email: string;
  avatar?: string;
  role: UserRole;
  status: UserStatus;
  lastActive: Date;
  createdAt: Date;
  phone?: string;
  department?: string;
}

export interface UserFilters {
  role: UserRole | 'all';
  status: UserStatus | 'all';
  search: string;
  dateFrom: string | null;
  dateTo: string | null;
}

interface UserTableProps {
  users: User[];
  onUpdateUser: (userId: string, updates: Partial<User>) => Promise<void>;
  onDeleteUsers: (userIds: string[]) => Promise<void>;
  onExportCSV: (userIds: string[]) => void;
  loading?: boolean;
}

// Role configuration
const roleConfig: Record<UserRole, { label: string; color: string }> = {
  admin: { label: 'Admin', color: colors.error },
  editor: { label: 'Editor', color: colors.warning },
  viewer: { label: 'Viewer', color: colors.info },
};

// Status configuration
const statusConfig: Record<UserStatus, { label: string; color: string }> = {
  active: { label: 'Active', color: colors.success },
  inactive: { label: 'Inactive', color: colors.textSecondary },
};

export const UserTable: React.FC<UserTableProps> = ({
  users,
  onUpdateUser,
  onDeleteUsers,
  onExportCSV,
  loading = false,
}) => {
  // State
  const [selected, setSelected] = useState<string[]>([]);
  const [filters, setFilters] = useState<UserFilters>({
    role: 'all',
    status: 'all',
    search: '',
    dateFrom: null,
    dateTo: null,
  });
  const [showFilters, setShowFilters] = useState(false);
  const [detailUser, setDetailUser] = useState<User | null>(null);
  const [confirmDialog, setConfirmDialog] = useState<{
    open: boolean;
    type: 'delete' | 'changeRole' | null;
    role?: UserRole;
  }>({ open: false, type: null });
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuUserId, setMenuUserId] = useState<string | null>(null);
  const [optimisticUpdates, setOptimisticUpdates] = useState<Record<string, Partial<User>>>({});

  // Apply optimistic updates to users
  const usersWithOptimistic = useMemo(() => {
    return users.map(user => ({
      ...user,
      ...optimisticUpdates[user.id],
    }));
  }, [users, optimisticUpdates]);

  // Client-side filtering
  const filteredUsers = useMemo(() => {
    return usersWithOptimistic.filter(user => {
      // Role filter
      if (filters.role !== 'all' && user.role !== filters.role) return false;
      
      // Status filter
      if (filters.status !== 'all' && user.status !== filters.status) return false;
      
      // Search filter
      if (filters.search) {
        const searchLower = filters.search.toLowerCase();
        const matchesName = user.name.toLowerCase().includes(searchLower);
        const matchesEmail = user.email.toLowerCase().includes(searchLower);
        if (!matchesName && !matchesEmail) return false;
      }
      
      // Date range filter
      if (filters.dateFrom) {
        const fromDate = new Date(filters.dateFrom);
        if (user.lastActive < fromDate) return false;
      }
      if (filters.dateTo) {
        const toDate = new Date(filters.dateTo);
        toDate.setHours(23, 59, 59, 999);
        if (user.lastActive > toDate) return false;
      }
      
      return true;
    });
  }, [usersWithOptimistic, filters]);

  // Selection handlers
  const handleSelectAll = useCallback((event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelected(filteredUsers.map(u => u.id));
    } else {
      setSelected([]);
    }
  }, [filteredUsers]);

  const handleSelectOne = useCallback((userId: string) => {
    setSelected(prev => {
      if (prev.includes(userId)) {
        return prev.filter(id => id !== userId);
      }
      return [...prev, userId];
    });
  }, []);

  // Status toggle with optimistic UI
  const handleStatusToggle = useCallback(async (user: User) => {
    const newStatus: UserStatus = user.status === 'active' ? 'inactive' : 'active';
    
    // Optimistic update
    setOptimisticUpdates(prev => ({
      ...prev,
      [user.id]: { status: newStatus },
    }));
    
    try {
      await onUpdateUser(user.id, { status: newStatus });
      // Clear optimistic update on success
      setOptimisticUpdates(prev => {
        const { [user.id]: _, ...rest } = prev;
        return rest;
      });
    } catch (error) {
      // Revert on error
      setOptimisticUpdates(prev => {
        const { [user.id]: _, ...rest } = prev;
        return rest;
      });
      console.error('Failed to update status:', error);
    }
  }, [onUpdateUser]);

  // Bulk actions
  const handleBulkDelete = useCallback(async () => {
    try {
      await onDeleteUsers(selected);
      setSelected([]);
      setConfirmDialog({ open: false, type: null });
    } catch (error) {
      console.error('Failed to delete users:', error);
    }
  }, [selected, onDeleteUsers]);

  const handleBulkRoleChange = useCallback(async (role: UserRole) => {
    try {
      await Promise.all(selected.map(id => onUpdateUser(id, { role })));
      setSelected([]);
      setConfirmDialog({ open: false, type: null });
    } catch (error) {
      console.error('Failed to change roles:', error);
    }
  }, [selected, onUpdateUser]);

  const handleExportCSV = useCallback(() => {
    onExportCSV(selected.length > 0 ? selected : filteredUsers.map(u => u.id));
  }, [selected, filteredUsers, onExportCSV]);

  // Menu handlers
  const handleMenuOpen = useCallback((event: React.MouseEvent<HTMLElement>, userId: string) => {
    setAnchorEl(event.currentTarget);
    setMenuUserId(userId);
  }, []);

  const handleMenuClose = useCallback(() => {
    setAnchorEl(null);
    setMenuUserId(null);
  }, []);

  // Filter handlers
  const handleFilterChange = useCallback(<K extends keyof UserFilters>(
    key: K,
    value: UserFilters[K]
  ) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  }, []);

  const handleClearFilters = useCallback(() => {
    setFilters({
      role: 'all',
      status: 'all',
      search: '',
      dateFrom: null,
      dateTo: null,
    });
  }, []);

  const activeFilterCount = useMemo(() => {
    let count = 0;
    if (filters.role !== 'all') count++;
    if (filters.status !== 'all') count++;
    if (filters.search) count++;
    if (filters.dateFrom || filters.dateTo) count++;
    return count;
  }, [filters]);

  // Check if all filtered users are selected
  const isAllSelected = filteredUsers.length > 0 && selected.length === filteredUsers.length;
  const isIndeterminate = selected.length > 0 && selected.length < filteredUsers.length;

  return (
    <Box>
      {/* Toolbar */}
      <Toolbar
        sx={{
          pl: { sm: 2 },
          pr: { xs: 1, sm: 1 },
          bgcolor: colors.background,
          borderRadius: 1,
          mb: 2,
          display: 'flex',
          flexWrap: 'wrap',
          gap: 1,
        }}
      >
        {selected.length > 0 ? (
          <>
            <Typography color="inherit" variant="subtitle1" component="div">
              {selected.length} selected
            </Typography>
            <Box sx={{ flexGrow: 1 }} />
            <Button
              size="small"
              onClick={() => setConfirmDialog({ open: true, type: 'changeRole' })}
              sx={{ color: colors.primary }}
            >
              Change Role
            </Button>
            <Button
              size="small"
              onClick={() => setConfirmDialog({ open: true, type: 'delete' })}
              sx={{ color: colors.error }}
            >
              Delete
            </Button>
          </>
        ) : (
          <>
            <Typography variant="h6" component="div">
              Users
            </Typography>
            <Box sx={{ flexGrow: 1 }} />
          </>
        )}
        <Button
          size="small"
          startIcon={<FileDownloadIcon />}
          onClick={handleExportCSV}
          sx={{ color: colors.primary }}
        >
          Export CSV
        </Button>
        <Button
          size="small"
          startIcon={<FilterListIcon />}
          onClick={() => setShowFilters(!showFilters)}
          sx={{ color: colors.primary }}
        >
          Filters
          {activeFilterCount > 0 && (
            <Badge badgeContent={activeFilterCount} color="error" sx={{ ml: 1 }} />
          )}
        </Button>
      </Toolbar>

      {/* Filters Panel */}
      {showFilters && (
        <Paper sx={{ p: 2, mb: 2, bgcolor: colors.card }}>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} alignItems="center">
            <TextField
              size="small"
              placeholder="Search by name or email"
              value={filters.search}
              onChange={(e) => handleFilterChange('search', e.target.value)}
              sx={{ minWidth: 200 }}
            />
            <FormControl size="small" sx={{ minWidth: 120 }}>
              <InputLabel>Role</InputLabel>
              <Select
                value={filters.role}
                label="Role"
                onChange={(e) => handleFilterChange('role', e.target.value as UserRole | 'all')}
              >
                <MenuItem value="all">All Roles</MenuItem>
                <MenuItem value="admin">Admin</MenuItem>
                <MenuItem value="editor">Editor</MenuItem>
                <MenuItem value="viewer">Viewer</MenuItem>
              </Select>
            </FormControl>
            <FormControl size="small" sx={{ minWidth: 120 }}>
              <InputLabel>Status</InputLabel>
              <Select
                value={filters.status}
                label="Status"
                onChange={(e) => handleFilterChange('status', e.target.value as UserStatus | 'all')}
              >
                <MenuItem value="all">All Status</MenuItem>
                <MenuItem value="active">Active</MenuItem>
                <MenuItem value="inactive">Inactive</MenuItem>
              </Select>
            </FormControl>
            <TextField
              size="small"
              type="date"
              label="From"
              value={filters.dateFrom || ''}
              onChange={(e) => handleFilterChange('dateFrom', e.target.value || null)}
              InputLabelProps={{ shrink: true }}
            />
            <TextField
              size="small"
              type="date"
              label="To"
              value={filters.dateTo || ''}
              onChange={(e) => handleFilterChange('dateTo', e.target.value || null)}
              InputLabelProps={{ shrink: true }}
            />
            <Button
              size="small"
              startIcon={<ClearIcon />}
              onClick={handleClearFilters}
              disabled={activeFilterCount === 0}
            >
              Clear
            </Button>
          </Stack>
        </Paper>
      )}

      {/* Filter Chips */}
      {activeFilterCount > 0 && (
        <Box sx={{ mb: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          {filters.role !== 'all' && (
            <Chip
              label={`Role: ${roleConfig[filters.role].label}`}
              onDelete={() => handleFilterChange('role', 'all')}
              size="small"
              sx={{ bgcolor: colors.primary, color: 'white' }}
            />
          )}
          {filters.status !== 'all' && (
            <Chip
              label={`Status: ${statusConfig[filters.status].label}`}
              onDelete={() => handleFilterChange('status', 'all')}
              size="small"
              sx={{ bgcolor: colors.success, color: 'white' }}
            />
          )}
          {filters.search && (
            <Chip
              label={`Search: ${filters.search}`}
              onDelete={() => handleFilterChange('search', '')}
              size="small"
            />
          )}
          {(filters.dateFrom || filters.dateTo) && (
            <Chip
              label={`Date: ${filters.dateFrom || '...'} to ${filters.dateTo || '...'}`}
              onDelete={() => {
                handleFilterChange('dateFrom', null);
                handleFilterChange('dateTo', null);
              }}
              size="small"
            />
          )}
        </Box>
      )}

      {/* Table */}
      <TableContainer component={Paper} sx={{ bgcolor: colors.card }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: colors.background }}>
              <TableCell padding="checkbox">
                <Checkbox
                  checked={isAllSelected}
                  indeterminate={isIndeterminate}
                  onChange={handleSelectAll}
                />
              </TableCell>
              <TableCell>User</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Role</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Last Active</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredUsers.map((user) => {
              const isSelected = selected.includes(user.id);
              const isOptimistic = !!optimisticUpdates[user.id];
              
              return (
                <TableRow
                  key={user.id}
                  hover
                  selected={isSelected}
                  onClick={() => setDetailUser(user)}
                  sx={{
                    cursor: 'pointer',
                    opacity: isOptimistic ? 0.7 : 1,
                    '&:hover': { bgcolor: `${colors.primary}10` },
                  }}
                >
                  <TableCell padding="checkbox" onClick={(e) => e.stopPropagation()}>
                    <Checkbox
                      checked={isSelected}
                      onChange={() => handleSelectOne(user.id)}
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                      <Avatar
                        src={user.avatar}
                        alt={user.name}
                        sx={{ width: 36, height: 36, bgcolor: colors.primary }}
                      >
                        {!user.avatar && <PersonIcon />}
                      </Avatar>
                      <Typography variant="body2" fontWeight={500}>
                        {user.name}
                      </Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color={colors.textSecondary}>
                      {user.email}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={roleConfig[user.role].label}
                      size="small"
                      sx={{
                        bgcolor: `${roleConfig[user.role].color}20`,
                        color: roleConfig[user.role].color,
                        fontWeight: 500,
                        fontSize: '0.75rem',
                      }}
                    />
                  </TableCell>
                  <TableCell onClick={(e) => e.stopPropagation()}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Switch
                        size="small"
                        checked={user.status === 'active'}
                        onChange={() => handleStatusToggle(user)}
                        sx={{
                          '& .MuiSwitch-switchBase.Mui-checked': {
                            color: colors.success,
                          },
                          '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
                            bgcolor: colors.success,
                          },
                        }}
                      />
                      <Chip
                        label={statusConfig[user.status].label}
                        size="small"
                        sx={{
                          bgcolor: `${statusConfig[user.status].color}20`,
                          color: statusConfig[user.status].color,
                          fontSize: '0.7rem',
                          height: 20,
                        }}
                      />
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2" color={colors.textSecondary}>
                      {format(user.lastActive, 'MMM d, yyyy')}
                    </Typography>
                  </TableCell>
                  <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                    <Tooltip title="Edit">
                      <IconButton
                        size="small"
                        onClick={() => setDetailUser(user)}
                        sx={{ color: colors.primary }}
                      >
                        <EditIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="More">
                      <IconButton
                        size="small"
                        onClick={(e) => handleMenuOpen(e, user.id)}
                      >
                        <MoreVertIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </TableCell>
                </TableRow>
              );
            })}
            {filteredUsers.length === 0 && (
              <TableRow>
                <TableCell colSpan={7} align="center" sx={{ py: 4 }}>
                  <Typography color={colors.textSecondary}>
                    No users found matching your filters.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* User Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem
          onClick={() => {
            if (menuUserId) {
              const user = users.find(u => u.id === menuUserId);
              if (user) setDetailUser(user);
            }
            handleMenuClose();
          }}
        >
          View Details
        </MenuItem>
        <MenuItem
          onClick={() => {
            if (menuUserId) {
              setSelected([menuUserId]);
              setConfirmDialog({ open: true, type: 'delete' });
            }
            handleMenuClose();
          }}
          sx={{ color: colors.error }}
        >
          Delete
        </MenuItem>
      </Menu>

      {/* Confirmation Dialog */}
      <Dialog
        open={confirmDialog.open}
        onClose={() => setConfirmDialog({ open: false, type: null })}
      >
        <DialogTitle>
          {confirmDialog.type === 'delete' ? 'Delete Users' : 'Change Role'}
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            {confirmDialog.type === 'delete'
              ? `Are you sure you want to delete ${selected.length} user(s)? This action cannot be undone.`
              : 'Select the new role for the selected users:'}
          </DialogContentText>
          {confirmDialog.type === 'changeRole' && (
            <FormControl fullWidth sx={{ mt: 2 }}>
              <InputLabel>New Role</InputLabel>
              <Select
                value={confirmDialog.role || ''}
                label="New Role"
                onChange={(e) =>
                  setConfirmDialog(prev => ({ ...prev, role: e.target.value as UserRole }))
                }
              >
                <MenuItem value="admin">Admin</MenuItem>
                <MenuItem value="editor">Editor</MenuItem>
                <MenuItem value="viewer">Viewer</MenuItem>
              </Select>
            </FormControl>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialog({ open: false, type: null })}>
            Cancel
          </Button>
          <Button
            onClick={() => {
              if (confirmDialog.type === 'delete') {
                handleBulkDelete();
              } else if (confirmDialog.type === 'changeRole' && confirmDialog.role) {
                handleBulkRoleChange(confirmDialog.role);
              }
            }}
            color={confirmDialog.type === 'delete' ? 'error' : 'primary'}
            disabled={confirmDialog.type === 'changeRole' && !confirmDialog.role}
          >
            {confirmDialog.type === 'delete' ? 'Delete' : 'Change Role'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* User Detail Drawer */}
      <Drawer
        anchor="right"
        open={Boolean(detailUser)}
        onClose={() => setDetailUser(null)}
        PaperProps={{ sx: { width: { xs: '100%', sm: 400 } } }}
      >
        {detailUser && (
          <Box sx={{ p: 3 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
              <Avatar
                src={detailUser.avatar}
                alt={detailUser.name}
                sx={{ width: 64, height: 64, bgcolor: colors.primary }}
              >
                {!detailUser.avatar && <PersonIcon sx={{ fontSize: 32 }} />}
              </Avatar>
              <Box>
                <Typography variant="h6">{detailUser.name}</Typography>
                <Typography variant="body2" color={colors.textSecondary}>
                  {detailUser.email}
                </Typography>
              </Box>
            </Box>

            <Stack spacing={2}>
              <Box>
                <Typography variant="caption" color={colors.textSecondary}>
                  Role
                </Typography>
                <Chip
                  label={roleConfig[detailUser.role].label}
                  sx={{
                    ml: 1,
                    bgcolor: `${roleConfig[detailUser.role].color}20`,
                    color: roleConfig[detailUser.role].color,
                  }}
                />
              </Box>
              <Box>
                <Typography variant="caption" color={colors.textSecondary}>
                  Status
                </Typography>
                <Chip
                  label={statusConfig[detailUser.status].label}
                  sx={{
                    ml: 1,
                    bgcolor: `${statusConfig[detailUser.status].color}20`,
                    color: statusConfig[detailUser.status].color,
                  }}
                />
              </Box>
              <Box>
                <Typography variant="caption" color={colors.textSecondary}>
                  Last Active
                </Typography>
                <Typography variant="body2">
                  {format(detailUser.lastActive, 'PPP p')}
                </Typography>
              </Box>
              <Box>
                <Typography variant="caption" color={colors.textSecondary}>
                  Member Since
                </Typography>
                <Typography variant="body2">
                  {format(detailUser.createdAt, 'PPP')}
                </Typography>
              </Box>
              {detailUser.phone && (
                <Box>
                  <Typography variant="caption" color={colors.textSecondary}>
                    Phone
                  </Typography>
                  <Typography variant="body2">{detailUser.phone}</Typography>
                </Box>
              )}
              {detailUser.department && (
                <Box>
                  <Typography variant="caption" color={colors.textSecondary}>
                    Department
                  </Typography>
                  <Typography variant="body2">{detailUser.department}</Typography>
                </Box>
              )}
            </Stack>

            <Box sx={{ mt: 4, display: 'flex', gap: 1 }}>
              <Button
                variant="contained"
                fullWidth
                sx={{ bgcolor: colors.primary }}
                onClick={() => setDetailUser(null)}
              >
                Close
              </Button>
            </Box>
          </Box>
        )}
      </Drawer>
    </Box>
  );
};

export default UserTable;

/**
 * GroupManager Component
 * 
 * User groups management with:
 * - Group list with member counts
 * - Add/edit/delete groups
 * - Assign users to groups
 * - Group permissions
 * - Search and filter groups
 */

import React, { useState, useCallback, useMemo } from 'react';
import {
  Paper,
  Box,
  Typography,
  Button,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  IconButton,
  Chip,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
  Menu,
  MenuItem,
  Stack,
  Grid,
  Autocomplete,
  Checkbox,
  Alert,
  Snackbar,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Group as GroupIcon,
  MoreVert as MoreVertIcon,
  Search as SearchIcon,
  Check as CheckIcon,
  ArrowBack as ArrowBackIcon,
} from '@mui/icons-material';
import type { User } from './UserTable';
import { UserCard } from './UserCard';

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

// Group interface
export interface UserGroup {
  id: string;
  name: string;
  description: string;
  color: string;
  memberIds: string[];
  permissions: string[];
  createdAt: Date;
  updatedAt: Date;
  createdBy: string;
}

// Default group colors
const groupColors = [
  '#4A90D9', // Primary
  '#43A047', // Success
  '#E53935', // Error
  '#FB8C00', // Warning
  '#2196F3', // Info
  '#9C27B0', // Purple
  '#00BCD4', // Cyan
  '#FF5722', // Deep Orange
  '#795548', // Brown
  '#607D8B', // Blue Grey
];

interface GroupManagerProps {
  /** List of all groups */
  groups: UserGroup[];
  /** List of all users for assignment */
  users: User[];
  /** Callback when group is created */
  onCreateGroup: (group: Omit<UserGroup, 'id' | 'createdAt' | 'updatedAt'>) => Promise<void>;
  /** Callback when group is updated */
  onUpdateGroup: (groupId: string, updates: Partial<UserGroup>) => Promise<void>;
  /** Callback when group is deleted */
  onDeleteGroup: (groupId: string) => Promise<void>;
  /** Callback when users are assigned to group */
  onAssignUsers: (groupId: string, userIds: string[]) => Promise<void>;
  /** Callback when users are removed from group */
  onRemoveUsers: (groupId: string, userIds: string[]) => Promise<void>;
  /** Loading state */
  loading?: boolean;
  /** Available permissions for groups */
  availablePermissions?: string[];
}

/**
 * GroupManager - User groups management interface
 * 
 * @example
 * ```tsx
 * <GroupManager
 *   groups={groups}
 *   users={users}
 *   onCreateGroup={handleCreate}
 *   onUpdateGroup={handleUpdate}
 *   onDeleteGroup={handleDelete}
 *   onAssignUsers={handleAssign}
 *   onRemoveUsers={handleRemove}
 * />
 * ```
 */
export const GroupManager: React.FC<GroupManagerProps> = ({
  groups,
  users,
  onCreateGroup,
  onUpdateGroup,
  onDeleteGroup,
  onAssignUsers,
  onRemoveUsers,
  loading: _loading = false,
  availablePermissions = [],
}) => {
  // State
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedGroup, setSelectedGroup] = useState<UserGroup | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState<'create' | 'edit'>('create');
  const [groupForm, setGroupForm] = useState({
    name: '',
    description: '',
    color: groupColors[0],
    permissions: [] as string[],
  });
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuGroupId, setMenuGroupId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState(0);
  const [selectedUserIds, setSelectedUserIds] = useState<string[]>([]);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [confirmDelete, setConfirmDelete] = useState<string | null>(null);

  // Filter groups by search
  const filteredGroups = useMemo(() => {
    if (!searchQuery.trim()) return groups;
    const query = searchQuery.toLowerCase();
    return groups.filter(
      group =>
        group.name.toLowerCase().includes(query) ||
        group.description.toLowerCase().includes(query)
    );
  }, [groups, searchQuery]);

  // Get members of selected group
  const groupMembers = useMemo(() => {
    if (!selectedGroup) return [];
    return users.filter(user => selectedGroup.memberIds.includes(user.id));
  }, [selectedGroup, users]);

  // Get available users (not in group)
  const availableUsers = useMemo(() => {
    if (!selectedGroup) return [];
    return users.filter(user => !selectedGroup.memberIds.includes(user.id));
  }, [selectedGroup, users]);

  // Handle create group
  const handleCreate = useCallback(async () => {
    try {
      await onCreateGroup({
        name: groupForm.name,
        description: groupForm.description,
        color: groupForm.color,
        memberIds: [],
        permissions: groupForm.permissions,
        createdBy: 'current-user', // Should come from auth context
      });
      setDialogOpen(false);
      setGroupForm({ name: '', description: '', color: groupColors[0], permissions: [] });
      setSnackbar({ open: true, message: 'Group created successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to create group', severity: 'error' });
    }
  }, [groupForm, onCreateGroup]);

  // Handle update group
  const handleUpdate = useCallback(async () => {
    if (!menuGroupId) return;
    try {
      await onUpdateGroup(menuGroupId, {
        name: groupForm.name,
        description: groupForm.description,
        color: groupForm.color,
        permissions: groupForm.permissions,
      });
      setDialogOpen(false);
      setSnackbar({ open: true, message: 'Group updated successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to update group', severity: 'error' });
    }
  }, [groupForm, menuGroupId, onUpdateGroup]);

  // Handle delete group
  const handleDelete = useCallback(async (groupId: string) => {
    try {
      await onDeleteGroup(groupId);
      setConfirmDelete(null);
      if (selectedGroup?.id === groupId) {
        setSelectedGroup(null);
      }
      setSnackbar({ open: true, message: 'Group deleted successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to delete group', severity: 'error' });
    }
  }, [onDeleteGroup, selectedGroup]);

  // Handle assign users
  const handleAssignUsers = useCallback(async () => {
    if (!selectedGroup || selectedUserIds.length === 0) return;
    try {
      await onAssignUsers(selectedGroup.id, selectedUserIds);
      setSelectedUserIds([]);
      setSnackbar({ open: true, message: 'Users assigned successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to assign users', severity: 'error' });
    }
  }, [selectedGroup, selectedUserIds, onAssignUsers]);

  // Handle remove users
  const handleRemoveUser = useCallback(async (userId: string) => {
    if (!selectedGroup) return;
    try {
      await onRemoveUsers(selectedGroup.id, [userId]);
      setSnackbar({ open: true, message: 'User removed from group', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to remove user', severity: 'error' });
    }
  }, [selectedGroup, onRemoveUsers]);

  // Open create dialog
  const openCreateDialog = useCallback(() => {
    setDialogMode('create');
    setGroupForm({ name: '', description: '', color: groupColors[0], permissions: [] });
    setDialogOpen(true);
  }, []);

  // Open edit dialog
  const openEditDialog = useCallback((group: UserGroup) => {
    setDialogMode('edit');
    setGroupForm({
      name: group.name,
      description: group.description,
      color: group.color,
      permissions: group.permissions,
    });
    setDialogOpen(true);
  }, []);

  // Menu handlers
  const handleMenuOpen = useCallback((event: React.MouseEvent<HTMLElement>, groupId: string) => {
    event.stopPropagation();
    setAnchorEl(event.currentTarget);
    setMenuGroupId(groupId);
  }, []);

  const handleMenuClose = useCallback(() => {
    setAnchorEl(null);
    setMenuGroupId(null);
  }, []);

  // Group list view
  if (!selectedGroup) {
    return (
      <Box>
        {/* Header */}
        <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
          <Box>
            <Typography variant="h6" gutterBottom>
              User Groups
            </Typography>
            <Typography variant="body2" color={colors.textSecondary}>
              Manage user groups and their permissions
            </Typography>
          </Box>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={openCreateDialog}
            sx={{ bgcolor: colors.primary }}
          >
            Create Group
          </Button>
        </Box>

        {/* Search */}
        <TextField
          fullWidth
          size="small"
          placeholder="Search groups..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1, color: colors.textSecondary }} />,
          }}
          sx={{ mb: 2, '& .MuiOutlinedInput-root': { bgcolor: colors.card } }}
        />

        {/* Groups List */}
        <Paper sx={{ bgcolor: colors.card }}>
          <List>
            {filteredGroups.map((group, index) => (
              <React.Fragment key={group.id}>
                {index > 0 && <Divider />}
                <ListItem
                  onClick={() => setSelectedGroup(group)}
                  sx={{
                    '&:hover': { bgcolor: `${colors.primary}08` },
                  }}
                >
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: group.color }}>
                      <GroupIcon />
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="subtitle2">{group.name}</Typography>
                        <Chip
                          label={`${group.memberIds.length} members`}
                          size="small"
                          sx={{
                            height: 18,
                            fontSize: '0.65rem',
                            bgcolor: `${group.color}20`,
                            color: group.color,
                          }}
                        />
                      </Box>
                    }
                    secondary={
                      <Typography variant="caption" color={colors.textSecondary} noWrap>
                        {group.description}
                      </Typography>
                    }
                  />
                  <IconButton
                    edge="end"
                    onClick={(e) => handleMenuOpen(e, group.id)}
                  >
                    <MoreVertIcon />
                  </IconButton>
                </ListItem>
              </React.Fragment>
            ))}
            {filteredGroups.length === 0 && (
              <ListItem>
                <ListItemText
                  primary={
                    <Typography color={colors.textSecondary} align="center" sx={{ py: 4 }}>
                      {searchQuery ? 'No groups match your search' : 'No groups created yet'}
                    </Typography>
                  }
                />
              </ListItem>
            )}
          </List>
        </Paper>

        {/* Group Menu */}
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={handleMenuClose}
        >
          <MenuItem
            onClick={() => {
              const group = groups.find(g => g.id === menuGroupId);
              if (group) {
                openEditDialog(group);
              }
              handleMenuClose();
            }}
          >
            <EditIcon fontSize="small" sx={{ mr: 1 }} />
            Edit
          </MenuItem>
          <MenuItem
            onClick={() => {
              if (menuGroupId) {
                const group = groups.find(g => g.id === menuGroupId);
                if (group) {
                  setSelectedGroup(group);
                }
              }
              handleMenuClose();
            }}
          >
            <GroupIcon fontSize="small" sx={{ mr: 1 }} />
            Manage Members
          </MenuItem>
          <MenuItem
            onClick={() => {
              if (menuGroupId) {
                setConfirmDelete(menuGroupId);
              }
              handleMenuClose();
            }}
            sx={{ color: colors.error }}
          >
            <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
            Delete
          </MenuItem>
        </Menu>

        {/* Create/Edit Dialog */}
        <Dialog
          open={dialogOpen}
          onClose={() => setDialogOpen(false)}
          maxWidth="sm"
          fullWidth
        >
          <DialogTitle>
            {dialogMode === 'create' ? 'Create New Group' : 'Edit Group'}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              <TextField
                label="Group Name"
                fullWidth
                value={groupForm.name}
                onChange={(e) => setGroupForm(prev => ({ ...prev, name: e.target.value }))}
                placeholder="e.g., Content Editors"
              />
              <TextField
                label="Description"
                fullWidth
                multiline
                rows={2}
                value={groupForm.description}
                onChange={(e) => setGroupForm(prev => ({ ...prev, description: e.target.value }))}
                placeholder="What is this group for?"
              />
              <Box>
                <Typography variant="caption" color={colors.textSecondary} sx={{ mb: 1, display: 'block' }}>
                  Group Color
                </Typography>
                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                  {groupColors.map(color => (
                    <IconButton
                      key={color}
                      onClick={() => setGroupForm(prev => ({ ...prev, color }))}
                      sx={{
                        width: 32,
                        height: 32,
                        bgcolor: color,
                        '&:hover': { bgcolor: color },
                        border: groupForm.color === color ? `2px solid ${colors.textPrimary}` : 'none',
                      }}
                    >
                      {groupForm.color === color && <CheckIcon sx={{ color: 'white', fontSize: 16 }} />}
                    </IconButton>
                  ))}
                </Box>
              </Box>
              {availablePermissions.length > 0 && (
                <Autocomplete
                  multiple
                  options={availablePermissions}
                  value={groupForm.permissions}
                  onChange={(_, value) => setGroupForm(prev => ({ ...prev, permissions: value }))}
                  renderTags={(value, getTagProps) =>
                    value.map((option, index) => (
                      <Chip
                        variant="outlined"
                        label={option}
                        size="small"
                        {...getTagProps({ index })}
                        key={option}
                      />
                    ))
                  }
                  renderInput={(params) => (
                    <TextField
                      {...params}
                      label="Group Permissions"
                      placeholder="Select permissions"
                    />
                  )}
                />
              )}
            </Stack>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
            <Button
              variant="contained"
              onClick={dialogMode === 'create' ? handleCreate : handleUpdate}
              disabled={!groupForm.name.trim()}
              sx={{ bgcolor: colors.primary }}
            >
              {dialogMode === 'create' ? 'Create' : 'Save'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Delete Confirmation */}
        <Dialog
          open={Boolean(confirmDelete)}
          onClose={() => setConfirmDelete(null)}
        >
          <DialogTitle>Delete Group</DialogTitle>
          <DialogContent>
            <Typography>
              Are you sure you want to delete this group? This action cannot be undone.
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setConfirmDelete(null)}>Cancel</Button>
            <Button
              color="error"
              variant="contained"
              onClick={() => confirmDelete && handleDelete(confirmDelete)}
            >
              Delete
            </Button>
          </DialogActions>
        </Dialog>

        {/* Snackbar */}
        <Snackbar
          open={snackbar.open}
          autoHideDuration={4000}
          onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
          anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        >
          <Alert
            severity={snackbar.severity}
            onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
          >
            {snackbar.message}
          </Alert>
        </Snackbar>
      </Box>
    );
  }

  // Group detail view
  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => setSelectedGroup(null)}
          sx={{ mb: 2, color: colors.textSecondary }}
        >
          Back to Groups
        </Button>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Avatar sx={{ bgcolor: selectedGroup.color, width: 48, height: 48 }}>
            <GroupIcon />
          </Avatar>
          <Box>
            <Typography variant="h6">{selectedGroup.name}</Typography>
            <Typography variant="body2" color={colors.textSecondary}>
              {selectedGroup.description}
            </Typography>
          </Box>
        </Box>
      </Box>

      {/* Tabs */}
      <Tabs
        value={activeTab}
        onChange={(_, value) => setActiveTab(value)}
        sx={{ mb: 2, borderBottom: 1, borderColor: 'divider' }}
      >
        <Tab label={`Members (${groupMembers.length})`} />
        <Tab label="Add Members" />
        <Tab label="Settings" />
      </Tabs>

      {/* Members Tab */}
      {activeTab === 0 && (
        <Box>
          {groupMembers.length === 0 ? (
            <Paper sx={{ p: 4, textAlign: 'center', bgcolor: colors.background }}>
              <GroupIcon sx={{ fontSize: 48, color: colors.textSecondary, mb: 2 }} />
              <Typography color={colors.textSecondary}>
                No members in this group yet.
              </Typography>
              <Button
                variant="outlined"
                sx={{ mt: 2 }}
                onClick={() => setActiveTab(1)}
              >
                Add Members
              </Button>
            </Paper>
          ) : (
            <Grid container spacing={2}>
              {groupMembers.map(user => (
                <Grid item xs={12} sm={6} md={4} key={user.id}>
                  <UserCard
                    user={user}
                    compact
                    showRemove
                    onRemove={() => handleRemoveUser(user.id)}
                  />
                </Grid>
              ))}
            </Grid>
          )}
        </Box>
      )}

      {/* Add Members Tab */}
      {activeTab === 1 && (
        <Box>
          <Paper sx={{ p: 2, mb: 2, bgcolor: colors.card }}>
            <Autocomplete
              multiple
              options={availableUsers}
              getOptionLabel={(user) => `${user.name} (${user.email})`}
              value={users.filter(u => selectedUserIds.includes(u.id))}
              onChange={(_, selectedUsers) => {
                setSelectedUserIds(selectedUsers.map(u => u.id));
              }}
              renderOption={(props, user) => (
                <li {...props}>
                  <Checkbox checked={selectedUserIds.includes(user.id)} />
                  <Box sx={{ ml: 1 }}>
                    <Typography variant="body2">{user.name}</Typography>
                    <Typography variant="caption" color={colors.textSecondary}>
                      {user.email}
                    </Typography>
                  </Box>
                </li>
              )}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="Select users to add"
                  placeholder="Search users..."
                />
              )}
              renderTags={(value, getTagProps) =>
                value.map((option, index) => (
                  <Chip
                    avatar={<Avatar src={option.avatar} alt={option.name} />}
                    label={option.name}
                    size="small"
                    {...getTagProps({ index })}
                    key={option.id}
                  />
                ))
              }
            />
            <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
              <Button
                variant="contained"
                onClick={handleAssignUsers}
                disabled={selectedUserIds.length === 0}
                sx={{ bgcolor: colors.primary }}
              >
                Add {selectedUserIds.length > 0 && `(${selectedUserIds.length})`} Members
              </Button>
            </Box>
          </Paper>

          {/* Available Users List */}
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Available Users ({availableUsers.length})
          </Typography>
          <Grid container spacing={2}>
            {availableUsers.slice(0, 6).map(user => (
              <Grid item xs={12} sm={6} md={4} key={user.id}>
                <UserCard
                  user={user}
                  compact
                  onClick={(u) => {
                    if (selectedUserIds.includes(u.id)) {
                      setSelectedUserIds(prev => prev.filter(id => id !== u.id));
                    } else {
                      setSelectedUserIds(prev => [...prev, u.id]);
                    }
                  }}
                  selected={selectedUserIds.includes(user.id)}
                />
              </Grid>
            ))}
          </Grid>
          {availableUsers.length > 6 && (
            <Typography variant="caption" color={colors.textSecondary} sx={{ mt: 1, display: 'block', textAlign: 'center' }}>
              And {availableUsers.length - 6} more users...
            </Typography>
          )}
        </Box>
      )}

      {/* Settings Tab */}
      {activeTab === 2 && (
        <Paper sx={{ p: 3, bgcolor: colors.card }}>
          <Typography variant="subtitle2" gutterBottom>
            Group Information
          </Typography>
          <Stack spacing={2}>
            <TextField
              label="Group Name"
              defaultValue={selectedGroup.name}
              size="small"
              fullWidth
            />
            <TextField
              label="Description"
              defaultValue={selectedGroup.description}
              size="small"
              fullWidth
              multiline
              rows={2}
            />
            <Box>
              <Typography variant="caption" color={colors.textSecondary} sx={{ mb: 1, display: 'block' }}>
                Group Color
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                {groupColors.map(color => (
                  <IconButton
                    key={color}
                    sx={{
                      width: 32,
                      height: 32,
                      bgcolor: color,
                      '&:hover': { bgcolor: color },
                      border: selectedGroup.color === color ? `2px solid ${colors.textPrimary}` : 'none',
                    }}
                  >
                    {selectedGroup.color === color && <CheckIcon sx={{ color: 'white', fontSize: 16 }} />}
                  </IconButton>
                ))}
              </Box>
            </Box>
            <Divider />
            <Box>
              <Typography variant="subtitle2" color={colors.error} gutterBottom>
                Danger Zone
              </Typography>
              <Button
                variant="outlined"
                color="error"
                startIcon={<DeleteIcon />}
                onClick={() => setConfirmDelete(selectedGroup.id)}
              >
                Delete Group
              </Button>
            </Box>
          </Stack>
        </Paper>
      )}

      {/* Snackbar */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={4000}
        onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          severity={snackbar.severity}
          onClose={() => setSnackbar(prev => ({ ...prev, open: false }))}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>

      {/* Delete Confirmation */}
      <Dialog
        open={Boolean(confirmDelete)}
        onClose={() => setConfirmDelete(null)}
      >
        <DialogTitle>Delete Group</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete this group? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDelete(null)}>Cancel</Button>
          <Button
            color="error"
            variant="contained"
            onClick={() => confirmDelete && handleDelete(confirmDelete)}
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default GroupManager;

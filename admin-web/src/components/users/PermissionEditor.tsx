/**
 * PermissionEditor Component
 * 
 * Role-based permission management with:
 * - Matrix: Permissions × Roles
 * - Checkboxes for each permission
 * - Default role templates (Admin, Editor, Viewer)
 * - Save permission changes
 * - Indeterminate state for partial permissions
 */

import React, { useState, useCallback, useMemo, useEffect } from 'react';
import {
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Checkbox,
  Button,
  Box,
  Typography,
  Chip,
  Alert,
  Snackbar,
  Divider,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  Stack,
} from '@mui/material';
import {
  Save as SaveIcon,
  Refresh as RefreshIcon,
  AdminPanelSettings as AdminIcon,
  Edit as EditorIcon,
  Visibility as ViewerIcon,

} from '@mui/icons-material';
import type { UserRole } from './UserTable';

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

// Permission categories and items
export interface Permission {
  id: string;
  name: string;
  description: string;
  category: string;
}

export interface RolePermissions {
  admin: Record<string, boolean>;
  editor: Record<string, boolean>;
  viewer: Record<string, boolean>;
}

// Default permissions list
// eslint-disable-next-line react-refresh/only-export-components
export const defaultPermissions: Permission[] = [
  // User Management
  { id: 'users.view', name: 'View Users', description: 'View user list and details', category: 'User Management' },
  { id: 'users.create', name: 'Create Users', description: 'Add new users', category: 'User Management' },
  { id: 'users.edit', name: 'Edit Users', description: 'Modify user information', category: 'User Management' },
  { id: 'users.delete', name: 'Delete Users', description: 'Remove users from system', category: 'User Management' },
  { id: 'users.manageRoles', name: 'Manage Roles', description: 'Assign and change user roles', category: 'User Management' },
  
  // Content Management
  { id: 'content.view', name: 'View Content', description: 'View learning content', category: 'Content Management' },
  { id: 'content.create', name: 'Create Content', description: 'Add new learning content', category: 'Content Management' },
  { id: 'content.edit', name: 'Edit Content', description: 'Modify existing content', category: 'Content Management' },
  { id: 'content.delete', name: 'Delete Content', description: 'Remove content from system', category: 'Content Management' },
  { id: 'content.publish', name: 'Publish Content', description: 'Publish or unpublish content', category: 'Content Management' },
  
  // Course Management
  { id: 'courses.view', name: 'View Courses', description: 'View course list and details', category: 'Course Management' },
  { id: 'courses.create', name: 'Create Courses', description: 'Add new courses', category: 'Course Management' },
  { id: 'courses.edit', name: 'Edit Courses', description: 'Modify course information', category: 'Course Management' },
  { id: 'courses.delete', name: 'Delete Courses', description: 'Remove courses from system', category: 'Course Management' },
  
  // Analytics
  { id: 'analytics.view', name: 'View Analytics', description: 'Access analytics dashboard', category: 'Analytics' },
  { id: 'analytics.export', name: 'Export Reports', description: 'Export analytics reports', category: 'Analytics' },
  
  // Settings
  { id: 'settings.view', name: 'View Settings', description: 'View system settings', category: 'Settings' },
  { id: 'settings.edit', name: 'Edit Settings', description: 'Modify system settings', category: 'Settings' },
  
  // Groups
  { id: 'groups.view', name: 'View Groups', description: 'View user groups', category: 'Groups' },
  { id: 'groups.manage', name: 'Manage Groups', description: 'Create and manage groups', category: 'Groups' },
];

// Default role templates
// eslint-disable-next-line react-refresh/only-export-components
export const roleTemplates: Record<UserRole, Record<string, boolean>> = {
  admin: defaultPermissions.reduce((acc, perm) => ({ ...acc, [perm.id]: true }), {}),
  editor: defaultPermissions.reduce((acc, perm) => ({
    ...acc,
    [perm.id]: [
      'users.view',
      'content.view', 'content.create', 'content.edit', 'content.publish',
      'courses.view', 'courses.create', 'courses.edit',
      'analytics.view',
      'groups.view',
    ].includes(perm.id),
  }), {}),
  viewer: defaultPermissions.reduce((acc, perm) => ({
    ...acc,
    [perm.id]: [
      'users.view',
      'content.view',
      'courses.view',
      'analytics.view',
      'groups.view',
    ].includes(perm.id),
  }), {}),
};

// Role configuration
const roleConfig: Record<UserRole, { label: string; color: string; icon: React.ReactNode }> = {
  admin: { label: 'Admin', color: colors.error, icon: <AdminIcon /> },
  editor: { label: 'Editor', color: colors.warning, icon: <EditorIcon /> },
  viewer: { label: 'Viewer', color: colors.info, icon: <ViewerIcon /> },
};

interface PermissionEditorProps {
  /** Current permissions state */
  permissions: RolePermissions;
  /** Callback when permissions are saved */
  onSave: (permissions: RolePermissions) => Promise<void>;
  /** Loading state */
  loading?: boolean;
  /** Read-only mode */
  readOnly?: boolean;
  /** Custom permission list */
  customPermissions?: Permission[];
}

/**
 * PermissionEditor - Role-based permission management matrix
 * 
 * @example
 * ```tsx
 * const [permissions, setPermissions] = useState(roleTemplates);
 * 
 * <PermissionEditor
 *   permissions={permissions}
 *   onSave={async (perms) => {
 *     await api.savePermissions(perms);
 *     setPermissions(perms);
 *   }}
 * />
 * ```
 */
export const PermissionEditor: React.FC<PermissionEditorProps> = ({
  permissions,
  onSave,
  loading: _loading = false,
  readOnly = false,
  customPermissions,
}) => {
  const permissionsList = customPermissions || defaultPermissions;
  
  // Local state for editing
  const [localPermissions, setLocalPermissions] = useState<RolePermissions>(permissions);
  const [hasChanges, setHasChanges] = useState(false);
  const [saving, setSaving] = useState(false);
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });
  const [templateDialogOpen, setTemplateDialogOpen] = useState(false);

  // Sync with props
  useEffect(() => {
    setLocalPermissions(permissions);
    setHasChanges(false);
  }, [permissions]);

  // Group permissions by category
  const groupedPermissions = useMemo(() => {
    const groups: Record<string, Permission[]> = {};
    permissionsList.forEach(perm => {
      if (!groups[perm.category]) {
        groups[perm.category] = [];
      }
      groups[perm.category].push(perm);
    });
    return groups;
  }, [permissionsList]);

  // Check if all permissions in a category are checked for a role
  const isCategoryChecked = useCallback((category: string, role: UserRole): boolean => {
    const categoryPerms = groupedPermissions[category];
    return categoryPerms.every(perm => localPermissions[role][perm.id]);
  }, [groupedPermissions, localPermissions]);

  // Check if some permissions in a category are checked for a role
  const isCategoryIndeterminate = useCallback((category: string, role: UserRole): boolean => {
    const categoryPerms = groupedPermissions[category];
    const checkedCount = categoryPerms.filter(perm => localPermissions[role][perm.id]).length;
    return checkedCount > 0 && checkedCount < categoryPerms.length;
  }, [groupedPermissions, localPermissions]);

  // Toggle a single permission
  const togglePermission = useCallback((role: UserRole, permissionId: string) => {
    if (readOnly) return;
    
    setLocalPermissions(prev => ({
      ...prev,
      [role]: {
        ...prev[role],
        [permissionId]: !prev[role][permissionId],
      },
    }));
    setHasChanges(true);
  }, [readOnly]);

  // Toggle all permissions in a category
  const toggleCategory = useCallback((category: string, role: UserRole) => {
    if (readOnly) return;
    
    const categoryPerms = groupedPermissions[category];
    const allChecked = isCategoryChecked(category, role);
    
    setLocalPermissions(prev => ({
      ...prev,
      [role]: {
        ...prev[role],
        ...categoryPerms.reduce((acc, perm) => ({
          ...acc,
          [perm.id]: !allChecked,
        }), {}),
      },
    }));
    setHasChanges(true);
  }, [groupedPermissions, isCategoryChecked, readOnly]);

  // Apply role template
  const applyTemplate = useCallback((role: UserRole) => {
    setLocalPermissions(prev => ({
      ...prev,
      [role]: { ...roleTemplates[role] },
    }));
    setHasChanges(true);
    setTemplateDialogOpen(false);
  }, []);

  // Reset to original
  const handleReset = useCallback(() => {
    setLocalPermissions(permissions);
    setHasChanges(false);
  }, [permissions]);

  // Save changes
  const handleSave = useCallback(async () => {
    setSaving(true);
    try {
      await onSave(localPermissions);
      setHasChanges(false);
      setSnackbar({ open: true, message: 'Permissions saved successfully', severity: 'success' });
    } catch {
      setSnackbar({ open: true, message: 'Failed to save permissions', severity: 'error' });
    } finally {
      setSaving(false);
    }
  }, [localPermissions, onSave]);

  // Calculate permission counts
  const permissionCounts = useMemo(() => {
    const counts: Record<UserRole, number> = { admin: 0, editor: 0, viewer: 0 };
    (Object.keys(counts) as UserRole[]).forEach(role => {
      counts[role] = Object.values(localPermissions[role]).filter(Boolean).length;
    });
    return counts;
  }, [localPermissions]);

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
        <Box>
          <Typography variant="h6" gutterBottom>
            Permission Matrix
          </Typography>
          <Typography variant="body2" color={colors.textSecondary}>
            Manage what each role can access and modify in the system
          </Typography>
        </Box>
        
        {!readOnly && (
          <Stack direction="row" spacing={1}>
            <Button
              variant="outlined"
              size="small"
              startIcon={<RefreshIcon />}
              onClick={() => setTemplateDialogOpen(true)}
            >
              Apply Template
            </Button>
            <Button
              variant="outlined"
              size="small"
              onClick={handleReset}
              disabled={!hasChanges}
            >
              Reset
            </Button>
            <Button
              variant="contained"
              size="small"
              startIcon={<SaveIcon />}
              onClick={handleSave}
              disabled={!hasChanges || saving}
              sx={{ bgcolor: colors.primary }}
            >
              {saving ? 'Saving...' : 'Save Changes'}
            </Button>
          </Stack>
        )}
      </Box>

      {/* Role Summary */}
      <Paper sx={{ p: 2, mb: 3, bgcolor: colors.background }}>
        <Stack direction="row" spacing={3} flexWrap="wrap">
          {(Object.keys(roleConfig) as UserRole[]).map(role => (
            <Box key={role} sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Box sx={{ color: roleConfig[role].color }}>
                {roleConfig[role].icon}
              </Box>
              <Box>
                <Typography variant="subtitle2">{roleConfig[role].label}</Typography>
                <Typography variant="caption" color={colors.textSecondary}>
                  {permissionCounts[role]} / {permissionsList.length} permissions
                </Typography>
              </Box>
            </Box>
          ))}
        </Stack>
      </Paper>

      {/* Permission Matrix */}
      <TableContainer component={Paper} sx={{ bgcolor: colors.card }}>
        <Table size="small">
          <TableHead>
            <TableRow sx={{ bgcolor: colors.background }}>
              <TableCell sx={{ minWidth: 250 }}>
                <Typography variant="subtitle2">Permission</Typography>
              </TableCell>
              {(Object.keys(roleConfig) as UserRole[]).map(role => (
                <TableCell key={role} align="center" sx={{ minWidth: 100 }}>
                  <Chip
                    icon={<>{roleConfig[role].icon}</>}
                    label={roleConfig[role].label}
                    size="small"
                    sx={{
                      bgcolor: `${roleConfig[role].color}15`,
                      color: roleConfig[role].color,
                      fontWeight: 500,
                    }}
                  />
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {Object.entries(groupedPermissions).map(([category, perms]) => (
              <React.Fragment key={category}>
                {/* Category Header */}
                <TableRow sx={{ bgcolor: `${colors.background}80` }}>
                  <TableCell colSpan={4} sx={{ py: 1 }}>
                    <Typography variant="subtitle2" color={colors.primary} fontWeight={600}>
                      {category}
                    </Typography>
                  </TableCell>
                </TableRow>
                
                {/* Category Select All Row */}
                {!readOnly && (
                  <TableRow sx={{ bgcolor: colors.background }}>
                    <TableCell sx={{ pl: 4 }}>
                      <Typography variant="caption" color={colors.textSecondary}>
                        Select All in {category}
                      </Typography>
                    </TableCell>
                    {(Object.keys(roleConfig) as UserRole[]).map(role => (
                      <TableCell key={role} align="center">
                        <Checkbox
                          size="small"
                          checked={isCategoryChecked(category, role)}
                          indeterminate={isCategoryIndeterminate(category, role)}
                          onChange={() => toggleCategory(category, role)}
                        />
                      </TableCell>
                    ))}
                  </TableRow>
                )}
                
                {/* Permission Rows */}
                {perms.map(perm => (
                  <TableRow key={perm.id} hover>
                    <TableCell sx={{ pl: 4 }}>
                      <Box>
                        <Typography variant="body2" fontWeight={500}>
                          {perm.name}
                        </Typography>
                        <Typography variant="caption" color={colors.textSecondary}>
                          {perm.description}
                        </Typography>
                      </Box>
                    </TableCell>
                    {(Object.keys(roleConfig) as UserRole[]).map(role => (
                      <TableCell key={role} align="center">
                        <Tooltip title={`${roleConfig[role].label}: ${perm.name}`}>
                          <Checkbox
                            size="small"
                            checked={!!localPermissions[role][perm.id]}
                            onChange={() => togglePermission(role, perm.id)}
                            disabled={readOnly}
                            sx={{
                              color: localPermissions[role][perm.id] ? roleConfig[role].color : undefined,
                              '&.Mui-checked': {
                                color: roleConfig[role].color,
                              },
                            }}
                          />
                        </Tooltip>
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </React.Fragment>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Unsaved Changes Warning */}
      {hasChanges && (
        <Alert severity="warning" sx={{ mt: 2 }}>
          You have unsaved changes. Don&apos;t forget to save your modifications.
        </Alert>
      )}

      {/* Template Dialog */}
      <Dialog
        open={templateDialogOpen}
        onClose={() => setTemplateDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Apply Role Template</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color={colors.textSecondary} sx={{ mb: 2 }}>
            Select a role to apply its default permission template. This will overwrite current permissions for that role.
          </Typography>
          <List>
            {(Object.keys(roleConfig) as UserRole[]).map(role => (
              <ListItem key={role} disablePadding>
                <ListItemButton onClick={() => applyTemplate(role)}>
                  <Box sx={{ color: roleConfig[role].color, mr: 2 }}>
                    {roleConfig[role].icon}
                  </Box>
                  <ListItemText
                    primary={roleConfig[role].label}
                    secondary={`${Object.values(roleTemplates[role]).filter(Boolean).length} permissions`}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setTemplateDialogOpen(false)}>Cancel</Button>
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
};

/**
 * PermissionSummary - Compact permission overview for a role
 */
interface PermissionSummaryProps {
  role: UserRole;
  permissions: Record<string, boolean>;
  permissionList?: Permission[];
}

export const PermissionSummary: React.FC<PermissionSummaryProps> = ({
  role,
  permissions,
  permissionList = defaultPermissions,
}) => {
  const grantedCount = Object.values(permissions).filter(Boolean).length;
  const totalCount = permissionList.length;
  
  // Group by category
  const categoryCounts = useMemo(() => {
    const counts: Record<string, { granted: number; total: number }> = {};
    permissionList.forEach(perm => {
      if (!counts[perm.category]) {
        counts[perm.category] = { granted: 0, total: 0 };
      }
      counts[perm.category].total++;
      if (permissions[perm.id]) {
        counts[perm.category].granted++;
      }
    });
    return counts;
  }, [permissions, permissionList]);

  return (
    <Paper sx={{ p: 2, bgcolor: colors.card }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 2 }}>
        <Box sx={{ color: roleConfig[role].color }}>
          {roleConfig[role].icon}
        </Box>
        <Box>
          <Typography variant="subtitle1">{roleConfig[role].label}</Typography>
          <Typography variant="caption" color={colors.textSecondary}>
            {grantedCount} of {totalCount} permissions granted
          </Typography>
        </Box>
      </Box>
      
      <Divider sx={{ my: 1.5 }} />
      
      <Stack spacing={1}>
        {Object.entries(categoryCounts).map(([category, counts]) => (
          <Box key={category} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="caption" color={colors.textSecondary}>
              {category}
            </Typography>
            <Chip
              label={`${counts.granted}/${counts.total}`}
              size="small"
              sx={{
                height: 18,
                fontSize: '0.65rem',
                bgcolor: counts.granted === counts.total ? `${colors.success}20` : `${colors.warning}20`,
                color: counts.granted === counts.total ? colors.success : colors.warning,
              }}
            />
          </Box>
        ))}
      </Stack>
    </Paper>
  );
};

export default PermissionEditor;

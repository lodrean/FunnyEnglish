/**
 * UserFilters Component
 * 
 * Comprehensive filter controls for user management:
 * - Role filter dropdown
 * - Status filter (Active/Inactive/All)
 * - Date range picker
 * - Search by name/email
 * - Clear filters button
 * - Filter chip display
 */

import React, { useCallback, useMemo } from 'react';
import {
  Paper,
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Chip,
  Stack,
  IconButton,
  Typography,
  Collapse,
  Tooltip,
  Divider,
} from '@mui/material';
import {
  FilterList as FilterListIcon,
  Clear as ClearIcon,
  Search as SearchIcon,
  CalendarToday as CalendarIcon,
  Person as PersonIcon,
  CheckCircle as CheckCircleIcon,
  Cancel as CancelIcon,
} from '@mui/icons-material';
import type { UserRole, UserStatus, UserFilters as UserFiltersType } from './UserTable';

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

// Role configuration
const roleConfig: Record<UserRole | 'all', { label: string; icon: React.ReactNode; color: string }> = {
  all: { label: 'All Roles', icon: <PersonIcon fontSize="small" />, color: colors.textSecondary },
  admin: { label: 'Admin', icon: <PersonIcon fontSize="small" />, color: colors.error },
  editor: { label: 'Editor', icon: <PersonIcon fontSize="small" />, color: colors.warning },
  viewer: { label: 'Viewer', icon: <PersonIcon fontSize="small" />, color: colors.info },
};

// Status configuration
const statusConfig: Record<UserStatus | 'all', { label: string; icon: React.ReactNode; color: string }> = {
  all: { label: 'All Status', icon: <FilterListIcon fontSize="small" />, color: colors.textSecondary },
  active: { label: 'Active', icon: <CheckCircleIcon fontSize="small" />, color: colors.success },
  inactive: { label: 'Inactive', icon: <CancelIcon fontSize="small" />, color: colors.textSecondary },
};

interface UserFiltersProps {
  /** Current filter values */
  filters: UserFiltersType;
  /** Callback when filters change */
  onChange: (filters: UserFiltersType) => void;
  /** Whether filters panel is expanded */
  expanded?: boolean;
  /** Callback to toggle expansion */
  onToggleExpand?: () => void;
  /** Show filter chips */
  showChips?: boolean;
  /** Compact mode for smaller layouts */
  compact?: boolean;
  /** Additional CSS class */
  className?: string;
}

/**
 * UserFilters - Comprehensive filter controls for user management
 * 
 * @example
 * ```tsx
 * const [filters, setFilters] = useState({
 *   role: 'all',
 *   status: 'all',
 *   search: '',
 *   dateFrom: null,
 *   dateTo: null,
 * });
 * 
 * <UserFilters
 *   filters={filters}
 *   onChange={setFilters}
 *   showChips
 * />
 * ```
 */
export const UserFilters: React.FC<UserFiltersProps> = ({
  filters,
  onChange,
  expanded = true,
  onToggleExpand,
  showChips = true,
  compact = false,
  className,
}) => {
  /**
   * Count active filters
   */
  const activeFilterCount = useMemo(() => {
    let count = 0;
    if (filters.role !== 'all') count++;
    if (filters.status !== 'all') count++;
    if (filters.search?.trim()) count++;
    if (filters.dateFrom) count++;
    if (filters.dateTo) count++;
    return count;
  }, [filters]);

  /**
   * Check if any filters are active
   */
  const hasActiveFilters = activeFilterCount > 0;

  /**
   * Update a single filter value
   */
  const handleFilterChange = useCallback(<K extends keyof UserFiltersType>(
    key: K,
    value: UserFiltersType[K]
  ) => {
    onChange({ ...filters, [key]: value });
  }, [filters, onChange]);

  /**
   * Clear all filters
   */
  const handleClearFilters = useCallback(() => {
    onChange({
      role: 'all',
      status: 'all',
      search: '',
      dateFrom: null,
      dateTo: null,
    });
  }, [onChange]);

  /**
   * Remove a specific filter
   */
  const handleRemoveFilter = useCallback((key: keyof UserFiltersType) => {
    const defaultValues: Record<keyof UserFiltersType, UserFiltersType[keyof UserFiltersType]> = {
      role: 'all',
      status: 'all',
      search: '',
      dateFrom: null,
      dateTo: null,
    };
    onChange({ ...filters, [key]: defaultValues[key] });
  }, [filters, onChange]);

  return (
    <Box className={className}>
      {/* Filter Toggle Button (when collapsible) */}
      {onToggleExpand && (
        <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
          <Button
            variant="outlined"
            size="small"
            startIcon={<FilterListIcon />}
            onClick={onToggleExpand}
            sx={{
              borderColor: colors.primary,
              color: colors.primary,
              '&:hover': {
                borderColor: colors.primary,
                bgcolor: `${colors.primary}10`,
              },
            }}
          >
            Filters
            {hasActiveFilters && (
              <Chip
                label={activeFilterCount}
                size="small"
                sx={{
                  ml: 1,
                  height: 18,
                  fontSize: '0.7rem',
                  bgcolor: colors.error,
                  color: 'white',
                }}
              />
            )}
          </Button>
          {hasActiveFilters && (
            <Tooltip title="Clear all filters">
              <IconButton
                size="small"
                onClick={handleClearFilters}
                sx={{ color: colors.error }}
              >
                <ClearIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      )}

      {/* Filter Controls */}
      <Collapse in={expanded}>
        <Paper
          elevation={0}
          sx={{
            p: compact ? 2 : 3,
            bgcolor: colors.background,
            borderRadius: 2,
            mb: 2,
          }}
        >
          <Stack
            direction={{ xs: 'column', sm: 'row' }}
            spacing={compact ? 1.5 : 2}
            alignItems={{ xs: 'stretch', sm: 'flex-end' }}
          >
            {/* Search Field */}
            <TextField
              size="small"
              placeholder="Search by name or email"
              value={filters.search}
              onChange={(e) => handleFilterChange('search', e.target.value)}
              InputProps={{
                startAdornment: <SearchIcon fontSize="small" sx={{ mr: 1, color: colors.textSecondary }} />,
              }}
              sx={{
                minWidth: { xs: '100%', sm: 220 },
                '& .MuiOutlinedInput-root': {
                  bgcolor: colors.card,
                },
              }}
            />

            {/* Role Filter */}
            <FormControl
              size="small"
              sx={{
                minWidth: { xs: '100%', sm: 140 },
              }}
            >
              <InputLabel>Role</InputLabel>
              <Select
                value={filters.role}
                label="Role"
                onChange={(e) => handleFilterChange('role', e.target.value as UserRole | 'all')}
                sx={{ bgcolor: colors.card }}
              >
                {(Object.keys(roleConfig) as Array<UserRole | 'all'>).map((role) => (
                  <MenuItem key={role} value={role}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box sx={{ color: roleConfig[role].color }}>
                        {roleConfig[role].icon}
                      </Box>
                      {roleConfig[role].label}
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Status Filter */}
            <FormControl
              size="small"
              sx={{
                minWidth: { xs: '100%', sm: 140 },
              }}
            >
              <InputLabel>Status</InputLabel>
              <Select
                value={filters.status}
                label="Status"
                onChange={(e) => handleFilterChange('status', e.target.value as UserStatus | 'all')}
                sx={{ bgcolor: colors.card }}
              >
                {(Object.keys(statusConfig) as Array<UserStatus | 'all'>).map((status) => (
                  <MenuItem key={status} value={status}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Box sx={{ color: statusConfig[status].color }}>
                        {statusConfig[status].icon}
                      </Box>
                      {statusConfig[status].label}
                    </Box>
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* Date Range */}
            <Box
              sx={{
                display: 'flex',
                gap: 1,
                flexDirection: { xs: 'column', sm: 'row' },
              }}
            >
              <TextField
                size="small"
                type="date"
                label="From"
                value={filters.dateFrom || ''}
                onChange={(e) => handleFilterChange('dateFrom', e.target.value || null)}
                InputLabelProps={{ shrink: true }}
                InputProps={{
                  startAdornment: <CalendarIcon fontSize="small" sx={{ mr: 0.5, color: colors.textSecondary }} />,
                }}
                sx={{
                  minWidth: 140,
                  '& .MuiOutlinedInput-root': {
                    bgcolor: colors.card,
                  },
                }}
              />
              <TextField
                size="small"
                type="date"
                label="To"
                value={filters.dateTo || ''}
                onChange={(e) => handleFilterChange('dateTo', e.target.value || null)}
                InputLabelProps={{ shrink: true }}
                InputProps={{
                  startAdornment: <CalendarIcon fontSize="small" sx={{ mr: 0.5, color: colors.textSecondary }} />,
                }}
                sx={{
                  minWidth: 140,
                  '& .MuiOutlinedInput-root': {
                    bgcolor: colors.card,
                  },
                }}
              />
            </Box>

            {/* Clear Button */}
            <Button
              size="small"
              startIcon={<ClearIcon />}
              onClick={handleClearFilters}
              disabled={!hasActiveFilters}
              sx={{
                color: colors.error,
                '&:disabled': {
                  color: colors.textSecondary,
                },
              }}
            >
              Clear
            </Button>
          </Stack>
        </Paper>
      </Collapse>

      {/* Filter Chips */}
      {showChips && hasActiveFilters && (
        <Box sx={{ mb: 2 }}>
          <Typography
            variant="caption"
            sx={{ color: colors.textSecondary, mb: 1, display: 'block' }}
          >
            Active Filters:
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {filters.role !== 'all' && (
              <Chip
                icon={<>{roleConfig[filters.role].icon}</>}
                label={`Role: ${roleConfig[filters.role].label}`}
                onDelete={() => handleRemoveFilter('role')}
                size="small"
                sx={{
                  bgcolor: `${roleConfig[filters.role].color}15`,
                  color: roleConfig[filters.role].color,
                  '& .MuiChip-icon': {
                    color: roleConfig[filters.role].color,
                  },
                  '& .MuiChip-deleteIcon': {
                    color: `${roleConfig[filters.role].color}80`,
                    '&:hover': {
                      color: roleConfig[filters.role].color,
                    },
                  },
                }}
              />
            )}
            {filters.status !== 'all' && (
              <Chip
                icon={<>{statusConfig[filters.status].icon}</>}
                label={`Status: ${statusConfig[filters.status].label}`}
                onDelete={() => handleRemoveFilter('status')}
                size="small"
                sx={{
                  bgcolor: `${statusConfig[filters.status].color}15`,
                  color: statusConfig[filters.status].color,
                  '& .MuiChip-icon': {
                    color: statusConfig[filters.status].color,
                  },
                  '& .MuiChip-deleteIcon': {
                    color: `${statusConfig[filters.status].color}80`,
                    '&:hover': {
                      color: statusConfig[filters.status].color,
                    },
                  },
                }}
              />
            )}
            {filters.search?.trim() && (
              <Chip
                icon={<SearchIcon fontSize="small" />}
                label={`Search: "${filters.search}"`}
                onDelete={() => handleRemoveFilter('search')}
                size="small"
                sx={{
                  bgcolor: `${colors.primary}15`,
                  color: colors.primary,
                }}
              />
            )}
            {filters.dateFrom && (
              <Chip
                icon={<CalendarIcon fontSize="small" />}
                label={`From: ${new Date(filters.dateFrom).toLocaleDateString()}`}
                onDelete={() => handleRemoveFilter('dateFrom')}
                size="small"
                sx={{
                  bgcolor: `${colors.info}15`,
                  color: colors.info,
                }}
              />
            )}
            {filters.dateTo && (
              <Chip
                icon={<CalendarIcon fontSize="small" />}
                label={`To: ${new Date(filters.dateTo).toLocaleDateString()}`}
                onDelete={() => handleRemoveFilter('dateTo')}
                size="small"
                sx={{
                  bgcolor: `${colors.info}15`,
                  color: colors.info,
                }}
              />
            )}
            {activeFilterCount > 1 && (
              <>
                <Divider orientation="vertical" flexItem sx={{ mx: 0.5 }} />
                <Button
                  size="small"
                  onClick={handleClearFilters}
                  sx={{
                    color: colors.error,
                    textTransform: 'none',
                    fontSize: '0.75rem',
                  }}
                >
                  Clear All
                </Button>
              </>
            )}
          </Box>
        </Box>
      )}
    </Box>
  );
};

/**
 * FilterChipGroup - Display filter chips in a compact group
 */
interface FilterChipGroupProps {
  filters: UserFiltersType;
  onRemoveFilter: (key: keyof UserFiltersType) => void;
  onClearAll: () => void;
}

export const FilterChipGroup: React.FC<FilterChipGroupProps> = ({
  filters,
  onRemoveFilter,
  onClearAll,
}) => {
  const activeFilters = useMemo(() => {
    const items: Array<{ key: keyof UserFiltersType; label: string; color: string }> = [];
    
    if (filters.role !== 'all') {
      items.push({
        key: 'role',
        label: `Role: ${roleConfig[filters.role].label}`,
        color: roleConfig[filters.role].color,
      });
    }
    if (filters.status !== 'all') {
      items.push({
        key: 'status',
        label: `Status: ${statusConfig[filters.status].label}`,
        color: statusConfig[filters.status].color,
      });
    }
    if (filters.search?.trim()) {
      items.push({
        key: 'search',
        label: `Search: "${filters.search}"`,
        color: colors.primary,
      });
    }
    if (filters.dateFrom) {
      items.push({
        key: 'dateFrom',
        label: `From: ${new Date(filters.dateFrom).toLocaleDateString()}`,
        color: colors.info,
      });
    }
    if (filters.dateTo) {
      items.push({
        key: 'dateTo',
        label: `To: ${new Date(filters.dateTo).toLocaleDateString()}`,
        color: colors.info,
      });
    }
    
    return items;
  }, [filters]);

  if (activeFilters.length === 0) return null;

  return (
    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
      {activeFilters.map((filter) => (
        <Chip
          key={filter.key}
          label={filter.label}
          onDelete={() => onRemoveFilter(filter.key)}
          size="small"
          sx={{
            bgcolor: `${filter.color}15`,
            color: filter.color,
            fontSize: '0.75rem',
            '& .MuiChip-deleteIcon': {
              color: `${filter.color}80`,
              '&:hover': {
                color: filter.color,
              },
            },
          }}
        />
      ))}
      {activeFilters.length > 1 && (
        <Button
          size="small"
          onClick={onClearAll}
          sx={{
            color: colors.error,
            textTransform: 'none',
            fontSize: '0.7rem',
            minWidth: 'auto',
            p: '2px 8px',
          }}
        >
          Clear All
        </Button>
      )}
    </Box>
  );
};

export default UserFilters;

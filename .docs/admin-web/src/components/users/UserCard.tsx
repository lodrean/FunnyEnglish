/**
 * UserCard Component
 * 
 * A compact user info card displaying:
 * - Avatar with fallback
 * - Name and email
 * - Role badge (Admin, Editor, Viewer)
 * - Status indicator
 * - Compact layout for groups/assignments
 */

import React from 'react';
import {
  Card,
  CardContent,
  Avatar,
  Typography,
  Chip,
  Box,
  IconButton,
  Tooltip,
  Skeleton,
} from '@mui/material';
import {
  Person as PersonIcon,
  Close as CloseIcon,
  Edit as EditIcon,
} from '@mui/icons-material';
import type { User, UserRole, UserStatus } from './UserTable';

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

interface UserCardProps {
  /** User data to display */
  user: User;
  /** Callback when card is clicked */
  onClick?: (user: User) => void;
  /** Callback when remove button is clicked */
  onRemove?: (userId: string) => void;
  /** Callback when edit button is clicked */
  onEdit?: (user: User) => void;
  /** Show remove button */
  showRemove?: boolean;
  /** Show edit button */
  showEdit?: boolean;
  /** Compact mode - smaller padding and font sizes */
  compact?: boolean;
  /** Selected state styling */
  selected?: boolean;
  /** Loading state */
  loading?: boolean;
  /** Additional CSS class */
  className?: string;
  /** Card variant */
  variant?: 'elevation' | 'outlined';
}

/**
 * Get initials from user name
 */
const getInitials = (name: string): string => {
  return name
    .split(' ')
    .map(part => part[0])
    .join('')
    .toUpperCase()
    .slice(0, 2);
};

/**
 * UserCard - Compact user information display
 * 
 * @example
 * ```tsx
 * <UserCard
 *   user={user}
 *   onClick={(u) => console.log('Clicked:', u.name)}
 *   onRemove={(id) => console.log('Remove:', id)}
 *   showRemove
 *   compact
 * />
 * ```
 */
export const UserCard: React.FC<UserCardProps> = ({
  user,
  onClick,
  onRemove,
  onEdit,
  showRemove = false,
  showEdit = false,
  compact = false,
  selected = false,
  loading = false,
  className,
  variant = 'outlined',
}) => {
  // Loading skeleton
  if (loading) {
    return (
      <Card
        variant={variant}
        className={className}
        sx={{
          bgcolor: colors.card,
          borderRadius: 2,
        }}
      >
        <CardContent sx={{ p: compact ? 1.5 : 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: compact ? 1.5 : 2 }}>
            <Skeleton variant="circular" width={compact ? 36 : 48} height={compact ? 36 : 48} />
            <Box sx={{ flex: 1 }}>
              <Skeleton variant="text" width="60%" height={compact ? 20 : 24} />
              <Skeleton variant="text" width="80%" height={compact ? 16 : 20} />
            </Box>
          </Box>
        </CardContent>
      </Card>
    );
  }

  const handleClick = () => {
    onClick?.(user);
  };

  const handleRemove = (e: React.MouseEvent) => {
    e.stopPropagation();
    onRemove?.(user.id);
  };

  const handleEdit = (e: React.MouseEvent) => {
    e.stopPropagation();
    onEdit?.(user);
  };

  return (
    <Card
      variant={variant}
      className={className}
      onClick={handleClick}
      sx={{
        bgcolor: colors.card,
        borderRadius: 2,
        cursor: onClick ? 'pointer' : 'default',
        borderColor: selected ? colors.primary : undefined,
        borderWidth: selected ? 2 : undefined,
        transition: 'all 0.2s ease',
        '&:hover': onClick
          ? {
              boxShadow: 2,
              borderColor: colors.primary,
              transform: 'translateY(-1px)',
            }
          : undefined,
      }}
    >
      <CardContent
        sx={{
          p: compact ? 1.5 : 2,
          '&:last-child': { pb: compact ? 1.5 : 2 },
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: compact ? 1.5 : 2 }}>
          {/* Avatar with status indicator */}
          <Box sx={{ position: 'relative' }}>
            <Avatar
              src={user.avatar}
              alt={user.name}
              sx={{
                width: compact ? 36 : 48,
                height: compact ? 36 : 48,
                bgcolor: colors.primary,
                fontSize: compact ? 14 : 16,
                fontWeight: 500,
              }}
            >
              {!user.avatar && (getInitials(user.name) || <PersonIcon />)}
            </Avatar>
            {/* Status indicator dot */}
            <Box
              sx={{
                position: 'absolute',
                bottom: 0,
                right: 0,
                width: compact ? 10 : 12,
                height: compact ? 10 : 12,
                borderRadius: '50%',
                bgcolor: statusConfig[user.status].color,
                border: `2px solid ${colors.card}`,
              }}
            />
          </Box>

          {/* User info */}
          <Box sx={{ flex: 1, minWidth: 0 }}>
            <Typography
              variant={compact ? 'body2' : 'subtitle2'}
              fontWeight={600}
              noWrap
              sx={{ color: colors.textPrimary }}
            >
              {user.name}
            </Typography>
            <Typography
              variant="caption"
              noWrap
              sx={{ color: colors.textSecondary, display: 'block' }}
            >
              {user.email}
            </Typography>
            
            {/* Role badge - shown in non-compact or when explicitly needed */}
            {!compact && (
              <Chip
                label={roleConfig[user.role].label}
                size="small"
                sx={{
                  mt: 0.5,
                  height: 18,
                  fontSize: '0.65rem',
                  bgcolor: `${roleConfig[user.role].color}20`,
                  color: roleConfig[user.role].color,
                  fontWeight: 500,
                }}
              />
            )}
          </Box>

          {/* Action buttons */}
          {(showEdit || showRemove) && (
            <Box sx={{ display: 'flex', gap: 0.5 }}>
              {showEdit && (
                <Tooltip title="Edit">
                  <IconButton
                    size={compact ? 'small' : 'medium'}
                    onClick={handleEdit}
                    sx={{
                      color: colors.primary,
                      '&:hover': { bgcolor: `${colors.primary}15` },
                    }}
                  >
                    <EditIcon fontSize={compact ? 'small' : 'medium'} />
                  </IconButton>
                </Tooltip>
              )}
              {showRemove && (
                <Tooltip title="Remove">
                  <IconButton
                    size={compact ? 'small' : 'medium'}
                    onClick={handleRemove}
                    sx={{
                      color: colors.error,
                      '&:hover': { bgcolor: `${colors.error}15` },
                    }}
                  >
                    <CloseIcon fontSize={compact ? 'small' : 'medium'} />
                  </IconButton>
                </Tooltip>
              )}
            </Box>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};

/**
 * UserCardList - Display multiple user cards in a grid or list
 */
interface UserCardListProps {
  users: User[];
  onUserClick?: (user: User) => void;
  onUserRemove?: (userId: string) => void;
  selectedIds?: string[];
  loading?: boolean;
  emptyMessage?: string;
  columns?: number;
}

export const UserCardList: React.FC<UserCardListProps> = ({
  users,
  onUserClick,
  onUserRemove,
  selectedIds = [],
  loading = false,
  emptyMessage = 'No users found',
  columns = 1,
}) => {
  if (loading) {
    return (
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: `repeat(${columns}, 1fr)`,
          gap: 2,
        }}
      >
        {[1, 2, 3].map(i => (
          <UserCard
            key={i}
            user={{} as User}
            loading
            compact
          />
        ))}
      </Box>
    );
  }

  if (users.length === 0) {
    return (
      <Box
        sx={{
          p: 4,
          textAlign: 'center',
          bgcolor: colors.background,
          borderRadius: 2,
        }}
      >
        <Typography color={colors.textSecondary}>{emptyMessage}</Typography>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        display: 'grid',
        gridTemplateColumns: {
          xs: '1fr',
          sm: columns > 1 ? `repeat(${Math.min(columns, 2)}, 1fr)` : '1fr',
          md: columns > 1 ? `repeat(${Math.min(columns, 3)}, 1fr)` : '1fr',
        },
        gap: 2,
      }}
    >
      {users.map(user => (
        <UserCard
          key={user.id}
          user={user}
          onClick={onUserClick}
          onRemove={onUserRemove}
          showRemove={!!onUserRemove}
          selected={selectedIds.includes(user.id)}
          compact
        />
      ))}
    </Box>
  );
};

export default UserCard;

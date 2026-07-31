/**
 * FunnyEnglish Admin Web - Header Component
 * Top navigation with menu toggle, search, notifications, and user menu
 */

import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Box,
  InputBase,
  Badge,
  Avatar,
  Menu,
  MenuItem,
  Divider,
  Tooltip,
  useMediaQuery,
  useTheme as useMuiTheme,
  alpha,
} from '@mui/material';
import {
  Menu as MenuIcon,
  Search as SearchIcon,
  Notifications as NotificationsIcon,
  AccountCircle as AccountCircleIcon,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { useTheme } from '../../theme/ThemeProvider';
import { Breadcrumbs } from './Breadcrumbs';

// Header height constant
export const HEADER_HEIGHT = 64;

/**
 * Props for Header component
 */
interface HeaderProps {
  /** Callback to toggle sidebar */
  onMenuToggle: () => void;
  /** Page title to display */
  title?: string;
  /** Whether to show breadcrumbs */
  showBreadcrumbs?: boolean;
}

/**
 * Header Component
 * Top navigation bar with search, notifications, and user menu
 */
export const Header: React.FC<HeaderProps> = ({
  onMenuToggle,
  title,
  showBreadcrumbs = true,
}) => {
  const navigate = useNavigate();
  const muiTheme = useMuiTheme();
  const { mode, toggleTheme, isDarkMode } = useTheme();
  const isMobile = useMediaQuery(muiTheme.breakpoints.down('md'));

  // User menu state
  const [userMenuAnchor, setUserMenuAnchor] = useState<null | HTMLElement>(null);
  const [notificationsAnchor, setNotificationsAnchor] = useState<null | HTMLElement>(null);

  // Search state
  const [searchQuery, setSearchQuery] = useState('');
  const [isSearchFocused, setIsSearchFocused] = useState(false);

  // User menu handlers
  const handleUserMenuOpen = useCallback((event: React.MouseEvent<HTMLElement>) => {
    setUserMenuAnchor(event.currentTarget);
  }, []);

  const handleUserMenuClose = useCallback(() => {
    setUserMenuAnchor(null);
  }, []);

  // Notifications handlers
  const handleNotificationsOpen = useCallback((event: React.MouseEvent<HTMLElement>) => {
    setNotificationsAnchor(event.currentTarget);
  }, []);

  const handleNotificationsClose = useCallback(() => {
    setNotificationsAnchor(null);
  }, []);

  // Search handler
  const handleSearchSubmit = useCallback(
    (event: React.FormEvent) => {
      event.preventDefault();
      if (searchQuery.trim()) {
        // TODO: Implement search functionality
        console.log('Search:', searchQuery);
      }
    },
    [searchQuery]
  );

  // Logout handler
  const handleLogout = useCallback(() => {
    handleUserMenuClose();
    // TODO: Implement logout functionality
    console.log('Logout clicked');
  }, [handleUserMenuClose]);

  // Mock notifications data
  const notifications = [
    { id: 1, message: 'New user registered', time: '2 min ago', read: false },
    { id: 2, message: 'Test completed by John Doe', time: '1 hour ago', read: false },
    { id: 3, message: 'System update scheduled', time: '3 hours ago', read: true },
  ];

  const unreadCount = notifications.filter((n) => !n.read).length;

  return (
    <>
      <AppBar
        position="fixed"
        sx={{
          zIndex: (theme) => theme.zIndex.drawer + 1,
          backgroundColor: 'background.paper',
          color: 'text.primary',
          boxShadow: (theme) =>
            theme.palette.mode === 'dark'
              ? '0 2px 8px rgba(0, 0, 0, 0.3)'
              : '0 2px 8px rgba(0, 0, 0, 0.08)',
          height: HEADER_HEIGHT,
        }}
      >
        <Toolbar
          sx={{
            height: HEADER_HEIGHT,
            minHeight: HEADER_HEIGHT,
            px: { xs: 1, sm: 2 },
          }}
        >
          {/* Menu toggle button */}
          <IconButton
            color="inherit"
            aria-label="toggle sidebar"
            onClick={onMenuToggle}
            edge="start"
            sx={{
              mr: 2,
              color: 'text.secondary',
            }}
          >
            <MenuIcon />
          </IconButton>

          {/* Page title (mobile only) */}
          {isMobile && title && (
            <Typography
              variant="h6"
              noWrap
              component="div"
              sx={{
                flexGrow: 1,
                fontWeight: 600,
              }}
            >
              {title}
            </Typography>
          )}

          {/* Search bar */}
          {!isMobile && (
            <Box
              component="form"
              onSubmit={handleSearchSubmit}
              sx={{
                position: 'relative',
                borderRadius: 2,
                backgroundColor: (theme) =>
                  isSearchFocused
                    ? alpha(theme.palette.primary.main, 0.08)
                    : alpha(theme.palette.action.hover, 0.04),
                '&:hover': {
                  backgroundColor: (theme) =>
                    alpha(theme.palette.primary.main, 0.06),
                },
                ml: 2,
                mr: 2,
                width: { sm: 240, md: 320, lg: 400 },
                transition: 'all 0.2s ease-in-out',
              }}
            >
              <Box
                sx={{
                  padding: '8px 12px',
                  height: '100%',
                  position: 'absolute',
                  pointerEvents: 'none',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'text.secondary',
                }}
              >
                <SearchIcon fontSize="small" />
              </Box>
              <InputBase
                placeholder="Search..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onFocus={() => setIsSearchFocused(true)}
                onBlur={() => setIsSearchFocused(false)}
                sx={{
                  color: 'inherit',
                  width: '100%',
                  '& .MuiInputBase-input': {
                    padding: '8px 12px 8px 40px',
                    fontSize: '0.875rem',
                    '&::placeholder': {
                      color: 'text.secondary',
                      opacity: 0.7,
                    },
                  },
                }}
              />
            </Box>
          )}

          {/* Spacer */}
          <Box sx={{ flexGrow: 1 }} />

          {/* Theme toggle */}
          <Tooltip title={isDarkMode ? 'Light mode' : 'Dark mode'}>
            <IconButton
              color="inherit"
              onClick={toggleTheme}
              sx={{
                color: 'text.secondary',
                mr: 1,
              }}
            >
              {isDarkMode ? <LightModeIcon /> : <DarkModeIcon />}
            </IconButton>
          </Tooltip>

          {/* Notifications */}
          <Tooltip title="Notifications">
            <IconButton
              color="inherit"
              onClick={handleNotificationsOpen}
              sx={{
                color: 'text.secondary',
                mr: 1,
              }}
            >
              <Badge badgeContent={unreadCount} color="error">
                <NotificationsIcon />
              </Badge>
            </IconButton>
          </Tooltip>

          {/* User menu */}
          <Tooltip title="Account settings">
            <IconButton
              onClick={handleUserMenuOpen}
              size="small"
              sx={{
                ml: 1,
              }}
              aria-controls={Boolean(userMenuAnchor) ? 'account-menu' : undefined}
              aria-haspopup="true"
              aria-expanded={Boolean(userMenuAnchor) ? 'true' : undefined}
            >
              <Avatar
                sx={{
                  width: 36,
                  height: 36,
                  bgcolor: 'primary.main',
                  fontSize: '0.875rem',
                  fontWeight: 600,
                }}
              >
                AD
              </Avatar>
            </IconButton>
          </Tooltip>
        </Toolbar>

        {/* Breadcrumbs section */}
        {showBreadcrumbs && !isMobile && <Breadcrumbs />}
      </AppBar>

      {/* User menu */}
      <Menu
        anchorEl={userMenuAnchor}
        id="account-menu"
        open={Boolean(userMenuAnchor)}
        onClose={handleUserMenuClose}
        onClick={handleUserMenuClose}
        PaperProps={{
          elevation: 3,
          sx: {
            minWidth: 200,
            mt: 1.5,
            '& .MuiAvatar-root': {
              width: 32,
              height: 32,
              ml: -0.5,
              mr: 1,
            },
          },
        }}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <Box sx={{ px: 2, py: 1.5 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
            Admin User
          </Typography>
          <Typography variant="body2" color="text.secondary">
            admin@funnyenglish.com
          </Typography>
        </Box>
        <Divider />
        <MenuItem onClick={() => navigate('/settings')}>
          <ListItemIcon>
            <SettingsIcon fontSize="small" />
          </ListItemIcon>
          Settings
        </MenuItem>
        <MenuItem onClick={() => navigate('/users/admins')}>
          <ListItemIcon>
            <PersonIcon fontSize="small" />
          </ListItemIcon>
          Profile
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon fontSize="small" />
          </ListItemIcon>
          Logout
        </MenuItem>
      </Menu>

      {/* Notifications menu */}
      <Menu
        anchorEl={notificationsAnchor}
        id="notifications-menu"
        open={Boolean(notificationsAnchor)}
        onClose={handleNotificationsClose}
        PaperProps={{
          elevation: 3,
          sx: {
            minWidth: 320,
            maxWidth: 360,
            maxHeight: 400,
            mt: 1.5,
          },
        }}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <Box
          sx={{
            px: 2,
            py: 1.5,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
          }}
        >
          <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
            Notifications
          </Typography>
          {unreadCount > 0 && (
            <Typography
              variant="caption"
              color="primary"
              sx={{ cursor: 'pointer' }}
              onClick={() => console.log('Mark all as read')}
            >
              Mark all as read
            </Typography>
          )}
        </Box>
        <Divider />
        {notifications.length === 0 ? (
          <Box sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              No notifications
            </Typography>
          </Box>
        ) : (
          notifications.map((notification) => (
            <MenuItem
              key={notification.id}
              onClick={handleNotificationsClose}
              sx={{
                py: 1.5,
                px: 2,
                backgroundColor: notification.read
                  ? 'transparent'
                  : (theme) =>
                      theme.palette.mode === 'dark'
                        ? alpha(theme.palette.primary.main, 0.08)
                        : alpha(theme.palette.primary.main, 0.04),
                borderLeft: notification.read
                  ? 'none'
                  : (theme) => `3px solid ${theme.palette.primary.main}`,
              }}
            >
              <Box sx={{ width: '100%' }}>
                <Typography
                  variant="body2"
                  sx={{
                    fontWeight: notification.read ? 400 : 500,
                  }}
                >
                  {notification.message}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {notification.time}
                </Typography>
              </Box>
            </MenuItem>
          ))
        )}
        <Divider />
        <MenuItem
          onClick={() => {
            handleNotificationsClose();
            navigate('/notifications');
          }}
          sx={{
            justifyContent: 'center',
            color: 'primary.main',
          }}
        >
          <Typography variant="body2" sx={{ fontWeight: 500 }}>
            View all notifications
          </Typography>
        </MenuItem>
      </Menu>
    </>
  );
};

export default Header;

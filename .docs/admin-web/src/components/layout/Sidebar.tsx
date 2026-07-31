/**
 * FunnyEnglish Admin Web - Sidebar Component
 * Collapsible navigation sidebar with logo, nav items, and responsive drawer
 */

import React, { useState, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  Box,
  Typography,
  IconButton,
  Divider,
  Tooltip,
  useMediaQuery,
  useTheme as useMuiTheme,
  alpha,
} from '@mui/material';
import {
  ExpandLess,
  ExpandMore,
  Menu as MenuIcon,
  ChevronLeft as ChevronLeftIcon,
  School as SchoolIcon,
} from '@mui/icons-material';
import { navItems, NavItem, isNavItemActive, hasActiveChild } from '../navigation/navItems';

// Sidebar dimensions
const SIDEBAR_WIDTH = 240;
const SIDEBAR_WIDTH_COLLAPSED = 64;

/**
 * Props for Sidebar component
 */
interface SidebarProps {
  /** Whether sidebar is open (expanded) */
  open: boolean;
  /** Callback to toggle sidebar state */
  onToggle: () => void;
  /** Callback when mobile drawer should close */
  onMobileClose?: () => void;
  /** Whether mobile drawer is open */
  mobileOpen?: boolean;
}

/**
 * Sidebar Component
 * Main navigation sidebar with collapsible functionality
 */
export const Sidebar: React.FC<SidebarProps> = ({
  open,
  onToggle,
  onMobileClose,
  mobileOpen = false,
}) => {
  const location = useLocation();
  const navigate = useNavigate();
  const muiTheme = useMuiTheme();
  const isMobile = useMediaQuery(muiTheme.breakpoints.down('md'));
  const currentPath = location.pathname;

  // Track expanded menu items
  const [expandedItems, setExpandedItems] = useState<string[]>(() => {
    // Auto-expand parent of active item
    const expanded: string[] = [];
    navItems.forEach((item) => {
      if (item.children && hasActiveChild(item, currentPath)) {
        expanded.push(item.id);
      }
    });
    return expanded;
  });

  // Toggle menu item expansion
  const toggleExpand = useCallback((itemId: string) => {
    setExpandedItems((prev) =>
      prev.includes(itemId)
        ? prev.filter((id) => id !== itemId)
        : [...prev, itemId]
    );
  }, []);

  // Handle navigation
  const handleNavigate = useCallback(
    (path: string) => {
      navigate(path);
      if (isMobile && onMobileClose) {
        onMobileClose();
      }
    },
    [navigate, isMobile, onMobileClose]
  );

  // Handle parent item click
  const handleParentClick = useCallback(
    (item: NavItem) => {
      if (item.children) {
        toggleExpand(item.id);
      } else if (item.path) {
        handleNavigate(item.path);
      }
    },
    [toggleExpand, handleNavigate]
  );

  // Render navigation item
  const renderNavItem = (item: NavItem, depth: number = 0) => {
    const IconComponent = item.icon;
    const isActive = isNavItemActive(item.path, currentPath);
    const isExpanded = expandedItems.includes(item.id);
    const hasChildren = item.children && item.children.length > 0;
    const isChildActive = hasChildren && hasActiveChild(item, currentPath);

    return (
      <React.Fragment key={item.id}>
        <ListItem
          disablePadding
          sx={{
            display: 'block',
            mb: 0.5,
          }}
        >
          {open ? (
            // Expanded state - full text visible
            <ListItemButton
              onClick={() => handleParentClick(item)}
              selected={isActive || isChildActive}
              sx={{
                minHeight: 44,
                px: 2,
                mx: 1,
                borderRadius: 1,
                justifyContent: 'initial',
                pl: depth > 0 ? 4 : 2,
              }}
            >
              <ListItemIcon
                sx={{
                  minWidth: 36,
                  color: isActive || isChildActive ? 'primary.main' : 'inherit',
                }}
              >
                <IconComponent fontSize="small" />
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  fontSize: '0.875rem',
                  fontWeight: isActive || isChildActive ? 600 : 500,
                }}
              />
              {hasChildren && (
                isExpanded ? <ExpandLess fontSize="small" /> : <ExpandMore fontSize="small" />
              )}
            </ListItemButton>
          ) : (
            // Collapsed state - icon only with tooltip
            <Tooltip title={item.label} placement="right" arrow>
              <ListItemButton
                onClick={() => handleParentClick(item)}
                selected={isActive || isChildActive}
                sx={{
                  minHeight: 44,
                  px: 1.5,
                  mx: 0.5,
                  borderRadius: 1,
                  justifyContent: 'center',
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: 0,
                    color: isActive || isChildActive ? 'primary.main' : 'inherit',
                  }}
                >
                  <IconComponent fontSize="small" />
                </ListItemIcon>
              </ListItemButton>
            </Tooltip>
          )}
        </ListItem>

        {/* Render children if expanded */}
        {hasChildren && open && (
          <Collapse in={isExpanded} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.children!.map((child) => renderNavItem(child, depth + 1))}
            </List>
          </Collapse>
        )}
      </React.Fragment>
    );
  };

  // Logo section
  const LogoSection = (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: open ? 'space-between' : 'center',
        p: 2,
        minHeight: 64,
      }}
    >
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1,
          overflow: 'hidden',
          transition: 'all 0.3s ease-in-out',
          opacity: open ? 1 : 0,
          width: open ? 'auto' : 0,
        }}
      >
        <SchoolIcon sx={{ color: 'primary.main', fontSize: 28 }} />
        <Typography
          variant="h6"
          sx={{
            fontWeight: 700,
            color: 'text.primary',
            whiteSpace: 'nowrap',
          }}
        >
          FunnyEnglish
        </Typography>
      </Box>

      {/* Toggle button - only show on desktop when sidebar is open */}
      {!isMobile && (
        <IconButton
          onClick={onToggle}
          size="small"
          sx={{
            color: 'text.secondary',
            transition: 'all 0.3s ease-in-out',
            transform: open ? 'rotate(0deg)' : 'rotate(180deg)',
          }}
        >
          {open ? <ChevronLeftIcon /> : <MenuIcon />}
        </IconButton>
      )}
    </Box>
  );

  // Navigation content
  const NavigationContent = (
    <>
      {LogoSection}
      <Divider sx={{ mx: 2, mb: 1 }} />
      <List sx={{ px: 0.5 }}>
        {navItems.map((item) => renderNavItem(item))}
      </List>
    </>
  );

  // Mobile drawer
  const mobileDrawer = (
    <Drawer
      variant="temporary"
      open={mobileOpen}
      onClose={onMobileClose}
      ModalProps={{
        keepMounted: true, // Better mobile performance
      }}
      sx={{
        display: { xs: 'block', md: 'none' },
        '& .MuiDrawer-paper': {
          boxSizing: 'border-box',
          width: SIDEBAR_WIDTH,
          backgroundColor: 'background.paper',
        },
      }}
    >
      {NavigationContent}
    </Drawer>
  );

  // Desktop drawer
  const desktopDrawer = (
    <Drawer
      variant="permanent"
      open={open}
      sx={{
        display: { xs: 'none', md: 'block' },
        '& .MuiDrawer-paper': {
          boxSizing: 'border-box',
          width: open ? SIDEBAR_WIDTH : SIDEBAR_WIDTH_COLLAPSED,
          transition: 'width 0.3s ease-in-out',
          overflowX: 'hidden',
          backgroundColor: 'background.paper',
          borderRight: (theme) =>
            `1px solid ${
              theme.palette.mode === 'dark'
                ? alpha('#FFFFFF', 0.12)
                : alpha('#000000', 0.08)
            }`,
        },
      }}
    >
      {NavigationContent}
    </Drawer>
  );

  return (
    <Box
      component="nav"
      sx={{
        width: { md: open ? SIDEBAR_WIDTH : SIDEBAR_WIDTH_COLLAPSED },
        flexShrink: { md: 0 },
        transition: 'width 0.3s ease-in-out',
      }}
    >
      {mobileDrawer}
      {desktopDrawer}
    </Box>
  );
};

// Export sidebar widths for use in other components
export { SIDEBAR_WIDTH, SIDEBAR_WIDTH_COLLAPSED };

export default Sidebar;

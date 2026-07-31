/**
 * FunnyEnglish Admin Web - Admin Layout Component
 * Main layout with fixed sidebar, header, and content area
 */

import React, { useState, useCallback, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import {
  Box,
  CssBaseline,
  useMediaQuery,
  useTheme as useMuiTheme,
  alpha,
} from '@mui/material';
import { Header, HEADER_HEIGHT } from './Header';
import { Sidebar, SIDEBAR_WIDTH, SIDEBAR_WIDTH_COLLAPSED } from './Sidebar';
import { GlobalStyles } from '../../theme/GlobalStyles';

/**
 * Props for AdminLayout component
 */
interface AdminLayoutProps {
  /** Optional page title override */
  title?: string;
  /** Whether to show breadcrumbs in header */
  showBreadcrumbs?: boolean;
  /** Optional children to render instead of Outlet */
  children?: React.ReactNode;
}

/**
 * Admin Layout Component
 * Main application layout with sidebar, header, and content area
 * Features:
 * - Fixed sidebar (240px expanded, 64px collapsed)
 * - Fixed header (64px height)
 * - Content area with proper margins
 * - Smooth transitions
 * - Responsive behavior with mobile drawer
 */
export const AdminLayout: React.FC<AdminLayoutProps> = ({
  title,
  showBreadcrumbs = true,
  children,
}) => {
  const muiTheme = useMuiTheme();
  const isMobile = useMediaQuery(muiTheme.breakpoints.down('md'));

  // Sidebar state
  const [sidebarOpen, setSidebarOpen] = useState(!isMobile);
  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);

  // Handle sidebar toggle
  const handleSidebarToggle = useCallback(() => {
    if (isMobile) {
      setMobileDrawerOpen((prev) => !prev);
    } else {
      setSidebarOpen((prev) => !prev);
    }
  }, [isMobile]);

  // Handle mobile drawer close
  const handleMobileDrawerClose = useCallback(() => {
    setMobileDrawerOpen(false);
  }, []);

  // Update sidebar state when screen size changes
  useEffect(() => {
    if (isMobile) {
      setSidebarOpen(false);
      setMobileDrawerOpen(false);
    } else {
      setSidebarOpen(true);
    }
  }, [isMobile]);

  // Calculate content margins based on sidebar state
  const getContentMarginLeft = () => {
    if (isMobile) return 0;
    return sidebarOpen ? SIDEBAR_WIDTH : SIDEBAR_WIDTH_COLLAPSED;
  };

  return (
    <Box
      sx={{
        display: 'flex',
        minHeight: '100vh',
        backgroundColor: 'background.default',
      }}
    >
      <CssBaseline />
      <GlobalStyles />

      {/* Header */}
      <Header
        onMenuToggle={handleSidebarToggle}
        title={title}
        showBreadcrumbs={showBreadcrumbs}
      />

      {/* Sidebar */}
      <Sidebar
        open={sidebarOpen}
        onToggle={handleSidebarToggle}
        mobileOpen={mobileDrawerOpen}
        onMobileClose={handleMobileDrawerClose}
      />

      {/* Main Content Area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          pt: `${HEADER_HEIGHT + (showBreadcrumbs && !isMobile ? 56 : 0)}px`,
          pb: 3,
          px: { xs: 2, sm: 3, md: 4 },
          ml: { md: `${getContentMarginLeft()}px` },
          transition: 'margin-left 0.3s ease-in-out',
          minHeight: '100vh',
          backgroundColor: 'background.default',
        }}
      >
        {/* Content Container */}
        <Box
          sx={{
            maxWidth: '1600px',
            mx: 'auto',
            animation: 'fadeIn 0.3s ease-in-out',
            '@keyframes fadeIn': {
              from: {
                opacity: 0,
                transform: 'translateY(10px)',
              },
              to: {
                opacity: 1,
                transform: 'translateY(0)',
              },
            },
          }}
        >
          {/* Page Content */}
          {children || <Outlet />}
        </Box>
      </Box>

      {/* Footer (optional) */}
      <Box
        component="footer"
        sx={{
          position: 'fixed',
          bottom: 0,
          left: { md: `${getContentMarginLeft()}px` },
          right: 0,
          py: 1.5,
          px: 3,
          backgroundColor: 'background.paper',
          borderTop: (theme) =>
            `1px solid ${
              theme.palette.mode === 'dark'
                ? alpha('#FFFFFF', 0.08)
                : alpha('#000000', 0.06)
            }`,
          textAlign: 'center',
          transition: 'left 0.3s ease-in-out',
          zIndex: (theme) => theme.zIndex.drawer - 1,
          display: { xs: 'none', md: 'block' },
        }}
      >
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            fontSize: '0.75rem',
            color: 'text.secondary',
          }}
        >
          <span>© 2024 FunnyEnglish. All rights reserved.</span>
          <span>Version 1.0.0</span>
        </Box>
      </Box>
    </Box>
  );
};

export default AdminLayout;

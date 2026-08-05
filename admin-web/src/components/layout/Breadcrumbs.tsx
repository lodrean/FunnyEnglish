/**
 * So to speak Admin Web - Breadcrumbs Component
 * Auto-generated breadcrumbs from current route
 */

import React, { useMemo } from 'react';
import { useLocation, Link as RouterLink } from 'react-router-dom';
import {
  Breadcrumbs as MuiBreadcrumbs,
  Link,
  Typography,
  Box,
  Chip,
} from '@mui/material';
import {
  NavigateNext as NavigateNextIcon,
  Home as HomeIcon,
} from '@mui/icons-material';
import { getBreadcrumbPath, NavItem } from '../navigation/navItems';

/**
 * Props for Breadcrumbs component
 */
interface BreadcrumbsProps {
  /** Optional custom title to override the last breadcrumb */
  customTitle?: string;
  /** Optional additional items to append to breadcrumbs */
  additionalItems?: Array<{ label: string; path?: string }>;
}

/**
 * Breadcrumbs Component
 * Automatically generates breadcrumbs based on current route
 */
export const Breadcrumbs: React.FC<BreadcrumbsProps> = ({
  customTitle,
  additionalItems = [],
}) => {
  const location = useLocation();
  const currentPath = location.pathname;

  // Generate breadcrumb items from current path
  const breadcrumbItems = useMemo(() => {
    const items = getBreadcrumbPath(currentPath);
    
    // Add additional items if provided
    if (additionalItems.length > 0) {
      additionalItems.forEach((item) => {
        items.push({
          id: `additional-${item.label}`,
          label: item.label,
          path: item.path || currentPath,
          icon: HomeIcon, // Placeholder, won't be used
        } as NavItem);
      });
    }

    return items;
  }, [currentPath, additionalItems]);

  // If only one item (current page), don't show breadcrumbs
  if (breadcrumbItems.length <= 1 && !customTitle) {
    return null;
  }

  return (
    <Box
      sx={{
        py: 2,
        px: { xs: 2, sm: 3 },
        backgroundColor: 'transparent',
      }}
    >
      <MuiBreadcrumbs
        separator={<NavigateNextIcon fontSize="small" sx={{ color: 'text.secondary' }} />}
        aria-label="breadcrumb"
        sx={{
          '& .MuiBreadcrumbs-ol': {
            alignItems: 'center',
          },
        }}
      >
        {breadcrumbItems.map((item, index) => {
          const isLast = index === breadcrumbItems.length - 1;
          const IconComponent = item.icon;

          // Last item (current page) - show as text
          if (isLast) {
            const displayLabel = customTitle || item.label;
            return (
              <Chip
                key={item.id}
                icon={<IconComponent fontSize="small" />}
                label={displayLabel}
                size="small"
                sx={{
                  backgroundColor: (theme) =>
                    theme.palette.mode === 'dark'
                      ? 'rgba(255, 255, 255, 0.1)'
                      : 'rgba(74, 144, 217, 0.1)',
                  color: 'primary.main',
                  fontWeight: 600,
                  '& .MuiChip-icon': {
                    color: 'primary.main',
                  },
                }}
              />
            );
          }

          // Previous items - show as links
          return (
            <Link
              key={item.id}
              component={RouterLink}
              to={item.path}
              sx={{
                display: 'flex',
                alignItems: 'center',
                color: 'text.secondary',
                textDecoration: 'none',
                fontSize: '0.875rem',
                fontWeight: 500,
                transition: 'color 0.2s ease-in-out',
                '&:hover': {
                  color: 'primary.main',
                  textDecoration: 'none',
                },
              }}
            >
              {index === 0 ? (
                <HomeIcon sx={{ mr: 0.5, fontSize: 18 }} />
              ) : (
                <IconComponent sx={{ mr: 0.5, fontSize: 18 }} />
              )}
              {item.label}
            </Link>
          );
        })}
      </MuiBreadcrumbs>
    </Box>
  );
};

/**
 * Page Title Component
 * Displays the current page title based on route
 */
export const PageTitle: React.FC = () => {
  const location = useLocation();
  const currentPath = location.pathname;

  const pageTitle = useMemo(() => {
    const pathSegments = currentPath.split('/').filter(Boolean);
    
    if (pathSegments.length === 0) {
      return 'Dashboard';
    }

    // Get the last segment and format it
    const lastSegment = pathSegments[pathSegments.length - 1];
    return lastSegment
      .split('-')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }, [currentPath]);

  return (
    <Typography
      variant="h4"
      component="h1"
      sx={{
        fontWeight: 600,
        color: 'text.primary',
        mb: 1,
      }}
    >
      {pageTitle}
    </Typography>
  );
};

export default Breadcrumbs;

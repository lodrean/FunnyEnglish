/**
 * FunnyEnglish Admin Web - Navigation Items Configuration
 * Defines all navigation items for the admin sidebar
 */

import {
  Dashboard as DashboardIcon,
  Category as CategoryIcon,
  Quiz as QuizIcon,
  Help as HelpIcon,
  People as PeopleIcon,
  AdminPanelSettings as AdminIcon,
  Groups as GroupsIcon,
  Analytics as AnalyticsIcon,
  Assessment as AssessmentIcon,
  BarChart as BarChartIcon,
  Settings as SettingsIcon,
  SvgIconComponent,
} from '@mui/icons-material';

/**
 * Navigation item interface
 */
export interface NavItem {
  /** Unique identifier for the nav item */
  id: string;
  /** Display label */
  label: string;
  /** Route path */
  path: string;
  /** Icon component from MUI icons */
  icon: SvgIconComponent;
  /** Child navigation items for nested menus */
  children?: NavItem[];
  /** Whether this item requires admin privileges */
  requiresAdmin?: boolean;
  /** Badge count for notifications */
  badge?: number;
}

/**
 * Navigation items configuration
 * Organized by sections for the admin sidebar
 */
export const navItems: NavItem[] = [
  {
    id: 'dashboard',
    label: 'Dashboard',
    path: '/',
    icon: DashboardIcon,
  },
  {
    id: 'content',
    label: 'Content',
    path: '/content',
    icon: CategoryIcon,
    children: [
      {
        id: 'categories',
        label: 'Categories',
        path: '/content/categories',
        icon: CategoryIcon,
      },
      {
        id: 'tests',
        label: 'Tests',
        path: '/content/tests',
        icon: QuizIcon,
      },
      {
        id: 'questions',
        label: 'Questions',
        path: '/content/questions',
        icon: HelpIcon,
      },
    ],
  },
  {
    id: 'users',
    label: 'Users',
    path: '/users',
    icon: PeopleIcon,
    children: [
      {
        id: 'students',
        label: 'Students',
        path: '/users/students',
        icon: PeopleIcon,
      },
      {
        id: 'admins',
        label: 'Admins',
        path: '/users/admins',
        icon: AdminIcon,
        requiresAdmin: true,
      },
      {
        id: 'groups',
        label: 'Groups',
        path: '/users/groups',
        icon: GroupsIcon,
      },
    ],
  },
  {
    id: 'analytics',
    label: 'Analytics',
    path: '/analytics',
    icon: AnalyticsIcon,
    children: [
      {
        id: 'reports',
        label: 'Reports',
        path: '/analytics/reports',
        icon: AssessmentIcon,
      },
      {
        id: 'statistics',
        label: 'Statistics',
        path: '/analytics/statistics',
        icon: BarChartIcon,
      },
    ],
  },
  {
    id: 'settings',
    label: 'Settings',
    path: '/settings',
    icon: SettingsIcon,
  },
];

/**
 * Flatten navigation items for breadcrumb generation
 * @returns Array of all navigation items including children
 */
export const getFlattenedNavItems = (): NavItem[] => {
  const flattened: NavItem[] = [];

  const flatten = (items: NavItem[]) => {
    items.forEach((item) => {
      flattened.push(item);
      if (item.children) {
        flatten(item.children);
      }
    });
  };

  flatten(navItems);
  return flattened;
};

/**
 * Find a navigation item by its path
 * @param path - The route path to search for
 * @returns The matching NavItem or undefined
 */
export const findNavItemByPath = (path: string): NavItem | undefined => {
  const flattened = getFlattenedNavItems();
  return flattened.find((item) => item.path === path);
};

/**
 * Find parent navigation item for a child path
 * @param path - The child route path
 * @returns The parent NavItem or undefined
 */
export const findParentNavItem = (path: string): NavItem | undefined => {
  return navItems.find((item) =>
    item.children?.some((child) => child.path === path)
  );
};

/**
 * Get breadcrumb path for a given route
 * @param currentPath - The current route path
 * @returns Array of NavItems representing the breadcrumb trail
 */
export const getBreadcrumbPath = (currentPath: string): NavItem[] => {
  const breadcrumbs: NavItem[] = [];
  
  // Always include dashboard as first item if not already on dashboard
  if (currentPath !== '/') {
    const dashboard = findNavItemByPath('/');
    if (dashboard) {
      breadcrumbs.push(dashboard);
    }
  }

  // Find parent if current path is a child
  const parent = findParentNavItem(currentPath);
  if (parent) {
    breadcrumbs.push(parent);
  }

  // Add current item
  const current = findNavItemByPath(currentPath);
  if (current && current.path !== '/') {
    breadcrumbs.push(current);
  }

  return breadcrumbs;
};

/**
 * Check if a navigation item is active
 * @param itemPath - The navigation item path
 * @param currentPath - The current route path
 * @returns Boolean indicating if the item is active
 */
export const isNavItemActive = (itemPath: string, currentPath: string): boolean => {
  if (itemPath === '/') {
    return currentPath === '/';
  }
  return currentPath.startsWith(itemPath);
};

/**
 * Check if a navigation item has active children
 * @param item - The navigation item to check
 * @param currentPath - The current route path
 * @returns Boolean indicating if any child is active
 */
export const hasActiveChild = (item: NavItem, currentPath: string): boolean => {
  if (!item.children) return false;
  return item.children.some((child) => isNavItemActive(child.path, currentPath));
};

export default navItems;

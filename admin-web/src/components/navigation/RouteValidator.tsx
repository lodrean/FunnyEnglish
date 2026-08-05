/**
 * Route Validator Component
 * 
 * Development helper to detect navigation/route mismatches.
 * Remove in production build.
 */

import { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { getFlattenedNavItems } from './navItems';

// List of all valid routes in the app
const VALID_ROUTES = [
  '/',
  '/login',
  '/speaking/libraries',
  '/speaking/libraries/new',
  '/speaking/libraries/:id/edit',
  '/speaking/topics',
  '/speaking/topics/new',
  '/speaking/topics/:id/edit',
  '/grading',
  '/grading/submissions/:id',
  '/users',
  '/users/students',
  '/users/admins',
  '/users/groups',
  '/analytics/reports',
  '/analytics/statistics',
  '/settings',
];

export const RouteValidator: React.FC = () => {
  const location = useLocation();
  
  useEffect(() => {
    if (process.env.NODE_ENV !== 'development') return;
    
    const currentPath = location.pathname;
    
    // Check if current route matches nav items
    const navItems = getFlattenedNavItems();
    const navPaths = navItems.map(item => item.path);
    
    // Check for exact match or pattern match
    const isValidRoute = VALID_ROUTES.some(route => {
      if (route.includes(':')) {
        // Handle dynamic routes like /tests/:id/edit
        const pattern = route.replace(/:\w+/g, '[^/]+');
        const regex = new RegExp(`^${pattern}$`);
        return regex.test(currentPath);
      }
      return route === currentPath;
    });
    
    if (!isValidRoute) {
      console.warn(
        `%c[RouteValidator] Unknown route: ${currentPath}`,
        'color: orange; font-weight: bold;'
      );
      console.warn('Valid routes:', VALID_ROUTES);
      console.warn('Navigation items:', navPaths);
    }
  }, [location]);
  
  return null;
};

export default RouteValidator;

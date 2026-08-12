/**
 * So to speak Admin Web - Nested Route Navigation Configuration
 * Defines routes where the header should show a back button.
 */

import { matchPath } from 'react-router-dom';

/**
 * Configuration for a nested route that should display a back button in the header.
 */
export interface NestedRouteConfig {
  /** Route path pattern (e.g. '/speaking/libraries/:id/edit'). */
  path: string;
  /** Parent list path to navigate back to (e.g. '/speaking/libraries'). */
  parentPath: string;
  /** Page title displayed next to the back arrow in the header. */
  title: string;
}

/**
 * Nested routes that should render a back button in the header.
 */
export const nestedRoutes: NestedRouteConfig[] = [
  {
    path: '/speaking/libraries/new',
    parentPath: '/speaking/libraries',
    title: 'New Library',
  },
  {
    path: '/speaking/libraries/:id/edit',
    parentPath: '/speaking/libraries',
    title: 'Edit Library',
  },
  {
    path: '/speaking/topics/new',
    parentPath: '/speaking/topics',
    title: 'New Topic',
  },
  {
    path: '/speaking/topics/:id/edit',
    parentPath: '/speaking/topics',
    title: 'Edit Topic',
  },
  {
    path: '/grading/submissions/:id',
    parentPath: '/grading',
    title: 'Grading Detail',
  },
];

/**
 * Find a matching nested route config for the current pathname.
 * @param pathname - Current location pathname.
 * @returns Matching config or null if not on a nested route.
 */
export const matchNestedRoute = (pathname: string): NestedRouteConfig | null => {
  for (const route of nestedRoutes) {
    if (matchPath(route.path, pathname)) {
      return route;
    }
  }
  return null;
};

export default nestedRoutes;

/**
 * Единственный источник маршрутов admin-web (bd b85.2).
 *
 * До рефактора пути дублировались в четырёх местах (App.tsx, navItems,
 * nestedRoutes, RouteValidator.VALID_ROUTES) и расходились (VALID_ROUTES
 * не знал про /logs). Все потребители теперь импортируют отсюда:
 *  - App.tsx        — Route-дерево (абсолютные пути, react-router v6
 *                     допускает их под pathless-родителем с path="/");
 *  - navItems.ts    — пути сайдбара;
 *  - nestedRoutes.ts— паттерны «экран с back-стрелкой»;
 *  - RouteValidator — плоский список валидных путей.
 * Новый маршрут добавляется ОДИН раз сюда.
 */

export const ROUTES = {
  dashboard: '/',
  login: '/login',
  speaking: {
    root: '/speaking',
    libraries: '/speaking/libraries',
    libraryNew: '/speaking/libraries/new',
    libraryEdit: '/speaking/libraries/:id/edit',
    topics: '/speaking/topics',
    topicNew: '/speaking/topics/new',
    topicEdit: '/speaking/topics/:id/edit',
  },
  grading: {
    root: '/grading',
    submission: '/grading/submissions/:id',
  },
  users: {
    root: '/users',
    students: '/users/students',
    admins: '/users/admins',
    groups: '/users/groups',
  },
  analytics: {
    root: '/analytics',
    reports: '/analytics/reports',
    statistics: '/analytics/statistics',
  },
  logs: '/logs',
} as const;

/** Плоский список всех валидных путей (для RouteValidator). */
export const ALL_ROUTE_PATHS: string[] = Object.values(ROUTES).flatMap((v) =>
  typeof v === 'string' ? [v] : Object.values(v)
);

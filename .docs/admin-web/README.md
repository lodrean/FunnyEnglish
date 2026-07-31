# FunnyEnglish Admin Web - Design System 2.0

A modern, comprehensive admin dashboard for the FunnyEnglish educational platform. Built with React 18, TypeScript, Material-UI v6, and modern development practices.

## 🚀 Features

- **34+ Reusable Components** - Complete component library for admin interfaces
- **7 Main Screens** - Dashboard, Categories, Tests, Test Editor, Users, Analytics, Settings
- **Dark/Light Theme** - Full theme switching with system preference detection
- **Responsive Design** - Desktop-first with tablet and mobile support
- **Accessibility** - WCAG 2.1 AA compliant with keyboard navigation
- **TypeScript** - Strict mode for type safety
- **Modern Stack** - React 18, Vite, MUI v6, React Query, React Hook Form

## 📁 Project Structure

```
admin-web/
├── src/
│   ├── components/
│   │   ├── layout/          # AdminLayout, Header, Sidebar, Breadcrumbs
│   │   ├── data/            # DataTable, StatsCard, StatusBadge, Pagination
│   │   ├── forms/           # FormField, RichTextEditor, ImageUploader
│   │   ├── feedback/        # Toast, ConfirmDialog, ErrorBoundary
│   │   ├── charts/          # LineChart, BarChart, PieChart
│   │   ├── content/         # CategoryTree, QuestionBuilder, TestList
│   │   ├── users/           # UserTable, PermissionEditor, GroupManager
│   │   └── navigation/      # navItems configuration
│   ├── screens/             # Dashboard, Categories, Tests, Users, etc.
│   ├── theme/               # Theme configuration, ThemeProvider, GlobalStyles
│   ├── hooks/               # useToast, useConfirm, useTable
│   ├── utils/               # formatters, validators
│   ├── App.tsx              # Main app with routes
│   ├── main.tsx             # Entry point
│   └── index.css            # Global styles
├── package.json
├── tsconfig.json
├── vite.config.ts
└── index.html
```

## 🛠️ Tech Stack

- **React 18.3.1** - UI library with hooks
- **TypeScript 5.6.3** - Type safety
- **Vite 5.4.10** - Build tool
- **Material-UI v6.1.6** - Component library
- **@tanstack/react-query** - Data fetching and caching
- **react-hook-form** - Form management
- **@dnd-kit** - Drag and drop
- **recharts** - Charts and visualizations
- **zustand** - State management
- **zod** - Schema validation

## 🎨 Design System

### Colors
```
Primary:    #4A90D9
Success:    #43A047
Error:      #E53935
Warning:    #FB8C00
Info:       #2196F3
Background: #F5F5F5
Card:       #FFFFFF
Text:       #212121 / #757575
```

### Typography
- **Font**: Inter (Google Fonts)
- **Base Size**: 16px
- **Scale**: 2.5rem, 2rem, 1.5rem, 1.25rem, 1.125rem, 1rem

### Spacing
- **Base**: 8px grid
- **Card Padding**: 24px
- **Section Gap**: 24px
- **Component Gap**: 16px

## 🚀 Getting Started

### Prerequisites
- Node.js 18+
- npm or yarn

### Installation

```bash
# Clone the repository
git clone <repository-url>

# Navigate to project
cd admin-web

# Install dependencies
npm install

# Start development server
npm run dev
```

### Build for Production

```bash
npm run build
```

### Run Tests

```bash
npm test
```

## 📱 Responsive Breakpoints

| Breakpoint | Width | Layout |
|------------|-------|--------|
| xs | 0-599px | Mobile - single column, drawer navigation |
| sm | 600-959px | Tablet - collapsed sidebar |
| md | 960-1279px | Small desktop - collapsible sidebar |
| lg | 1280px+ | Full desktop - expanded sidebar |

## ♿ Accessibility

- WCAG 2.1 Level AA compliance
- Keyboard navigation for all interactive elements
- Focus visible indicators (2px outline)
- ARIA labels for icons and buttons
- Screen reader announcements
- Color contrast ratio 4.5:1 minimum
- Reduced motion support

## 📊 Components Overview

### Layout Components
- `AdminLayout` - Main layout shell with sidebar and header
- `Header` - Top navigation with search, notifications, user menu
- `Sidebar` - Collapsible navigation (240px/64px)
- `Breadcrumbs` - Auto-generated from route

### Data Display
- `DataTable` - Full-featured table with sorting, pagination, selection
- `StatsCard` - Metric cards with sparkline charts
- `StatusBadge` - Status indicators
- `Pagination` - Table pagination

### Forms
- `FormField` - Universal form input with validation
- `RichTextEditor` - WYSIWYG editor (TipTap)
- `ImageUploader` - Drag-drop image upload
- `SearchInput` - Global search with debounce

### Content Management
- `CategoryTree` - Nested category tree with drag-drop
- `TestList` - Test list with actions
- `QuestionList` - Question reordering
- `QuestionBuilder` - Question creation form

### User Management
- `UserTable` - User list with filters and bulk actions
- `PermissionEditor` - Role permission matrix
- `GroupManager` - User groups management

### Feedback
- `Toast` - Notification toasts
- `ConfirmDialog` - Confirmation modals
- `ErrorBoundary` - Error handling
- `EmptyState` - Empty list states

## 🔌 API Integration

The application uses the following API endpoints:

### Categories
- `GET /api/admin/categories`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/:id`
- `DELETE /api/admin/categories/:id`
- `POST /api/admin/categories/reorder`

### Tests
- `GET /api/admin/tests`
- `POST /api/admin/tests`
- `PUT /api/admin/tests/:id`
- `DELETE /api/admin/tests/:id`

### Questions
- `GET /api/admin/tests/:testId/questions`
- `POST /api/admin/tests/:testId/questions`
- `PUT /api/admin/questions/:id`
- `DELETE /api/admin/questions/:id`

### Users
- `GET /api/admin/users`
- `PUT /api/admin/users/:id`
- `DELETE /api/admin/users/:id`

### Analytics
- `GET /api/admin/analytics/dashboard`
- `GET /api/admin/analytics/users`
- `GET /api/admin/analytics/tests`

## 🧪 Testing

- **Unit Tests**: Vitest for component and utility testing
- **E2E Tests**: Playwright for critical user flows (optional)
- **Visual Regression**: Storybook with Chromatic

## 📈 Performance Targets

- Lighthouse Performance score > 90
- Lighthouse Accessibility score > 95
- Bundle size < 500KB (gzipped)
- Time to Interactive < 3s

## 📝 License

Private - FunnyEnglish Team

## 👥 Team

- Frontend Team - Implementation
- Design Team - Design System
- Backend Team - API Integration

---

**Version**: 2.0.0  
**Last Updated**: 2024

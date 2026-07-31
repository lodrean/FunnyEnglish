# FunnyEnglish Admin Web - Design System 2.0 Plan

## 📋 Project Overview

**Tech Stack:**
- React 18.3.1 + TypeScript 5.6.3
- Vite 5.4.10 (build tool)
- Material-UI (MUI) v6.1.6
- @dnd-kit (drag & drop)
- @tanstack/react-query (data fetching)
- react-hook-form (forms)
- recharts (charts)
- zustand (state management)
- react-router-dom (routing)

**Target:** Modern admin dashboard for content management, analytics, and user management

---

## 🎨 Design System Requirements

### 1. Foundation Layer

#### Theme Configuration
```typescript
// src/theme/Theme.ts
interface AdminTheme {
  // Brand Colors
  primary: {
    main: '#4A90D9'
    light: '#6BA5E7'
    dark: '#1E5AA8'
    contrastText: '#FFFFFF'
  }
  
  // Semantic Colors
  success: { main: '#43A047', light: '#66BB6A', dark: '#2E7D32' }
  error: { main: '#E53935', light: '#FF897D', dark: '#C62828' }
  warning: { main: '#FB8C00', light: '#FFB74D', dark: '#EF6C00' }
  info: { main: '#2196F3', light: '#90CAF9', dark: '#1565C0' }
  
  // Extended Admin Colors
  admin: {
    sidebar: '#1a237e'
    sidebarText: '#ffffff'
    header: '#ffffff'
    background: '#f5f5f5'
    card: '#ffffff'
    border: '#e0e0e0'
    hover: 'rgba(0,0,0,0.04)'
    selected: 'rgba(74,144,217,0.12)'
  }
  
  // Data Visualization Colors
  chart: [
    '#4A90D9', '#43A047', '#FB8C00', '#E53935', 
    '#9C27B0', '#00BCD4', '#FFEB3B', '#795548'
  ]
}
```

#### Typography
```typescript
// Using Inter font (Google Fonts)
fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif'

// Type Scale
h1: { size: '2.5rem', weight: 600, lineHeight: 1.2 }     // 40px - Page titles
h2: { size: '2rem', weight: 600, lineHeight: 1.3 }       // 32px - Section headers
h3: { size: '1.5rem', weight: 600, lineHeight: 1.4 }     // 24px - Card titles
h4: { size: '1.25rem', weight: 600, lineHeight: 1.4 }    // 20px - Subsection
h5: { size: '1.125rem', weight: 500, lineHeight: 1.5 }   // 18px - List headers
h6: { size: '1rem', weight: 500, lineHeight: 1.5 }       // 16px - Small headers
body1: { size: '1rem', weight: 400, lineHeight: 1.5 }    // 16px - Primary text
body2: { size: '0.875rem', weight: 400, lineHeight: 1.5 } // 14px - Secondary text
caption: { size: '0.75rem', weight: 400, lineHeight: 1.5 } // 12px - Labels
```

#### Spacing System
```typescript
// 8px base grid
spacing: {
  xs: '4px'    // 0.5x
  sm: '8px'    // 1x
  md: '16px'   // 2x
  lg: '24px'   // 3x
  xl: '32px'   // 4x
  xxl: '48px'  // 6x
}

// Border radius
borderRadius: {
  sm: '4px'
  md: '8px'
  lg: '12px'
  xl: '16px'
  full: '9999px'
}

// Shadows
shadows: {
  sm: '0 1px 2px rgba(0,0,0,0.05)'
  md: '0 4px 6px -1px rgba(0,0,0,0.1)'
  lg: '0 10px 15px -3px rgba(0,0,0,0.1)'
  xl: '0 20px 25px -5px rgba(0,0,0,0.1)'
  card: '0 2px 8px rgba(0,0,0,0.08)'
}
```

---

### 2. Layout Components

#### AdminLayout
```typescript
// src/components/layout/AdminLayout.tsx
interface AdminLayoutProps {
  children: React.ReactNode
  sidebarCollapsed?: boolean
  onSidebarToggle?: () => void
}

// Features:
// - Fixed sidebar (240px expanded, 64px collapsed)
// - Fixed header (64px height)
// - Content area with scroll
// - Breadcrumb navigation
// - Responsive (mobile: drawer)
```

#### Sidebar Navigation
```typescript
// src/components/navigation/Sidebar.tsx
interface SidebarProps {
  items: NavItem[]
  collapsed: boolean
  activeItem: string
  onItemClick: (id: string) => void
}

interface NavItem {
  id: string
  label: string
  icon: React.ComponentType
  badge?: number
  children?: NavItem[]
  requiresPermission?: string
}

// Sections:
// - Dashboard
// - Content (Categories, Tests, Questions)
// - Users (Students, Admins, Groups)
// - Analytics (Reports, Statistics)
// - Settings
```

#### Header
```typescript
// src/components/layout/Header.tsx
interface HeaderProps {
  title: string
  breadcrumbs: Breadcrumb[]
  actions?: React.ReactNode
  onMenuToggle: () => void
}

// Features:
// - Search bar (global)
// - Notifications dropdown
// - User menu (avatar, profile, logout)
// - Quick actions
```

---

### 3. Data Display Components

#### DataTable
```typescript
// src/components/data/DataTable.tsx
interface DataTableProps<T> {
  data: T[]
  columns: Column<T>[]
  loading?: boolean
  pagination?: PaginationConfig
  sorting?: SortingConfig
  selection?: SelectionConfig
  actions?: TableAction<T>[]
  onRowClick?: (row: T) => void
  emptyState?: React.ReactNode
}

interface Column<T> {
  key: string
  header: string
  accessor: (row: T) => React.ReactNode
  sortable?: boolean
  width?: string | number
  align?: 'left' | 'center' | 'right'
}

// Features:
// - Sortable columns
// - Row selection (checkboxes)
// - Pagination
// - Loading skeleton
// - Empty state
// - Row actions menu
// - Sticky header
// - Column resizing
```

#### Stats Cards
```typescript
// src/components/data/StatsCard.tsx
interface StatsCardProps {
  title: string
  value: string | number
  change?: {
    value: number
    isPositive: boolean
  }
  icon: React.ComponentType
  color?: 'primary' | 'success' | 'warning' | 'error' | 'info'
  chart?: 'sparkline' | 'mini-bar'
  chartData?: number[]
}

// Variants: 4-col, 3-col, 2-col grid
```

#### Charts
```typescript
// src/components/charts/
// - LineChart: Time series data
// - BarChart: Comparisons
// - PieChart: Distributions
// - AreaChart: Trends
// - RadarChart: Multi-dimensional

// Using recharts with MUI theme integration
```

---

### 4. Form Components

#### FormField
```typescript
// src/components/forms/FormField.tsx
interface FormFieldProps {
  name: string
  label: string
  type?: 'text' | 'email' | 'password' | 'number' | 'select' | 'multiline' | 'date' | 'file'
  placeholder?: string
  helperText?: string
  required?: boolean
  disabled?: boolean
  validation?: ValidationRule[]
}
```

#### RichTextEditor
```typescript
// src/components/forms/RichTextEditor.tsx
// For question descriptions, category info
// Features: Bold, italic, lists, links, images
```

#### ImageUploader
```typescript
// src/components/forms/ImageUploader.tsx
interface ImageUploaderProps {
  value?: File | string
  onChange: (file: File | null) => void
  accept?: string
  maxSize?: number // MB
  aspectRatio?: number
  preview?: boolean
}
```

#### QuestionBuilder
```typescript
// src/components/forms/QuestionBuilder.tsx
// Drag-drop question form
// Multiple question types:
// - TEXT_SELECT (multiple choice)
// - IMAGE_SELECT (image choice)
// - AUDIO_SELECT (audio choice)
// - DRAG_DROP
// - TEXT_INPUT
```

---

### 5. Feedback Components

#### Toast/Notification
```typescript
// src/components/feedback/Toast.tsx
interface ToastProps {
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
  duration?: number
  onClose: () => void
  action?: {
    label: string
    onClick: () => void
  }
}
```

#### ConfirmDialog
```typescript
// src/components/feedback/ConfirmDialog.tsx
interface ConfirmDialogProps {
  open: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  danger?: boolean
  onConfirm: () => void
  onCancel: () => void
}
```

#### Loading States
```typescript
// src/components/feedback/
// - PageLoader: Full page spinner
// - SkeletonCard: Card placeholder
// - SkeletonTable: Table placeholder
// - SkeletonForm: Form placeholder
```

---

### 6. Content Management Components

#### CategoryTree
```typescript
// src/components/content/CategoryTree.tsx
// Drag-drop nested categories
// Expand/collapse
// Edit inline
// Add child
// Delete with confirmation
```

#### TestList
```typescript
// src/components/content/TestList.tsx
// List of tests with:
// - Status badges (draft, published, archived)
// - Progress indicators
// - Quick actions (edit, delete, preview)
// - Sortable by drag-drop
```

#### QuestionList
```typescript
// src/components/content/QuestionList.tsx
// Drag-drop reordering
// Question preview
// Type icons
// Points display
// Edit/Delete actions
```

---

### 7. User Management Components

#### UserTable
```typescript
// src/components/users/UserTable.tsx
// Columns: Avatar, Name, Email, Role, Status, Last Active, Actions
// Filters: Role, Status, Date range
// Bulk actions
```

#### UserCard
```typescript
// src/components/users/UserCard.tsx
// Compact user info for groups/assignments
// Avatar, name, email, role badge
```

#### PermissionEditor
```typescript
// src/components/users/PermissionEditor.tsx
// Matrix of permissions
// Role-based defaults
// Custom overrides
```

---

### 8. Screen Designs

### 8.1 Dashboard
```
┌─────────────────────────────────────────────────────────────┐
│ [Sidebar] │ Header: Dashboard                    [🔍][👤] │
│           ├───────────────────────────────────────────────┤
│ [Logo]    │ [Stats Cards: 4 columns]                     │
│           │ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐  │
│ [Nav]     │ │ Users  │ │ Tests  │ │Complet │ │ Active │  │
│           │ │ 1,234  │ │   56   │ │ 89%    │ │  45m   │  │
│           │ └────────┘ └────────┘ └────────┘ └────────┘  │
│           ├───────────────────────────────────────────────┤
│           │ [Charts Row]                                   │
│           │ ┌──────────────────┐ ┌──────────────────────┐ │
│           │ │ User Growth      │ │ Test Completion      │ │
│           │ │ [Line Chart]     │ │ [Bar Chart]          │ │
│           │ └──────────────────┘ └──────────────────────┘ │
│           ├───────────────────────────────────────────────┤
│           │ [Recent Activity]     │ [Quick Actions]       │
│           │ ┌──────────────────┐ ┌──────────────────────┐ │
│           │ │ • User registered│ │ [+ New Test]         │ │
│           │ │ • Test published │ │ [+ New Category]     │ │
│           │ │ • Question added │ │ [+ New User]         │ │
│           │ └──────────────────┘ └──────────────────────┘ │
└───────────┴───────────────────────────────────────────────┘
```

### 8.2 Categories Management
```
┌─────────────────────────────────────────────────────────────┐
│ [Sidebar] │ Header: Categories                 [+ New] [🔍] │
│           ├───────────────────────────────────────────────┤
│           │ [Toolbar: Filter | Search | Bulk Actions]     │
│           ├───────────────────────────────────────────────┤
│           │ ┌─────────────────────────────────────────────┤
│           │ │ 📁 Animals                        [3 tests] │
│           │ │   📁 Farm Animals                 [5 tests] │
│           │ │   📁 Wild Animals                 [4 tests] │
│           │ │ 📁 Colors                         [2 tests] │
│           │ │ 📁 Numbers                        [6 tests] │
│           │ │   📁 Counting                     [3 tests] │
│           │ └─────────────────────────────────────────────┤
└───────────┴───────────────────────────────────────────────┘
```

### 8.3 Test Editor
```
┌─────────────────────────────────────────────────────────────┐
│ [Sidebar] │ Header: Edit Test "Animals 101"    [Save] [👁] │
│           ├───────────────────────────────────────────────┤
│           │ [Tabs: General | Questions | Settings | Stats]│
│           ├───────────────────────────────────────────────┤
│           │ Title: [_________________________]             │
│           │ Category: [Dropdown____________] ★ Required    │
│           │ Description: [Rich text____________________]   │
│           │ Difficulty: [Easy ○ Medium ○ Hard ○]          │
│           │ [Image Upload]                                 │
│           ├───────────────────────────────────────────────┤
│           │ Questions (12)                    [+ Add Q]   │
│           │ ┌─────────────────────────────────────────────┤
│           │ │ 1. What sound does a dog make?    [⋮] [✕]  │
│           │ │    [○] Woof  [○] Meow  [○] Moo           │
│           │ ├─────────────────────────────────────────────┤
│           │ │ 2. Which is a farm animal?        [⋮] [✕]  │
│           │ │    [Image options...]                       │
│           │ └─────────────────────────────────────────────┤
└───────────┴───────────────────────────────────────────────┘
```

### 8.4 User Management
```
┌─────────────────────────────────────────────────────────────┐
│ [Sidebar] │ Header: Users                      [+ New] [🔍] │
│           ├───────────────────────────────────────────────┤
│           │ [Filters: All | Students | Admins | Inactive] │
│           ├───────────────────────────────────────────────┤
│           │ ☑ │ Avatar │ Name      │ Email         │ Role │
│           │───┼────────┼───────────┼───────────────┼──────┤
│           │ ☑ │ [Img]  │ John Doe  │john@email.com │Admin │
│           │ ☐ │ [Img]  │ Jane Smith│jane@email.com │User  │
│           │ ☐ │ [Img]  │ Bob Wilson│bob@email.com  │User  │
│           │   │        │           │               │      │
│           │ [Bulk: Delete | Change Role | Export]   [1-10] │
└───────────┴───────────────────────────────────────────────┘
```

### 8.5 Analytics/Reports
```
┌─────────────────────────────────────────────────────────────┐
│ [Sidebar] │ Header: Analytics Reports          [Export] [🔍]│
│           ├───────────────────────────────────────────────┤
│           │ [Date Range: [Start] to [End]] [Apply]        │
│           ├───────────────────────────────────────────────┤
│           │ [Metric Cards]                                  │
│           ├───────────────────────────────────────────────┤
│           │ [Large Chart: User Activity Over Time]         │
│           ├───────────────────────────────────────────────┤
│           │ [Table: Top Performing Tests]                   │
│           │ [Table: Most Active Users]                      │
└───────────┴───────────────────────────────────────────────┘
```

---

### 9. Component File Structure

```
admin-web/src/
├── components/
│   ├── layout/
│   │   ├── AdminLayout.tsx
│   │   ├── Header.tsx
│   │   ├── Sidebar.tsx
│   │   └── Breadcrumbs.tsx
│   ├── data/
│   │   ├── DataTable.tsx
│   │   ├── StatsCard.tsx
│   │   ├── StatusBadge.tsx
│   │   └── Pagination.tsx
│   ├── forms/
│   │   ├── FormField.tsx
│   │   ├── RichTextEditor.tsx
│   │   ├── ImageUploader.tsx
│   │   └── SearchInput.tsx
│   ├── feedback/
│   │   ├── Toast.tsx
│   │   ├── ConfirmDialog.tsx
│   │   ├── PageLoader.tsx
│   │   └── SkeletonCard.tsx
│   ├── content/
│   │   ├── CategoryTree.tsx
│   │   ├── TestList.tsx
│   │   ├── QuestionList.tsx
│   │   └── QuestionBuilder.tsx
│   ├── users/
│   │   ├── UserTable.tsx
│   │   ├── UserCard.tsx
│   │   └── PermissionEditor.tsx
│   └── charts/
│       ├── LineChart.tsx
│       ├── BarChart.tsx
│       └── PieChart.tsx
├── theme/
│   ├── Theme.ts
│   ├── ThemeProvider.tsx
│   └── GlobalStyles.ts
├── hooks/
│   ├── useToast.ts
│   ├── useConfirm.ts
│   └── useTable.ts
└── utils/
    ├── formatters.ts
    └── validators.ts
```

---

### 10. Key Features to Implement

#### Drag & Drop
- Category reordering
- Test ordering within categories
- Question ordering within tests
- Using @dnd-kit

#### Data Fetching
- React Query for caching
- Optimistic updates
- Infinite scroll for tables
- Real-time updates (optional)

#### Form Handling
- react-hook-form integration
- Zod validation
- Auto-save drafts
- Form dirty state tracking

#### Performance
- Virtualized lists (react-window)
- Code splitting by route
- Image lazy loading
- Debounced search

---

### 11. Accessibility Requirements

- WCAG 2.1 Level AA compliance
- Keyboard navigation
- Screen reader support
- Focus management
- Color contrast 4.5:1 minimum
- Reduced motion support

---

### 12. Responsive Breakpoints

```typescript
const breakpoints = {
  xs: '0px',      // Mobile portrait
  sm: '600px',    // Mobile landscape
  md: '960px',    // Tablet
  lg: '1280px',   // Desktop
  xl: '1920px',   // Large desktop
}

// Sidebar behavior:
// - xs-sm: Hidden drawer
// - md+: Fixed sidebar (collapsible)
```

---

### 13. Estimated Component Count

| Category | Components | Lines of Code (est.) |
|----------|-----------|---------------------|
| Layout | 4 | ~800 |
| Data Display | 5 | ~1,200 |
| Forms | 6 | ~1,500 |
| Feedback | 5 | ~600 |
| Content | 4 | ~1,800 |
| Users | 3 | ~900 |
| Charts | 4 | ~400 |
| Theme | 3 | ~300 |
| **TOTAL** | **34** | **~7,500** |

---

### 14. Acceptance Criteria

- [ ] All components use MUI v6 theming
- [ ] TypeScript strict mode compatibility
- [ ] Storybook stories for all components
- [ ] Unit tests with vitest
- [ ] E2E tests for critical paths
- [ ] Dark mode support
- [ ] Responsive design
- [ ] Accessibility audit passed
- [ ] Build size < 500KB (gzipped)
- [ ] Lighthouse score > 90

---

### 15. Dependencies to Add

```json
{
  "dependencies": {
    "@fontsource/inter": "^5.0.0",
    "date-fns": "^3.0.0",
    "zod": "^3.22.0",
    "@hookform/resolvers": "^3.3.0",
    "react-window": "^1.8.10",
    "@mui/x-date-pickers": "^6.0.0"
  },
  "devDependencies": {
    "@storybook/react": "^8.0.0",
    "@storybook/addon-essentials": "^8.0.0"
  }
}
```

---

**Total Estimated Effort:** 40-50 hours
**Priority:** High (needed for admin MVP)
**Assigned to:** SWARM Agents

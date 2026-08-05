# SWARM Agent Task: So to Speak Admin Web Redesign

## Task ID: ADMIN-REDSIGN-001
## Priority: HIGH
## Estimated Time: 40-50 hours
## Assigned Agents: Frontend Specialist, UI/UX Implementer

---

## 📌 Task Overview

Implement a comprehensive Design System 2.0 for the So to Speak Admin Web application. The admin panel is used by content managers and administrators to manage educational content (categories, tests, questions), users, and view analytics.

**Current Stack:** React 18 + TypeScript + Vite + MUI v6 + react-hook-form + @tanstack/react-query + @dnd-kit + recharts

**Reference Documents:**
- `.docs/ADMIN_WEB_DESIGN_SYSTEM_PLAN.md` - Full design system specification
- `.docs/ADMIN_COMPONENTS_SPEC.md` - Detailed component specifications with code examples

---

## 🎯 Objectives

1. Create a modern, professional admin interface matching the So to Speak brand
2. Implement reusable component library (34 components)
3. Build 7 main screens with full functionality
4. Ensure responsive design (desktop-first, tablet/mobile adaptive)
5. Maintain accessibility (WCAG 2.1 AA)
6. Support dark/light theme switching

---

## 📋 Scope of Work

### Phase 1: Foundation (8 hours)

**Files to Create/Modify:**
```
admin-web/src/
├── theme/
│   ├── Theme.ts                    # Create - Main theme configuration
│   ├── ThemeProvider.tsx           # Create - Theme context & toggle
│   └── GlobalStyles.ts             # Create - Global CSS overrides
├── components/layout/
│   ├── AdminLayout.tsx             # Create - Main layout shell
│   ├── Header.tsx                  # Create - Top navigation bar
│   ├── Sidebar.tsx                 # Create - Collapsible navigation
│   └── Breadcrumbs.tsx             # Create - Navigation breadcrumbs
└── components/navigation/
    └── navItems.ts                 # Create - Navigation configuration
```

**Requirements:**
- Use MUI v6 `createTheme` with custom palette (see spec for colors)
- Implement Inter font from Google Fonts
- Sidebar: 240px expanded, 64px collapsed, responsive drawer on mobile
- Header: 64px height, contains search, notifications, user menu
- Breadcrumbs: Auto-generated from route

**Acceptance Criteria:**
- [ ] Theme switches between light/dark modes
- [ ] Sidebar collapses/expands smoothly
- [ ] Mobile view shows hamburger menu
- [ ] All navigation items render correctly
- [ ] Breadcrumbs show correct path

---

### Phase 2: Data Display Components (8 hours)

**Files to Create:**
```
admin-web/src/components/
├── data/
│   ├── DataTable.tsx               # Create - Full-featured table
│   ├── StatsCard.tsx               # Create - Metric display cards
│   ├── StatusBadge.tsx             # Create - Status indicators
│   ├── Pagination.tsx              # Create - Table pagination
│   └── SkeletonCard.tsx            # Create - Loading skeletons
└── charts/
    ├── LineChart.tsx               # Create - Time series charts
    ├── BarChart.tsx                # Create - Comparison charts
    └── PieChart.tsx                # Create - Distribution charts
```

**DataTable Requirements:**
- Props: `data`, `columns`, `loading`, `pagination`, `sorting`, `selection`, `rowActions`
- Features: Sortable columns, row selection (checkboxes), pagination, loading skeleton
- Actions: Row-level menu with edit/delete actions
- Empty state: Custom empty message

**StatsCard Requirements:**
- Props: `title`, `value`, `change` (percent + direction), `icon`, `color`, `chartData`
- Show trend indicator (up/down arrow)
- Optional sparkline chart using recharts
- Color variants: primary, success, warning, error, info

**Acceptance Criteria:**
- [ ] DataTable renders 1000+ rows with virtualization
- [ ] Sorting works on all sortable columns
- [ ] Row selection persists across pagination
- [ ] StatsCard shows correct trend colors
- [ ] Charts are responsive and use MUI theme colors

---

### Phase 3: Form Components (6 hours)

**Files to Create:**
```
admin-web/src/components/forms/
├── FormField.tsx                   # Create - Universal form input
├── RichTextEditor.tsx              # Create - WYSIWYG editor
├── ImageUploader.tsx               # Create - Drag-drop image upload
├── SearchInput.tsx                 # Create - Global search
└── FormActions.tsx                 # Create - Save/Cancel buttons
```

**FormField Requirements:**
- Integrate with react-hook-form Controller
- Support types: text, email, password, number, select, multiline, date
- Validation: Required, min/max length, pattern matching
- Error display with MUI error states

**ImageUploader Requirements:**
- Use react-dropzone
- Accept: image/*, max 5MB
- Preview uploaded image
- Remove/change functionality
- Drag overlay styling

**RichTextEditor Requirements:**
- Use TipTap or Slate.js (lightweight)
- Toolbar: Bold, italic, lists, links
- Output HTML format
- Max height with scroll

**Acceptance Criteria:**
- [ ] FormField validates correctly with Zod schema
- [ ] ImageUploader shows preview and validates file size
- [ ] RichTextEditor outputs clean HTML
- [ ] All form components are accessible (keyboard nav, ARIA)

---

### Phase 4: Content Management Components (10 hours)

**Files to Create:**
```
admin-web/src/components/content/
├── CategoryTree.tsx                # Create - Nested category tree
├── TestList.tsx                    # Create - Test list with actions
├── QuestionList.tsx                # Create - Question reordering
├── QuestionBuilder.tsx             # Create - Question creation form
├── QuestionTypeSelector.tsx        # Create - Question type picker
└── TestSettings.tsx                # Create - Test configuration
```

**CategoryTree Requirements:**
- Use @dnd-kit for drag-drop
- Nested structure (parent/child categories)
- Expand/collapse functionality
- Inline editing (click to edit name)
- Add child, delete with confirmation
- Visual indentation (20px per level)

**QuestionBuilder Requirements:**
- Support question types: TEXT_SELECT, IMAGE_SELECT, AUDIO_SELECT, DRAG_DROP, TEXT_INPUT
- Dynamic form based on selected type
- Add/remove answer options
- Mark correct answer(s)
- Points per question
- Image upload for IMAGE_SELECT

**Acceptance Criteria:**
- [ ] Drag-drop reordering saves new order to API
- [ ] Category tree handles 50+ nested items
- [ ] Question builder validates minimum 2 options
- [ ] Correct answer selection works for single/multiple choice
- [ ] Auto-save draft functionality

---

### Phase 5: User Management Components (6 hours)

**Files to Create:**
```
admin-web/src/components/users/
├── UserTable.tsx                   # Create - User list table
├── UserCard.tsx                    # Create - Compact user info
├── UserFilters.tsx                 # Create - Filter controls
├── PermissionEditor.tsx            # Create - Role permissions
└── GroupManager.tsx                # Create - User groups
```

**UserTable Requirements:**
- Columns: Avatar, Name, Email, Role, Status, Last Active, Actions
- Filters: Role dropdown, Status toggle, Date range
- Bulk actions: Delete, Change role, Export CSV
- Status: Active/Inactive toggle with switch
- Click row to open user detail drawer

**PermissionEditor Requirements:**
- Matrix: Permissions × Roles
- Checkboxes for each permission
- Default role templates (Admin, Editor, Viewer)
- Save permission changes

**Acceptance Criteria:**
- [ ] UserTable filters work client-side
- [ ] Bulk actions show confirmation dialog
- [ ] Permission changes persist to backend
- [ ] Status toggle updates immediately (optimistic UI)

---

### Phase 6: Feedback Components (4 hours)

**Files to Create:**
```
admin-web/src/components/feedback/
├── Toast.tsx                       # Create - Notification toast
├── ToastProvider.tsx               # Create - Toast context
├── ConfirmDialog.tsx               # Create - Confirmation modal
├── PageLoader.tsx                  # Create - Full page loading
├── ErrorBoundary.tsx               # Create - Error handling
└── EmptyState.tsx                  # Create - Empty list state
```

**Toast Requirements:**
- Types: success, error, warning, info
- Position: top-right
- Auto-dismiss after 5 seconds
- Stacking multiple toasts
- Progress bar indicator

**ConfirmDialog Requirements:**
- Props: `title`, `message`, `confirmText`, `cancelText`, `danger`, `onConfirm`, `onCancel`
- Danger mode (red confirm button) for destructive actions
- Keyboard support (Enter to confirm, Escape to cancel)
- Focus trap inside modal

**Acceptance Criteria:**
- [ ] Toast shows with slide-in animation
- [ ] ConfirmDialog prevents accidental deletions
- [ ] ErrorBoundary catches React errors gracefully
- [ ] EmptyState shows for all empty lists

---

### Phase 7: Screen Implementation (8 hours)

**Files to Create:**
```
admin-web/src/screens/
├── Dashboard.tsx                   # Create - Main dashboard
├── Categories.tsx                  # Create - Category management
├── Tests.tsx                       # Create - Test list
├── TestEditor.tsx                  # Create - Test creation/edit
├── Users.tsx                       # Create - User management
├── Analytics.tsx                   # Create - Reports & charts
└── Settings.tsx                    # Create - System settings
```

**Dashboard Screen:**
- 4 StatsCards (Users, Tests, Completion Rate, Avg Session)
- 2 Charts (User growth line chart, Test completion bar chart)
- Recent Activity list (last 10 events)
- Quick Actions buttons

**Categories Screen:**
- CategoryTree component with all CRUD
- "Add Root Category" button
- Search/filter categories

**Tests Screen:**
- DataTable with test list
- Filters: Category, Difficulty, Status
- Actions: Edit, Delete, Preview
- "Create Test" button

**TestEditor Screen:**
- Tabs: General | Questions | Settings | Analytics
- General: Title, Category (select), Description (rich text), Image, Difficulty
- Questions: QuestionList + QuestionBuilder
- Settings: Time limit, Passing score, Attempts

**Users Screen:**
- UserTable with filters
- "Add User" button (opens form drawer)
- Export to CSV

**Acceptance Criteria:**
- [ ] All screens are responsive
- [ ] Forms validate before submit
- [ ] Loading states shown during API calls
- [ ] Error states handled gracefully
- [ ] Navigation works with browser back button

---

## 🎨 Design Requirements

### Colors (from spec)
```css
Primary: #4A90D9
Success: #43A047
Error: #E53935
Warning: #FB8C00
Info: #2196F3
Background: #F5F5F5
Card: #FFFFFF
Text Primary: #212121
Text Secondary: #757575
```

### Typography
- Font: Inter (Google Fonts)
- Base size: 16px
- Headings: 600 weight
- Body: 400 weight

### Spacing
- Base: 8px grid
- Card padding: 24px
- Section gap: 24px
- Component gap: 16px

### Responsive Breakpoints
```
xs: 0-599px    (Mobile - single column)
sm: 600-959px  (Tablet - sidebar drawer)
md: 960-1279px (Small desktop)
lg: 1280px+    (Full desktop)
```

---

## 🔌 API Integration

**Base URL:** `process.env.VITE_API_URL` (already configured)

**Required Endpoints (already exist in backend):**
```typescript
// Categories
GET    /api/admin/categories
POST   /api/admin/categories
PUT    /api/admin/categories/:id
DELETE /api/admin/categories/:id
POST   /api/admin/categories/reorder

// Tests
GET    /api/admin/tests
POST   /api/admin/tests
PUT    /api/admin/tests/:id
DELETE /api/admin/tests/:id
GET    /api/admin/tests/:id/statistics

// Questions
GET    /api/admin/tests/:testId/questions
POST   /api/admin/tests/:testId/questions
PUT    /api/admin/questions/:id
DELETE /api/admin/questions/:id
POST   /api/admin/questions/reorder

// Users
GET    /api/admin/users
PUT    /api/admin/users/:id
DELETE /api/admin/users/:id
GET    /api/admin/users/export

// Analytics
GET    /api/admin/analytics/dashboard
GET    /api/admin/analytics/users
GET    /api/admin/analytics/tests
```

**State Management:**
- Use @tanstack/react-query for server state
- Use zustand for client state (theme, sidebar collapse, user preferences)

---

## ♿ Accessibility Requirements

- WCAG 2.1 Level AA compliance
- Keyboard navigation for all interactive elements
- Focus visible indicators (2px outline)
- ARIA labels for icons and buttons
- Screen reader announcements for dynamic content
- Color contrast ratio 4.5:1 minimum
- Reduced motion support (`@media (prefers-reduced-motion: reduce)`)

---

## 📱 Responsive Behavior

**Desktop (1280px+):**
- Fixed sidebar (240px)
- Multi-column grids (4 col stats, 2 col charts)

**Tablet (960-1279px):**
- Collapsible sidebar (64px icons only)
- 2-column grids

**Mobile (<960px):**
- Hidden sidebar (drawer on menu click)
- Single column layout
- Stacked cards
- Full-width tables with horizontal scroll

---

## 🧪 Testing Requirements

**Unit Tests (Vitest):**
- Component rendering tests
- Hook behavior tests
- Utility function tests

**E2E Tests (Playwright - optional):**
- Login flow
- Create test flow
- User management flow

**Visual Regression:**
- Storybook stories for all components
- Chromatic or Loki for visual testing

---

## 📦 Dependencies to Add

```bash
npm install @fontsource/inter date-fns zod @hookform/resolvers @mui/x-date-pickers
npm install -D @storybook/react @storybook/addon-essentials
```

---

## 🚫 Out of Scope

- Authentication/Login (already exists)
- Backend API modifications (use existing)
- Mobile app version
- Multi-language i18n (English only)
- Real-time notifications (WebSockets)

---

## ✅ Definition of Done

- [ ] All 34 components implemented and documented
- [ ] All 7 screens functional with API integration
- [ ] Responsive design tested on 3 breakpoints
- [ ] Dark/light theme toggle works
- [ ] Accessibility audit passed (axe DevTools)
- [ ] No console errors or warnings
- [ ] Build successful (`npm run build`)
- [ ] Linting passes (`npm run lint`)
- [ ] Code reviewed and approved
- [ ] Documentation updated (README with component usage)

---

## 📊 Success Metrics

- Lighthouse Performance score > 90
- Lighthouse Accessibility score > 95
- Bundle size < 500KB (gzipped)
- Time to Interactive < 3s
- No critical or high severity accessibility issues

---

## 🆘 Support Resources

- **MUI Documentation:** https://mui.com/material-ui/getting-started/
- **React Query:** https://tanstack.com/query/latest
- **React Hook Form:** https://react-hook-form.com/
- **Dnd-kit:** https://docs.dndkit.com/
- **Existing code:** Check `admin-web/src/` for current implementation

---

## 💬 Communication

- Daily progress updates in thread
- Blockers reported immediately
- Code reviews via PRs
- Questions? Ping @lead-developer

---

**Ready to start:** Yes
**Blocking issues:** None
**Dependencies:** None (all libs already installed)

**Good luck! 🚀**

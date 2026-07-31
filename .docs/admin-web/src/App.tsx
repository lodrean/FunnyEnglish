import { Routes, Route } from 'react-router-dom'
import { ThemeProvider } from './theme/ThemeProvider'
import { ToastProvider } from './components/feedback'
import { AdminLayout } from './components/layout'
import {
  Dashboard,
  Categories,
  Tests,
  TestEditor,
  Users,
  Analytics,
  Settings,
} from './screens'

function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
        <Routes>
          <Route path="/" element={<AdminLayout />}>
            <Route index element={<Dashboard />} />
            <Route path="categories" element={<Categories />} />
            <Route path="tests" element={<Tests />} />
            <Route path="tests/new" element={<TestEditor />} />
            <Route path="tests/:id/edit" element={<TestEditor />} />
            <Route path="questions" element={<Tests />} />
            <Route path="users" element={<Users />} />
            <Route path="users/students" element={<Users />} />
            <Route path="users/admins" element={<Users />} />
            <Route path="users/groups" element={<Users />} />
            <Route path="analytics/reports" element={<Analytics />} />
            <Route path="analytics/statistics" element={<Analytics />} />
            <Route path="settings" element={<Settings />} />
          </Route>
        </Routes>
      </ToastProvider>
    </ThemeProvider>
  )
}

export default App

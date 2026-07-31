import { useEffect } from 'react'
import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { ThemeProvider } from './theme/ThemeProvider'
import { ToastProvider } from './components/feedback'
import { AdminLayout } from './components/layout'
import { useAuthStore } from './store/authStore'
import { RouteValidator } from './components/navigation/RouteValidator'
import {
  Dashboard,
  Categories,
  Tests,
  TestEditor,
  Users,
  Analytics,
  Settings,
  Login,
} from './screens'

// Protected Route wrapper component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthStore()
  const location = useLocation()
  
  if (isLoading) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          minHeight: '100vh' 
        }}
      >
        <CircularProgress />
      </Box>
    )
  }
  
  if (!isAuthenticated) {
    // Save intended URL to redirect back after login
    sessionStorage.setItem('intendedUrl', location.pathname + location.search)
    return <Navigate to="/login" replace />
  }
  
  return <>{children}</>
}

// Public Route wrapper - redirects to home if already authenticated
const PublicRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthStore()
  
  if (isLoading) {
    return (
      <Box 
        sx={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          minHeight: '100vh' 
        }}
      >
        <CircularProgress />
      </Box>
    )
  }
  
  if (isAuthenticated) {
    // Check if there's an intended URL to redirect to
    const intendedUrl = sessionStorage.getItem('intendedUrl')
    if (intendedUrl) {
      sessionStorage.removeItem('intendedUrl')
      return <Navigate to={intendedUrl} replace />
    }
    return <Navigate to="/" replace />
  }
  
  return <>{children}</>
}

// App initializer component
const AppInitializer: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const initialize = useAuthStore((state) => state.initialize)
  
  useEffect(() => {
    initialize()
  }, [initialize])
  
  return <>{children}</>
}

function App() {
  return (
    <ThemeProvider>
      <ToastProvider>
        <RouteValidator />
        <AppInitializer>
          <Routes>
            {/* Public routes */}
            <Route 
              path="/login" 
              element={
                <PublicRoute>
                  <Login />
                </PublicRoute>
              } 
            />
            
            {/* Protected admin routes */}
            <Route 
              path="/" 
              element={
                <ProtectedRoute>
                  <AdminLayout />
                </ProtectedRoute>
              }
            >
              {/* Dashboard */}
              <Route index element={<Dashboard />} />
              
              {/* Content Management - nested routes */}
              <Route path="content">
                <Route index element={<Navigate to="/content/categories" replace />} />
                <Route path="categories" element={<Categories />} />
                <Route path="tests" element={<Tests />} />
                <Route path="tests/:id" element={<TestEditor />} />
                <Route path="questions" element={<Tests />} />
              </Route>
              
              {/* Legacy redirects */}
              <Route path="categories" element={<Navigate to="/content/categories" replace />} />
              <Route path="tests" element={<Navigate to="/content/tests" replace />} />
              <Route path="questions" element={<Navigate to="/content/questions" replace />} />
              
              {/* User Management */}
              <Route path="users" element={<Users />} />
              <Route path="users/students" element={<Users />} />
              <Route path="users/admins" element={<Users />} />
              <Route path="users/groups" element={<Users />} />
              
              {/* Analytics */}
              <Route path="analytics">
                <Route index element={<Navigate to="/analytics/reports" replace />} />
                <Route path="reports" element={<Analytics />} />
                <Route path="statistics" element={<Analytics />} />
              </Route>
              
              {/* Settings */}
              <Route path="settings" element={<Settings />} />
            </Route>
            
            {/* Catch all - redirect to login */}
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </AppInitializer>
      </ToastProvider>
    </ThemeProvider>
  )
}

export default App

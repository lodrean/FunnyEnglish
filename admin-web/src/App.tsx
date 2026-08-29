import { useEffect } from 'react'
import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { ThemeProvider } from './theme/ThemeProvider'
import { ToastProvider, ErrorBoundary } from './components/feedback'
import { AdminLayout } from './components/layout'
import { useAuthStore } from './store/authStore'
import { RouteValidator } from './components/navigation/RouteValidator'
import {
  Dashboard,
  Users,
  Analytics,
  Login,
  SpeakingLibraries,
  SpeakingLibraryEditor,
  SpeakingTopics,
  SpeakingTopicEditor,
  GradingInbox,
  GradingDetail,
  ClientLogs,
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
        {/* Top-level boundary: краш роутов/инициализации не должен давать белый экран (memory №42) */}
        <ErrorBoundary>
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
              
              {/* Speaking Content */}
              <Route path="speaking">
                <Route index element={<Navigate to="/speaking/libraries" replace />} />
                <Route path="libraries" element={<SpeakingLibraries />} />
                <Route path="libraries/new" element={<SpeakingLibraryEditor />} />
                <Route path="libraries/:id/edit" element={<SpeakingLibraryEditor />} />
                <Route path="topics" element={<SpeakingTopics />} />
                <Route path="topics/new" element={<SpeakingTopicEditor />} />
                <Route path="topics/:id/edit" element={<SpeakingTopicEditor />} />
              </Route>

              {/* Grading */}
              <Route path="grading">
                <Route index element={<GradingInbox />} />
                <Route path="submissions/:id" element={<GradingDetail />} />
              </Route>

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
              
              {/* Client Logs (OpenSpec add-client-logging) */}
              <Route path="logs" element={<ClientLogs />} />
            </Route>
            
            {/* Catch all - redirect to login */}
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </AppInitializer>
        </ErrorBoundary>
      </ToastProvider>
    </ThemeProvider>
  )
}

export default App

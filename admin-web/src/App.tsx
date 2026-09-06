import { useEffect } from 'react'
import { Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { Box, CircularProgress } from '@mui/material'
import { ThemeProvider } from './theme/ThemeProvider'
import { ToastProvider, ErrorBoundary } from './components/feedback'
import { AdminLayout } from './components/layout'
import { useAuthStore } from './store/authStore'
import { RouteValidator } from './components/navigation/RouteValidator'
import { ROUTES } from './routes'
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
    return <Navigate to={ROUTES.login} replace />
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
    return <Navigate to={ROUTES.dashboard} replace />
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
              path={ROUTES.login} 
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
              <Route path={ROUTES.speaking.root}>
                <Route index element={<Navigate to={ROUTES.speaking.libraries} replace />} />
                <Route path={ROUTES.speaking.libraries} element={<SpeakingLibraries />} />
                <Route path={ROUTES.speaking.libraryNew} element={<SpeakingLibraryEditor />} />
                <Route path={ROUTES.speaking.libraryEdit} element={<SpeakingLibraryEditor />} />
                <Route path={ROUTES.speaking.topics} element={<SpeakingTopics />} />
                <Route path={ROUTES.speaking.topicNew} element={<SpeakingTopicEditor />} />
                <Route path={ROUTES.speaking.topicEdit} element={<SpeakingTopicEditor />} />
              </Route>

              {/* Grading */}
              <Route path={ROUTES.grading.root}>
                <Route index element={<GradingInbox />} />
                <Route path={ROUTES.grading.submission} element={<GradingDetail />} />
              </Route>

              {/* User Management */}
              <Route path={ROUTES.users.root} element={<Users />} />
              <Route path={ROUTES.users.students} element={<Users />} />
              <Route path={ROUTES.users.admins} element={<Users />} />
              <Route path={ROUTES.users.groups} element={<Users />} />
              
              {/* Analytics */}
              <Route path={ROUTES.analytics.root}>
                <Route index element={<Navigate to={ROUTES.analytics.reports} replace />} />
                <Route path={ROUTES.analytics.reports} element={<Analytics />} />
                <Route path={ROUTES.analytics.statistics} element={<Analytics />} />
              </Route>
              
              {/* Client Logs (OpenSpec add-client-logging) */}
              <Route path={ROUTES.logs} element={<ClientLogs />} />
            </Route>
            
            {/* Catch all - redirect to login */}
            <Route path="*" element={<Navigate to={ROUTES.login} replace />} />
          </Routes>
        </AppInitializer>
        </ErrorBoundary>
      </ToastProvider>
    </ThemeProvider>
  )
}

export default App

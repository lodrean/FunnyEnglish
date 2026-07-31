/**
 * Login Screen
 * 
 * Admin authentication page with email/password login.
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Paper,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
} from '@mui/material';
import {
  Visibility,
  VisibilityOff,
  AdminPanelSettings as AdminIcon,
} from '@mui/icons-material';
import { useAuthStore } from '../store/authStore';
import { login } from '../api/client';

export const Login: React.FC = () => {
  const navigate = useNavigate();
  const { login: storeLogin } = useAuthStore();
  
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    
    if (!email.trim() || !password.trim()) {
      setError('Please enter both email and password');
      return;
    }

    setIsLoading(true);
    
    try {
      const response = await login({ email: email.trim(), password });
      storeLogin(response.token, response.user);
      navigate('/');
    } catch (err: any) {
      const message = err.response?.data?.message || 'Invalid email or password';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleTogglePassword = () => {
    setShowPassword(!showPassword);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: (theme) =>
          theme.palette.mode === 'dark' ? '#121212' : '#F5F5F5',
        p: 2,
      }}
    >
      <Paper
        elevation={3}
        sx={{
          maxWidth: 420,
          width: '100%',
          p: 4,
          borderRadius: 3,
        }}
      >
        {/* Logo / Header */}
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <Box
            sx={{
              width: 64,
              height: 64,
              borderRadius: '50%',
              backgroundColor: 'primary.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              mx: 'auto',
              mb: 2,
            }}
          >
            <AdminIcon sx={{ fontSize: 32, color: 'white' }} />
          </Box>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 700, mb: 1 }}>
            FunnyEnglish
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Admin Panel
          </Typography>
        </Box>

        {/* Error Alert */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }} data-testid="login-error">
            {error}
          </Alert>
        )}

        {/* Login Form */}
        <Box component="form" onSubmit={handleSubmit} noValidate>
          <TextField
            fullWidth
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isLoading}
            margin="normal"
            required
            autoFocus
            autoComplete="email"
            placeholder="admin@funnyenglish.com"
            inputProps={{ 'data-testid': 'login-email' }}
          />
          
          <TextField
            fullWidth
            label="Password"
            type={showPassword ? 'text' : 'password'}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            disabled={isLoading}
            margin="normal"
            required
            autoComplete="current-password"
            inputProps={{ 'data-testid': 'login-password' }}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    onClick={handleTogglePassword}
                    edge="end"
                    disabled={isLoading}
                    data-testid="login-toggle-password"
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />

          <Button
            type="submit"
            fullWidth
            variant="contained"
            size="large"
            disabled={isLoading}
            data-testid="login-submit"
            sx={{
              mt: 3,
              mb: 2,
              py: 1.5,
              textTransform: 'none',
              fontWeight: 600,
              fontSize: '1rem',
            }}
          >
            {isLoading ? (
              <CircularProgress size={24} color="inherit" />
            ) : (
              'Sign In'
            )}
          </Button>
        </Box>

        {/* Demo credentials hint */}
        <Box sx={{ mt: 3, p: 2, backgroundColor: 'action.hover', borderRadius: 1 }}>
          <Typography variant="caption" color="text.secondary" display="block">
            <strong>Demo credentials:</strong>
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            Email: admin@funnyenglish.com
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            Password: admin123
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
};

export default Login;

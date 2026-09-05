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
} from '@mui/icons-material';
import { useAuthStore } from '../store/authStore';
import { login } from '../api/client';
import { Logo } from '../components/common/Logo';

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
          theme.palette.background.default, // light #EEF3FF / dark #121212 — токены темы (2oz.4)
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
          <Logo
            variant="full"
            height={96}
            sx={{
              maxWidth: 280,
              width: '100%',
              height: 'auto',
              display: 'block',
              mx: 'auto',
            }}
          />
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
            placeholder="admin@sotospeak.com"
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

        {/* Demo credentials hint (dev only, не попадает в prod-сборку) */}
        {import.meta.env.DEV && (
        <Box sx={{ mt: 3, p: 2, backgroundColor: 'action.hover', borderRadius: 1 }}>
          <Typography variant="caption" color="text.secondary" display="block">
            <strong>Demo credentials:</strong>
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            Email: admin@sotospeak.com
          </Typography>
          <Typography variant="caption" color="text.secondary" display="block">
            Password: admin123
          </Typography>
        </Box>
        )}
      </Paper>
    </Box>
  );
};

export default Login;

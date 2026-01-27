import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { login } from '../api/client';
import { useAuthStore } from '../store/authStore';
import type { LoginRequest } from '../types';

export default function LoginPage() {
  const navigate = useNavigate();
  const loginStore = useAuthStore((state) => state.login);
  const [error, setError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginRequest>();

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      if (data.user.role !== 'ADMIN') {
        setError('Доступ запрещён. Требуются права администратора.');
        return;
      }
      loginStore(data.token, data.user);
      navigate('/');
    },
    onError: (error: Error) => {
      setError(error.message || 'Ошибка авторизации');
    },
  });

  const onSubmit = (data: LoginRequest) => {
    setError(null);
    mutation.mutate(data);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        bgcolor: 'background.default',
        p: 2,
      }}
    >
      <Card sx={{ maxWidth: 400, width: '100%' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography
            variant="h4"
            component="h1"
            gutterBottom
            color="primary"
            fontWeight="bold"
            textAlign="center"
          >
            FunnyEnglish
          </Typography>
          <Typography
            variant="subtitle1"
            color="text.secondary"
            textAlign="center"
            mb={4}
          >
            Вход в админ-панель
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit(onSubmit)}>
            <TextField
              fullWidth
              label="Email"
              type="email"
              margin="normal"
              {...register('email', {
                required: 'Email обязателен',
                pattern: {
                  value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                  message: 'Некорректный email',
                },
              })}
              error={!!errors.email}
              helperText={errors.email?.message}
            />
            <TextField
              fullWidth
              label="Пароль"
              type="password"
              margin="normal"
              {...register('password', {
                required: 'Пароль обязателен',
                minLength: {
                  value: 6,
                  message: 'Минимум 6 символов',
                },
              })}
              error={!!errors.password}
              helperText={errors.password?.message}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              sx={{ mt: 3 }}
              disabled={mutation.isPending}
            >
              {mutation.isPending ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                'Войти'
              )}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}

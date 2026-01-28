import axios from 'axios';
import type {
  AuthResponse,
  LoginRequest,
  Test,
  Category,
  CreateTestRequest,
  AdminUserSummary,
  AdminUserDetail,
  AdminAnalytics,
  AdminSettings
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/login', data);
  return response.data;
};

// Categories
export const getCategories = async (): Promise<Category[]> => {
  const response = await api.get<Category[]>('/categories');
  return response.data;
};

// Tests - Admin
export const getAdminTests = async (): Promise<Test[]> => {
  const response = await api.get<Test[]>('/admin/tests');
  return response.data;
};

export const getAdminTest = async (id: string): Promise<Test> => {
  const response = await api.get<Test>(`/admin/tests/${id}`);
  return response.data;
};

export const createTest = async (data: CreateTestRequest): Promise<Test> => {
  const response = await api.post<Test>('/admin/tests', data);
  return response.data;
};

export const updateTest = async (id: string, data: Partial<CreateTestRequest>): Promise<Test> => {
  const response = await api.put<Test>(`/admin/tests/${id}`, data);
  return response.data;
};

export const deleteTest = async (id: string): Promise<void> => {
  await api.delete(`/admin/tests/${id}`);
};

// Users - Admin
export const getAdminUsers = async (options: {
  query?: string;
  role?: string;
} = {}): Promise<AdminUserSummary[]> => {
  const response = await api.get<AdminUserSummary[]>('/admin/users', {
    params: {
      q: options.query,
      role: options.role,
    },
  });
  return response.data;
};

export const getAdminUser = async (id: string): Promise<AdminUserDetail> => {
  const response = await api.get<AdminUserDetail>(`/admin/users/${id}`);
  return response.data;
};

// Analytics - Admin
export const getAdminAnalytics = async (): Promise<AdminAnalytics> => {
  const response = await api.get<AdminAnalytics>('/admin/analytics');
  return response.data;
};

// Settings - Admin
export const getAdminSettings = async (): Promise<AdminSettings> => {
  const response = await api.get<AdminSettings>('/admin/settings');
  return response.data;
};

// Media upload
export const uploadMedia = async (file: File, folder: string = 'media'): Promise<string> => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);

  const response = await api.post<{ url: string }>('/admin/media/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data.url;
};

export const deleteMedia = async (url: string): Promise<void> => {
  await api.delete('/admin/media', { params: { url } });
};

export default api;

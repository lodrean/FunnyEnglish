import axios from 'axios';
import { z } from 'zod';
import {
  adminAnalyticsSchema,
  adminMessageSchema,
  adminSettingsSchema,
  adminUserDetailSchema,
  adminUserPageSchema,
  authResponseSchema,
  clientLogsPageSchema,
  groupDetailSchema,
  groupProgressSummarySchema,
  joinRequestSchema,
  dailyActivitySchema,
  guestAnalyticsSchema,
  levelDistributionSchema,
  popularTestSchema,
  recentActivityItemSchema,
  parseApi,
  prdMetricsSchema,
  studentGroupSchema,
  userSchema,
} from './schemas';
import { logger } from '../utils/logger';
import type {
  AuthResponse,
  LoginRequest,
  User,
  AdminUserSummary,
  AdminUserDetail,
  AdminAnalytics,
  AdminSettings,
  DailyActivity,
  LevelDistribution,
  PopularTest,
  RecentActivityItem,
  StudentGroup,
  GroupDetail,
  CreateGroupRequest,
  UpdateGroupRequest,
  JoinRequest,
  ProcessJoinRequest,
  GroupProgressSummary,
  // StudentProgress
} from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
});

// Add auth token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 errors + remote-логирование ошибок API (OpenSpec add-client-logging)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status: number | undefined = error.response?.status;
    const requestUrl: string = error.config?.url ?? '';
    // 401 от /auth/* (неверные креды и т.п.) — это ответ ФОРМЕ логина, а не истёкшая
    // сессия: window.location.href='/login' делал полный reload и стирал Alert
    // с ошибкой (гонка, стабильный красный в E2E). На /login редирект тоже бессмыслен.
    const isAuthEndpoint = requestUrl.includes('/auth/');
    const alreadyOnLogin = window.location.pathname === '/login';
    if (status === 401 && !isAuthEndpoint && !alreadyOnLogin) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    } else if (status !== 401) {
      // Без тел ответов (приватность); 401 не логируем — это штатный редирект на логин
      const method = (error.config?.method ?? '?').toUpperCase();
      const url = error.config?.url ?? '?';
      logger.warn('ApiClient', `${method} ${url} -> ${status ?? 'network'}`);
    }
    return Promise.reject(error);
  }
);

// Auth
export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/auth/login', data);
  return parseApi(authResponseSchema, response.data, 'auth/login');
};

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get<User>('/users/me');
  return parseApi(userSchema, response.data, 'users/me');
};

// Users - Admin (bd wy7.6: серверная пагинация — Spring Page-контракт)
export interface AdminUserPage {
  content: AdminUserSummary[];
  totalElements: number;
  totalPages: number;
  number: number; // текущая страница (0-based)
  size: number;
}

export const getAdminUsers = async (options: {
  query?: string;
  role?: string;
  page?: number;
  size?: number;
} = {}): Promise<AdminUserPage> => {
  const response = await api.get<AdminUserPage>('/admin/users', {
    params: {
      q: options.query,
      role: options.role,
      page: options.page,
      size: options.size,
    },
  });
  return parseApi(adminUserPageSchema, response.data, 'admin/users');
};

export const getAdminUser = async (id: string): Promise<AdminUserDetail> => {
  const response = await api.get<AdminUserDetail>(`/admin/users/${id}`);
  return parseApi(adminUserDetailSchema, response.data, `admin/users/${id}`);
};

// Messages - Admin (сообщения/комментарии ученику)
export interface SendMessagePayload {
  text: string;
  type?: 'MESSAGE' | 'COMMENT';
  testId?: string;
}

export interface AdminMessage {
  id: string;
  senderId: string;
  senderName: string;
  recipientId: string;
  text: string;
  type: 'MESSAGE' | 'COMMENT';
  testId: string | null;
  createdAt: string;
  readAt: string | null;
}

export const sendMessageToUser = async (
  userId: string,
  payload: SendMessagePayload
): Promise<AdminMessage> => {
  const response = await api.post<AdminMessage>(`/admin/users/${userId}/messages`, payload);
  return parseApi(adminMessageSchema, response.data, 'admin/users/:id/messages');
};

export const getUserMessages = async (userId: string): Promise<AdminMessage[]> => {
  const response = await api.get<AdminMessage[]>(`/admin/users/${userId}/messages`);
  return parseApi(z.array(adminMessageSchema), response.data, 'admin/users/:id/messages');
};

// Analytics - Admin
export const getAdminAnalytics = async (): Promise<AdminAnalytics> => {
  const response = await api.get<AdminAnalytics>('/admin/analytics');
  return parseApi(adminAnalyticsSchema, response.data, 'admin/analytics');
};

export const getAdminDailyActivity = async (days: number = 7): Promise<DailyActivity[]> => {
  const response = await api.get<DailyActivity[]>('/admin/analytics/daily-activity', {
    params: { days },
  });
  return parseApi(z.array(dailyActivitySchema), response.data, 'admin/analytics/daily-activity');
};

// Guest (anonymous) analytics — обезличенные пользователи гостевого режима
export interface GuestAnalytics {
  totalGuests: number;
  activeGuests7d: number;
  guestTestCompletions: number;
  convertedGuests: number;
  conversionRate: number;
}

export const getGuestAnalytics = async (): Promise<GuestAnalytics> => {
  const response = await api.get<GuestAnalytics>('/admin/analytics/guests');
  return parseApi(guestAnalyticsSchema, response.data, 'admin/analytics/guests');
};

// Метрики PRD (Speaking Trainer §Metrics, bd FunnyEnglish-h3l.3) — реальные агрегаты backend
export interface PrdMetrics {
  practiceSubmissionsLast7d: number;
  activeStudentsLast7d: number;
  practicePerStudentPerWeek: number;
  reviewedTotal: number;
  reviewedWithin48h: number;
  reviewedWithin48hShare: number;
  totalGuests: number;
  convertedGuests: number;
  guestConversionRate: number;
}

export const getPrdMetrics = async (): Promise<PrdMetrics> => {
  const response = await api.get<PrdMetrics>('/admin/analytics/prd-metrics');
  return parseApi(prdMetricsSchema, response.data, 'admin/analytics/prd-metrics');
};

// ==================== Client Logs (OpenSpec add-client-logging) ====================

export interface ClientLogEntry {
  id: string;
  anonymousId: string | null;
  level: 'WARN' | 'ERROR';
  tag: string;
  message: string;
  stackTrace: string | null;
  platform: string;
  appVersion: string | null;
  clientTimestamp: string | null;
  createdAt: string;
}

/** Spring Page JSON (как в admin submissions) */
export interface ClientLogsPage {
  content: ClientLogEntry[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ClientLogsParams {
  level?: string;
  platform?: string;
  from?: string; // ISO-8601
  to?: string;   // ISO-8601
  q?: string;
  page?: number;
  size?: number;
}

export const getClientLogs = async (params: ClientLogsParams = {}): Promise<ClientLogsPage> => {
  const response = await api.get<ClientLogsPage>('/admin/logs', { params });
  return parseApi(clientLogsPageSchema, response.data, 'admin/logs');
};

export const getAdminLevelDistribution = async (): Promise<LevelDistribution[]> => {
  const response = await api.get<LevelDistribution[]>('/admin/analytics/levels');
  return parseApi(z.array(levelDistributionSchema), response.data, 'admin/analytics/levels');
};

export const getPopularTests = async (limit: number = 5): Promise<PopularTest[]> => {
  const response = await api.get<PopularTest[]>('/admin/analytics/popular-tests', {
    params: { limit },
  });
  return parseApi(z.array(popularTestSchema), response.data, 'admin/analytics/popular-tests');
};

export const getRecentActivity = async (limit: number = 10): Promise<RecentActivityItem[]> => {
  const response = await api.get<RecentActivityItem[]>('/admin/analytics/recent-activity', {
    params: { limit },
  });
  return parseApi(z.array(recentActivityItemSchema), response.data, 'admin/analytics/recent-activity');
};

// Settings - Admin
export const getAdminSettings = async (): Promise<AdminSettings> => {
  const response = await api.get<AdminSettings>('/admin/settings');
  return parseApi(adminSettingsSchema, response.data, 'admin/settings');
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

// ==================== Student Groups API ====================

export const getTeacherGroups = async (): Promise<StudentGroup[]> => {
  const response = await api.get<StudentGroup[]>('/groups/teacher/my-groups');
  return parseApi(z.array(studentGroupSchema), response.data, 'groups/teacher/my-groups');
};

export const getGroupDetail = async (groupId: string): Promise<GroupDetail> => {
  const response = await api.get<GroupDetail>(`/groups/${groupId}/detail`);
  return parseApi(groupDetailSchema, response.data, 'groups/:id/detail');
};

export const createGroup = async (data: CreateGroupRequest): Promise<StudentGroup> => {
  const response = await api.post<StudentGroup>('/groups', data);
  return parseApi(studentGroupSchema, response.data, 'groups (create)');
};

export const updateGroup = async (groupId: string, data: UpdateGroupRequest): Promise<StudentGroup> => {
  const response = await api.put<StudentGroup>(`/groups/${groupId}`, data);
  return parseApi(studentGroupSchema, response.data, 'groups/:id (update)');
};

export const deleteGroup = async (groupId: string): Promise<void> => {
  await api.delete(`/groups/${groupId}`);
};

export const removeStudentFromGroup = async (groupId: string, studentId: string): Promise<void> => {
  await api.delete(`/groups/${groupId}/students/${studentId}`);
};

export const getPendingRequests = async (groupId: string): Promise<JoinRequest[]> => {
  const response = await api.get<JoinRequest[]>(`/groups/${groupId}/join-requests`);
  return parseApi(z.array(joinRequestSchema), response.data, 'groups/:id/join-requests');
};

export const processJoinRequest = async (
  groupId: string,
  requestId: string,
  data: ProcessJoinRequest
): Promise<void> => {
  await api.post(`/groups/${groupId}/join-requests/${requestId}`, data);
};

export const getGroupProgress = async (groupId: string): Promise<GroupProgressSummary> => {
  const response = await api.get<GroupProgressSummary>(`/groups/${groupId}/progress`);
  return parseApi(groupProgressSummarySchema, response.data, 'groups/:id/progress');
};

export const apiClient = api;

export default api;

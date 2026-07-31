import axios from 'axios';
import type {
  AuthResponse,
  LoginRequest,
  User,
  Test,
  Category,
  CreateTestRequest,
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
import type {
  QuestionV2,
  CreateQuestionRequest,
  UpdateQuestionRequest,
  ReorderQuestionsRequest,
  QuestionTypeV2,
  QuestionContentRequest,
  AudioTestListItem,
  AudioTest,
  CreateAudioTestRequest,
  UpdateAudioTestRequest,
} from '../types/questions';

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

export const getCurrentUser = async (): Promise<User> => {
  const response = await api.get<User>('/users/me');
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
  return response.data;
};

export const getUserMessages = async (userId: string): Promise<AdminMessage[]> => {
  const response = await api.get<AdminMessage[]>(`/admin/users/${userId}/messages`);
  return response.data;
};

// Analytics - Admin
export const getAdminAnalytics = async (): Promise<AdminAnalytics> => {
  const response = await api.get<AdminAnalytics>('/admin/analytics');
  return response.data;
};

export const getAdminDailyActivity = async (days: number = 7): Promise<DailyActivity[]> => {
  const response = await api.get<DailyActivity[]>('/admin/analytics/daily-activity', {
    params: { days },
  });
  return response.data;
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
  return response.data;
};

export const getAdminLevelDistribution = async (): Promise<LevelDistribution[]> => {
  const response = await api.get<LevelDistribution[]>('/admin/analytics/levels');
  return response.data;
};

export const getPopularTests = async (limit: number = 5): Promise<PopularTest[]> => {
  const response = await api.get<PopularTest[]>('/admin/analytics/popular-tests', {
    params: { limit },
  });
  return response.data;
};

export const getRecentActivity = async (limit: number = 10): Promise<RecentActivityItem[]> => {
  const response = await api.get<RecentActivityItem[]>('/admin/analytics/recent-activity', {
    params: { limit },
  });
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

// Questions V2 API
export const getQuestionsByTest = async (testId: string): Promise<QuestionV2[]> => {
  // Используем /details endpoint для получения полных данных включая IMAGE_WORD_MATCH
  const response = await api.get<QuestionV2[]>(`/questions/test/${testId}/details`);
  return response.data;
};

export const getQuestionForAdmin = async (id: string): Promise<QuestionV2> => {
  const response = await api.get<QuestionV2>(`/questions/${id}/admin`);
  return response.data;
};

export const createQuestion = async (data: CreateQuestionRequest): Promise<QuestionV2> => {
  const response = await api.post<QuestionV2>('/questions', data);
  return response.data;
};

export const updateQuestion = async (id: string, data: UpdateQuestionRequest): Promise<QuestionV2> => {
  const response = await api.put<QuestionV2>(`/questions/${id}`, data);
  return response.data;
};

// Image Word Match specific endpoints
export const createImageWordMatchQuestion = async (data: {
  testId: string;
  instruction: string;
  imageUrl: string;
  words: { id: string; text: string; translation?: string; audioUrl?: string }[];
  hotspots: { id: string; x: number; y: number; width: number; height: number; shape: string; wordId: string }[];
  points: number;
}): Promise<QuestionV2> => {
  const response = await api.post<QuestionV2>('/questions/image-word-match', data);
  return response.data;
};

export const updateImageWordMatchQuestion = async (id: string, data: {
  testId: string;
  instruction: string;
  imageUrl: string;
  words: { id: string; text: string; translation?: string; audioUrl?: string }[];
  hotspots: { id: string; x: number; y: number; width: number; height: number; shape: string; wordId: string }[];
  points: number;
}): Promise<QuestionV2> => {
  const response = await api.put<QuestionV2>(`/questions/image-word-match/${id}`, data);
  return response.data;
};

export const deleteQuestion = async (id: string): Promise<void> => {
  await api.delete(`/questions/${id}`);
};

export const duplicateQuestion = async (id: string): Promise<QuestionV2> => {
  const response = await api.post<QuestionV2>(`/questions/${id}/duplicate`);
  return response.data;
};

export const reorderQuestions = async (data: ReorderQuestionsRequest): Promise<void> => {
  await api.post('/questions/reorder', data);
};

export const validateQuestionContent = async (
  type: QuestionTypeV2,
  content: QuestionContentRequest
): Promise<{ valid: boolean }> => {
  const response = await api.post<{ valid: boolean }>('/admin/questions/validate', content, {
    params: { type },
  });
  return response.data;
};

// ==================== Student Groups API ====================

export const getTeacherGroups = async (): Promise<StudentGroup[]> => {
  const response = await api.get<StudentGroup[]>('/groups/teacher/my-groups');
  return response.data;
};

export const getGroupDetail = async (groupId: string): Promise<GroupDetail> => {
  const response = await api.get<GroupDetail>(`/groups/${groupId}/detail`);
  return response.data;
};

export const createGroup = async (data: CreateGroupRequest): Promise<StudentGroup> => {
  const response = await api.post<StudentGroup>('/groups', data);
  return response.data;
};

export const updateGroup = async (groupId: string, data: UpdateGroupRequest): Promise<StudentGroup> => {
  const response = await api.put<StudentGroup>(`/groups/${groupId}`, data);
  return response.data;
};

export const deleteGroup = async (groupId: string): Promise<void> => {
  await api.delete(`/groups/${groupId}`);
};

export const removeStudentFromGroup = async (groupId: string, studentId: string): Promise<void> => {
  await api.delete(`/groups/${groupId}/students/${studentId}`);
};

export const getPendingRequests = async (groupId: string): Promise<JoinRequest[]> => {
  const response = await api.get<JoinRequest[]>(`/groups/${groupId}/join-requests`);
  return response.data;
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
  return response.data;
};

// apiClient export for other modules
// ==================== Audio Tests API ====================

export const getAudioTests = async (): Promise<AudioTestListItem[]> => {
  const response = await api.get<AudioTestListItem[]>('/admin/audio-tests');
  return response.data;
};

export const getAudioTest = async (id: string): Promise<AudioTest> => {
  const response = await api.get<AudioTest>(`/admin/audio-tests/${id}`);
  return response.data;
};

export const createAudioTest = async (data: CreateAudioTestRequest): Promise<AudioTest> => {
  const response = await api.post<AudioTest>('/admin/audio-tests', data);
  return response.data;
};

export const updateAudioTest = async (id: string, data: UpdateAudioTestRequest): Promise<AudioTest> => {
  const response = await api.put<AudioTest>(`/admin/audio-tests/${id}`, data);
  return response.data;
};

export const deleteAudioTest = async (id: string): Promise<void> => {
  await api.delete(`/admin/audio-tests/${id}`);
};

export const publishAudioTest = async (id: string, isPublished: boolean): Promise<AudioTest> => {
  const response = await api.patch<AudioTest>(`/admin/audio-tests/${id}/publish`, { isPublished });
  return response.data;
};

export const apiClient = api;

export default api;

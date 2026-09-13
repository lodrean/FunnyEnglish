import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock axios before importing the client
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => mockApi),
  },
}));

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Mock window.location
const locationMock = { href: '' };
Object.defineProperty(window, 'location', {
  value: locationMock,
  writable: true,
});

// ==================== Фикстуры под zod-схемы (bd b85.4) ====================
// parseApi валидирует каждый ответ — моки обязаны удовлетворять схемам
// (api/schemas.ts), иначе ApiContractError. Фикстуры заодно фиксируют, что
// контрактные формы проходят валидацию.

const validUser = {
  id: 'u1',
  email: 'a@b.c',
  displayName: 'A B',
  avatarUrl: null,
  level: 1,
  totalPoints: 10,
  currentStreak: 0,
  role: 'USER',
  createdAt: '2026-09-01T00:00:00Z',
};

const stats = {
  testsCompleted: 1,
  totalStars: 3,
  perfectScores: 0,
  currentLevel: 1,
  pointsToNextLevel: 90,
};

const group = {
  id: 'g123',
  name: 'Group 1',
  description: null,
  teacherId: 't1',
  teacherName: null,
  inviteCode: 'ABCD12',
  maxStudents: 10,
  currentStudents: 1,
  isActive: true,
  createdAt: '2026-09-01T00:00:00Z',
};

const studentProgress = {
  userId: 'u1',
  displayName: 'A B',
  email: 'a@b.c',
  avatarUrl: null,
  level: 1,
  totalPoints: 10,
  currentStreak: 0,
  longestStreak: 2,
  completedTests: 1,
  averageScore: 80,
  totalTimeSpent: 300,
  achievementsCount: 0,
  lastActivityAt: null,
  joinedAt: '2026-09-01T00:00:00Z',
};

const fixtures: Record<string, unknown> = {
  // Auth
  'POST /auth/login': { token: 'jwt-token', user: validUser },
  // Users
  'GET /admin/users': {
    content: [{ ...validUser, stats }],
    totalElements: 1,
    totalPages: 1,
    number: 0,
    size: 10,
  },
  'GET /admin/users/123': {
    user: validUser,
    stats,
    achievements: [],
    progressSummary: {
      totalTests: 0,
      completedTests: 0,
      totalStars: 0,
      maxPossibleStars: 0,
      categoriesProgress: [],
    },
    progress: [],
  },
  // Analytics
  'GET /admin/analytics': {
    totalUsers: 1,
    totalTests: 2,
    publishedTests: 1,
    totalQuestions: 5,
    totalAnswers: 20,
    totalCompletions: 1,
    totalCategories: 2,
    totalAchievements: 3,
    topCategories: [],
  },
  'GET /admin/analytics/daily-activity': [
    { date: '2026-09-01', newUsers: 1, testsCompleted: 2, achievementsEarned: 0 },
  ],
  'GET /admin/analytics/levels': [{ level: 1, users: 5 }],
  'GET /admin/analytics/popular-tests': [
    { id: 't1', name: 'Test', completions: 3, category: 'Grammar' },
  ],
  'GET /admin/analytics/recent-activity': [
    { userName: 'A B', type: 'TEST_COMPLETED', timestamp: '2026-09-01T00:00:00Z', details: null },
  ],
  // Media
  'POST /admin/media/upload': { url: 'http://example.com/test.png' },
  // Groups
  'GET /groups/teacher/my-groups': [group],
  'GET /groups/g123/detail': {
    ...group,
    members: [],
    pendingRequests: 0,
  },
  'POST /groups': group,
  'PUT /groups/g123': group,
  'GET /groups/g123/join-requests': [
    { id: 'r1', userId: 'u1', userName: 'A B', userEmail: 'a@b.c', requestedAt: '2026-09-01T00:00:00Z' },
  ],
  'GET /groups/g123/progress': {
    groupId: 'g123',
    groupName: 'Group 1',
    totalStudents: 1,
    averageLevel: 1,
    averagePoints: 10,
    totalCompletedTests: 1,
    mostActiveStudents: [studentProgress],
    studentsNeedingAttention: [],
  },
};

// Create mock API — ответ подбирается по «METHOD url», дефолт {} (void-эндпоинты)
const mockApi = {
  get: vi.fn((url: string) => Promise.resolve({ data: fixtures[`GET ${url}`] ?? {} })),
  post: vi.fn((url: string) => Promise.resolve({ data: fixtures[`POST ${url}`] ?? {} })),
  put: vi.fn((url: string) => Promise.resolve({ data: fixtures[`PUT ${url}`] ?? {} })),
  delete: vi.fn((url: string) => Promise.resolve({ data: fixtures[`DELETE ${url}`] ?? {} })),
  patch: vi.fn((url: string) => Promise.resolve({ data: fixtures[`PATCH ${url}`] ?? {} })),
  interceptors: {
    request: { use: vi.fn() },
    response: { use: vi.fn() },
  },
};

describe('API Client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  // Import the mocked module
  const importClient = async () => {
    const axios = await import('axios');
    (axios.default.create as any).mockReturnValue(mockApi);
    return import('../api/client');
  };

  describe('Auth', () => {
    it('should call login endpoint', async () => {
      const { login } = await importClient();
      const credentials = { email: 'test@example.com', password: 'password' };
      const result = await login(credentials);
      expect(mockApi.post).toHaveBeenCalledWith('/auth/login', credentials);
      expect(result.token).toBe('jwt-token');
    });
  });

  describe('Users', () => {
    it('should fetch admin users', async () => {
      const { getAdminUsers } = await importClient();
      const page = await getAdminUsers();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/users', { params: { q: undefined, role: undefined } });
      expect(page.totalElements).toBe(1);
    });

    it('should fetch users with filters', async () => {
      const { getAdminUsers } = await importClient();
      await getAdminUsers({ query: 'john', role: 'admin' });
      expect(mockApi.get).toHaveBeenCalledWith('/admin/users', { params: { q: 'john', role: 'admin' } });
    });

    it('should fetch single user', async () => {
      const { getAdminUser } = await importClient();
      const detail = await getAdminUser('123');
      expect(mockApi.get).toHaveBeenCalledWith('/admin/users/123');
      expect(detail.user.id).toBe('u1');
    });
  });

  describe('Analytics', () => {
    it('should fetch analytics', async () => {
      const { getAdminAnalytics } = await importClient();
      const analytics = await getAdminAnalytics();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics');
      expect(analytics.totalUsers).toBe(1);
    });

    it('should fetch daily activity with default days', async () => {
      const { getAdminDailyActivity } = await importClient();
      const activity = await getAdminDailyActivity();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/daily-activity', { params: { days: 7 } });
      expect(activity).toHaveLength(1);
    });

    it('should fetch daily activity with custom days', async () => {
      const { getAdminDailyActivity } = await importClient();
      await getAdminDailyActivity(30);
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/daily-activity', { params: { days: 30 } });
    });

    it('should fetch level distribution', async () => {
      const { getAdminLevelDistribution } = await importClient();
      const levels = await getAdminLevelDistribution();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/levels');
      expect(levels[0].level).toBe(1);
    });

    it('should fetch popular tests', async () => {
      const { getPopularTests } = await importClient();
      const tests = await getPopularTests();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/popular-tests', { params: { limit: 5 } });
      expect(tests[0].id).toBe('t1');
    });

    it('should fetch recent activity', async () => {
      const { getRecentActivity } = await importClient();
      const activity = await getRecentActivity(20);
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/recent-activity', { params: { limit: 20 } });
      expect(activity).toHaveLength(1);
    });
  });

  describe('Media', () => {
    it('should upload media', async () => {
      const { uploadMedia } = await importClient();
      const file = new File(['content'], 'test.png', { type: 'image/png' });
      const url = await uploadMedia(file, 'thumbnails');
      expect(mockApi.post).toHaveBeenCalledWith(
        '/admin/media/upload',
        expect.any(FormData),
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
      expect(url).toBe('http://example.com/test.png');
    });

    it('should delete media', async () => {
      const { deleteMedia } = await importClient();
      await deleteMedia('http://example.com/image.png');
      expect(mockApi.delete).toHaveBeenCalledWith('/admin/media', { params: { url: 'http://example.com/image.png' } });
    });
  });

  describe('Student Groups', () => {
    it('should fetch teacher groups', async () => {
      const { getTeacherGroups } = await importClient();
      const groups = await getTeacherGroups();
      expect(mockApi.get).toHaveBeenCalledWith('/groups/teacher/my-groups');
      expect(groups[0].id).toBe('g123');
    });

    it('should fetch group detail', async () => {
      const { getGroupDetail } = await importClient();
      const detail = await getGroupDetail('g123');
      expect(mockApi.get).toHaveBeenCalledWith('/groups/g123/detail');
      expect(detail.id).toBe('g123');
    });

    it('should create group', async () => {
      const { createGroup } = await importClient();
      const groupData = { name: 'Group 1' };
      const created = await createGroup(groupData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/groups', groupData);
      expect(created.id).toBe('g123');
    });

    it('should update group', async () => {
      const { updateGroup } = await importClient();
      const updateData = { name: 'Updated' };
      const updated = await updateGroup('g123', updateData as any);
      expect(mockApi.put).toHaveBeenCalledWith('/groups/g123', updateData);
      expect(updated.name).toBe('Group 1');
    });

    it('should delete group', async () => {
      const { deleteGroup } = await importClient();
      await deleteGroup('g123');
      expect(mockApi.delete).toHaveBeenCalledWith('/groups/g123');
    });

    it('should remove student from group', async () => {
      const { removeStudentFromGroup } = await importClient();
      await removeStudentFromGroup('g123', 's456');
      expect(mockApi.delete).toHaveBeenCalledWith('/groups/g123/students/s456');
    });

    it('should fetch pending requests', async () => {
      const { getPendingRequests } = await importClient();
      const requests = await getPendingRequests('g123');
      expect(mockApi.get).toHaveBeenCalledWith('/groups/g123/join-requests');
      expect(requests[0].id).toBe('r1');
    });

    it('should process join request', async () => {
      const { processJoinRequest } = await importClient();
      const processData = { action: 'approve' };
      await processJoinRequest('g123', 'r456', processData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/groups/g123/join-requests/r456', processData);
    });

    it('should fetch group progress', async () => {
      const { getGroupProgress } = await importClient();
      const progress = await getGroupProgress('g123');
      expect(mockApi.get).toHaveBeenCalledWith('/groups/g123/progress');
      expect(progress.groupId).toBe('g123');
    });
  });
});

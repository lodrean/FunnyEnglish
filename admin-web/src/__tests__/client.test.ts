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

// Create mock API
const mockApi = {
  get: vi.fn().mockResolvedValue({ data: {} }),
  post: vi.fn().mockResolvedValue({ data: {} }),
  put: vi.fn().mockResolvedValue({ data: {} }),
  delete: vi.fn().mockResolvedValue({ data: {} }),
  patch: vi.fn().mockResolvedValue({ data: {} }),
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
      await login(credentials);
      expect(mockApi.post).toHaveBeenCalledWith('/auth/login', credentials);
    });
  });

  describe('Categories', () => {
    it('should fetch categories', async () => {
      const { getCategories } = await importClient();
      await getCategories();
      expect(mockApi.get).toHaveBeenCalledWith('/categories');
    });
  });

  describe('Tests', () => {
    it('should fetch admin tests', async () => {
      const { getAdminTests } = await importClient();
      await getAdminTests();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/tests');
    });

    it('should fetch single test', async () => {
      const { getAdminTest } = await importClient();
      await getAdminTest('123');
      expect(mockApi.get).toHaveBeenCalledWith('/admin/tests/123');
    });

    it('should create test', async () => {
      const { createTest } = await importClient();
      const testData = {
        title: 'New Test',
        categoryId: 'cat1',
        difficulty: 'EASY',
        questions: [],
      };
      await createTest(testData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/admin/tests', testData);
    });

    it('should update test', async () => {
      const { updateTest } = await importClient();
      const updateData = { title: 'Updated' };
      await updateTest('123', updateData);
      expect(mockApi.put).toHaveBeenCalledWith('/admin/tests/123', updateData);
    });

    it('should delete test', async () => {
      const { deleteTest } = await importClient();
      await deleteTest('123');
      expect(mockApi.delete).toHaveBeenCalledWith('/admin/tests/123');
    });
  });

  describe('Users', () => {
    it('should fetch admin users', async () => {
      const { getAdminUsers } = await importClient();
      await getAdminUsers();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/users', { params: { q: undefined, role: undefined } });
    });

    it('should fetch users with filters', async () => {
      const { getAdminUsers } = await importClient();
      await getAdminUsers({ query: 'john', role: 'admin' });
      expect(mockApi.get).toHaveBeenCalledWith('/admin/users', { params: { q: 'john', role: 'admin' } });
    });

    it('should fetch single user', async () => {
      const { getAdminUser } = await importClient();
      await getAdminUser('123');
      expect(mockApi.get).toHaveBeenCalledWith('/admin/users/123');
    });
  });

  describe('Analytics', () => {
    it('should fetch analytics', async () => {
      const { getAdminAnalytics } = await importClient();
      await getAdminAnalytics();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics');
    });

    it('should fetch daily activity with default days', async () => {
      const { getAdminDailyActivity } = await importClient();
      await getAdminDailyActivity();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/daily-activity', { params: { days: 7 } });
    });

    it('should fetch daily activity with custom days', async () => {
      const { getAdminDailyActivity } = await importClient();
      await getAdminDailyActivity(30);
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/daily-activity', { params: { days: 30 } });
    });

    it('should fetch level distribution', async () => {
      const { getAdminLevelDistribution } = await importClient();
      await getAdminLevelDistribution();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/levels');
    });

    it('should fetch popular tests', async () => {
      const { getPopularTests } = await importClient();
      await getPopularTests();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/popular-tests', { params: { limit: 5 } });
    });

    it('should fetch recent activity', async () => {
      const { getRecentActivity } = await importClient();
      await getRecentActivity(20);
      expect(mockApi.get).toHaveBeenCalledWith('/admin/analytics/recent-activity', { params: { limit: 20 } });
    });
  });

  describe('Media', () => {
    it('should upload media', async () => {
      const { uploadMedia } = await importClient();
      const file = new File(['content'], 'test.png', { type: 'image/png' });
      await uploadMedia(file, 'thumbnails');
      expect(mockApi.post).toHaveBeenCalledWith(
        '/admin/media/upload',
        expect.any(FormData),
        { headers: { 'Content-Type': 'multipart/form-data' } }
      );
    });

    it('should delete media', async () => {
      const { deleteMedia } = await importClient();
      await deleteMedia('http://example.com/image.png');
      expect(mockApi.delete).toHaveBeenCalledWith('/admin/media', { params: { url: 'http://example.com/image.png' } });
    });
  });

  describe('Questions', () => {
    it('should fetch questions by test', async () => {
      const { getQuestionsByTest } = await importClient();
      await getQuestionsByTest('test123');
      expect(mockApi.get).toHaveBeenCalledWith('/questions/test/test123/details');
    });

    it('should fetch question for admin', async () => {
      const { getQuestionForAdmin } = await importClient();
      await getQuestionForAdmin('q123');
      expect(mockApi.get).toHaveBeenCalledWith('/questions/q123/admin');
    });

    it('should create question', async () => {
      const { createQuestion } = await importClient();
      const questionData = { text: 'Q1', type: 'TEXT_SELECT' };
      await createQuestion(questionData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/questions', questionData);
    });

    it('should update question', async () => {
      const { updateQuestion } = await importClient();
      const updateData = { text: 'Updated Q' };
      await updateQuestion('q123', updateData as any);
      expect(mockApi.put).toHaveBeenCalledWith('/questions/q123', updateData);
    });

    it('should delete question', async () => {
      const { deleteQuestion } = await importClient();
      await deleteQuestion('q123');
      expect(mockApi.delete).toHaveBeenCalledWith('/questions/q123');
    });

    it('should duplicate question', async () => {
      const { duplicateQuestion } = await importClient();
      await duplicateQuestion('q123');
      expect(mockApi.post).toHaveBeenCalledWith('/questions/q123/duplicate');
    });

    it('should reorder questions', async () => {
      const { reorderQuestions } = await importClient();
      const reorderData = { testId: 't1', questionIds: ['q1', 'q2'] };
      await reorderQuestions(reorderData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/questions/reorder', reorderData);
    });
  });

  describe('Student Groups', () => {
    it('should fetch teacher groups', async () => {
      const { getTeacherGroups } = await importClient();
      await getTeacherGroups();
      expect(mockApi.get).toHaveBeenCalledWith('/groups/teacher/my-groups');
    });

    it('should fetch group detail', async () => {
      const { getGroupDetail } = await importClient();
      await getGroupDetail('g123');
      expect(mockApi.get).toHaveBeenCalledWith('/groups/g123/detail');
    });

    it('should create group', async () => {
      const { createGroup } = await importClient();
      const groupData = { name: 'Group 1' };
      await createGroup(groupData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/groups', groupData);
    });

    it('should update group', async () => {
      const { updateGroup } = await importClient();
      const updateData = { name: 'Updated' };
      await updateGroup('g123', updateData as any);
      expect(mockApi.put).toHaveBeenCalledWith('/groups/g123', updateData);
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
      await getPendingRequests('g123');
      expect(mockApi.get).toHaveBeenCalledWith('/groups/g123/join-requests');
    });

    it('should process join request', async () => {
      const { processJoinRequest } = await importClient();
      const processData = { action: 'approve' };
      await processJoinRequest('g123', 'r456', processData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/groups/g123/join-requests/r456', processData);
    });

    it('should fetch group progress', async () => {
      const { getGroupProgress } = await importClient();
      await getGroupProgress('g123');
      expect(mockApi.get).toHaveBeenCalledWith('/groups/g123/progress');
    });
  });

  describe('Audio Tests', () => {
    it('should fetch audio tests', async () => {
      const { getAudioTests } = await importClient();
      await getAudioTests();
      expect(mockApi.get).toHaveBeenCalledWith('/admin/audio-tests');
    });

    it('should fetch single audio test', async () => {
      const { getAudioTest } = await importClient();
      await getAudioTest('at123');
      expect(mockApi.get).toHaveBeenCalledWith('/admin/audio-tests/at123');
    });

    it('should create audio test', async () => {
      const { createAudioTest } = await importClient();
      const testData = { title: 'Audio Test', audioUrl: 'url' };
      await createAudioTest(testData as any);
      expect(mockApi.post).toHaveBeenCalledWith('/admin/audio-tests', testData);
    });

    it('should update audio test', async () => {
      const { updateAudioTest } = await importClient();
      const updateData = { title: 'Updated' };
      await updateAudioTest('at123', updateData as any);
      expect(mockApi.put).toHaveBeenCalledWith('/admin/audio-tests/at123', updateData);
    });

    it('should delete audio test', async () => {
      const { deleteAudioTest } = await importClient();
      await deleteAudioTest('at123');
      expect(mockApi.delete).toHaveBeenCalledWith('/admin/audio-tests/at123');
    });

    it('should publish audio test', async () => {
      const { publishAudioTest } = await importClient();
      await publishAudioTest('at123', true);
      expect(mockApi.patch).toHaveBeenCalledWith('/admin/audio-tests/at123/publish', { isPublished: true });
    });
  });
});

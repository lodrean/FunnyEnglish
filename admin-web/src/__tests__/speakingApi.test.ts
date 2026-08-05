/**
 * Контрактные тесты speakingApi: URL/params и маппинг backend ↔ UI-типы.
 * Backend (Part 1) отличается от спеки Part 3 — адаптер проверяем здесь.
 */
import { describe, it, expect, vi, beforeEach } from 'vitest';

const { getMock, postMock, putMock, deleteMock } = vi.hoisted(() => ({
  getMock: vi.fn(),
  postMock: vi.fn(),
  putMock: vi.fn(),
  deleteMock: vi.fn(),
}));

vi.mock('../api/client', () => ({
  default: { get: getMock, post: postMock, put: putMock, delete: deleteMock },
}));

import {
  getSpeakingLibraries,
  publishSpeakingLibrary,
  getSpeakingTopics,
  getAllSpeakingTopics,
  upsertTopicVideo,
  getSubmissions,
  createGrade,
  updateGrade,
  reorderTopicQuestions,
} from '../api/speakingApi';

const backendLibrary = {
  id: 'lib-1',
  title: 'Everyday English',
  description: 'desc',
  coverUrl: null,
  displayOrder: 0,
  isPublished: true,
  topicCount: 3,
  createdAt: '2026-07-20T10:00:00Z',
  updatedAt: '2026-07-21T10:00:00Z',
};

const backendTopic = {
  id: 'topic-1',
  libraryId: 'lib-1',
  title: 'Знакомство',
  description: null,
  displayOrder: 0,
  isPublished: true,
  isDeleted: false,
  video: { videoUrl: 'http://media/v.mp4', subtitleUrl: 'http://media/v.vtt', durationSeconds: 95 },
  questions: [{ id: 'q-1', text: 'Hi?', displayOrder: 0 }],
  createdAt: null,
  updatedAt: null,
};

const backendSubmission = {
  id: 'sub-1',
  userId: 'u-1',
  userEmail: 'demo@x.app',
  userDisplayName: 'Demo',
  topicId: 'topic-1',
  topicTitle: 'Знакомство',
  audioUrl: 'http://media/a.m4a',
  durationSec: 30,
  status: 'NEW',
  grade: null,
  createdAt: '2026-07-30T14:05:00Z',
};

const backendGrade = {
  grammar: 8,
  vocabulary: 7,
  pronunciation: 9,
  fluency: 6,
  total: '7.5',
  comment: 'ok',
  reviewerName: 'Admin',
  createdAt: '2026-07-30T15:00:00Z',
  updatedAt: '2026-07-30T16:00:00Z',
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe('speakingApi — libraries', () => {
  it('getSpeakingLibraries: GET /admin/speaking/libraries + маппинг title/topicCount', async () => {
    getMock.mockResolvedValue({ data: [backendLibrary] });
    const result = await getSpeakingLibraries();
    expect(getMock).toHaveBeenCalledWith('/admin/speaking/libraries');
    expect(result[0]).toMatchObject({
      id: 'lib-1',
      name: 'Everyday English',
      topicsCount: 3,
      isPublished: true,
    });
  });

  it('publishSpeakingLibrary: отдельного endpoint нет → PUT {isPublished}', async () => {
    putMock.mockResolvedValue({ data: backendLibrary });
    await publishSpeakingLibrary('lib-1', false);
    expect(putMock).toHaveBeenCalledWith('/admin/speaking/libraries/lib-1', { isPublished: false });
  });
});

describe('speakingApi — topics', () => {
  it('getSpeakingTopics: обязательный params.libraryId + flatten video', async () => {
    getMock.mockResolvedValue({ data: [backendTopic] });
    const result = await getSpeakingTopics('lib-1');
    expect(getMock).toHaveBeenCalledWith('/admin/speaking/topics', {
      params: { libraryId: 'lib-1' },
    });
    expect(result[0]).toMatchObject({
      name: 'Знакомство',
      videoUrl: 'http://media/v.mp4',
      subtitlesUrl: 'http://media/v.vtt',
      durationSeconds: 95,
      isArchived: false,
      questionsCount: 1,
    });
  });

  it('getAllSpeakingTopics: агрегация по всем темам + libraryName', async () => {
    getMock
      .mockResolvedValueOnce({ data: [backendLibrary] }) // libraries
      .mockResolvedValueOnce({ data: [backendTopic] }); // topics lib-1
    const result = await getAllSpeakingTopics();
    expect(result[0].libraryName).toBe('Everyday English');
  });

  it('upsertTopicVideo: PUT /topics/{id}/video с subtitleUrl', async () => {
    putMock.mockResolvedValue({ data: backendTopic });
    await upsertTopicVideo('topic-1', {
      videoUrl: 'http://media/v.mp4',
      subtitlesUrl: 'http://media/v.vtt',
      durationSeconds: 95,
    });
    expect(putMock).toHaveBeenCalledWith('/admin/speaking/topics/topic-1/video', {
      videoUrl: 'http://media/v.mp4',
      subtitleUrl: 'http://media/v.vtt',
      durationSeconds: 95,
    });
  });
});

describe('speakingApi — submissions', () => {
  it('getSubmissions: маппинг фильтров → dateFrom/dateTo, дефолты page=0/size=20, Spring Page', async () => {
    getMock.mockResolvedValue({
      data: {
        content: [backendSubmission],
        totalElements: 1,
        totalPages: 1,
        number: 0,
        size: 20,
      },
    });
    const result = await getSubmissions({ status: 'NEW', from: '2026-07-01', to: '2026-07-31' });
    expect(getMock).toHaveBeenCalledWith('/admin/speaking/submissions', {
      params: {
        status: 'NEW',
        userId: undefined,
        topicId: undefined,
        dateFrom: '2026-07-01',
        dateTo: '2026-07-31',
        page: 0,
        size: 20,
      },
    });
    expect(result.page).toBe(0);
    expect(result.content[0]).toMatchObject({
      student: { id: 'u-1', name: 'Demo', email: 'demo@x.app' },
      topic: { id: 'topic-1', name: 'Знакомство' },
      durationSeconds: 30,
      status: 'NEW',
      submittedAt: '2026-07-30T14:05:00Z',
    });
  });

  it('createGrade: POST grade, payload без totalScore, маппинг total→totalScore', async () => {
    postMock.mockResolvedValue({ data: backendGrade });
    const payload = { grammar: 8, vocabulary: 7, pronunciation: 9, fluency: 6, comment: 'ok' };
    const result = await createGrade('sub-1', payload);
    expect(postMock).toHaveBeenCalledWith('/admin/speaking/submissions/sub-1/grade', payload);
    expect(result.totalScore).toBe(7.5);
    expect(result.gradedAt).toBe('2026-07-30T15:00:00Z');
  });

  it('updateGrade: PUT grade', async () => {
    putMock.mockResolvedValue({ data: backendGrade });
    const payload = { grammar: 8, vocabulary: 7, pronunciation: 9, fluency: 10 };
    await updateGrade('sub-1', payload);
    expect(putMock).toHaveBeenCalledWith('/admin/speaking/submissions/sub-1/grade', payload);
  });
});

describe('speakingApi — reorder', () => {
  it('reorderTopicQuestions: PUT каждого вопроса с displayOrder = позиция', async () => {
    putMock.mockResolvedValue({ data: {} });
    const ordered = [
      { id: 'q-2', text: 'B', displayOrder: 0 },
      { id: 'q-1', text: 'A', displayOrder: 1 },
      { id: 'q-3', text: 'C', displayOrder: 2 },
    ];
    await reorderTopicQuestions('topic-1', ordered);
    expect(putMock).toHaveBeenCalledTimes(3);
    expect(putMock).toHaveBeenNthCalledWith(1, '/admin/speaking/questions/q-2', {
      text: 'B',
      displayOrder: 0,
    });
    expect(putMock).toHaveBeenNthCalledWith(2, '/admin/speaking/questions/q-1', {
      text: 'A',
      displayOrder: 1,
    });
    expect(putMock).toHaveBeenNthCalledWith(3, '/admin/speaking/questions/q-3', {
      text: 'C',
      displayOrder: 2,
    });
  });
});

/**
 * Общие моки и декораторы для Storybook-историй speaking-раздела (AW-T17).
 * Данные — через свежий QueryClient (retry: false) + setQueryData (без msw).
 */
import type { Decorator } from '@storybook/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from '../components/feedback';
import { speakingKeys } from '../hooks/useSpeaking';
import type {
  SpeakingLibrary,
  SpeakingSubmission,
  SpeakingTopic,
} from '../api/speakingApi';

// ==================== Мок-данные ====================

export const mockLibraries: SpeakingLibrary[] = [
  {
    id: 'lib-1',
    name: 'Everyday English',
    description: 'Диалоги на каждый день',
    coverUrl: undefined,
    displayOrder: 0,
    isPublished: true,
    topicsCount: 2,
    createdAt: '2026-07-20T10:00:00Z',
    updatedAt: '2026-07-25T10:00:00Z',
  },
  {
    id: 'lib-2',
    name: 'Business English',
    description: 'Встречи, переговоры, письма',
    coverUrl: undefined,
    displayOrder: 1,
    isPublished: false,
    topicsCount: 0,
    createdAt: '2026-07-21T10:00:00Z',
    updatedAt: '2026-07-21T10:00:00Z',
  },
  {
    id: 'lib-3',
    name: 'Travel English',
    description: 'Аэропорт, отель, ресторан',
    coverUrl: undefined,
    displayOrder: 2,
    isPublished: true,
    topicsCount: 1,
    createdAt: '2026-07-22T10:00:00Z',
    updatedAt: '2026-07-23T10:00:00Z',
  },
];

export const mockTopics: SpeakingTopic[] = [
  {
    id: 'topic-1',
    libraryId: 'lib-1',
    libraryName: 'Everyday English',
    name: 'Знакомство',
    description: 'Приветствия и small talk',
    videoUrl: 'https://example.com/videos/intro.mp4',
    subtitlesUrl: 'https://example.com/videos/intro.vtt',
    durationSeconds: 95,
    displayOrder: 0,
    isPublished: true,
    isArchived: false,
    questionsCount: 3,
    questions: [],
    createdAt: '2026-07-22T10:00:00Z',
    updatedAt: '2026-07-24T10:00:00Z',
  },
  {
    id: 'topic-2',
    libraryId: 'lib-1',
    libraryName: 'Everyday English',
    name: 'В кафе',
    videoUrl: 'https://example.com/videos/cafe.mp4',
    durationSeconds: 120,
    displayOrder: 1,
    isPublished: true,
    isArchived: false,
    questionsCount: 0, // warning «not playable»: опубликован без вопросов
    questions: [],
    createdAt: '2026-07-23T10:00:00Z',
    updatedAt: '2026-07-23T10:00:00Z',
  },
  {
    id: 'topic-3',
    libraryId: 'lib-3',
    libraryName: 'Travel English',
    name: 'В аэропорту',
    displayOrder: 0,
    isPublished: false,
    isArchived: true,
    questionsCount: 4,
    questions: [],
    createdAt: '2026-07-24T10:00:00Z',
    updatedAt: '2026-07-26T10:00:00Z',
  },
];

export const mockQuestions = [
  { id: 'q-1', text: 'What is your name and where are you from?', displayOrder: 0 },
  { id: 'q-2', text: 'What do you do for a living?', displayOrder: 1 },
  { id: 'q-3', text: 'What are your hobbies?', displayOrder: 2 },
];

export const mockSubmissionNew: SpeakingSubmission = {
  id: 'sub-1',
  student: { id: 'user-1', name: 'Demo Student', email: 'demo@sotospeak.app' },
  topic: { id: 'topic-1', name: 'Знакомство', libraryName: 'Everyday English' },
  audioUrl: 'https://example.com/audio/sub-1.m4a',
  durationSeconds: 30,
  status: 'NEW',
  submittedAt: '2026-07-30T14:05:00Z',
};

export const mockSubmissionReviewed: SpeakingSubmission = {
  ...mockSubmissionNew,
  id: 'sub-2',
  status: 'REVIEWED',
  submittedAt: '2026-07-29T09:12:00Z',
  grade: {
    grammar: 8,
    vocabulary: 7,
    pronunciation: 9,
    fluency: 6,
    totalScore: 7.5,
    comment: 'Хорошая работа! Обратите внимание на темп речи.',
    reviewerName: 'Admin',
    gradedAt: '2026-07-29T10:00:00Z',
    updatedAt: '2026-07-29T11:00:00Z',
  },
};

// ==================== Декоратор ====================

export interface SpeakingMockOptions {
  /** Пары [queryKey, data] для предзаполнения QueryClient */
  queries?: Array<[readonly unknown[], unknown]>;
  /** Путь роута для экрана (напр. '/speaking/libraries/:id/edit') — нужен при useParams */
  routePath?: string;
  /** Начальный URL (напр. '/speaking/libraries/lib-1/edit' или '/grading?status=NEW') */
  initialEntry?: string;
}

/**
 * Декоратор: свежий QueryClient (переопределяет общий из preview.tsx) + мок-данные
 * + MemoryRouter (+ Routes при routePath) + ToastProvider (useToast в экранах).
 */
export const withSpeakingMocks = (options: SpeakingMockOptions = {}): Decorator => {
  const { queries = [], routePath, initialEntry = '/' } = options;
  return (Story) => {
    const queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false, staleTime: Infinity } },
    });
    for (const [key, data] of queries) {
      queryClient.setQueryData([...key], data);
    }
    return (
      <QueryClientProvider client={queryClient}>
        <MemoryRouter initialEntries={[initialEntry]}>
          <ToastProvider>
            {routePath ? (
              <Routes>
                <Route path={routePath} element={<Story />} />
              </Routes>
            ) : (
              <Story />
            )}
          </ToastProvider>
        </MemoryRouter>
      </QueryClientProvider>
    );
  };
};

/** Готовые queryKey-записи для типовых наборов данных */
export const librariesQuery = (libs = mockLibraries) =>
  [speakingKeys.libraries, libs] as [readonly unknown[], unknown];

export const allTopicsQuery = (topics = mockTopics) =>
  [speakingKeys.topics(), topics] as [readonly unknown[], unknown];

export const questionsQuery = (topicId: string, questions = mockQuestions) =>
  [speakingKeys.questions(topicId), questions] as [readonly unknown[], unknown];

export const submissionsQuery = (content: SpeakingSubmission[]) =>
  [
    speakingKeys.submissions({
      status: 'NEW',
      userId: undefined,
      topicId: undefined,
      from: undefined,
      to: undefined,
      page: 0,
      size: 20,
    }),
    { content, totalElements: content.length, totalPages: 1, page: 0, size: 20 },
  ] as [readonly unknown[], unknown];

/** Лента NEW для кнопки «Пропустить» в GradingDetail (size 100 — ключ запроса skip) */
export const newSubmissionsQuery = (content: SpeakingSubmission[]) =>
  [
    speakingKeys.submissions({ status: 'NEW', page: 0, size: 100 }),
    { content, totalElements: content.length, totalPages: 1, page: 0, size: 100 },
  ] as [readonly unknown[], unknown];

export const adminUsersQuery = [
  ['admin', 'users', ''],
  [
    {
      id: 'user-1',
      email: 'demo@sotospeak.app',
      displayName: 'Demo Student',
      role: 'USER',
      level: 1,
      totalPoints: 0,
      currentStreak: 0,
      createdAt: '2026-07-01T10:00:00Z',
      stats: {},
    },
  ],
] as [readonly unknown[], unknown];

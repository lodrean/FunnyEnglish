/**
 * Speaking Trainer Admin API (SPEAKING-TRAINER-001, Фаза 3).
 *
 * ВАЖНО: backend-контракт (Part 1, реализован) отличается от спеки Part 3 §3 —
 * вся адаптация сосредоточена ЗДЕСЬ (спека §3.1: «правим здесь, а не в компонентах»):
 *  - publish через PUT {isPublished} (отдельных PATCH-endpoint'ов нет);
 *  - Library: name ← title, topicsCount ← topicCount;
 *  - Topic: name ← title, isArchived ← isDeleted, видео — вложенный объект video
 *    {videoUrl, subtitleUrl, durationSeconds}, upsert видео — PUT /topics/{id}/video;
 *  - GET /topics требует обязательный libraryId → «все топики» = агрегация по темам;
 *  - Submission: плоская структура (userId/userEmail/userDisplayName/topicId/topicTitle),
 *    durationSeconds ← durationSec, submittedAt ← createdAt; фильтры дат — dateFrom/dateTo;
 *    пагинация — Spring Page (page ← number);
 *  - Grade: totalScore ← total, gradedAt ← createdAt, есть reviewerName;
 *  - нет GET submissions/{id} и reorder-endpoint — см. useSpeaking.ts.
 */
import api from './client';

// ==================== Speaking Libraries ====================

export interface SpeakingLibrary {
  id: string;
  name: string;
  description?: string;
  coverUrl?: string;
  displayOrder: number;
  isPublished: boolean;
  topicsCount: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateLibraryRequest {
  name: string;
  description?: string;
  coverUrl?: string;
  displayOrder: number;
  isPublished: boolean;
}

export type UpdateLibraryRequest = Partial<CreateLibraryRequest>;

// ==================== Speaking Topics ====================

export interface SpeakingTopic {
  id: string;
  libraryId: string;
  libraryName?: string;
  name: string;
  description?: string;
  videoUrl?: string;
  subtitlesUrl?: string;
  durationSeconds?: number;
  displayOrder: number;
  isPublished: boolean;
  isArchived: boolean;
  questionsCount: number;
  /** Вопросы вложены в AdminTopicResponse (сортированы по displayOrder) */
  questions: SpeakingQuestion[];
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateTopicRequest {
  libraryId: string;
  name: string;
  description?: string;
  displayOrder: number;
  isPublished: boolean;
}

export type UpdateTopicRequest = Partial<Omit<CreateTopicRequest, 'libraryId'>>;

export interface UpsertTopicVideoRequest {
  videoUrl: string;
  subtitlesUrl?: string;
  durationSeconds: number;
}

// ==================== Speaking Questions ====================

export interface SpeakingQuestion {
  id: string;
  text: string;
  displayOrder: number;
}

export interface CreateSpeakingQuestionRequest {
  text: string;
  displayOrder: number;
}

// ==================== Grading (Submissions) ====================

export type SubmissionStatus = 'NEW' | 'REVIEWED';

export interface Grade {
  grammar: number;
  vocabulary: number;
  pronunciation: number;
  fluency: number;
  totalScore: number;
  comment?: string;
  reviewerName: string;
  gradedAt?: string;
  updatedAt?: string;
}

export interface SpeakingSubmission {
  id: string;
  student: {
    id: string;
    name: string;
    email: string;
  };
  topic: {
    id: string;
    name: string;
    libraryName?: string;
  };
  audioUrl: string;
  durationSeconds: number;
  status: SubmissionStatus;
  submittedAt?: string;
  grade?: Grade;
}

export interface SubmissionFilters {
  status?: SubmissionStatus;
  userId?: string;
  topicId?: string;
  from?: string; // ISO date 'yyyy-MM-dd'
  to?: string; // ISO date
  page?: number; // 0-based
  size?: number; // default 20
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface GradeRequest {
  grammar: number;
  vocabulary: number;
  pronunciation: number;
  fluency: number;
  comment?: string;
}

// ==================== Backend raw DTO (не экспортируем наружу) ====================

interface BackendLibrary {
  id: string;
  title: string;
  description?: string;
  coverUrl?: string;
  displayOrder: number;
  isPublished: boolean;
  topicCount: number;
  createdAt?: string;
  updatedAt?: string;
}

interface BackendVideo {
  videoUrl: string;
  subtitleUrl?: string;
  durationSeconds: number;
}

interface BackendTopic {
  id: string;
  libraryId: string;
  title: string;
  description?: string;
  displayOrder: number;
  isPublished: boolean;
  isDeleted: boolean;
  video?: BackendVideo;
  questions: SpeakingQuestion[];
  createdAt?: string;
  updatedAt?: string;
}

interface BackendGrade {
  grammar: number;
  vocabulary: number;
  pronunciation: number;
  fluency: number;
  total: number;
  comment?: string;
  reviewerName: string;
  createdAt?: string;
  updatedAt?: string;
}

interface BackendSubmission {
  id: string;
  userId: string;
  userEmail: string;
  userDisplayName: string;
  topicId: string;
  topicTitle: string;
  audioUrl: string;
  durationSec: number;
  status: SubmissionStatus;
  grade?: BackendGrade;
  createdAt?: string;
}

interface SpringPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

// ==================== Мапперы backend → UI-типы ====================

const mapLibrary = (raw: BackendLibrary): SpeakingLibrary => ({
  id: raw.id,
  name: raw.title,
  description: raw.description,
  coverUrl: raw.coverUrl,
  displayOrder: raw.displayOrder,
  isPublished: raw.isPublished,
  topicsCount: raw.topicCount,
  createdAt: raw.createdAt,
  updatedAt: raw.updatedAt,
});

const mapTopic = (raw: BackendTopic, libraryName?: string): SpeakingTopic => ({
  id: raw.id,
  libraryId: raw.libraryId,
  libraryName,
  name: raw.title,
  description: raw.description,
  videoUrl: raw.video?.videoUrl,
  subtitlesUrl: raw.video?.subtitleUrl,
  durationSeconds: raw.video?.durationSeconds,
  displayOrder: raw.displayOrder,
  isPublished: raw.isPublished,
  isArchived: raw.isDeleted,
  questionsCount: raw.questions?.length ?? 0,
  questions: [...(raw.questions ?? [])].sort((a, b) => a.displayOrder - b.displayOrder),
  createdAt: raw.createdAt,
  updatedAt: raw.updatedAt,
});

const mapGrade = (raw: BackendGrade): Grade => ({
  grammar: raw.grammar,
  vocabulary: raw.vocabulary,
  pronunciation: raw.pronunciation,
  fluency: raw.fluency,
  totalScore: Number(raw.total),
  comment: raw.comment,
  reviewerName: raw.reviewerName,
  gradedAt: raw.createdAt,
  updatedAt: raw.updatedAt,
});

const mapSubmission = (raw: BackendSubmission): SpeakingSubmission => ({
  id: raw.id,
  student: {
    id: raw.userId,
    name: raw.userDisplayName,
    email: raw.userEmail,
  },
  topic: {
    id: raw.topicId,
    name: raw.topicTitle,
  },
  audioUrl: raw.audioUrl,
  durationSeconds: raw.durationSec,
  status: raw.status,
  submittedAt: raw.createdAt,
  grade: raw.grade ? mapGrade(raw.grade) : undefined,
});

// ==================== Libraries (admin CRUD) ====================

export const getSpeakingLibraries = async (): Promise<SpeakingLibrary[]> => {
  const response = await api.get<BackendLibrary[]>('/admin/speaking/libraries');
  return response.data.map(mapLibrary);
};

export const createSpeakingLibrary = async (data: CreateLibraryRequest): Promise<SpeakingLibrary> => {
  const response = await api.post<BackendLibrary>('/admin/speaking/libraries', {
    title: data.name,
    description: data.description,
    coverUrl: data.coverUrl,
    displayOrder: data.displayOrder,
    isPublished: data.isPublished,
  });
  return mapLibrary(response.data);
};

export const updateSpeakingLibrary = async (id: string, data: UpdateLibraryRequest): Promise<SpeakingLibrary> => {
  const response = await api.put<BackendLibrary>(`/admin/speaking/libraries/${id}`, {
    ...(data.name !== undefined && { title: data.name }),
    ...(data.description !== undefined && { description: data.description }),
    ...(data.coverUrl !== undefined && { coverUrl: data.coverUrl }),
    ...(data.displayOrder !== undefined && { displayOrder: data.displayOrder }),
    ...(data.isPublished !== undefined && { isPublished: data.isPublished }),
  });
  return mapLibrary(response.data);
};

export const deleteSpeakingLibrary = async (id: string): Promise<void> => {
  await api.delete(`/admin/speaking/libraries/${id}`);
};

/** Отдельного publish-endpoint'а нет — publish через PUT {isPublished} */
export const publishSpeakingLibrary = async (id: string, isPublished: boolean): Promise<SpeakingLibrary> => {
  const response = await api.put<BackendLibrary>(`/admin/speaking/libraries/${id}`, { isPublished });
  return mapLibrary(response.data);
};

// ==================== Topics (admin CRUD) ====================

/** GET /topics требует обязательный libraryId */
export const getSpeakingTopics = async (libraryId: string): Promise<SpeakingTopic[]> => {
  const response = await api.get<BackendTopic[]>('/admin/speaking/topics', {
    params: { libraryId },
  });
  return response.data.map((t) => mapTopic(t));
};

/** Все топики всех тем (агрегация по libraries — обязательный libraryId на backend) */
export const getAllSpeakingTopics = async (): Promise<SpeakingTopic[]> => {
  const libraries = await getSpeakingLibraries();
  const perLibrary = await Promise.all(
    libraries.map(async (lib) => {
      const topics = await getSpeakingTopics(lib.id);
      return topics.map((t) => ({ ...t, libraryName: lib.name }));
    })
  );
  return perLibrary.flat();
};

export const createSpeakingTopic = async (data: CreateTopicRequest): Promise<SpeakingTopic> => {
  const response = await api.post<BackendTopic>('/admin/speaking/topics', {
    libraryId: data.libraryId,
    title: data.name,
    description: data.description,
    displayOrder: data.displayOrder,
    isPublished: data.isPublished,
  });
  return mapTopic(response.data);
};

export const updateSpeakingTopic = async (id: string, data: UpdateTopicRequest): Promise<SpeakingTopic> => {
  const response = await api.put<BackendTopic>(`/admin/speaking/topics/${id}`, {
    ...(data.name !== undefined && { title: data.name }),
    ...(data.description !== undefined && { description: data.description }),
    ...(data.displayOrder !== undefined && { displayOrder: data.displayOrder }),
    ...(data.isPublished !== undefined && { isPublished: data.isPublished }),
  });
  return mapTopic(response.data);
};

/** Видео/субтитры — отдельный upsert (videoUrl/subtitleUrl/durationSeconds) */
export const upsertTopicVideo = async (id: string, data: UpsertTopicVideoRequest): Promise<SpeakingTopic> => {
  const response = await api.put<BackendTopic>(`/admin/speaking/topics/${id}/video`, {
    videoUrl: data.videoUrl,
    subtitleUrl: data.subtitlesUrl,
    durationSeconds: data.durationSeconds,
  });
  return mapTopic(response.data);
};

export const deleteSpeakingTopic = async (id: string): Promise<void> => {
  // Backend делает soft delete → топик isDeleted, записи учеников сохраняются
  await api.delete(`/admin/speaking/topics/${id}`);
};

export const publishSpeakingTopic = async (id: string, isPublished: boolean): Promise<SpeakingTopic> => {
  const response = await api.put<BackendTopic>(`/admin/speaking/topics/${id}`, { isPublished });
  return mapTopic(response.data);
};

// ==================== Questions ====================

/**
 * Вопросы топика для админки. Публичный GET /public/speaking/topics/{id} НЕ подходит:
 * он отдаёт только опубликованные топики (404 для черновиков). Вопросы вложены в
 * AdminTopicResponse — берём из агрегированного списка.
 */
export const getTopicQuestions = async (topicId: string): Promise<SpeakingQuestion[]> => {
  const topics = await getAllSpeakingTopics();
  const topic = topics.find((t) => t.id === topicId);
  if (!topic) throw new Error('Топик не найден');
  return topic.questions;
};

export const createTopicQuestion = async (topicId: string, data: CreateSpeakingQuestionRequest): Promise<SpeakingQuestion> => {
  const response = await api.post<SpeakingQuestion>(`/admin/speaking/topics/${topicId}/questions`, data);
  return response.data;
};

export const updateTopicQuestion = async (id: string, data: CreateSpeakingQuestionRequest): Promise<SpeakingQuestion> => {
  const response = await api.put<SpeakingQuestion>(`/admin/speaking/questions/${id}`, data);
  return response.data;
};

export const deleteTopicQuestion = async (id: string): Promise<void> => {
  await api.delete(`/admin/speaking/questions/${id}`);
};

/**
 * Reorder-endpoint'а нет — последовательные PUT с displayOrder = позиция в списке.
 * Принимает полный упорядоченный список вопросов (id + text обязателен для PUT).
 * Шлём PUT для всех: компонент передаёт displayOrder уже перемапленным по индексу,
 * поэтому «пропустить неизменённые» по displayOrder !== index определить нельзя.
 */
export const reorderTopicQuestions = async (
  _topicId: string,
  orderedQuestions: SpeakingQuestion[]
): Promise<void> => {
  for (let i = 0; i < orderedQuestions.length; i++) {
    const q = orderedQuestions[i];
    await updateTopicQuestion(q.id, { text: q.text, displayOrder: i });
  }
};

// ==================== Grading ====================

export const getSubmissions = async (filters: SubmissionFilters = {}): Promise<PagedResponse<SpeakingSubmission>> => {
  const response = await api.get<SpringPage<BackendSubmission>>('/admin/speaking/submissions', {
    params: {
      status: filters.status,
      userId: filters.userId,
      topicId: filters.topicId,
      dateFrom: filters.from,
      dateTo: filters.to,
      page: filters.page ?? 0,
      size: filters.size ?? 20,
    },
  });
  const page = response.data;
  return {
    content: page.content.map(mapSubmission),
    totalElements: page.totalElements,
    totalPages: page.totalPages,
    page: page.number,
    size: page.size,
  };
};

export const createGrade = async (submissionId: string, data: GradeRequest): Promise<Grade> => {
  const response = await api.post<BackendGrade>(`/admin/speaking/submissions/${submissionId}/grade`, data);
  return mapGrade(response.data);
};

export const updateGrade = async (submissionId: string, data: GradeRequest): Promise<Grade> => {
  const response = await api.put<BackendGrade>(`/admin/speaking/submissions/${submissionId}/grade`, data);
  return mapGrade(response.data);
};

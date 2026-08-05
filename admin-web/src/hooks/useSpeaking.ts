/**
 * TanStack Query хуки speaking-раздела (SPEAKING_TRAINER_SPEC_PART3 §3.4, адаптировано
 * под фактический backend-контракт — см. комментарий в api/speakingApi.ts):
 *  - нет GET library/topic/submission by id → детали берём из кэша списков (select/find);
 *  - нет reorder-endpoint → useReorderQuestions делает цепочку PUT displayOrder.
 */
import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import {
  createGrade,
  createSpeakingLibrary,
  createSpeakingTopic,
  createTopicQuestion,
  deleteSpeakingLibrary,
  deleteSpeakingTopic,
  deleteTopicQuestion,
  getAllSpeakingTopics,
  getSpeakingLibraries,
  getSpeakingTopics,
  getSubmissions,
  getTopicQuestions,
  GradeRequest,
  publishSpeakingLibrary,
  publishSpeakingTopic,
  reorderTopicQuestions,
  SpeakingLibrary,
  SpeakingQuestion,
  SpeakingSubmission,
  SpeakingTopic,
  SubmissionFilters,
  updateGrade,
  updateSpeakingLibrary,
  updateSpeakingTopic,
  updateTopicQuestion,
  UpdateLibraryRequest,
  UpdateTopicRequest,
  upsertTopicVideo,
  UpsertTopicVideoRequest,
  CreateLibraryRequest,
  CreateTopicRequest,
  CreateSpeakingQuestionRequest,
} from '../api/speakingApi';

// Query keys — централизованно, для точечной инвалидации
export const speakingKeys = {
  libraries: ['speaking', 'libraries'] as const,
  library: (id: string) => ['speaking', 'libraries', id] as const,
  topics: (libraryId?: string) => ['speaking', 'topics', libraryId ?? 'all'] as const,
  topic: (id: string) => ['speaking', 'topics', 'detail', id] as const,
  questions: (topicId: string) => ['speaking', 'topics', topicId, 'questions'] as const,
  submissions: (filters: SubmissionFilters) => ['speaking', 'submissions', filters] as const,
  submissionsAll: ['speaking', 'submissions'] as const,
  submission: (id: string) => ['speaking', 'submissions', 'detail', id] as const,
};

// ==================== Libraries ====================

export const useSpeakingLibraries = () =>
  useQuery({ queryKey: speakingKeys.libraries, queryFn: getSpeakingLibraries });

/** Деталь темы — из кэша списка (GET /libraries/{id} на backend нет) */
export const useSpeakingLibrary = (id?: string) =>
  useQuery({
    queryKey: speakingKeys.libraries,
    queryFn: getSpeakingLibraries,
    enabled: !!id,
    select: (libs): SpeakingLibrary | undefined => libs.find((l) => l.id === id),
  });

export const useSaveLibrary = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id?: string; data: CreateLibraryRequest | UpdateLibraryRequest }) =>
      id
        ? updateSpeakingLibrary(id, data)
        : createSpeakingLibrary(data as CreateLibraryRequest),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: speakingKeys.libraries });
    },
  });
};

export const useDeleteLibrary = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteSpeakingLibrary(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: speakingKeys.libraries });
      queryClient.invalidateQueries({ queryKey: ['speaking', 'topics'] });
    },
  });
};

export const usePublishLibrary = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, isPublished }: { id: string; isPublished: boolean }) =>
      publishSpeakingLibrary(id, isPublished),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: speakingKeys.libraries });
    },
  });
};

// ==================== Topics ====================

/** libraryId задан → топики темы; не задан → все топики всех тем (агрегация) */
export const useSpeakingTopics = (libraryId?: string) =>
  useQuery({
    queryKey: speakingKeys.topics(libraryId),
    queryFn: () => (libraryId ? getSpeakingTopics(libraryId) : getAllSpeakingTopics()),
  });

/** Деталь топика — из кэша агрегированного списка (GET /topics/{id} на admin API нет) */
export const useSpeakingTopic = (id?: string) =>
  useQuery({
    queryKey: speakingKeys.topics(),
    queryFn: getAllSpeakingTopics,
    enabled: !!id,
    select: (topics): SpeakingTopic | undefined => topics.find((t) => t.id === id),
  });

const useInvalidateTopics = () => {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ['speaking', 'topics'] });
};

export const useSaveTopic = () => {
  const invalidate = useInvalidateTopics();
  return useMutation({
    mutationFn: ({ id, data }: { id?: string; data: CreateTopicRequest | UpdateTopicRequest }) =>
      id ? updateSpeakingTopic(id, data) : createSpeakingTopic(data as CreateTopicRequest),
    onSuccess: invalidate,
  });
};

export const useUpsertTopicVideo = () => {
  const invalidate = useInvalidateTopics();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpsertTopicVideoRequest }) =>
      upsertTopicVideo(id, data),
    onSuccess: invalidate,
  });
};

export const useDeleteTopic = () => {
  const invalidate = useInvalidateTopics();
  return useMutation({
    mutationFn: (id: string) => deleteSpeakingTopic(id),
    onSuccess: invalidate,
  });
};

export const usePublishTopic = () => {
  const invalidate = useInvalidateTopics();
  return useMutation({
    mutationFn: ({ id, isPublished }: { id: string; isPublished: boolean }) =>
      publishSpeakingTopic(id, isPublished),
    onSuccess: invalidate,
  });
};

// ==================== Questions ====================

export const useTopicQuestions = (topicId?: string) =>
  useQuery({
    queryKey: speakingKeys.questions(topicId ?? ''),
    queryFn: () => getTopicQuestions(topicId as string),
    enabled: !!topicId,
  });

const useInvalidateQuestions = (topicId: string) => {
  const queryClient = useQueryClient();
  return () => {
    queryClient.invalidateQueries({ queryKey: speakingKeys.questions(topicId) });
    // questionsCount живёт в списках топиков
    queryClient.invalidateQueries({ queryKey: ['speaking', 'topics'] });
  };
};

export const useSaveQuestion = (topicId: string) => {
  const invalidate = useInvalidateQuestions(topicId);
  return useMutation({
    mutationFn: ({ id, data }: { id?: string; data: CreateSpeakingQuestionRequest }) =>
      id ? updateTopicQuestion(id, data) : createTopicQuestion(topicId, data),
    onSuccess: invalidate,
  });
};

export const useDeleteQuestion = (topicId: string) => {
  const invalidate = useInvalidateQuestions(topicId);
  return useMutation({
    mutationFn: (questionId: string) => deleteTopicQuestion(questionId),
    onSuccess: invalidate,
  });
};

export const useReorderQuestions = (topicId: string) => {
  const invalidate = useInvalidateQuestions(topicId);
  return useMutation({
    mutationFn: (orderedQuestions: SpeakingQuestion[]) =>
      reorderTopicQuestions(topicId, orderedQuestions),
    onSuccess: invalidate,
  });
};

// ==================== Grading ====================

export const useSubmissions = (filters: SubmissionFilters) =>
  useQuery({
    queryKey: speakingKeys.submissions(filters),
    queryFn: () => getSubmissions(filters),
    placeholderData: keepPreviousData,
  });

/**
 * Деталь submission — из кэша списков inbox (GET /submissions/{id} на backend нет).
 * GradingDetail открывается из inbox → список уже закэширован.
 */
export const useSubmission = (id?: string) => {
  const queryClient = useQueryClient();
  return useQuery({
    queryKey: speakingKeys.submission(id ?? ''),
    enabled: !!id,
    staleTime: Infinity,
    queryFn: (): SpeakingSubmission => {
      const cached = queryClient.getQueriesData<{ content: SpeakingSubmission[] }>({
        queryKey: speakingKeys.submissionsAll,
      });
      for (const [, data] of cached) {
        const found = data?.content.find((s) => s.id === id);
        if (found) return found;
      }
      throw new Error('Запись не найдена в загруженном inbox — откройте её из списка');
    },
  });
};

export const useSaveGrade = (submissionId: string, mode: 'create' | 'edit') => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: GradeRequest) =>
      mode === 'create' ? createGrade(submissionId, data) : updateGrade(submissionId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: speakingKeys.submissionsAll });
      queryClient.invalidateQueries({ queryKey: speakingKeys.submission(submissionId) });
    },
  });
};

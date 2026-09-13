/**
 * Zod-схемы ответов API (bd b85.4).
 *
 * «Граница API»: каждый ответ client.ts парсится схемой — дрейф контрактов
 * бэкенда ловится сразу (ApiContractError с перечнем расхождений), а не как
 * undefined-поля глубоко в UI. Экстра-поля ответа разрешены (zod object по
 * умолчанию их игнорирует), проверяются только используемые клиентом поля.
 * Nullable-поля бэкенда (Jackson шлёт null) — .nullish().
 */
import { z } from 'zod';
import { logger } from '../utils/logger';

export class ApiContractError extends Error {
  constructor(
    readonly endpoint: string,
    readonly issues: z.ZodIssue[]
  ) {
    super(
      `API contract drift on ${endpoint}: ` +
        issues.map((i) => `${i.path.join('.') || '<root>'}: ${i.message}`).join('; ')
    );
    this.name = 'ApiContractError';
  }
}

/**
 * Парсит ответ схемой; при расхождении — лог и ApiContractError (fail-fast).
 * Возврат приводится к T объявленного типа: рантайм-проверка — здесь, compile-
 * время соответствие z.infer ↔ интерфейс типов — не цель (бэкенд шлёт null там,
 * где интерфейс допускает undefined; см. .nullish() в схемах).
 * В вызовах без контекстного типа (тесты) T задаётся явно: z.infer<typeof schema>.
 */
export function parseApi<T>(schema: z.ZodTypeAny, data: unknown, endpoint: string): T {
  const result = schema.safeParse(data);
  if (!result.success) {
    const err = new ApiContractError(endpoint, result.error.issues);
    logger.error('ApiClient', `[contract] ${err.message}`);
    throw err;
  }
  return result.data as T;
}

// ==================== Общие ====================

export const userSchema = z.object({
  id: z.string(),
  email: z.string(),
  displayName: z.string(),
  avatarUrl: z.string().nullish(),
  level: z.number(),
  totalPoints: z.number(),
  currentStreak: z.number(),
  role: z.string(),
  createdAt: z.string(),
});

export const userStatsSchema = z.object({
  testsCompleted: z.number(),
  totalStars: z.number(),
  perfectScores: z.number(),
  currentLevel: z.number(),
  pointsToNextLevel: z.number(),
});

// ==================== Auth ====================

export const authResponseSchema = z.object({
  token: z.string(),
  user: userSchema,
});

// ==================== Users (admin) ====================

export const adminUserSummarySchema = z.object({
  id: z.string(),
  email: z.string(),
  displayName: z.string(),
  avatarUrl: z.string().nullish(),
  role: z.string(),
  level: z.number(),
  totalPoints: z.number(),
  currentStreak: z.number(),
  createdAt: z.string(),
  stats: userStatsSchema,
});

/** Spring Page-контракт (number — 0-based страница) */
export function springPageSchema<T extends z.ZodTypeAny>(item: T) {
  return z.object({
    content: z.array(item),
    totalElements: z.number(),
    totalPages: z.number(),
    number: z.number(),
    size: z.number(),
  });
}

export const adminUserPageSchema = springPageSchema(adminUserSummarySchema);

export const achievementSchema = z.object({
  id: z.string(),
  code: z.string(),
  name: z.string(),
  description: z.string(),
  iconUrl: z.string().nullish(),
  pointsReward: z.number(),
  earned: z.boolean(),
});

export const categoryProgressSchema = z.object({
  categoryId: z.string(),
  categoryName: z.string(),
  testsCount: z.number(),
  completedCount: z.number(),
  totalStars: z.number(),
  maxStars: z.number(),
});

export const userProgressSummarySchema = z.object({
  totalTests: z.number(),
  completedTests: z.number(),
  totalStars: z.number(),
  maxPossibleStars: z.number(),
  categoriesProgress: z.array(categoryProgressSchema),
});

export const userProgressSchema = z.object({
  testId: z.string(),
  testTitle: z.string(),
  score: z.number(),
  maxScore: z.number(),
  stars: z.number(),
  attemptsCount: z.number(),
  bestScore: z.number(),
  completedAt: z.string(),
  lastAttemptAt: z.string(),
});

export const adminUserDetailSchema = z.object({
  user: userSchema,
  stats: userStatsSchema,
  achievements: z.array(achievementSchema),
  progressSummary: userProgressSummarySchema,
  progress: z.array(userProgressSchema),
});

// ==================== Analytics ====================

export const adminAnalyticsSchema = z.object({
  totalUsers: z.number(),
  totalTests: z.number(),
  publishedTests: z.number(),
  totalQuestions: z.number(),
  totalAnswers: z.number(),
  totalCompletions: z.number(),
  totalCategories: z.number(),
  totalAchievements: z.number(),
  topCategories: z.array(
    z.object({
      categoryId: z.string(),
      categoryName: z.string(),
      completions: z.number(),
    })
  ),
});

export const dailyActivitySchema = z.object({
  date: z.string(),
  newUsers: z.number(),
  testsCompleted: z.number(),
  achievementsEarned: z.number(),
});

export const levelDistributionSchema = z.object({
  level: z.number(),
  users: z.number(),
});

export const popularTestSchema = z.object({
  id: z.string(),
  name: z.string(),
  completions: z.number(),
  category: z.string(),
});

export const recentActivityItemSchema = z.object({
  userName: z.string(),
  type: z.string(),
  timestamp: z.string(),
  details: z.string().nullish(),
});

export const guestAnalyticsSchema = z.object({
  totalGuests: z.number(),
  activeGuests7d: z.number(),
  guestTestCompletions: z.number(),
  convertedGuests: z.number(),
  conversionRate: z.number(),
});

export const prdMetricsSchema = z.object({
  practiceSubmissionsLast7d: z.number(),
  activeStudentsLast7d: z.number(),
  practicePerStudentPerWeek: z.number(),
  reviewedTotal: z.number(),
  reviewedWithin48h: z.number(),
  reviewedWithin48hShare: z.number(),
  totalGuests: z.number(),
  convertedGuests: z.number(),
  guestConversionRate: z.number(),
});

// ==================== Messages ====================

export const adminMessageSchema = z.object({
  id: z.string(),
  senderId: z.string(),
  senderName: z.string(),
  recipientId: z.string(),
  text: z.string(),
  type: z.enum(['MESSAGE', 'COMMENT']),
  testId: z.string().nullish(),
  createdAt: z.string(),
  readAt: z.string().nullish(),
});

// ==================== Client Logs ====================

export const clientLogEntrySchema = z.object({
  id: z.string(),
  anonymousId: z.string().nullish(),
  level: z.enum(['WARN', 'ERROR']),
  tag: z.string(),
  message: z.string(),
  stackTrace: z.string().nullish(),
  platform: z.string(),
  appVersion: z.string().nullish(),
  clientTimestamp: z.string().nullish(),
  createdAt: z.string(),
});

export const clientLogsPageSchema = springPageSchema(clientLogEntrySchema);

// ==================== Settings ====================

export const adminSettingsSchema = z.object({
  s3Endpoint: z.string(),
  s3Bucket: z.string(),
  s3Region: z.string(),
  maxFileSize: z.string(),
  maxRequestSize: z.string(),
  corsAllowedOrigins: z.array(z.string()),
});

// ==================== Groups ====================

export const studentGroupSchema = z.object({
  id: z.string(),
  name: z.string(),
  description: z.string().nullish(),
  teacherId: z.string(),
  teacherName: z.string().nullish(),
  inviteCode: z.string(),
  maxStudents: z.number(),
  currentStudents: z.number(),
  isActive: z.boolean(),
  createdAt: z.string(),
});

export const groupMemberSchema = z.object({
  id: z.string(),
  userId: z.string(),
  displayName: z.string(),
  email: z.string(),
  avatarUrl: z.string().nullish(),
  joinedAt: z.string(),
  level: z.number(),
  totalPoints: z.number(),
  completedTests: z.number(),
  currentStreak: z.number(),
});

export const studentProgressSchema = z.object({
  userId: z.string(),
  displayName: z.string(),
  email: z.string(),
  avatarUrl: z.string().nullish(),
  level: z.number(),
  totalPoints: z.number(),
  currentStreak: z.number(),
  longestStreak: z.number(),
  completedTests: z.number(),
  averageScore: z.number(),
  totalTimeSpent: z.number(),
  achievementsCount: z.number(),
  lastActivityAt: z.string().nullish(),
  joinedAt: z.string(),
});

export const groupDetailSchema = z.object({
  id: z.string(),
  name: z.string(),
  description: z.string().nullish(),
  teacherId: z.string(),
  teacherName: z.string().nullish(),
  inviteCode: z.string(),
  maxStudents: z.number(),
  isActive: z.boolean(),
  createdAt: z.string(),
  members: z.array(groupMemberSchema),
  pendingRequests: z.number(),
});

export const joinRequestSchema = z.object({
  id: z.string(),
  userId: z.string(),
  userName: z.string(),
  userEmail: z.string(),
  requestedAt: z.string(),
});

export const groupProgressSummarySchema = z.object({
  groupId: z.string(),
  groupName: z.string(),
  totalStudents: z.number(),
  averageLevel: z.number(),
  averagePoints: z.number(),
  totalCompletedTests: z.number(),
  mostActiveStudents: z.array(studentProgressSchema),
  studentsNeedingAttention: z.array(studentProgressSchema),
});

// ==================== Speaking (admin, raw backend DTO) ====================
// Источник контракта — backend/src/main/kotlin/com/sotospeak/dto/SpeakingDtos.kt
// (AdminLibraryResponse / AdminTopicResponse / VideoResponse / GradeResponse /
// AdminSubmissionResponse), GradeAuditResponse.kt, GradingAnalyticsResponse.
// Адаптация raw → UI-типы — в speakingApi.ts (mapLibrary/mapTopic/mapGrade).

export const backendVideoSchema = z.object({
  videoUrl: z.string(),
  subtitleUrl: z.string().nullish(),
  durationSeconds: z.number(),
});

export const speakingQuestionSchema = z.object({
  id: z.string(),
  text: z.string(),
  displayOrder: z.number(),
});

export const backendLibrarySchema = z.object({
  id: z.string(),
  title: z.string(),
  description: z.string().nullish(),
  coverUrl: z.string().nullish(),
  displayOrder: z.number(),
  isPublished: z.boolean(),
  topicCount: z.number(),
  createdAt: z.string().nullish(),
  updatedAt: z.string().nullish(),
});

export const backendTopicSchema = z.object({
  id: z.string(),
  libraryId: z.string(),
  title: z.string(),
  description: z.string().nullish(),
  displayOrder: z.number(),
  isPublished: z.boolean(),
  isDeleted: z.boolean(),
  video: backendVideoSchema.nullish(),
  questions: z.array(speakingQuestionSchema),
  createdAt: z.string().nullish(),
  updatedAt: z.string().nullish(),
});

export const backendGradeSchema = z.object({
  grammar: z.number(),
  vocabulary: z.number(),
  pronunciation: z.number(),
  fluency: z.number(),
  /** BigDecimal в JSON — число (Spring Boot default, кастомизаций Jackson нет) */
  total: z.number(),
  comment: z.string().nullish(),
  reviewerName: z.string(),
  createdAt: z.string().nullish(),
  updatedAt: z.string().nullish(),
});

export const backendSubmissionSchema = z.object({
  id: z.string(),
  userId: z.string(),
  userEmail: z.string(),
  userDisplayName: z.string(),
  topicId: z.string(),
  topicTitle: z.string(),
  audioUrl: z.string(),
  durationSec: z.number(),
  status: z.enum(['NEW', 'REVIEWED']),
  grade: backendGradeSchema.nullish(),
  createdAt: z.string().nullish(),
});

export const gradeAuditEntrySchema = z.object({
  id: z.string(),
  submissionId: z.string(),
  action: z.enum(['CREATE', 'EDIT']),
  reviewerName: z.string(),
  grammar: z.number(),
  vocabulary: z.number(),
  pronunciation: z.number(),
  fluency: z.number(),
  total: z.number(),
  comment: z.string().nullish(),
  createdAt: z.string().nullish(),
});

export const gradingAnalyticsSchema = z.object({
  averages: z.object({
    grammar: z.number().nullable(),
    vocabulary: z.number().nullable(),
    pronunciation: z.number().nullable(),
    fluency: z.number().nullable(),
    total: z.number().nullable(),
  }),
  newCount: z.number(),
  reviewedCount: z.number(),
  avgReviewTimeMinutes: z.number().nullable(),
  byTopic: z.array(
    z.object({
      topicId: z.string(),
      topicTitle: z.string(),
      gradeCount: z.number(),
      avgTotal: z.number().nullable(),
    })
  ),
});

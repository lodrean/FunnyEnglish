export interface User {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  level: number;
  totalPoints: number;
  currentStreak: number;
  role: string;
  createdAt: string;
}

export interface AdminAnalytics {
  totalUsers: number;
  totalTests: number;
  publishedTests: number;
  totalQuestions: number;
  totalAnswers: number;
  totalCompletions: number;
  totalCategories: number;
  totalAchievements: number;
}

export interface AdminSettings {
  s3Endpoint: string;
  s3Bucket: string;
  s3Region: string;
  maxFileSize: string;
  maxRequestSize: string;
  corsAllowedOrigins: string[];
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  iconUrl?: string;
  testsCount: number;
}

export interface Test {
  id: string;
  categoryId: string;
  title: string;
  description?: string;
  thumbnailUrl?: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  pointsReward: number;
  timeLimitSeconds?: number;
  isPublished: boolean;
  displayOrder: number;
  questions: Question[];
}

export interface Question {
  id: string;
  type: QuestionType;
  text?: string;
  audioUrl?: string;
  imageUrl?: string;
  displayOrder: number;
  points: number;
  answers: Answer[];
}

export type QuestionType =
  | 'DRAG_DROP_IMAGE'
  | 'AUDIO_SELECT'
  | 'IMAGE_SELECT'
  | 'TEXT_SELECT'
  | 'FILL_BLANK';

export interface Answer {
  id: string;
  text?: string;
  imageUrl?: string;
  audioUrl?: string;
  isCorrect: boolean;
  displayOrder: number;
  matchTarget?: string;
}

export interface CreateTestRequest {
  categoryId: string;
  title: string;
  description?: string;
  thumbnailUrl?: string;
  difficulty: string;
  pointsReward: number;
  timeLimitSeconds?: number;
  isPublished: boolean;
  displayOrder: number;
  questions: CreateQuestionRequest[];
}

export interface CreateQuestionRequest {
  type: string;
  text?: string;
  audioUrl?: string;
  imageUrl?: string;
  displayOrder: number;
  points: number;
  answers: CreateAnswerRequest[];
}

export interface CreateAnswerRequest {
  text?: string;
  imageUrl?: string;
  audioUrl?: string;
  isCorrect: boolean;
  displayOrder: number;
  matchTarget?: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UserStats {
  testsCompleted: number;
  totalStars: number;
  perfectScores: number;
  currentLevel: number;
  pointsToNextLevel: number;
}

export interface Achievement {
  id: string;
  code: string;
  name: string;
  description: string;
  iconUrl?: string;
  pointsReward: number;
  earned: boolean;
}

export interface CategoryProgress {
  categoryId: string;
  categoryName: string;
  testsCount: number;
  completedCount: number;
  totalStars: number;
  maxStars: number;
}

export interface UserProgressSummary {
  totalTests: number;
  completedTests: number;
  totalStars: number;
  maxPossibleStars: number;
  categoriesProgress: CategoryProgress[];
}

export interface UserProgress {
  testId: string;
  testTitle: string;
  score: number;
  maxScore: number;
  stars: number;
  attemptsCount: number;
  bestScore: number;
  completedAt: string;
  lastAttemptAt: string;
}

export interface AdminUserSummary {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  role: string;
  level: number;
  totalPoints: number;
  currentStreak: number;
  createdAt: string;
  stats: UserStats;
}

export interface AdminUserDetail {
  user: User;
  stats: UserStats;
  achievements: Achievement[];
  progressSummary: UserProgressSummary;
  progress: UserProgress[];
}

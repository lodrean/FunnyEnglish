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

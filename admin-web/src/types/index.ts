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
  topCategories: CategoryCompletion[];
}

export interface CategoryCompletion {
  categoryId: string;
  categoryName: string;
  completions: number;
}

export interface DailyActivity {
  date: string;
  newUsers: number;
  testsCompleted: number;
  achievementsEarned: number;
}

export interface LevelDistribution {
  level: number;
  users: number;
}

export interface PopularTest {
  id: string;
  name: string;
  completions: number;
}

export interface RecentActivityItem {
  userName: string;
  type: string;
  timestamp: string;
  details?: string;
}

export interface AdminSettings {
  s3Endpoint: string;
  s3Bucket: string;
  s3Region: string;
  maxFileSize: string;
  maxRequestSize: string;
  corsAllowedOrigins: string[];
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

// ==================== Student Groups ====================

export interface StudentGroup {
  id: string;
  name: string;
  description?: string;
  teacherId: string;
  teacherName?: string;
  inviteCode: string;
  maxStudents: number;
  currentStudents: number;
  isActive: boolean;
  createdAt: string;
}

export interface GroupDetail {
  id: string;
  name: string;
  description?: string;
  teacherId: string;
  teacherName?: string;
  inviteCode: string;
  maxStudents: number;
  isActive: boolean;
  createdAt: string;
  members: GroupMember[];
  pendingRequests: number;
}

export interface GroupMember {
  id: string;
  userId: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
  joinedAt: string;
  level: number;
  totalPoints: number;
  completedTests: number;
  currentStreak: number;
}

export interface JoinRequest {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  requestedAt: string;
}

export interface CreateGroupRequest {
  name: string;
  description?: string;
  maxStudents?: number;
}

export interface UpdateGroupRequest {
  name?: string;
  description?: string;
  maxStudents?: number;
  isActive?: boolean;
}

export interface ProcessJoinRequest {
  approve: boolean;
}

export interface StudentProgress {
  userId: string;
  displayName: string;
  email: string;
  avatarUrl?: string;
  level: number;
  totalPoints: number;
  currentStreak: number;
  longestStreak: number;
  completedTests: number;
  averageScore: number;
  totalTimeSpent: number; // in minutes
  achievementsCount: number;
  lastActivityAt?: string;
  joinedAt: string;
}

export interface GroupProgressSummary {
  groupId: string;
  groupName: string;
  totalStudents: number;
  averageLevel: number;
  averagePoints: number;
  totalCompletedTests: number;
  mostActiveStudents: StudentProgress[];
  studentsNeedingAttention: StudentProgress[];
}

export interface User {
  name: string;
  avatar: string;
  level: number;
  xp: number;
  streak: number;
  longestStreak: number;
  lessonsCompleted: number;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  color: string;
  gradient: string;
}

export interface Question {
  id: string;
  image: string;
  question: string;
  options: string[];
  correctAnswer: number;
}

export interface Achievement {
  id: string;
  icon: string;
  title: string;
  description: string;
  unlocked: boolean;
  xpReward: number;
}

export interface Quest {
  id: string;
  title: string;
  progress: number;
  total: number;
  completed: boolean;
}

export interface LeaderboardEntry {
  rank: number;
  name: string;
  avatar: string;
  xp: number;
  isCurrentUser: boolean;
}

export type Screen = 'home' | 'test' | 'profile' | 'achievements' | 'leaderboard';

export type Theme = 'light' | 'dark';

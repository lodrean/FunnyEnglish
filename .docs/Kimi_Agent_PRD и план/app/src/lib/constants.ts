import type { Category, Question, Achievement, Quest, LeaderboardEntry, User } from '@/types';

export const CATEGORIES: Category[] = [
  {
    id: 'animals',
    name: 'Животные',
    icon: '🐾',
    color: '#22C55E',
    gradient: 'from-green-400 to-green-600',
  },
  {
    id: 'colors',
    name: 'Цвета',
    icon: '🎨',
    color: '#8B5CF6',
    gradient: 'from-purple-400 to-purple-600',
  },
  {
    id: 'numbers',
    name: 'Числа',
    icon: '🔢',
    color: '#3B82F6',
    gradient: 'from-blue-400 to-blue-600',
  },
  {
    id: 'home',
    name: 'Дом',
    icon: '🏠',
    color: '#F97316',
    gradient: 'from-orange-400 to-orange-600',
  },
  {
    id: 'food',
    name: 'Еда',
    icon: '🍎',
    color: '#EF4444',
    gradient: 'from-red-400 to-red-600',
  },
  {
    id: 'travel',
    name: 'Путешествия',
    icon: '🚀',
    color: '#14B8A6',
    gradient: 'from-teal-400 to-teal-600',
  },
];

export const QUESTIONS: Question[] = [
  {
    id: '1',
    image: '/images/elephant.png',
    question: 'Как это по-английски?',
    options: ['Lion', 'Elephant', 'Giraffe'],
    correctAnswer: 1,
  },
  {
    id: '2',
    image: '/images/lion.png',
    question: 'Как это по-английски?',
    options: ['Lion', 'Tiger', 'Leopard'],
    correctAnswer: 0,
  },
  {
    id: '3',
    image: '/images/giraffe.png',
    question: 'Как это по-английски?',
    options: ['Horse', 'Giraffe', 'Zebra'],
    correctAnswer: 1,
  },
  {
    id: '4',
    image: '/images/cat.png',
    question: 'Как это по-английски?',
    options: ['Dog', 'Cat', 'Rabbit'],
    correctAnswer: 1,
  },
  {
    id: '5',
    image: '/images/dog.png',
    question: 'Как это по-английски?',
    options: ['Dog', 'Wolf', 'Fox'],
    correctAnswer: 0,
  },
  {
    id: '6',
    image: '/images/bird.png',
    question: 'Как это по-английски?',
    options: ['Eagle', 'Bird', 'Sparrow'],
    correctAnswer: 1,
  },
];

export const ACHIEVEMENTS: Achievement[] = [
  {
    id: '1',
    icon: '🏆',
    title: 'Начинающий лингвист',
    description: 'Пройди 10 уроков',
    unlocked: true,
    xpReward: 50,
  },
  {
    id: '2',
    icon: '🔥',
    title: 'Серийный ученик',
    description: '7 дней подряд',
    unlocked: true,
    xpReward: 100,
  },
  {
    id: '3',
    icon: '⭐',
    title: 'XP Мастер',
    description: 'Накопи 1000 XP',
    unlocked: true,
    xpReward: 200,
  },
  {
    id: '4',
    icon: '🎯',
    title: 'Идеальная серия',
    description: '10 правильных ответов подряд',
    unlocked: false,
    xpReward: 150,
  },
  {
    id: '5',
    icon: '📚',
    title: 'Книжный червь',
    description: 'Пройди 50 уроков',
    unlocked: false,
    xpReward: 500,
  },
  {
    id: '6',
    icon: '💎',
    title: 'Мастер английского',
    description: 'Достигни уровня 10',
    unlocked: false,
    xpReward: 1000,
  },
];

export const QUESTS: Quest[] = [
  {
    id: '1',
    title: 'Пройди 3 урока',
    progress: 3,
    total: 3,
    completed: true,
  },
  {
    id: '2',
    title: 'Занимайся 10 минут',
    progress: 10,
    total: 10,
    completed: true,
  },
  {
    id: '3',
    title: 'Выучи 5 новых слов',
    progress: 2,
    total: 5,
    completed: false,
  },
];

export const LEADERBOARD: LeaderboardEntry[] = [
  { rank: 1, name: 'Alex', avatar: '/images/avatar.png', xp: 1250, isCurrentUser: false },
  { rank: 2, name: 'Sam', avatar: '/images/avatar.png', xp: 980, isCurrentUser: false },
  { rank: 3, name: 'Kim', avatar: '/images/avatar.png', xp: 875, isCurrentUser: false },
  { rank: 4, name: 'You', avatar: '/images/avatar.png', xp: 720, isCurrentUser: true },
  { rank: 5, name: 'Tom', avatar: '/images/avatar.png', xp: 650, isCurrentUser: false },
];

export const CURRENT_USER: User = {
  name: 'Алекс',
  avatar: '/images/avatar.png',
  level: 5,
  xp: 1250,
  streak: 12,
  longestStreak: 15,
  lessonsCompleted: 48,
};

export const DAILY_PROGRESS = {
  completed: 3,
  total: 5,
  percentage: 60,
};

export const CONTINUE_LESSON = {
  category: 'Дом',
  icon: '🏠',
  progress: 75,
  image: '/images/house.png',
};

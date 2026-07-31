import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import type { Screen, Achievement } from '@/types';
import { BottomNav } from '@/components/navigation/bottom-nav';
import { DailyProgressCard } from '@/components/cards/daily-progress-card';
import { CategoriesCarousel } from '@/components/cards/category-card';
import { ContinueLearningCard } from '@/components/cards/continue-learning-card';
import { StreakWidget } from '@/components/gamification/streak-widget';
import { QuestsWidget } from '@/components/gamification/quest-card';
import { LevelProgress } from '@/components/gamification/level-progress';
import { AchievementBadge } from '@/components/gamification/achievement-badge';
import { AchievementModal } from '@/components/gamification/achievement-modal';
import { QuestionCard } from '@/components/cards/question-card';
import { AnswerFeedback } from '@/components/feedback/answer-feedback';
import { StatCard } from '@/components/cards/stat-card';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { 
  Flame, 
  Star, 
  Trophy, 
  BookOpen, 
  ArrowLeft,
  Settings,
  Moon,
  Sun,
  Medal,
  Crown
} from 'lucide-react';
import { 
  CATEGORIES, 
  QUESTIONS, 
  ACHIEVEMENTS, 
  QUESTS, 
  LEADERBOARD,
  CURRENT_USER,
  DAILY_PROGRESS,
  CONTINUE_LESSON 
} from '@/lib/constants';
import { pageTransition } from '@/lib/animations';

function HomeScreen({ onNavigate }: { onNavigate: (screen: Screen) => void }) {
  return (
    <motion.div
      className="space-y-6 pb-24"
      initial="initial"
      animate="animate"
      exit="exit"
      variants={pageTransition}
    >
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <motion.h1
            className="text-2xl font-bold"
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
          >
            Привет, {CURRENT_USER.name}! 👋
          </motion.h1>
          <motion.p
            className="text-muted-foreground"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.1 }}
          >
            Готов учиться сегодня?
          </motion.p>
        </div>
        <motion.div
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ type: 'spring', delay: 0.2 }}
        >
          <Avatar className="w-12 h-12 border-2 border-primary">
            <AvatarImage src={CURRENT_USER.avatar} />
            <AvatarFallback>{CURRENT_USER.name[0]}</AvatarFallback>
          </Avatar>
        </motion.div>
      </div>

      {/* Daily Progress */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <DailyProgressCard
          completed={DAILY_PROGRESS.completed}
          total={DAILY_PROGRESS.total}
          percentage={DAILY_PROGRESS.percentage}
        />
      </motion.div>

      {/* Streak & Quests */}
      <div className="grid grid-cols-2 gap-4">
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2 }}
        >
          <StreakWidget streak={CURRENT_USER.streak} />
        </motion.div>
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.2 }}
        >
          <QuestsWidget quests={QUESTS} />
        </motion.div>
      </div>

      {/* Categories */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <CategoriesCarousel
          categories={CATEGORIES}
          onCategoryClick={() => onNavigate('test')}
        />
      </motion.div>

      {/* Continue Learning */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
      >
        <ContinueLearningCard
          category={CONTINUE_LESSON.category}
          icon={CONTINUE_LESSON.icon}
          progress={CONTINUE_LESSON.progress}
          image={CONTINUE_LESSON.image}
          onContinue={() => onNavigate('test')}
        />
      </motion.div>
    </motion.div>
  );
}

function TestScreen({ onBack }: { onBack: () => void }) {
  const [currentQuestion, setCurrentQuestion] = useState(0);
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [showFeedback, setShowFeedback] = useState(false);
  const [isCorrect, setIsCorrect] = useState(false);
  const [score, setScore] = useState(0);

  const question = QUESTIONS[currentQuestion];

  const handleAnswer = (index: number) => {
    if (selectedAnswer !== null) return;
    
    setSelectedAnswer(index);
    const correct = index === question.correctAnswer;
    setIsCorrect(correct);
    setShowFeedback(true);
    
    if (correct) {
      setScore(score + 10);
    }
  };

  const handleContinue = () => {
    setShowFeedback(false);
    setSelectedAnswer(null);
    
    if (currentQuestion < QUESTIONS.length - 1) {
      setCurrentQuestion(currentQuestion + 1);
    } else {
      // Test completed
      setCurrentQuestion(0);
      onBack();
    }
  };

  const progress = ((currentQuestion + 1) / QUESTIONS.length) * 100;

  return (
    <motion.div
      className="min-h-screen pb-24"
      initial="initial"
      animate="animate"
      exit="exit"
      variants={pageTransition}
    >
      {/* Header */}
      <div className="flex items-center gap-4 mb-6">
        <motion.button
          onClick={onBack}
          className="p-2 rounded-full hover:bg-muted"
          whileTap={{ scale: 0.9 }}
        >
          <ArrowLeft className="w-6 h-6" />
        </motion.button>
        <div className="flex-1">
          <h1 className="font-bold text-lg">Тест: Животные</h1>
          <div className="flex items-center gap-2 mt-1">
            <Progress value={progress} className="h-2 flex-1" />
            <span className="text-sm text-muted-foreground">
              {currentQuestion + 1}/{QUESTIONS.length}
            </span>
          </div>
        </div>
      </div>

      {/* Question */}
      <div className="space-y-8">
        <motion.div
          className="flex justify-center"
          animate={{ y: [0, -10, 0] }}
          transition={{ duration: 3, repeat: Infinity, ease: 'easeInOut' }}
        >
          <img
            src={question.image}
            alt="Question"
            className="w-48 h-48 object-contain"
          />
        </motion.div>

        <motion.h2
          className="text-xl font-bold text-center"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          key={question.id}
        >
          {question.question}
        </motion.h2>

        <div className="space-y-3">
          {question.options.map((option, index) => (
            <QuestionCard
              key={index}
              option={option}
              isSelected={selectedAnswer === index}
              isCorrect={
                selectedAnswer === null
                  ? null
                  : index === question.correctAnswer
              }
              onClick={() => handleAnswer(index)}
              disabled={selectedAnswer !== null}
            />
          ))}
        </div>
      </div>

      {/* Feedback */}
      <AnimatePresence>
        {showFeedback && (
          <AnswerFeedback
            isCorrect={isCorrect}
            correctAnswer={question.options[question.correctAnswer]}
            xpGain={10}
            onContinue={handleContinue}
          />
        )}
      </AnimatePresence>
    </motion.div>
  );
}

function ProfileScreen({ onBack }: { onBack: () => void }) {
  const [showAchievement, setShowAchievement] = useState<Achievement | null>(null);

  return (
    <motion.div
      className="space-y-6 pb-24"
      initial="initial"
      animate="animate"
      exit="exit"
      variants={pageTransition}
    >
      {/* Header */}
      <div className="flex items-center gap-4">
        <motion.button
          onClick={onBack}
          className="p-2 rounded-full hover:bg-muted"
          whileTap={{ scale: 0.9 }}
        >
          <ArrowLeft className="w-6 h-6" />
        </motion.button>
        <h1 className="text-xl font-bold">Профиль</h1>
        <div className="flex-1" />
        <Button variant="ghost" size="icon">
          <Settings className="w-5 h-5" />
        </Button>
      </div>

      {/* Profile Header */}
      <motion.div
        className="text-center"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <motion.div
          className="relative inline-block"
          whileHover={{ scale: 1.05 }}
        >
          <Avatar className="w-24 h-24 border-4 border-primary mx-auto">
            <AvatarImage src={CURRENT_USER.avatar} />
            <AvatarFallback className="text-2xl">{CURRENT_USER.name[0]}</AvatarFallback>
          </Avatar>
          <div className="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-accent-purple text-white text-xs font-bold px-3 py-1 rounded-full">
            LVL {CURRENT_USER.level}
          </div>
        </motion.div>
        <h2 className="text-2xl font-bold mt-4">{CURRENT_USER.name}</h2>
      </motion.div>

      {/* Stats */}
      <motion.div
        className="grid grid-cols-2 gap-4"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <StatCard
          icon={Flame}
          value={CURRENT_USER.streak}
          label="Текущая серия"
          color="text-streak"
        />
        <StatCard
          icon={Star}
          value={CURRENT_USER.xp}
          label="Всего XP"
          color="text-xp-gold"
        />
        <StatCard
          icon={Trophy}
          value={CURRENT_USER.longestStreak}
          label="Лучшая серия"
          color="text-accent-purple"
        />
        <StatCard
          icon={BookOpen}
          value={CURRENT_USER.lessonsCompleted}
          label="Уроков пройдено"
          color="text-primary"
        />
      </motion.div>

      {/* Level Progress */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <LevelProgress
          level={CURRENT_USER.level}
          progress={250}
          total={500}
        />
      </motion.div>

      {/* Achievements */}
      <motion.div
        className="space-y-3"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <h3 className="font-bold text-lg flex items-center gap-2">
          <Trophy className="w-5 h-5" />
          Достижения
        </h3>
        <div className="space-y-3">
          {ACHIEVEMENTS.slice(0, 3).map((achievement) => (
            <AchievementBadge
              key={achievement.id}
              achievement={achievement}
              onClick={() => setShowAchievement(achievement)}
            />
          ))}
        </div>
        <Button variant="outline" className="w-full">
          Все достижения
        </Button>
      </motion.div>

      {/* Achievement Modal */}
      <AchievementModal
        achievement={showAchievement}
        isOpen={showAchievement !== null}
        onClose={() => setShowAchievement(null)}
      />
    </motion.div>
  );
}

function LeaderboardScreen({ onBack }: { onBack: () => void }) {
  return (
    <motion.div
      className="space-y-6 pb-24"
      initial="initial"
      animate="animate"
      exit="exit"
      variants={pageTransition}
    >
      {/* Header */}
      <div className="flex items-center gap-4">
        <motion.button
          onClick={onBack}
          className="p-2 rounded-full hover:bg-muted"
          whileTap={{ scale: 0.9 }}
        >
          <ArrowLeft className="w-6 h-6" />
        </motion.button>
        <h1 className="text-xl font-bold">Рейтинг</h1>
      </div>

      {/* Top 3 Podium */}
      <motion.div
        className="flex items-end justify-center gap-4 py-4"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        {/* 2nd place */}
        <motion.div
          className="flex flex-col items-center"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
        >
          <Avatar className="w-14 h-14 border-2 border-gray-400">
            <AvatarImage src={LEADERBOARD[1].avatar} />
          </Avatar>
          <div className="w-16 h-20 bg-gray-200 dark:bg-gray-700 rounded-t-lg mt-2 flex items-center justify-center">
            <span className="text-2xl font-bold text-gray-600 dark:text-gray-400">2</span>
          </div>
          <Medal className="w-6 h-6 text-gray-400 -mt-3" />
        </motion.div>

        {/* 1st place */}
        <motion.div
          className="flex flex-col items-center"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0 }}
        >
          <Crown className="w-8 h-8 text-yellow-500 mb-1" />
          <Avatar className="w-16 h-16 border-4 border-yellow-400">
            <AvatarImage src={LEADERBOARD[0].avatar} />
          </Avatar>
          <div className="w-20 h-28 bg-gradient-to-t from-yellow-200 to-yellow-100 dark:from-yellow-900 dark:to-yellow-800 rounded-t-lg mt-2 flex items-center justify-center">
            <span className="text-3xl font-bold text-yellow-600 dark:text-yellow-400">1</span>
          </div>
        </motion.div>

        {/* 3rd place */}
        <motion.div
          className="flex flex-col items-center"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <Avatar className="w-14 h-14 border-2 border-amber-600">
            <AvatarImage src={LEADERBOARD[2].avatar} />
          </Avatar>
          <div className="w-16 h-16 bg-amber-700/20 dark:bg-amber-900/40 rounded-t-lg mt-2 flex items-center justify-center">
            <span className="text-2xl font-bold text-amber-700 dark:text-amber-500">3</span>
          </div>
          <Medal className="w-6 h-6 text-amber-700 -mt-3" />
        </motion.div>
      </motion.div>

      {/* Leaderboard List */}
      <motion.div
        className="space-y-2"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.3 }}
      >
        {LEADERBOARD.map((entry, index) => (
          <motion.div
            key={entry.name}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.3 + index * 0.05 }}
          >
            <Card
              className={`p-4 flex items-center gap-4 ${
                entry.isCurrentUser
                  ? 'bg-primary/10 border-primary'
                  : ''
              }`}
            >
              <span
                className={`w-8 text-center font-bold ${
                  entry.rank <= 3 ? 'text-lg' : 'text-muted-foreground'
                }`}
              >
                {entry.rank}
              </span>
              <Avatar className="w-10 h-10">
                <AvatarImage src={entry.avatar} />
              </Avatar>
              <div className="flex-1">
                <span className="font-bold">{entry.name}</span>
                {entry.isCurrentUser && (
                  <span className="ml-2 text-xs text-primary">(Вы)</span>
                )}
              </div>
              <div className="flex items-center gap-1 text-xp-gold font-bold">
                <Star className="w-4 h-4 fill-xp-gold" />
                {entry.xp}
              </div>
            </Card>
          </motion.div>
        ))}
      </motion.div>
    </motion.div>
  );
}

function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('home');
  const [isDark, setIsDark] = useState(false);

  const toggleTheme = () => {
    setIsDark(!isDark);
    document.documentElement.classList.toggle('dark');
  };

  const renderScreen = () => {
    switch (currentScreen) {
      case 'home':
        return <HomeScreen onNavigate={setCurrentScreen} />;
      case 'test':
        return <TestScreen onBack={() => setCurrentScreen('home')} />;
      case 'profile':
        return <ProfileScreen onBack={() => setCurrentScreen('home')} />;
      case 'leaderboard':
        return <LeaderboardScreen onBack={() => setCurrentScreen('home')} />;
      default:
        return <HomeScreen onNavigate={setCurrentScreen} />;
    }
  };

  return (
    <div className={`min-h-screen bg-background ${isDark ? 'dark' : ''}`}>
      <div className="max-w-lg mx-auto px-4 py-6">
        {/* Theme Toggle */}
        <motion.button
          onClick={toggleTheme}
          className="fixed top-4 right-4 p-2 rounded-full bg-muted z-50"
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
        >
          {isDark ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
        </motion.button>

        <AnimatePresence mode="wait">
          <motion.div
            key={currentScreen}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.3 }}
          >
            {renderScreen()}
          </motion.div>
        </AnimatePresence>
      </div>

      {/* Bottom Navigation */}
      <BottomNav currentScreen={currentScreen} onNavigate={setCurrentScreen} />
    </div>
  );
}

export default App;

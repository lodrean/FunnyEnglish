import { motion } from 'framer-motion';
import { Home, BookOpen, Target, Users, User } from 'lucide-react';
import type { Screen } from '@/types';

interface BottomNavProps {
  currentScreen: Screen;
  onNavigate: (screen: Screen) => void;
}

const navItems: { screen: Screen; icon: typeof Home; label: string }[] = [
  { screen: 'home', icon: Home, label: 'Главная' },
  { screen: 'test', icon: BookOpen, label: 'Учиться' },
  { screen: 'test', icon: Target, label: 'Практика' },
  { screen: 'leaderboard', icon: Users, label: 'Рейтинг' },
  { screen: 'profile', icon: User, label: 'Профиль' },
];

export function BottomNav({ currentScreen, onNavigate }: BottomNavProps) {
  return (
    <nav className="fixed bottom-0 left-0 right-0 bg-background/80 backdrop-blur-lg border-t border-border z-50 safe-area-pb">
      <div className="flex items-center justify-around h-16 max-w-lg mx-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentScreen === item.screen;
          
          return (
            <motion.button
              key={item.label}
              onClick={() => onNavigate(item.screen)}
              className={`flex flex-col items-center justify-center gap-1 flex-1 h-full relative ${
                isActive ? 'text-primary' : 'text-muted-foreground'
              }`}
              whileTap={{ scale: 0.9 }}
            >
              {isActive && (
                <motion.div
                  layoutId="bottomNavIndicator"
                  className="absolute -top-1 w-1 h-1 rounded-full bg-primary"
                  transition={{ type: 'spring', stiffness: 300, damping: 30 }}
                />
              )}
              <motion.div
                animate={isActive ? { scale: 1.1 } : { scale: 1 }}
                transition={{ duration: 0.2 }}
              >
                <Icon className="w-6 h-6" fill={isActive ? 'currentColor' : 'none'} />
              </motion.div>
              <span className="text-xs font-medium">{item.label}</span>
            </motion.button>
          );
        })}
      </div>
    </nav>
  );
}

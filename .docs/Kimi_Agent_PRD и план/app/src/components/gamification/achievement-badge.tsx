import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { Lock, Trophy } from 'lucide-react';
import type { Achievement } from '@/types';

interface AchievementBadgeProps {
  achievement: Achievement;
  onClick?: () => void;
}

export function AchievementBadge({ achievement, onClick }: AchievementBadgeProps) {
  return (
    <motion.div
      whileHover={achievement.unlocked ? { y: -4 } : {}}
      whileTap={achievement.unlocked ? { scale: 0.98 } : {}}
      onClick={onClick}
    >
      <Card
        className={`p-4 flex items-center gap-4 transition-all ${
          achievement.unlocked
            ? 'bg-gradient-to-br from-white to-yellow-50 dark:from-slate-800 dark:to-slate-900 border-yellow-200 dark:border-yellow-900'
            : 'bg-muted/50 border-muted opacity-60'
        }`}
      >
        <motion.div
          className={`w-14 h-14 rounded-2xl flex items-center justify-center text-3xl ${
            achievement.unlocked
              ? 'bg-gradient-to-br from-yellow-400 to-orange-400 shadow-lg'
              : 'bg-muted'
          }`}
          animate={
            achievement.unlocked
              ? {
                  rotate: [0, -5, 5, -5, 5, 0],
                }
              : {}
          }
          transition={{
            duration: 0.5,
            delay: 0.2,
          }}
        >
          {achievement.unlocked ? (
            achievement.icon
          ) : (
            <Lock className="w-6 h-6 text-muted-foreground" />
          )}
        </motion.div>

        <div className="flex-1 min-w-0">
          <h3 className="font-bold text-base truncate">{achievement.title}</h3>
          <p className="text-sm text-muted-foreground">{achievement.description}</p>
        </div>

        {achievement.unlocked && (
          <motion.div
            className="flex items-center gap-1 text-xp-gold font-bold"
            initial={{ opacity: 0, scale: 0 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.3, type: 'spring' }}
          >
            <Trophy className="w-4 h-4" />
            <span className="text-sm">+{achievement.xpReward}</span>
          </motion.div>
        )}
      </Card>
    </motion.div>
  );
}

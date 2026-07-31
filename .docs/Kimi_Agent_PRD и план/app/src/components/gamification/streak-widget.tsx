import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { Flame } from 'lucide-react';

interface StreakWidgetProps {
  streak: number;
}

export function StreakWidget({ streak }: StreakWidgetProps) {
  const weekDays = ['П', 'В', 'С', 'Ч', 'П', 'С', 'В'];
  
  return (
    <Card className="p-4 border-2 border-streak/30 bg-gradient-to-br from-white to-orange-50 dark:from-slate-800 dark:to-slate-900">
      <div className="flex items-center gap-3 mb-3">
        <motion.div
          animate={{
            scale: [1, 1.1, 1],
            opacity: [1, 0.9, 1],
          }}
          transition={{
            duration: 2,
            repeat: Infinity,
            ease: 'easeInOut',
          }}
          className="relative"
        >
          <Flame className="w-10 h-10 text-streak fill-streak" />
          <motion.div
            className="absolute inset-0 bg-streak/20 rounded-full blur-md"
            animate={{
              scale: [1, 1.2, 1],
              opacity: [0.5, 0.8, 0.5],
            }}
            transition={{
              duration: 2,
              repeat: Infinity,
              ease: 'easeInOut',
            }}
          />
        </motion.div>
        <div>
          <div className="text-2xl font-bold text-streak">{streak}</div>
          <div className="text-sm text-muted-foreground">дней подряд</div>
        </div>
      </div>
      
      <div className="flex justify-between gap-1">
        {weekDays.map((day, index) => (
          <motion.div
            key={index}
            className={`flex flex-col items-center gap-1 p-1 rounded-lg ${
              index < streak % 7
                ? 'bg-streak/10'
                : 'bg-muted'
            }`}
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.95 }}
          >
            <span className="text-xs text-muted-foreground">{day}</span>
            <div
              className={`w-2 h-2 rounded-full ${
                index < streak % 7
                  ? 'bg-streak'
                  : 'bg-muted-foreground/30'
              }`}
            />
          </motion.div>
        ))}
      </div>
    </Card>
  );
}

import { motion, AnimatePresence } from 'framer-motion';
import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import type { Achievement } from '@/types';
import { Star } from 'lucide-react';
import { useEffect, useState } from 'react';

interface AchievementModalProps {
  achievement: Achievement | null;
  isOpen: boolean;
  onClose: () => void;
}

export function AchievementModal({ achievement, isOpen, onClose }: AchievementModalProps) {
  const [showConfetti, setShowConfetti] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setShowConfetti(true);
      const timer = setTimeout(() => setShowConfetti(false), 2000);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  if (!achievement) return null;

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md p-0 overflow-hidden bg-gradient-to-br from-white to-yellow-50 dark:from-slate-900 dark:to-slate-800">
        <DialogTitle className="sr-only">Достижение разблокировано</DialogTitle>
        
        {/* Confetti effect */}
        <AnimatePresence>
          {showConfetti && (
            <div className="absolute inset-0 pointer-events-none overflow-hidden">
              {[...Array(20)].map((_, i) => (
                <motion.div
                  key={i}
                  className="absolute w-3 h-3 rounded-full"
                  style={{
                    backgroundColor: ['#FFD700', '#FF6B35', '#3B82F6', '#10B981', '#8B5CF6'][i % 5],
                    left: `${Math.random() * 100}%`,
                    top: '-20px',
                  }}
                  initial={{ y: -20, opacity: 1, rotate: 0 }}
                  animate={{
                    y: 400,
                    opacity: 0,
                    rotate: 360 * (Math.random() > 0.5 ? 1 : -1),
                    x: (Math.random() - 0.5) * 200,
                  }}
                  exit={{ opacity: 0 }}
                  transition={{
                    duration: 1.5 + Math.random(),
                    ease: 'easeOut',
                    delay: Math.random() * 0.3,
                  }}
                />
              ))}
            </div>
          )}
        </AnimatePresence>

        <div className="p-8 text-center relative">
          <motion.div
            className="text-6xl mb-4"
            initial={{ scale: 0, rotate: -180 }}
            animate={{ scale: 1, rotate: 0 }}
            transition={{
              type: 'spring',
              damping: 15,
              stiffness: 200,
              delay: 0.1,
            }}
          >
            {achievement.icon}
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <p className="text-sm text-muted-foreground uppercase tracking-wide font-bold mb-2">
              Достижение разблокировано!
            </p>
            <h2 className="text-2xl font-bold mb-2">{achievement.title}</h2>
            <p className="text-muted-foreground">{achievement.description}</p>
          </motion.div>

          <motion.div
            className="mt-6 flex items-center justify-center gap-2 text-xp-gold"
            initial={{ opacity: 0, scale: 0 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: 0.4, type: 'spring' }}
          >
            <Star className="w-6 h-6 fill-xp-gold" />
            <span className="text-2xl font-bold">+{achievement.xpReward} XP</span>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="mt-6"
          >
            <Button
              onClick={onClose}
              className="w-full bg-gradient-to-r from-primary to-accent-purple hover:opacity-90 text-white font-bold py-6"
            >
              Круто! 🎉
            </Button>
          </motion.div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

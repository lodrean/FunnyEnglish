import { motion, AnimatePresence } from 'framer-motion';
import { Button } from '@/components/ui/button';
import { Check, X, Star } from 'lucide-react';

interface AnswerFeedbackProps {
  isCorrect: boolean;
  correctAnswer?: string;
  xpGain?: number;
  onContinue: () => void;
}

export function AnswerFeedback({
  isCorrect,
  correctAnswer,
  xpGain = 10,
  onContinue,
}: AnswerFeedbackProps) {
  return (
    <AnimatePresence>
      <motion.div
        className={`fixed inset-x-0 bottom-0 z-50 p-4 pb-20 ${
          isCorrect
            ? 'bg-gradient-to-t from-green-500 to-green-400'
            : 'bg-gradient-to-t from-red-500 to-red-400'
        }`}
        initial={{ y: '100%' }}
        animate={{ y: 0 }}
        exit={{ y: '100%' }}
        transition={{ type: 'spring', damping: 25, stiffness: 300 }}
      >
        <div className="max-w-lg mx-auto">
          <div className="flex items-center gap-4 mb-4">
            <motion.div
              className="w-16 h-16 rounded-full bg-white flex items-center justify-center"
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ type: 'spring', delay: 0.1 }}
            >
              {isCorrect ? (
                <Check className="w-8 h-8 text-green-500" />
              ) : (
                <X className="w-8 h-8 text-red-500" />
              )}
            </motion.div>

            <div className="text-white">
              <motion.h3
                className="text-2xl font-bold"
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.2 }}
              >
                {isCorrect ? 'Правильно!' : 'Неправильно'}
              </motion.h3>
              {!isCorrect && correctAnswer && (
                <motion.p
                  className="text-white/90"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: 0.3 }}
                >
                  Правильный ответ: <span className="font-bold">{correctAnswer}</span>
                </motion.p>
              )}
            </div>
          </div>

          {isCorrect && (
            <motion.div
              className="flex items-center gap-2 text-white mb-4"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              <Star className="w-5 h-5 fill-yellow-300 text-yellow-300" />
              <span className="font-bold">+{xpGain} XP</span>
            </motion.div>
          )}

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
          >
            <Button
              onClick={onContinue}
              className="w-full bg-white hover:bg-white/90 text-gray-900 font-bold py-6"
            >
              Продолжить
            </Button>
          </motion.div>
        </div>
      </motion.div>
    </AnimatePresence>
  );
}

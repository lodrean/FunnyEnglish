import { motion } from 'framer-motion';
import { Check, X } from 'lucide-react';

interface QuestionCardProps {
  option: string;
  isSelected: boolean;
  isCorrect: boolean | null;
  onClick: () => void;
  disabled: boolean;
}

export function QuestionCard({
  option,
  isSelected,
  isCorrect,
  onClick,
  disabled,
}: QuestionCardProps) {
  const getBorderColor = () => {
    if (isCorrect === null) {
      return isSelected ? 'border-primary ring-2 ring-primary/20' : 'border-border';
    }
    if (isCorrect) return 'border-green-500 ring-2 ring-green-500/20 bg-green-50 dark:bg-green-900/20';
    if (isSelected && !isCorrect) return 'border-red-500 ring-2 ring-red-500/20 bg-red-50 dark:bg-red-900/20';
    return 'border-border opacity-50';
  };

  const getIcon = () => {
    if (isCorrect === null) return null;
    if (isCorrect) return <Check className="w-5 h-5 text-green-500" />;
    if (isSelected) return <X className="w-5 h-5 text-red-500" />;
    return null;
  };

  return (
    <motion.button
      onClick={onClick}
      disabled={disabled}
      className={`w-full p-4 rounded-xl border-2 text-left font-semibold text-lg transition-all ${getBorderColor()}`}
      whileHover={!disabled ? { scale: 1.02, y: -2 } : {}}
      whileTap={!disabled ? { scale: 0.98 } : {}}
      animate={
        isSelected && isCorrect === false
          ? { x: [0, -5, 5, -5, 5, 0] }
          : {}
      }
      transition={{ duration: 0.5 }}
    >
      <div className="flex items-center justify-between">
        <span>{option}</span>
        <motion.div
          initial={{ scale: 0 }}
          animate={{ scale: getIcon() ? 1 : 0 }}
          transition={{ type: 'spring', stiffness: 300 }}
        >
          {getIcon()}
        </motion.div>
      </div>
    </motion.button>
  );
}

import { motion, useSpring, useTransform } from 'framer-motion';
import { useEffect } from 'react';
import { Star } from 'lucide-react';

interface XPCounterProps {
  xp: number;
  showLabel?: boolean;
  size?: 'sm' | 'md' | 'lg';
}

export function XPCounter({ xp, showLabel = true, size = 'md' }: XPCounterProps) {
  const spring = useSpring(0, { duration: 1500, bounce: 0 });
  const display = useTransform(spring, (current) => Math.floor(current));

  useEffect(() => {
    spring.set(xp);
  }, [xp, spring]);

  const sizeClasses = {
    sm: 'text-sm gap-1',
    md: 'text-base gap-2',
    lg: 'text-2xl gap-2',
  };

  const iconSizes = {
    sm: 'w-4 h-4',
    md: 'w-5 h-5',
    lg: 'w-8 h-8',
  };

  return (
    <motion.div
      className={`flex items-center font-bold text-xp-gold ${sizeClasses[size]}`}
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.3 }}
    >
      <Star className={`${iconSizes[size]} fill-xp-gold text-xp-gold`} />
      <motion.span>{display}</motion.span>
      {showLabel && <span className="text-muted-foreground font-normal">XP</span>}
    </motion.div>
  );
}

interface XPGainProps {
  amount: number;
  onComplete?: () => void;
}

export function XPGain({ amount, onComplete }: XPGainProps) {
  return (
    <motion.div
      className="flex items-center gap-2 text-xp-gold font-bold text-lg"
      initial={{ opacity: 0, y: 20, scale: 0.8 }}
      animate={{ opacity: 1, y: 0, scale: 1 }}
      exit={{ opacity: 0, y: -30 }}
      transition={{ duration: 0.5, ease: 'easeOut' }}
      onAnimationComplete={onComplete}
    >
      <motion.div
        animate={{ rotate: [0, 360] }}
        transition={{ duration: 0.5 }}
      >
        <Star className="w-6 h-6 fill-xp-gold text-xp-gold" />
      </motion.div>
      <span>+{amount} XP</span>
    </motion.div>
  );
}

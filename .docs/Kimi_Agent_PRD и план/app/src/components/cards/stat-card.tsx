import { motion, useSpring, useTransform } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { useEffect } from 'react';
import type { LucideIcon } from 'lucide-react';

interface StatCardProps {
  icon: LucideIcon;
  value: number;
  label: string;
  color?: string;
}

export function StatCard({ icon: Icon, value, label, color = 'text-primary' }: StatCardProps) {
  const spring = useSpring(0, { duration: 1500, bounce: 0 });
  const display = useTransform(spring, (current) => Math.floor(current));

  useEffect(() => {
    spring.set(value);
  }, [value, spring]);

  return (
    <Card className="p-4 flex flex-col items-center text-center">
      <motion.div
        className={`w-12 h-12 rounded-xl bg-muted flex items-center justify-center mb-3 ${color}`}
        whileHover={{ scale: 1.1, rotate: 5 }}
        whileTap={{ scale: 0.95 }}
      >
        <Icon className="w-6 h-6" />
      </motion.div>
      <motion.span className="text-2xl font-bold">
        {display}
      </motion.span>
      <span className="text-sm text-muted-foreground">{label}</span>
    </Card>
  );
}

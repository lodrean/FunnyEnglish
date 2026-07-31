import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { ArrowRight } from 'lucide-react';

interface ContinueLearningCardProps {
  category: string;
  icon: string;
  progress: number;
  image: string;
  onContinue?: () => void;
}

export function ContinueLearningCard({
  category,
  icon,
  progress,
  image,
  onContinue,
}: ContinueLearningCardProps) {
  return (
    <Card className="p-4 overflow-hidden">
      <h3 className="font-bold text-lg mb-3 flex items-center gap-2">
        <span>📝</span>
        <span>Продолжить обучение</span>
      </h3>
      
      <div className="flex items-center gap-4">
        <motion.div
          className="w-20 h-20 rounded-xl overflow-hidden flex-shrink-0 bg-muted"
          whileHover={{ scale: 1.05 }}
        >
          <img
            src={image}
            alt={category}
            className="w-full h-full object-cover"
          />
        </motion.div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-2xl">{icon}</span>
            <span className="font-bold text-lg">{category}</span>
          </div>
          
          <p className="text-sm text-muted-foreground mb-2">
            {progress}% завершено
          </p>
          
          <div className="h-2 bg-muted rounded-full overflow-hidden mb-3">
            <motion.div
              className="h-full bg-gradient-to-r from-primary to-accent-purple rounded-full"
              initial={{ width: 0 }}
              animate={{ width: `${progress}%` }}
              transition={{ duration: 0.8, ease: 'easeOut' }}
            />
          </div>

          <motion.div
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            <Button
              onClick={onContinue}
              className="w-full"
            >
              Продолжить
              <ArrowRight className="w-4 h-4 ml-2" />
            </Button>
          </motion.div>
        </div>
      </div>
    </Card>
  );
}

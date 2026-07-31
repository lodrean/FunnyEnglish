import { motion } from 'framer-motion';
import { Card } from '@/components/ui/card';
import { Check } from 'lucide-react';
import type { Quest } from '@/types';

interface QuestCardProps {
  quest: Quest;
}

export function QuestCard({ quest }: QuestCardProps) {
  const progress = (quest.progress / quest.total) * 100;

  return (
    <Card className="p-4">
      <div className="flex items-start gap-3">
        <motion.div
          className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5 ${
            quest.completed
              ? 'bg-green-500'
              : 'border-2 border-muted-foreground/30'
          }`}
          animate={
            quest.completed
              ? {
                  scale: [1, 1.2, 1],
                }
              : {}
          }
          transition={{ duration: 0.3 }}
        >
          {quest.completed && <Check className="w-4 h-4 text-white" />}
        </motion.div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between gap-2">
            <p
              className={`font-medium ${
                quest.completed ? 'text-muted-foreground line-through' : ''
              }`}
            >
              {quest.title}
            </p>
            <span className="text-sm text-muted-foreground flex-shrink-0">
              {quest.progress}/{quest.total}
            </span>
          </div>

          <div className="mt-2">
            <div className="h-2 bg-muted rounded-full overflow-hidden">
              <motion.div
                className={`h-full rounded-full ${
                  quest.completed ? 'bg-green-500' : 'bg-primary'
                }`}
                initial={{ width: 0 }}
                animate={{ width: `${progress}%` }}
                transition={{ duration: 0.5, ease: 'easeOut' }}
              />
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
}

interface QuestsWidgetProps {
  quests: Quest[];
}

export function QuestsWidget({ quests }: QuestsWidgetProps) {
  return (
    <Card className="p-4">
      <h3 className="font-bold text-lg mb-3 flex items-center gap-2">
        <span>📋</span>
        <span>Задания</span>
      </h3>
      <div className="space-y-3">
        {quests.map((quest, index) => (
          <motion.div
            key={quest.id}
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: index * 0.1 }}
          >
            <QuestCard quest={quest} />
          </motion.div>
        ))}
      </div>
    </Card>
  );
}

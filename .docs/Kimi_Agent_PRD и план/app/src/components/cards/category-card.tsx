import { motion } from 'framer-motion';
import type { Category } from '@/types';

interface CategoryCardProps {
  category: Category;
  onClick?: () => void;
}

export function CategoryCard({ category, onClick }: CategoryCardProps) {
  return (
    <motion.button
      onClick={onClick}
      className={`flex-shrink-0 w-[120px] h-[140px] rounded-2xl bg-gradient-to-br ${category.gradient} p-4 flex flex-col items-center justify-center gap-3 text-white shadow-lg relative overflow-hidden`}
      whileHover={{ scale: 1.05, y: -4 }}
      whileTap={{ scale: 0.95 }}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
    >
      {/* Background decoration */}
      <div className="absolute top-0 right-0 w-16 h-16 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2" />
      <div className="absolute bottom-0 left-0 w-12 h-12 bg-black/10 rounded-full translate-y-1/2 -translate-x-1/2" />
      
      <motion.span
        className="text-4xl relative z-10"
        animate={{ y: [0, -5, 0] }}
        transition={{ duration: 2, repeat: Infinity, ease: 'easeInOut' }}
      >
        {category.icon}
      </motion.span>
      <span className="font-bold text-sm text-center relative z-10">{category.name}</span>
    </motion.button>
  );
}

interface CategoriesCarouselProps {
  categories: Category[];
  onCategoryClick?: (category: Category) => void;
}

export function CategoriesCarousel({ categories, onCategoryClick }: CategoriesCarouselProps) {
  return (
    <div className="space-y-3">
      <h3 className="font-bold text-lg flex items-center gap-2">
        <span>📚</span>
        <span>Категории</span>
      </h3>
      <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide snap-x">
        {categories.map((category, index) => (
          <motion.div
            key={category.id}
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: index * 0.05 }}
            className="snap-start"
          >
            <CategoryCard
              category={category}
              onClick={() => onCategoryClick?.(category)}
            />
          </motion.div>
        ))}
      </div>
    </div>
  );
}

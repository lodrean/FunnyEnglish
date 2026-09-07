import api from './client';

/**
 * Grading-аналитика (bd h3l.4, PROJECT-REVIEW §4.2.1).
 * Отдельный модуль: client.ts в проекте — общая точка всех API-вызовов,
 * здесь только speaking-grading поверхность.
 */

export interface CriterionAverages {
  grammar: number | null;
  vocabulary: number | null;
  pronunciation: number | null;
  fluency: number | null;
  total: number | null;
}

export interface TopicGradeDistribution {
  topicId: string;
  topicTitle: string;
  gradeCount: number;
  avgTotal: number | null;
}

export interface GradingAnalytics {
  averages: CriterionAverages;
  newCount: number;
  reviewedCount: number;
  avgReviewTimeMinutes: number | null;
  byTopic: TopicGradeDistribution[];
}

export const getGradingAnalytics = async (): Promise<GradingAnalytics> => {
  const response = await api.get<GradingAnalytics>('/admin/speaking/grading/analytics');
  return response.data;
};

/** Скачивает CSV-экспорт оценённых записей (blob с авторизацией → файл в Download). */
export const exportGradingCsv = async (): Promise<void> => {
  const response = await api.get<Blob>('/admin/speaking/grading/export.csv', {
    responseType: 'blob',
  });
  const url = window.URL.createObjectURL(new Blob([response.data], { type: 'text/csv' }));
  const link = document.createElement('a');
  link.href = url;
  link.download = 'grading-export.csv';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
};

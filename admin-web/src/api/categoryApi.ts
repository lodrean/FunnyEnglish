import api from './client';

export interface Category {
  id: string;
  name: string;
  description?: string;
  iconUrl?: string;
  displayOrder: number;
}

export const categoryApi = {
  getAll: () => api.get<Category[]>('/categories'),
};

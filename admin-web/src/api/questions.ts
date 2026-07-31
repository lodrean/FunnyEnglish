import { apiClient } from './client';
import {
  QuestionV2,
  CreateQuestionRequest,
  UpdateQuestionRequest,
} from '../types/questions';

const BASE_URL = '/admin/questions';

export const questionApi = {
  // Get all questions for a test
  getByTest: async (testId: string): Promise<QuestionV2[]> => {
    const response = await apiClient.get<QuestionV2[]>(`${BASE_URL}/test/${testId}`);
    return response.data;
  },

  // Create new question
  create: async (data: CreateQuestionRequest): Promise<QuestionV2> => {
    const response = await apiClient.post<QuestionV2>(BASE_URL, data);
    return response.data;
  },

  // Update question
  update: async (id: string, data: UpdateQuestionRequest): Promise<QuestionV2> => {
    const response = await apiClient.put<QuestionV2>(`${BASE_URL}/${id}`, data);
    return response.data;
  },

  // Delete question
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`${BASE_URL}/${id}`);
  },

  // Duplicate question
  duplicate: async (id: string): Promise<QuestionV2> => {
    const response = await apiClient.post<QuestionV2>(`${BASE_URL}/${id}/duplicate`);
    return response.data;
  },

  // Reorder questions
  reorder: async (testId: string, questionIds: string[]): Promise<void> => {
    await apiClient.post(`${BASE_URL}/reorder`, { testId, questionIds });
  },

  // Publish/unpublish question
  publish: async (id: string, isPublished: boolean): Promise<QuestionV2> => {
    const response = await apiClient.patch<QuestionV2>(`${BASE_URL}/${id}/publish`, {
      isPublished,
    });
    return response.data;
  },
};

// Legacy API for backward compatibility
export const legacyQuestionApi = {
  // Legacy create (for old question format)
  createLegacy: async (testId: string, data: {
    type: string;
    text?: string;
    audioUrl?: string;
    imageUrl?: string;
    displayOrder: number;
    points: number;
    answers: { text: string; imageUrl?: string; isCorrect: boolean }[];
  }) => {
    const response = await apiClient.post(`/admin/tests/${testId}/questions`, data);
    return response.data;
  },

  // Legacy update
  updateLegacy: async (questionId: string, data: Partial<{
    type: string;
    text?: string;
    audioUrl?: string;
    imageUrl?: string;
    displayOrder: number;
    points: number;
    answers: { text: string; imageUrl?: string; isCorrect: boolean }[];
  }>) => {
    const response = await apiClient.put(`/admin/questions/${questionId}/legacy`, data);
    return response.data;
  },
};

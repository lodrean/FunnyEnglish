import { apiClient } from './client';
import type { Test, CreateTestRequest } from '../types';

const BASE_URL = '/admin/tests';

export const testApi = {
  // Get all tests
  getAll: async (): Promise<Test[]> => {
    const response = await apiClient.get<Test[]>(BASE_URL);
    return response.data;
  },

  // Get test by ID
  getById: async (id: string): Promise<Test> => {
    const response = await apiClient.get<Test>(`${BASE_URL}/${id}`);
    return response.data;
  },

  // Create new test
  create: async (data: CreateTestRequest): Promise<Test> => {
    const response = await apiClient.post<Test>(BASE_URL, data);
    return response.data;
  },

  // Update test
  update: async (id: string, data: Partial<CreateTestRequest>): Promise<Test> => {
    const response = await apiClient.put<Test>(`${BASE_URL}/${id}`, data);
    return response.data;
  },

  // Delete test
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`${BASE_URL}/${id}`);
  },

  // Publish/unpublish test
  publish: async (id: string, isPublished: boolean): Promise<Test> => {
    const response = await apiClient.patch<Test>(`${BASE_URL}/${id}/publish`, {
      isPublished,
    });
    return response.data;
  },
};

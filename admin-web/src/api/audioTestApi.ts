import api from './client';

export interface AudioTest {
  id: string;
  title: string;
  description?: string;
  audioFileUrl: string;
  durationSeconds: number;
  difficulty: number;
  category?: {
    id: string;
    name: string;
  };
  isPublished: boolean;
  playsLimit?: number;
  questionCount: number;
  createdAt: string;
}

export interface AudioTestDetail extends AudioTest {
  questions: AudioTestQuestion[];
  transcript?: AudioTranscript;
}

export interface AudioTestQuestion {
  id: string;
  questionType: 'LISTENING_COMPREHENSION' | 'FILL_BLANK' | 'TRUE_FALSE' | 'DICTATION';
  title?: string;
  text?: string;
  startTimeSeconds: number;
  endTimeSeconds: number;
  points: number;
  displayOrder: number;
  answers: AudioTestAnswer[];
}

export interface AudioTestAnswer {
  id: string;
  text: string;
  isCorrect: boolean;
  displayOrder: number;
}

export interface AudioTranscript {
  id: string;
  content: string;
  language: string;
  isGenerated: boolean;
}

export interface CreateAudioTestRequest {
  title: string;
  description?: string;
  audioFileUrl: string;
  durationSeconds: number;
  difficulty: number;
  categoryId?: string;
  playsLimit?: number;
  questions: CreateAudioQuestionRequest[];
  transcript?: CreateTranscriptRequest;
}

export interface CreateAudioQuestionRequest {
  questionType: 'LISTENING_COMPREHENSION' | 'FILL_BLANK' | 'TRUE_FALSE' | 'DICTATION';
  title?: string;
  text?: string;
  startTimeSeconds: number;
  endTimeSeconds: number;
  points: number;
  displayOrder: number;
  answers: CreateAudioAnswerRequest[];
}

export interface CreateAudioAnswerRequest {
  text: string;
  isCorrect: boolean;
  displayOrder: number;
}

export interface CreateTranscriptRequest {
  content: string;
  language: string;
  isGenerated?: boolean;
}

export interface UpdateAudioTestRequest {
  title?: string;
  description?: string;
  durationSeconds?: number;
  difficulty?: number;
  categoryId?: string;
  playsLimit?: number;
  isPublished?: boolean;
}

export interface AudioUploadResponse {
  url: string;
  originalFilename: string;
  fileSize: number;
  durationSeconds?: number;
  contentType: string;
}

export const audioTestApi = {
  // List & Get
  getAll: (page = 0, size = 20) => 
    api.get(`/audio-tests/admin/all?page=${page}&size=${size}`),
  
  getById: (id: string) => 
    api.get<AudioTestDetail>(`/audio-tests/admin/${id}`),
  
  // CRUD
  create: (data: CreateAudioTestRequest) => 
    api.post<AudioTestDetail>('/audio-tests/admin', data),
  
  update: (id: string, data: UpdateAudioTestRequest) => 
    api.put<AudioTestDetail>(`/audio-tests/admin/${id}`, data),
  
  delete: (id: string) => 
    api.delete(`/audio-tests/admin/${id}`),
  
  // Publishing
  publish: (id: string) => 
    api.post<AudioTestDetail>(`/audio-tests/admin/${id}/publish`),
  
  unpublish: (id: string) => 
    api.post<AudioTestDetail>(`/audio-tests/admin/${id}/unpublish`),
  
  // Upload
  uploadAudio: (file: File, onProgress?: (progress: number) => void) => {
    const formData = new FormData();
    formData.append('file', file);
    
    return api.post<AudioUploadResponse>('/media/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(progress);
        }
      },
    });
  },
};

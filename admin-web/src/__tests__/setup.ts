import '@testing-library/jest-dom';
import { vi, afterEach } from 'vitest';

// Mock import.meta.env
global.import = {
  meta: {
    env: {
      VITE_API_URL: '/api',
    },
  },
} as any;

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};
Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
});

// Clean up after each test
afterEach(() => {
  vi.clearAllMocks();
});

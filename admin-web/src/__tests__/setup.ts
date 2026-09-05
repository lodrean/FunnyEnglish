import '@testing-library/jest-dom';
import { vi, afterEach } from 'vitest';

// Mock import.meta.env
const globalWithImport = globalThis as typeof globalThis & {
  import: { meta: { env: Record<string, string> } };
};
globalWithImport.import = {
  meta: {
    env: {
      VITE_API_URL: '/api',
    },
  },
};

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

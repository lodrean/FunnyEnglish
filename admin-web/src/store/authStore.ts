import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User } from '../types';
import { getCurrentUser } from '../api/client';

interface AuthState {
  token: string | null;
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
  initialize: () => Promise<void>;
  refreshUser: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      isAuthenticated: false,
      isLoading: true,
      
      login: (token, user) => {
        localStorage.setItem('token', token);
        set({ token, user, isAuthenticated: true, isLoading: false });
      },
      
      logout: () => {
        localStorage.removeItem('token');
        set({ token: null, user: null, isAuthenticated: false, isLoading: false });
      },
      
      initialize: async () => {
        const token = localStorage.getItem('token');
        if (!token) {
          set({ isLoading: false });
          return;
        }
        
        try {
          const user = await getCurrentUser();
          set({ token, user, isAuthenticated: true, isLoading: false });
        } catch {
          // Token is invalid or expired
          localStorage.removeItem('token');
          set({ token: null, user: null, isAuthenticated: false, isLoading: false });
        }
      },
      
      refreshUser: async () => {
        if (!get().isAuthenticated) return;
        
        try {
          const user = await getCurrentUser();
          set({ user });
        } catch (error) {
          console.error('Failed to refresh user:', error);
        }
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ token: state.token }), // Only persist token
    }
  )
);

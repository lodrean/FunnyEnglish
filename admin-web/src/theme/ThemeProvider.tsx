/**
 * So to speak Admin Web - Theme Provider
 * Theme context with dark/light mode toggle, localStorage persistence, and system preference detection
 */

import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
  useMemo,
  ReactNode,
} from 'react';
import { ThemeProvider as MuiThemeProvider, PaletteMode } from '@mui/material';
import { createAppTheme } from './Theme';

// Storage key for theme preference
const THEME_STORAGE_KEY = 'sotospeak-theme-mode';

// Theme context interface
interface ThemeContextType {
  /** Current theme mode */
  mode: PaletteMode;
  /** Toggle between light and dark mode */
  toggleTheme: () => void;
  /** Set specific theme mode */
  setThemeMode: (mode: PaletteMode) => void;
  /** Whether dark mode is active */
  isDarkMode: boolean;
}

// Create theme context with default values
const ThemeContext = createContext<ThemeContextType>({
  mode: 'light',
  toggleTheme: () => {},
  setThemeMode: () => {},
  isDarkMode: false,
});

// Hook to use theme context
// eslint-disable-next-line react-refresh/only-export-components
export const useTheme = (): ThemeContextType => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

// Props for ThemeProvider component
interface ThemeProviderProps {
  children: ReactNode;
  /** Default theme mode if no preference is stored */
  defaultMode?: PaletteMode;
}

/**
 * Theme Provider Component
 * Manages theme state with localStorage persistence and system preference detection
 */
export const ThemeProvider: React.FC<ThemeProviderProps> = ({
  children,
  defaultMode = 'light',
}) => {
  // Initialize state from localStorage or system preference
  const [mode, setMode] = useState<PaletteMode>(defaultMode);
  const [isInitialized, setIsInitialized] = useState(false);

  // Detect system color scheme preference
  const getSystemPreference = useCallback((): PaletteMode => {
    if (typeof window !== 'undefined' && window.matchMedia) {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)');
      return prefersDark.matches ? 'dark' : 'light';
    }
    return defaultMode;
  }, [defaultMode]);

  // Initialize theme from localStorage or system preference
  useEffect(() => {
    if (typeof window === 'undefined') return;

    try {
      const storedMode = localStorage.getItem(THEME_STORAGE_KEY) as PaletteMode | null;
      
      if (storedMode && (storedMode === 'light' || storedMode === 'dark')) {
        setMode(storedMode);
      } else {
        // Use system preference if no stored preference
        const systemMode = getSystemPreference();
        setMode(systemMode);
      }
    } catch (error) {
      console.warn('Failed to read theme preference from localStorage:', error);
      setMode(defaultMode);
    }
    
    setIsInitialized(true);
  }, [defaultMode, getSystemPreference]);

  // Listen for system preference changes
  useEffect(() => {
    if (typeof window === 'undefined' || !window.matchMedia) return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    const handleChange = (event: MediaQueryListEvent) => {
      // Only update if user hasn't set a preference
      const storedMode = localStorage.getItem(THEME_STORAGE_KEY);
      if (!storedMode) {
        setMode(event.matches ? 'dark' : 'light');
      }
    };

    // Add listener (with fallback for older browsers)
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
    } else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleChange as any);
    }

    return () => {
      if (mediaQuery.removeEventListener) {
        mediaQuery.removeEventListener('change', handleChange);
      } else if (mediaQuery.removeListener) {
        mediaQuery.removeListener(handleChange as any);
      }
    };
  }, []);

  // Persist theme mode to localStorage
  useEffect(() => {
    if (!isInitialized || typeof window === 'undefined') return;

    try {
      localStorage.setItem(THEME_STORAGE_KEY, mode);
    } catch (error) {
      console.warn('Failed to save theme preference to localStorage:', error);
    }
  }, [mode, isInitialized]);

  // Toggle theme mode
  const toggleTheme = useCallback(() => {
    setMode((prevMode) => (prevMode === 'light' ? 'dark' : 'light'));
  }, []);

  // Set specific theme mode
  const setThemeMode = useCallback((newMode: PaletteMode) => {
    setMode(newMode);
  }, []);

  // Create MUI theme based on current mode
  const theme = useMemo(() => createAppTheme(mode), [mode]);

  // Context value
  const contextValue = useMemo(
    () => ({
      mode,
      toggleTheme,
      setThemeMode,
      isDarkMode: mode === 'dark',
    }),
    [mode, toggleTheme, setThemeMode]
  );

  // Prevent flash of unstyled content
  if (!isInitialized) {
    return null;
  }

  return (
    <ThemeContext.Provider value={contextValue}>
      <MuiThemeProvider theme={theme}>
        {children}
      </MuiThemeProvider>
    </ThemeContext.Provider>
  );
};

export default ThemeProvider;

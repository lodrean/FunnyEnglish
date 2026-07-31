/**
 * FunnyEnglish Admin Web - Global Styles
 */

import { useEffect } from 'react';
import { useTheme } from './ThemeProvider';

export const GlobalStyles: React.FC = () => {
  const { isDarkMode } = useTheme();

  useEffect(() => {
    document.body.style.backgroundColor = isDarkMode ? '#121212' : '#F5F5F5';
    document.body.style.color = isDarkMode ? '#FFFFFF' : '#212121';
  }, [isDarkMode]);

  return null;
};

export default GlobalStyles;

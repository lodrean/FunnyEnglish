/**
 * So to speak Admin Web - Global Styles
 */

import { useEffect } from 'react';
import { useTheme } from '@mui/material/styles';

export const GlobalStyles: React.FC = () => {
  const theme = useTheme();

  useEffect(() => {
    // Токены темы (2oz.4): light background.default #EEF3FF / dark #121212 (MUI default)
    document.body.style.backgroundColor = theme.palette.background.default;
    document.body.style.color = theme.palette.text.primary;
  }, [theme]);

  return null;
};

export default GlobalStyles;

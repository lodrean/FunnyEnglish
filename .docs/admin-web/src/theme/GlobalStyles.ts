/**
 * FunnyEnglish Admin Web - Global Styles
 * Global CSS overrides using MUI GlobalStyles component
 */

import { GlobalStyles as MuiGlobalStyles } from '@mui/material';
import { useTheme } from './ThemeProvider';
import { alpha } from '@mui/material/styles';

/**
 * Global Styles Component
 * Applies global CSS overrides including Inter font import and base styles
 */
export const GlobalStyles: React.FC = () => {
  const { isDarkMode } = useTheme();

  return (
    <MuiGlobalStyles
      styles={{
        // Import Inter font from Google Fonts
        '@import': [
          'url(https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap)',
        ],

        // Reset and base styles
        '*': {
          margin: 0,
          padding: 0,
          boxSizing: 'border-box',
        },

        'html': {
          WebkitFontSmoothing: 'antialiased',
          MozOsxFontSmoothing: 'grayscale',
          textRendering: 'optimizeLegibility',
          scrollBehavior: 'smooth',
        },

        'body': {
          fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
          fontSize: '14px',
          fontWeight: 400,
          lineHeight: 1.5,
          backgroundColor: isDarkMode ? '#121212' : '#F5F5F5',
          color: isDarkMode ? '#FFFFFF' : '#212121',
          minHeight: '100vh',
          overflowX: 'hidden',
        },

        // Scrollbar styling
        '::-webkit-scrollbar': {
          width: '8px',
          height: '8px',
        },

        '::-webkit-scrollbar-track': {
          background: isDarkMode ? alpha('#FFFFFF', 0.05) : alpha('#000000', 0.05),
          borderRadius: '4px',
        },

        '::-webkit-scrollbar-thumb': {
          background: isDarkMode ? alpha('#FFFFFF', 0.2) : alpha('#000000', 0.2),
          borderRadius: '4px',
          '&:hover': {
            background: isDarkMode ? alpha('#FFFFFF', 0.3) : alpha('#000000', 0.3),
          },
        },

        // Selection styling
        '::selection': {
          backgroundColor: alpha('#4A90D9', 0.3),
          color: 'inherit',
        },

        // Focus outline styling
        ':focus-visible': {
          outline: `2px solid ${alpha('#4A90D9', 0.5)}`,
          outlineOffset: '2px',
        },

        // Link styling
        'a': {
          color: '#4A90D9',
          textDecoration: 'none',
          transition: 'color 0.2s ease-in-out',
          '&:hover': {
            color: alpha('#4A90D9', 0.8),
            textDecoration: 'underline',
          },
        },

        // Form element base styling
        'input, textarea, select': {
          fontFamily: 'inherit',
          fontSize: 'inherit',
        },

        // Button reset
        'button': {
          fontFamily: 'inherit',
          cursor: 'pointer',
          border: 'none',
          background: 'none',
        },

        // Image styling
        'img': {
          maxWidth: '100%',
          height: 'auto',
          display: 'block',
        },

        // Table styling
        'table': {
          borderCollapse: 'collapse',
          width: '100%',
        },

        // Code styling
        'code, pre': {
          fontFamily: '"Fira Code", "Monaco", "Consolas", monospace',
          fontSize: '0.875em',
        },

        // Utility classes
        '.visually-hidden': {
          position: 'absolute',
          width: '1px',
          height: '1px',
          padding: 0,
          margin: '-1px',
          overflow: 'hidden',
          clip: 'rect(0, 0, 0, 0)',
          whiteSpace: 'nowrap',
          border: 0,
        },

        '.text-truncate': {
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
        },

        // Animation keyframes
        '@keyframes fadeIn': {
          from: {
            opacity: 0,
          },
          to: {
            opacity: 1,
          },
        },

        '@keyframes slideInFromLeft': {
          from: {
            transform: 'translateX(-100%)',
            opacity: 0,
          },
          to: {
            transform: 'translateX(0)',
            opacity: 1,
          },
        },

        '@keyframes slideInFromRight': {
          from: {
            transform: 'translateX(100%)',
            opacity: 0,
          },
          to: {
            transform: 'translateX(0)',
            opacity: 1,
          },
        },

        '@keyframes slideInFromBottom': {
          from: {
            transform: 'translateY(20px)',
            opacity: 0,
          },
          to: {
            transform: 'translateY(0)',
            opacity: 1,
          },
        },

        '@keyframes pulse': {
          '0%, 100%': {
            opacity: 1,
          },
          '50%': {
            opacity: 0.5,
          },
        },

        '@keyframes spin': {
          from: {
            transform: 'rotate(0deg)',
          },
          to: {
            transform: 'rotate(360deg)',
          },
        },

        // Animation utility classes
        '.animate-fadeIn': {
          animation: 'fadeIn 0.3s ease-in-out',
        },

        '.animate-slideInLeft': {
          animation: 'slideInFromLeft 0.3s ease-out',
        },

        '.animate-slideInRight': {
          animation: 'slideInFromRight 0.3s ease-out',
        },

        '.animate-slideInBottom': {
          animation: 'slideInFromBottom 0.3s ease-out',
        },

        '.animate-pulse': {
          animation: 'pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite',
        },

        '.animate-spin': {
          animation: 'spin 1s linear infinite',
        },
      }}
    />
  );
};

export default GlobalStyles;

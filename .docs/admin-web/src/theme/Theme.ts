/**
 * FunnyEnglish Admin Web - Theme Configuration
 * Main theme configuration with MUI v6 createTheme
 */

import { createTheme, ThemeOptions, alpha } from '@mui/material/styles';
import { PaletteMode } from '@mui/material';

// Design System Colors
const colors = {
  primary: '#4A90D9',
  success: '#43A047',
  error: '#E53935',
  warning: '#FB8C00',
  info: '#2196F3',
  background: '#F5F5F5',
  card: '#FFFFFF',
  textPrimary: '#212121',
  textSecondary: '#757575',
  sidebar: '#1a237e',
};

// Custom shadows
const customShadows = {
  card: '0 2px 8px rgba(0, 0, 0, 0.08)',
  cardHover: '0 4px 16px rgba(0, 0, 0, 0.12)',
  dropdown: '0 4px 20px rgba(0, 0, 0, 0.15)',
  sidebar: '2px 0 8px rgba(0, 0, 0, 0.1)',
  header: '0 2px 8px rgba(0, 0, 0, 0.08)',
};

// Get theme options based on mode
const getThemeOptions = (mode: PaletteMode): ThemeOptions => {
  const isDark = mode === 'dark';

  return {
    palette: {
      mode,
      primary: {
        main: colors.primary,
        light: alpha(colors.primary, 0.8),
        dark: alpha(colors.primary, 0.9),
        contrastText: '#FFFFFF',
      },
      secondary: {
        main: colors.sidebar,
        light: alpha(colors.sidebar, 0.8),
        dark: '#0d1642',
        contrastText: '#FFFFFF',
      },
      success: {
        main: colors.success,
        light: alpha(colors.success, 0.8),
        dark: alpha(colors.success, 0.9),
        contrastText: '#FFFFFF',
      },
      error: {
        main: colors.error,
        light: alpha(colors.error, 0.8),
        dark: alpha(colors.error, 0.9),
        contrastText: '#FFFFFF',
      },
      warning: {
        main: colors.warning,
        light: alpha(colors.warning, 0.8),
        dark: alpha(colors.warning, 0.9),
        contrastText: '#FFFFFF',
      },
      info: {
        main: colors.info,
        light: alpha(colors.info, 0.8),
        dark: alpha(colors.info, 0.9),
        contrastText: '#FFFFFF',
      },
      background: {
        default: isDark ? '#121212' : colors.background,
        paper: isDark ? '#1E1E1E' : colors.card,
      },
      text: {
        primary: isDark ? '#FFFFFF' : colors.textPrimary,
        secondary: isDark ? alpha('#FFFFFF', 0.7) : colors.textSecondary,
      },
      divider: isDark ? alpha('#FFFFFF', 0.12) : alpha('#000000', 0.12),
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
      h1: {
        fontSize: '2.5rem',
        fontWeight: 600,
        lineHeight: 1.2,
      },
      h2: {
        fontSize: '2rem',
        fontWeight: 600,
        lineHeight: 1.3,
      },
      h3: {
        fontSize: '1.75rem',
        fontWeight: 600,
        lineHeight: 1.3,
      },
      h4: {
        fontSize: '1.5rem',
        fontWeight: 600,
        lineHeight: 1.4,
      },
      h5: {
        fontSize: '1.25rem',
        fontWeight: 600,
        lineHeight: 1.4,
      },
      h6: {
        fontSize: '1rem',
        fontWeight: 600,
        lineHeight: 1.5,
      },
      subtitle1: {
        fontSize: '1rem',
        fontWeight: 500,
        lineHeight: 1.5,
      },
      subtitle2: {
        fontSize: '0.875rem',
        fontWeight: 500,
        lineHeight: 1.5,
      },
      body1: {
        fontSize: '1rem',
        fontWeight: 400,
        lineHeight: 1.5,
      },
      body2: {
        fontSize: '0.875rem',
        fontWeight: 400,
        lineHeight: 1.5,
      },
      button: {
        fontSize: '0.875rem',
        fontWeight: 500,
        textTransform: 'none',
      },
      caption: {
        fontSize: '0.75rem',
        fontWeight: 400,
        lineHeight: 1.5,
      },
      overline: {
        fontSize: '0.75rem',
        fontWeight: 500,
        textTransform: 'uppercase',
        letterSpacing: '0.5px',
      },
    },
    shape: {
      borderRadius: 8,
    },
    shadows: [
      'none',
      customShadows.card,
      '0 2px 4px rgba(0,0,0,0.1)',
      '0 3px 6px rgba(0,0,0,0.12)',
      '0 4px 8px rgba(0,0,0,0.14)',
      '0 5px 10px rgba(0,0,0,0.15)',
      '0 6px 12px rgba(0,0,0,0.16)',
      '0 7px 14px rgba(0,0,0,0.17)',
      '0 8px 16px rgba(0,0,0,0.18)',
      '0 9px 18px rgba(0,0,0,0.19)',
      '0 10px 20px rgba(0,0,0,0.20)',
      '0 11px 22px rgba(0,0,0,0.21)',
      '0 12px 24px rgba(0,0,0,0.22)',
      '0 13px 26px rgba(0,0,0,0.23)',
      '0 14px 28px rgba(0,0,0,0.24)',
      '0 15px 30px rgba(0,0,0,0.25)',
      '0 16px 32px rgba(0,0,0,0.26)',
      '0 17px 34px rgba(0,0,0,0.27)',
      '0 18px 36px rgba(0,0,0,0.28)',
      '0 19px 38px rgba(0,0,0,0.29)',
      '0 20px 40px rgba(0,0,0,0.30)',
      '0 21px 42px rgba(0,0,0,0.31)',
      '0 22px 44px rgba(0,0,0,0.32)',
      '0 23px 46px rgba(0,0,0,0.33)',
      '0 24px 48px rgba(0,0,0,0.34)',
    ] as any,
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            textTransform: 'none',
            fontWeight: 500,
            padding: '8px 16px',
            transition: 'all 0.2s ease-in-out',
            '&:hover': {
              transform: 'translateY(-1px)',
              boxShadow: customShadows.cardHover,
            },
          },
          contained: {
            boxShadow: customShadows.card,
          },
          outlined: {
            borderWidth: 1.5,
            '&:hover': {
              borderWidth: 1.5,
            },
          },
          sizeSmall: {
            padding: '4px 12px',
            fontSize: '0.8125rem',
          },
          sizeLarge: {
            padding: '12px 24px',
            fontSize: '1rem',
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            boxShadow: customShadows.card,
            transition: 'box-shadow 0.2s ease-in-out',
            '&:hover': {
              boxShadow: customShadows.cardHover,
            },
          },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            padding: 24,
            '&:last-child': {
              paddingBottom: 24,
            },
          },
        },
      },
      MuiCardHeader: {
        styleOverrides: {
          root: {
            padding: '16px 24px',
          },
          title: {
            fontSize: '1.125rem',
            fontWeight: 600,
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            padding: '16px',
            borderBottom: `1px solid ${isDark ? alpha('#FFFFFF', 0.12) : alpha('#000000', 0.08)}`,
          },
          head: {
            fontWeight: 600,
            backgroundColor: isDark ? alpha('#FFFFFF', 0.05) : alpha('#000000', 0.02),
            color: isDark ? '#FFFFFF' : colors.textPrimary,
          },
        },
      },
      MuiTableRow: {
        styleOverrides: {
          root: {
            '&:hover': {
              backgroundColor: isDark ? alpha('#FFFFFF', 0.04) : alpha('#000000', 0.02),
            },
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            boxShadow: customShadows.header,
          },
        },
      },
      MuiDrawer: {
        styleOverrides: {
          paper: {
            borderRight: 'none',
            boxShadow: customShadows.sidebar,
          },
        },
      },
      MuiListItemButton: {
        styleOverrides: {
          root: {
            borderRadius: 8,
            margin: '4px 8px',
            padding: '10px 16px',
            transition: 'all 0.2s ease-in-out',
            '&:hover': {
              backgroundColor: isDark ? alpha('#FFFFFF', 0.08) : alpha(colors.primary, 0.08),
            },
            '&.Mui-selected': {
              backgroundColor: isDark ? alpha(colors.primary, 0.2) : alpha(colors.primary, 0.12),
              color: colors.primary,
              '&:hover': {
                backgroundColor: isDark ? alpha(colors.primary, 0.25) : alpha(colors.primary, 0.16),
              },
              '& .MuiListItemIcon-root': {
                color: colors.primary,
              },
            },
          },
        },
      },
      MuiListItemIcon: {
        styleOverrides: {
          root: {
            minWidth: 40,
            color: isDark ? alpha('#FFFFFF', 0.7) : colors.textSecondary,
          },
        },
      },
      MuiTextField: {
        styleOverrides: {
          root: {
            '& .MuiOutlinedInput-root': {
              borderRadius: 8,
              '& fieldset': {
                borderWidth: 1,
              },
              '&:hover fieldset': {
                borderWidth: 1.5,
              },
              '&.Mui-focused fieldset': {
                borderWidth: 2,
              },
            },
          },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: {
            borderRadius: 6,
            fontWeight: 500,
          },
        },
      },
      MuiAvatar: {
        styleOverrides: {
          root: {
            fontWeight: 500,
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
          },
        },
      },
    },
  };
};

// Create theme function
export const createAppTheme = (mode: PaletteMode) => {
  return createTheme(getThemeOptions(mode));
};

// Export colors and shadows for use in components
export { colors, customShadows };

// Default theme (light mode)
export const defaultTheme = createAppTheme('light');

export default defaultTheme;

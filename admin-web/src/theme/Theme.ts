/**
 * FunnyEnglish Admin Web - Design System 2.0 Theme Configuration
 * Full light/dark theme support with MUI v6
 */

import { createTheme, ThemeOptions, alpha, PaletteMode } from '@mui/material/styles';

// =============================================================================
// DESIGN TOKENS
// =============================================================================

// Brand Colors
const brandColors = {
  primary: {
    50: '#E3F2FD',
    100: '#BBDEFB',
    200: '#90CAF9',
    300: '#64B5F6',
    400: '#42A5F5',
    500: '#4A90D9', // Main brand color
    600: '#1E88E5',
    700: '#1976D2',
    800: '#1565C0',
    900: '#0D47A1',
  },
};

// Semantic Colors
const semanticColors = {
  success: {
    light: '#43A047',
    main: '#43A047',
    dark: '#2E7D32',
    contrastText: '#FFFFFF',
  },
  error: {
    light: '#FF897D',
    main: '#E53935',
    dark: '#C62828',
    contrastText: '#FFFFFF',
  },
  warning: {
    light: '#FFB74D',
    main: '#FB8C00',
    dark: '#EF6C00',
    contrastText: '#FFFFFF',
  },
  info: {
    light: '#90CAF9',
    main: '#2196F3',
    dark: '#1565C0',
    contrastText: '#FFFFFF',
  },
};

// Chart Colors for Data Visualization
const chartColors = [
  '#4A90D9', // Primary Blue
  '#43A047', // Success Green
  '#FB8C00', // Warning Orange
  '#E53935', // Error Red
  '#9C27B0', // Purple
  '#00BCD4', // Cyan
  '#FFEB3B', // Yellow
  '#795548', // Brown
  '#607D8B', // Blue Grey
  '#FF5722', // Deep Orange
];

// =============================================================================
// LIGHT THEME
// =============================================================================

const lightThemeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: brandColors.primary[500],
      light: brandColors.primary[300],
      dark: brandColors.primary[700],
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: '#1a237e',
      light: '#534bae',
      dark: '#000051',
      contrastText: '#FFFFFF',
    },
    ...semanticColors,
    background: {
      default: '#F5F5F5',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#212121',
      secondary: '#757575',
      disabled: '#9E9E9E',
    },
    divider: alpha('#000000', 0.12),
    action: {
      active: alpha('#000000', 0.54),
      hover: alpha('#000000', 0.04),
      selected: alpha(brandColors.primary[500], 0.12),
      disabled: alpha('#000000', 0.26),
      disabledBackground: alpha('#000000', 0.12),
    },
    // Custom admin colors
    admin: {
      sidebar: '#1a237e',
      sidebarText: '#FFFFFF',
      header: '#FFFFFF',
      border: '#E0E0E0',
      hover: alpha('#000000', 0.04),
      selected: alpha(brandColors.primary[500], 0.12),
      chart: chartColors,
    },
  },
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h1: {
      fontSize: '2.5rem',
      fontWeight: 600,
      lineHeight: 1.2,
      letterSpacing: '-0.01562em',
    },
    h2: {
      fontSize: '2rem',
      fontWeight: 600,
      lineHeight: 1.3,
      letterSpacing: '-0.00833em',
    },
    h3: {
      fontSize: '1.75rem',
      fontWeight: 600,
      lineHeight: 1.4,
      letterSpacing: '0em',
    },
    h4: {
      fontSize: '1.5rem',
      fontWeight: 600,
      lineHeight: 1.4,
      letterSpacing: '0.00735em',
    },
    h5: {
      fontSize: '1.25rem',
      fontWeight: 600,
      lineHeight: 1.5,
      letterSpacing: '0em',
    },
    h6: {
      fontSize: '1.125rem',
      fontWeight: 500,
      lineHeight: 1.5,
      letterSpacing: '0.0075em',
    },
    subtitle1: {
      fontSize: '1rem',
      fontWeight: 500,
      lineHeight: 1.5,
      letterSpacing: '0.00938em',
    },
    subtitle2: {
      fontSize: '0.875rem',
      fontWeight: 500,
      lineHeight: 1.5,
      letterSpacing: '0.00714em',
    },
    body1: {
      fontSize: '1rem',
      fontWeight: 400,
      lineHeight: 1.5,
      letterSpacing: '0.00938em',
    },
    body2: {
      fontSize: '0.875rem',
      fontWeight: 400,
      lineHeight: 1.5,
      letterSpacing: '0.01071em',
    },
    button: {
      fontSize: '0.875rem',
      fontWeight: 500,
      lineHeight: 1.75,
      letterSpacing: '0.02857em',
      textTransform: 'none',
    },
    caption: {
      fontSize: '0.75rem',
      fontWeight: 400,
      lineHeight: 1.66,
      letterSpacing: '0.03333em',
    },
    overline: {
      fontSize: '0.75rem',
      fontWeight: 500,
      lineHeight: 2.66,
      letterSpacing: '0.08333em',
      textTransform: 'uppercase',
    },
  },
  shape: {
    borderRadius: 8,
  },
  shadows: [
    'none',
    '0 1px 2px rgba(0,0,0,0.05)',
    '0 2px 4px rgba(0,0,0,0.08)',
    '0 3px 6px rgba(0,0,0,0.1)',
    '0 4px 8px rgba(0,0,0,0.12)',
    '0 5px 10px rgba(0,0,0,0.14)',
    '0 6px 12px rgba(0,0,0,0.16)',
    '0 7px 14px rgba(0,0,0,0.18)',
    '0 8px 16px rgba(0,0,0,0.2)',
    '0 9px 18px rgba(0,0,0,0.22)',
    '0 10px 20px rgba(0,0,0,0.24)',
    '0 11px 22px rgba(0,0,0,0.26)',
    '0 12px 24px rgba(0,0,0,0.28)',
    '0 13px 26px rgba(0,0,0,0.3)',
    '0 14px 28px rgba(0,0,0,0.32)',
    '0 15px 30px rgba(0,0,0,0.34)',
    '0 16px 32px rgba(0,0,0,0.36)',
    '0 17px 34px rgba(0,0,0,0.38)',
    '0 18px 36px rgba(0,0,0,0.4)',
    '0 19px 38px rgba(0,0,0,0.42)',
    '0 20px 40px rgba(0,0,0,0.44)',
    '0 21px 42px rgba(0,0,0,0.46)',
    '0 22px 44px rgba(0,0,0,0.48)',
    '0 23px 46px rgba(0,0,0,0.5)',
    '0 24px 48px rgba(0,0,0,0.52)',
  ],
  spacing: 8,
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
            boxShadow: '0 4px 12px rgba(74, 144, 217, 0.3)',
          },
        },
        contained: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
        },
        containedPrimary: {
          background: 'linear-gradient(135deg, #4A90D9 0%, #357ABD 100%)',
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
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(0,0,0,0.12)',
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
          borderBottom: `1px solid ${alpha('#000000', 0.08)}`,
        },
        head: {
          fontWeight: 600,
          backgroundColor: alpha('#000000', 0.02),
          color: '#212121',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha('#000000', 0.02),
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: 'none',
          boxShadow: '2px 0 8px rgba(0,0,0,0.1)',
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
            backgroundColor: alpha(brandColors.primary[500], 0.08),
          },
          '&.Mui-selected': {
            backgroundColor: alpha(brandColors.primary[500], 0.12),
            color: brandColors.primary[500],
            '&:hover': {
              backgroundColor: alpha(brandColors.primary[500], 0.16),
            },
            '& .MuiListItemIcon-root': {
              color: brandColors.primary[500],
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          minWidth: 40,
          color: '#757575',
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
    MuiTooltip: {
      styleOverrides: {
        tooltip: {
          borderRadius: 6,
          fontSize: '0.75rem',
          fontWeight: 500,
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          borderRadius: 8,
          boxShadow: '0 4px 20px rgba(0,0,0,0.15)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 12,
          boxShadow: '0 24px 48px rgba(0,0,0,0.2)',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          borderRadius: 4,
        },
      },
    },
  },
};

// =============================================================================
// DARK THEME
// =============================================================================

const darkThemeOptions: ThemeOptions = {
  palette: {
    mode: 'dark',
    primary: {
      main: '#60A5FA', // Lighter blue for dark mode
      light: '#93C5FD',
      dark: '#3B82F6',
      contrastText: '#0F172A',
    },
    secondary: {
      main: '#818CF8',
      light: '#A5B4FC',
      dark: '#6366F1',
      contrastText: '#0F172A',
    },
    success: {
      light: '#4ADE80',
      main: '#22C55E',
      dark: '#16A34A',
      contrastText: '#0F172A',
    },
    error: {
      light: '#F87171',
      main: '#EF4444',
      dark: '#DC2626',
      contrastText: '#FFFFFF',
    },
    warning: {
      light: '#FBBF24',
      main: '#F59E0B',
      dark: '#D97706',
      contrastText: '#0F172A',
    },
    info: {
      light: '#60A5FA',
      main: '#3B82F6',
      dark: '#2563EB',
      contrastText: '#FFFFFF',
    },
    background: {
      default: '#0A1929', // Deep blue-black
      paper: '#1E293B', // Dark slate
    },
    text: {
      primary: '#F8FAFC',
      secondary: '#94A3B8',
      disabled: '#64748B',
    },
    divider: alpha('#FFFFFF', 0.12),
    action: {
      active: alpha('#FFFFFF', 0.7),
      hover: alpha('#FFFFFF', 0.08),
      selected: alpha('#60A5FA', 0.16),
      disabled: alpha('#FFFFFF', 0.3),
      disabledBackground: alpha('#FFFFFF', 0.12),
    },
    // Custom admin colors for dark mode
    admin: {
      sidebar: '#0F172A',
      sidebarText: '#F8FAFC',
      header: '#1E293B',
      border: '#334155',
      hover: alpha('#FFFFFF', 0.08),
      selected: alpha('#60A5FA', 0.2),
      chart: chartColors.map(c => c + 'CC'), // Add transparency for dark mode
    },
  },
  typography: lightThemeOptions.typography,
  shape: lightThemeOptions.shape,
  shadows: [
    'none',
    '0 1px 2px rgba(0,0,0,0.3)',
    '0 2px 4px rgba(0,0,0,0.4)',
    '0 3px 6px rgba(0,0,0,0.5)',
    '0 4px 8px rgba(0,0,0,0.5)',
    '0 5px 10px rgba(0,0,0,0.5)',
    '0 6px 12px rgba(0,0,0,0.5)',
    '0 7px 14px rgba(0,0,0,0.5)',
    '0 8px 16px rgba(0,0,0,0.5)',
    '0 9px 18px rgba(0,0,0,0.5)',
    '0 10px 20px rgba(0,0,0,0.5)',
    '0 11px 22px rgba(0,0,0,0.5)',
    '0 12px 24px rgba(0,0,0,0.5)',
    '0 13px 26px rgba(0,0,0,0.5)',
    '0 14px 28px rgba(0,0,0,0.5)',
    '0 15px 30px rgba(0,0,0,0.5)',
    '0 16px 32px rgba(0,0,0,0.5)',
    '0 17px 34px rgba(0,0,0,0.5)',
    '0 18px 36px rgba(0,0,0,0.5)',
    '0 19px 38px rgba(0,0,0,0.5)',
    '0 20px 40px rgba(0,0,0,0.5)',
    '0 21px 42px rgba(0,0,0,0.5)',
    '0 22px 44px rgba(0,0,0,0.5)',
    '0 23px 46px rgba(0,0,0,0.5)',
    '0 24px 48px rgba(0,0,0,0.5)',
  ],
  spacing: 8,
  components: {
    ...lightThemeOptions.components,
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
            boxShadow: '0 4px 12px rgba(96, 165, 250, 0.3)',
          },
        },
        contained: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
        },
        containedPrimary: {
          background: 'linear-gradient(135deg, #3B82F6 0%, #2563EB 100%)',
        },
        outlined: {
          borderWidth: 1.5,
          borderColor: alpha('#FFFFFF', 0.23),
          '&:hover': {
            borderWidth: 1.5,
            borderColor: alpha('#FFFFFF', 0.4),
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
          backgroundColor: '#1E293B',
          border: `1px solid ${alpha('#FFFFFF', 0.08)}`,
          boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
          transition: 'all 0.2s ease-in-out',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
            borderColor: alpha('#FFFFFF', 0.12),
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: '16px',
          borderBottom: `1px solid ${alpha('#FFFFFF', 0.08)}`,
          color: '#F8FAFC',
        },
        head: {
          fontWeight: 600,
          backgroundColor: alpha('#FFFFFF', 0.05),
          color: '#F8FAFC',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha('#FFFFFF', 0.04),
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#1E293B',
          boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#0F172A',
          borderRight: `1px solid ${alpha('#FFFFFF', 0.08)}`,
          boxShadow: '2px 0 8px rgba(0,0,0,0.3)',
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
            backgroundColor: alpha('#FFFFFF', 0.08),
          },
          '&.Mui-selected': {
            backgroundColor: alpha('#60A5FA', 0.16),
            color: '#60A5FA',
            '&:hover': {
              backgroundColor: alpha('#60A5FA', 0.24),
            },
            '& .MuiListItemIcon-root': {
              color: '#60A5FA',
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          minWidth: 40,
          color: '#94A3B8',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 8,
            backgroundColor: alpha('#FFFFFF', 0.05),
            '& fieldset': {
              borderWidth: 1,
              borderColor: alpha('#FFFFFF', 0.23),
            },
            '&:hover fieldset': {
              borderWidth: 1.5,
              borderColor: alpha('#FFFFFF', 0.4),
            },
            '&.Mui-focused fieldset': {
              borderWidth: 2,
              borderColor: '#60A5FA',
            },
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: '#1E293B',
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          backgroundColor: '#1E293B',
          border: `1px solid ${alpha('#FFFFFF', 0.08)}`,
          boxShadow: '0 4px 20px rgba(0,0,0,0.4)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundColor: '#1E293B',
          border: `1px solid ${alpha('#FFFFFF', 0.08)}`,
          boxShadow: '0 24px 48px rgba(0,0,0,0.4)',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          backgroundColor: alpha('#FFFFFF', 0.1),
          borderRadius: 4,
        },
      },
    },
  },
};

// =============================================================================
// THEME CREATION
// =============================================================================

export const createAppTheme = (mode: PaletteMode) => {
  return createTheme(mode === 'light' ? lightThemeOptions : darkThemeOptions);
};

// Export individual themes for specific use cases
export const lightTheme = createTheme(lightThemeOptions);
export const darkTheme = createTheme(darkThemeOptions);

// Export default theme (light)
export default lightTheme;

// Export chart colors for use in charts
export { chartColors };

// Type augmentation for custom palette values
declare module '@mui/material/styles' {
  interface Palette {
    admin: {
      sidebar: string;
      sidebarText: string;
      header: string;
      border: string;
      hover: string;
      selected: string;
      chart: string[];
    };
  }
  interface PaletteOptions {
    admin?: {
      sidebar?: string;
      sidebarText?: string;
      header?: string;
      border?: string;
      hover?: string;
      selected?: string;
      chart?: string[];
    };
  }
}

/**
 * So to speak Admin Web — Design System "Playful Coach" v1.1
 * Источник HEX: .docs/design-system/tokens.json v1.1 (вариант B, утверждён владельцем 2026-07-31)
 * Full light/dark theme support with MUI v6
 */

import { createTheme, ThemeOptions, alpha, PaletteMode } from '@mui/material/styles';

// =============================================================================
// DESIGN TOKENS (tokens.json v1.1 — HEX 1:1)
// =============================================================================

// Brand Colors — шкала вокруг primary #5B8DEF
const brandColors = {
  primary: {
    50: '#EEF3FF', // neutral.background (periwinkle)
    100: '#DDE8FD', // primaryContainer
    200: '#BBD0FA',
    300: '#8FB3F5', // dark.primary
    400: '#719FF2',
    500: '#5B8DEF', // Main brand color
    600: '#4A78D4',
    700: '#3B63B8',
    800: '#2F4F96',
    900: '#1A2F5E', // onPrimaryContainer
  },
  secondary: {
    100: '#E5DCFF', // secondaryContainer
    300: '#B79EED', // dark.secondary
    500: '#9B7EDE', // Main secondary (firm violet)
    700: '#5B3FA8', // onSecondaryContainer
  },
};

// Semantic Colors (tokens.json color.semantic + color.status)
const semanticColors = {
  success: {
    light: '#66BB6A',
    main: '#43A047', // semantic.success / status.reviewed
    dark: '#2E7D32',
    contrastText: '#FFFFFF',
  },
  error: {
    light: '#FF897D',
    main: '#E53935', // semantic.error
    dark: '#C62828',
    contrastText: '#FFFFFF',
  },
  warning: {
    light: '#FFB74D',
    main: '#FB8C00', // semantic.warning / status.new
    dark: '#EF6C00',
    contrastText: '#FFFFFF',
  },
  info: {
    light: '#8FB3F5',
    main: '#5B8DEF', // = brand primary
    dark: '#3B6FD4', // brand primaryStrong (аудит 2026-08-01: белый текст AA)
    contrastText: '#FFFFFF',
  },
};

// Speaking Trainer custom palette (record/timer/status)
const speakingLight = {
  record: '#FF9F6B', // semantic.record — дружелюбный персиковый, НЕ красный
  recordActive: '#D97238',
  recordShadow: '#D97238',
  recordContainer: '#FFE3D1',
  onRecordContainer: '#8A3B0E',
  onRecord: '#2D3561', // тёмный на record (5.81:1 AA)
  primaryStrong: '#3B6FD4', // кнопки/активные чипы/nav с белым текстом
  waveformPlayback: '#5B8DEF',
  timer: {
    level80: '#4A7FE8', // затемнены ≥3:1 (аудит 2026-08-01)
    level50: '#8A68D6',
    level30: '#D97238',
  },
  status: {
    new: '#FB8C00',
    newContainer: '#FFE0B2',
    reviewed: '#43A047',
    reviewedContainer: '#C8E6C9',
  },
};

// Dark-вариант: record осветлён для контраста (tokens.json color.dark)
const speakingDark = {
  ...speakingLight,
  record: '#FFB27D',
};

// Chart Colors for Data Visualization (первый = brand primary)
const chartColors = [
  '#5B8DEF', // Primary Blue
  '#43A047', // Success Green
  '#FB8C00', // Warning Orange
  '#E53935', // Error Red
  '#9B7EDE', // Brand Violet
  '#00BCD4', // Cyan
  '#FF9F6B', // Record Peach
  '#795548', // Brown
  '#607D8B', // Blue Grey
  '#FF5722', // Deep Orange
];

// Фирменная тень карточки (tokens.json elevation.card)
const cardShadow = '0 1px 2px rgba(45,53,97,0.06), 0 2px 8px rgba(45,53,97,0.05)';
// Focus ring (tokens.json elevation.focusRing)
const focusRing = '0 0 0 2px #EEF3FF, 0 0 0 4px #5B8DEF';

// Тёплые индиго-тени вместо нейтрально-чёрных
const lightShadows = [
  'none',
  '0 1px 2px rgba(45,53,97,0.05)',
  cardShadow,
  '0 3px 6px rgba(45,53,97,0.07)',
  '0 4px 8px rgba(45,53,97,0.08)',
  '0 5px 10px rgba(45,53,97,0.09)',
  '0 6px 12px rgba(45,53,97,0.10)',
  '0 7px 14px rgba(45,53,97,0.11)',
  '0 8px 16px rgba(45,53,97,0.12)',
  '0 9px 18px rgba(45,53,97,0.13)',
  '0 10px 20px rgba(45,53,97,0.14)',
  '0 11px 22px rgba(45,53,97,0.15)',
  '0 12px 24px rgba(45,53,97,0.16)',
  '0 13px 26px rgba(45,53,97,0.17)',
  '0 14px 28px rgba(45,53,97,0.18)',
  '0 15px 30px rgba(45,53,97,0.19)',
  '0 16px 32px rgba(45,53,97,0.20)',
  '0 17px 34px rgba(45,53,97,0.21)',
  '0 18px 36px rgba(45,53,97,0.22)',
  '0 19px 38px rgba(45,53,97,0.23)',
  '0 20px 40px rgba(45,53,97,0.24)',
  '0 21px 42px rgba(45,53,97,0.25)',
  '0 22px 44px rgba(45,53,97,0.26)',
  '0 23px 46px rgba(45,53,97,0.27)',
  '0 24px 48px rgba(45,53,97,0.28)',
] as ThemeOptions['shadows'];

const darkShadows = [
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
] as ThemeOptions['shadows'];

// =============================================================================
// SHARED (typography / shape / spacing)
// =============================================================================

const typography: ThemeOptions['typography'] = {
  fontFamily: 'Nunito, -apple-system, "Segoe UI", Roboto, sans-serif',
  h1: {
    fontSize: '2.5rem',
    fontWeight: 800,
    lineHeight: 1.2,
    letterSpacing: '-0.01562em',
  },
  h2: {
    fontSize: '2rem',
    fontWeight: 700,
    lineHeight: 1.3,
    letterSpacing: '-0.00833em',
  },
  h3: {
    fontSize: '1.75rem',
    fontWeight: 700,
    lineHeight: 1.4,
    letterSpacing: '0em',
  },
  h4: {
    fontSize: '1.5rem',
    fontWeight: 700,
    lineHeight: 1.4,
    letterSpacing: '0.00735em',
  },
  h5: {
    fontSize: '1.25rem',
    fontWeight: 700,
    lineHeight: 1.5,
    letterSpacing: '0em',
  },
  h6: {
    fontSize: '1.125rem',
    fontWeight: 600,
    lineHeight: 1.5,
    letterSpacing: '0.0075em',
  },
  subtitle1: {
    fontSize: '1rem',
    fontWeight: 600,
    lineHeight: 1.5,
    letterSpacing: '0.00938em',
  },
  subtitle2: {
    fontSize: '0.875rem',
    fontWeight: 600,
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
    fontWeight: 600,
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
    fontWeight: 600,
    lineHeight: 2.66,
    letterSpacing: '0.08333em',
    textTransform: 'uppercase',
  },
};

// =============================================================================
// LIGHT THEME
// =============================================================================

const lightThemeOptions: ThemeOptions = {
  palette: {
    mode: 'light',
    primary: {
      main: '#3B6FD4', // brand primaryStrong — белый текст AA (аудит 2026-08-01)
      light: brandColors.primary[500], // #5B8DEF — навигация/акценты
      dark: brandColors.primary[700],
      contrastText: '#FFFFFF',
    },
    secondary: {
      main: brandColors.secondary[500],
      light: brandColors.secondary[300],
      dark: brandColors.secondary[700],
      contrastText: '#FFFFFF',
    },
    ...semanticColors,
    background: {
      default: '#EEF3FF', // neutral.background — светлая перивинкл-подложка
      paper: '#FFFFFF',
    },
    text: {
      primary: '#2D3561', // neutral.text — глубокий индиго-чаркоал
      secondary: '#58609A', // neutral.textMuted (5.32:1 AA, аудит 2026-08-01)
      disabled: alpha('#2D3561', 0.38),
    },
    divider: '#B9C7EE', // neutral.outline
    action: {
      active: alpha('#2D3561', 0.54),
      hover: alpha(brandColors.primary[500], 0.06),
      selected: alpha(brandColors.primary[500], 0.12),
      disabled: alpha('#2D3561', 0.26),
      disabledBackground: alpha('#2D3561', 0.12),
    },
    // Custom admin colors
    admin: {
      sidebar: '#FFFFFF',
      sidebarText: '#2D3561',
      header: '#FFFFFF',
      border: '#B9C7EE',
      hover: alpha(brandColors.primary[500], 0.06),
      selected: alpha(brandColors.primary[500], 0.12),
      chart: chartColors,
    },
    // Speaking Trainer palette (record/timer/status)
    speaking: speakingLight,
  },
  typography,
  shape: {
    borderRadius: 16, // radius.button — игровая мягкость
  },
  shadows: lightShadows,
  spacing: 8,
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          textTransform: 'none',
          fontWeight: 600,
          padding: '8px 16px',
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 12px rgba(91, 141, 239, 0.3)',
          },
          '&:focus-visible': {
            boxShadow: focusRing,
          },
        },
        contained: {
          boxShadow: cardShadow,
        },
        containedPrimary: {
          background: brandColors.primary[500], // плоский brand, без градиента
          '&:hover': {
            background: brandColors.primary[600],
          },
        },
        containedSecondary: {
          background: brandColors.secondary[500],
          '&:hover': {
            background: brandColors.secondary[700],
          },
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
          borderRadius: 22, // radius.card — фирменный радиус Variant B
          boxShadow: cardShadow,
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(45,53,97,0.10)',
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
          fontWeight: 700,
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: '16px',
          borderBottom: `1px solid ${alpha('#2D3561', 0.08)}`,
        },
        head: {
          fontWeight: 700,
          backgroundColor: alpha('#5B8DEF', 0.06),
          color: '#2D3561',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha('#5B8DEF', 0.04),
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          boxShadow: cardShadow,
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          borderRight: 'none',
          boxShadow: '2px 0 8px rgba(45,53,97,0.06)',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          margin: '4px 8px',
          padding: '10px 16px',
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          '&:hover': {
            backgroundColor: alpha(brandColors.primary[500], 0.08),
          },
          '&.Mui-selected': {
            backgroundColor: brandColors.primary[100], // primaryContainer #DDE8FD
            color: brandColors.primary[900], // onPrimaryContainer #1A2F5E
            '&:hover': {
              backgroundColor: alpha(brandColors.primary[500], 0.18),
            },
            '& .MuiListItemIcon-root': {
              color: brandColors.primary[900],
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          minWidth: 40,
          color: '#58609A',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 16,
            '& fieldset': {
              borderWidth: 1,
              borderColor: '#B9C7EE',
            },
            '&:hover fieldset': {
              borderWidth: 1.5,
              borderColor: brandColors.primary[300],
            },
            '&.Mui-focused fieldset': {
              borderWidth: 2,
              borderColor: brandColors.primary[500],
            },
          },
        },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 12, // radius.chip
          fontWeight: 600,
        },
      },
    },
    MuiAvatar: {
      styleOverrides: {
        root: {
          fontWeight: 600,
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
          borderRadius: 8,
          fontSize: '0.75rem',
          fontWeight: 600,
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          borderRadius: 16,
          boxShadow: '0 4px 20px rgba(45,53,97,0.14)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 22,
          boxShadow: '0 24px 48px rgba(45,53,97,0.22)',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
  },
};

// =============================================================================
// DARK THEME (tokens.json color.dark)
// =============================================================================

const darkThemeOptions: ThemeOptions = {
  palette: {
    mode: 'dark',
    primary: {
      main: '#8FB3F5', // dark.primary
      light: '#BBD0FA',
      dark: '#5B8DEF',
      contrastText: '#161A2E',
    },
    secondary: {
      main: '#B79EED', // dark.secondary
      light: '#D5C5F5',
      dark: '#9B7EDE',
      contrastText: '#161A2E',
    },
    success: {
      light: '#81C784',
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
      contrastText: '#161A2E',
    },
    info: {
      light: '#BBD0FA',
      main: '#8FB3F5',
      dark: '#5B8DEF',
      contrastText: '#161A2E',
    },
    background: {
      default: '#161A2E', // dark.background — индиго-ночь
      paper: '#1F2440', // dark.surface
    },
    text: {
      primary: '#E8EAF6', // dark.text
      secondary: '#9AA0C4', // dark.textMuted
      disabled: alpha('#E8EAF6', 0.38),
    },
    divider: '#3D4568', // dark.outline
    action: {
      active: alpha('#E8EAF6', 0.7),
      hover: alpha('#8FB3F5', 0.08),
      selected: alpha('#8FB3F5', 0.16),
      disabled: alpha('#E8EAF6', 0.3),
      disabledBackground: alpha('#E8EAF6', 0.12),
    },
    // Custom admin colors for dark mode
    admin: {
      sidebar: '#1F2440',
      sidebarText: '#E8EAF6',
      header: '#1F2440',
      border: '#3D4568',
      hover: alpha('#8FB3F5', 0.08),
      selected: alpha('#8FB3F5', 0.2),
      chart: chartColors.map(c => c + 'CC'), // Add transparency for dark mode
    },
    speaking: speakingDark,
  },
  typography,
  shape: lightThemeOptions.shape,
  shadows: darkShadows,
  spacing: 8,
  components: {
    ...lightThemeOptions.components,
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          textTransform: 'none',
          fontWeight: 600,
          padding: '8px 16px',
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          '&:hover': {
            transform: 'translateY(-1px)',
            boxShadow: '0 4px 12px rgba(143, 179, 245, 0.3)',
          },
          '&:focus-visible': {
            boxShadow: '0 0 0 2px #161A2E, 0 0 0 4px #8FB3F5',
          },
        },
        contained: {
          boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
        },
        containedPrimary: {
          background: '#8FB3F5',
          color: '#161A2E',
          '&:hover': {
            background: '#5B8DEF',
          },
        },
        containedSecondary: {
          background: '#B79EED',
          color: '#161A2E',
          '&:hover': {
            background: '#9B7EDE',
          },
        },
        outlined: {
          borderWidth: 1.5,
          borderColor: alpha('#E8EAF6', 0.23),
          '&:hover': {
            borderWidth: 1.5,
            borderColor: alpha('#E8EAF6', 0.4),
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
          borderRadius: 22,
          backgroundColor: '#1F2440',
          border: `1px solid ${alpha('#E8EAF6', 0.08)}`,
          boxShadow: '0 4px 12px rgba(0,0,0,0.3)',
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          '&:hover': {
            boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
            borderColor: alpha('#E8EAF6', 0.12),
          },
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          padding: '16px',
          borderBottom: `1px solid ${alpha('#E8EAF6', 0.08)}`,
          color: '#E8EAF6',
        },
        head: {
          fontWeight: 700,
          backgroundColor: alpha('#8FB3F5', 0.08),
          color: '#E8EAF6',
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:hover': {
            backgroundColor: alpha('#8FB3F5', 0.06),
          },
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#1F2440',
          boxShadow: '0 2px 8px rgba(0,0,0,0.3)',
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          backgroundColor: '#1F2440',
          borderRight: `1px solid ${alpha('#E8EAF6', 0.08)}`,
          boxShadow: '2px 0 8px rgba(0,0,0,0.3)',
        },
      },
    },
    MuiListItemButton: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          margin: '4px 8px',
          padding: '10px 16px',
          transition: 'all 0.2s cubic-bezier(0.16, 1, 0.3, 1)',
          '&:hover': {
            backgroundColor: alpha('#8FB3F5', 0.08),
          },
          '&.Mui-selected': {
            backgroundColor: alpha('#8FB3F5', 0.16),
            color: '#8FB3F5',
            '&:hover': {
              backgroundColor: alpha('#8FB3F5', 0.24),
            },
            '& .MuiListItemIcon-root': {
              color: '#8FB3F5',
            },
          },
        },
      },
    },
    MuiListItemIcon: {
      styleOverrides: {
        root: {
          minWidth: 40,
          color: '#9AA0C4',
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          '& .MuiOutlinedInput-root': {
            borderRadius: 16,
            backgroundColor: alpha('#E8EAF6', 0.05),
            '& fieldset': {
              borderWidth: 1,
              borderColor: '#3D4568',
            },
            '&:hover fieldset': {
              borderWidth: 1.5,
              borderColor: alpha('#E8EAF6', 0.4),
            },
            '&.Mui-focused fieldset': {
              borderWidth: 2,
              borderColor: '#8FB3F5',
            },
          },
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: 'none',
          backgroundColor: '#1F2440',
        },
      },
    },
    MuiMenu: {
      styleOverrides: {
        paper: {
          backgroundColor: '#1F2440',
          border: `1px solid ${alpha('#E8EAF6', 0.08)}`,
          boxShadow: '0 4px 20px rgba(0,0,0,0.4)',
        },
      },
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          backgroundColor: '#1F2440',
          border: `1px solid ${alpha('#E8EAF6', 0.08)}`,
          boxShadow: '0 24px 48px rgba(0,0,0,0.4)',
        },
      },
    },
    MuiSkeleton: {
      styleOverrides: {
        root: {
          backgroundColor: alpha('#E8EAF6', 0.1),
          borderRadius: 8,
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
  interface SpeakingPalette {
    record: string;
    recordActive: string;
    recordShadow: string;
    waveformPlayback: string;
    timer: {
      level80: string;
      level50: string;
      level30: string;
    };
    status: {
      new: string;
      newContainer: string;
      reviewed: string;
      reviewedContainer: string;
    };
  }
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
    speaking: SpeakingPalette;
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
    speaking?: SpeakingPalette;
  }
}

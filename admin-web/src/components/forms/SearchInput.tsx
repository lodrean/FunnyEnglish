import React, { useState, useCallback, useRef, useEffect } from 'react';
import {
  TextField,
  InputAdornment,
  IconButton,
  Box,
  Paper,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  CircularProgress,
  Typography,
  Fade,
  ClickAwayListener,
  useTheme,
  alpha,
} from '@mui/material';
import {
  Search,
  Clear,
  History,
  TrendingUp,
} from '@mui/icons-material';

// Цвета — из темы (bd ku2: уход от хардкодов #4A90D9/MUI-grey)

// Search result item type
export interface SearchResult {
  id: string | number;
  title: string;
  subtitle?: string;
  icon?: React.ReactNode;
  category?: string;
}

// Search input props
export interface SearchInputProps {
  /** Input placeholder */
  placeholder?: string;
  /** Debounce delay in milliseconds */
  debounceMs?: number;
  /** Whether search is loading */
  loading?: boolean;
  /** Search results to display */
  results?: SearchResult[];
  /** Recent searches */
  recentSearches?: string[];
  /** Popular searches */
  popularSearches?: string[];
  /** Callback when search value changes */
  onSearch: (value: string) => void;
  /** Callback when a result is selected */
  onSelectResult?: (result: SearchResult) => void;
  /** Callback when a recent search is selected */
  onSelectRecent?: (search: string) => void;
  /** Callback when search is cleared */
  onClear?: () => void;
  /** Custom class name */
  className?: string;
  /** Full width styling */
  fullWidth?: boolean;
  /** Input size */
  size?: 'small' | 'medium';
  /** Whether to show results dropdown */
  showResults?: boolean;
  /** Whether to show recent searches */
  showRecent?: boolean;
  /** Whether to show popular searches */
  showPopular?: boolean;
  /** Custom no results message */
  noResultsMessage?: string;
  /** Input value (controlled) */
  value?: string;
  /** Default input value */
  defaultValue?: string;
  /** Whether input is disabled */
  disabled?: boolean;
  /** Auto-focus on mount */
  autoFocus?: boolean;
}

/**
 * Search Input Component
 * 
 * A debounced search input with loading state, clear button,
 * and optional results dropdown with recent/popular searches.
 * 
 * @example
 * ```tsx
 * <SearchInput
 *   placeholder="Search users..."
 *   onSearch={(value) => console.log('Search:', value)}
 *   loading={isSearching}
 *   results={searchResults}
 *   onSelectResult={(result) => console.log('Selected:', result)}
 *   debounceMs={300}
 * />
 * ```
 */
export function SearchInput({
  placeholder = 'Search...',
  debounceMs = 300,
  loading = false,
  results = [],
  recentSearches = [],
  popularSearches = [],
  onSearch,
  onSelectResult,
  onSelectRecent,
  onClear,
  className,
  fullWidth = true,
  size = 'small',
  showResults = true,
  showRecent = true,
  showPopular = true,
  noResultsMessage = 'No results found',
  value: controlledValue,
  defaultValue = '',
  disabled = false,
  autoFocus = false,
}: SearchInputProps): React.ReactElement {
  
  const theme = useTheme();
  const [inputValue, setInputValue] = useState(defaultValue);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);
  const inputRef = useRef<HTMLInputElement>(null);
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);

  // Use controlled or uncontrolled value
  const value = controlledValue !== undefined ? controlledValue : inputValue;

  // Debounced search handler
  const debouncedSearch = useCallback(
    (searchValue: string) => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }

      debounceTimerRef.current = setTimeout(() => {
        onSearch(searchValue);
      }, debounceMs);
    },
    [debounceMs, onSearch]
  );

  // Handle input change
  const handleChange = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      const newValue = event.target.value;
      setInputValue(newValue);
      debouncedSearch(newValue);
      setIsDropdownOpen(true);
      setActiveIndex(-1);
    },
    [debouncedSearch]
  );

  // Handle clear
  const handleClear = useCallback(() => {
    setInputValue('');
    onSearch('');
    onClear?.();
    setIsDropdownOpen(false);
    setActiveIndex(-1);
    inputRef.current?.focus();
  }, [onClear, onSearch]);

  // Handle result selection
  const handleSelectResult = useCallback(
    (result: SearchResult) => {
      setInputValue(result.title);
      onSelectResult?.(result);
      setIsDropdownOpen(false);
    },
    [onSelectResult]
  );

  // Handle recent search selection
  const handleSelectRecent = useCallback(
    (search: string) => {
      setInputValue(search);
      onSearch(search);
      onSelectRecent?.(search);
      setIsDropdownOpen(false);
    },
    [onSearch, onSelectRecent]
  );

  // Handle keyboard navigation
  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent) => {
      const allItems = [
        ...(showRecent && !value ? recentSearches : []),
        ...(showPopular && !value ? popularSearches : []),
        ...(showResults && value ? results : []),
      ];

      switch (event.key) {
        case 'ArrowDown':
          event.preventDefault();
          setActiveIndex((prev) =>
            prev < allItems.length - 1 ? prev + 1 : prev
          );
          setIsDropdownOpen(true);
          break;
        case 'ArrowUp':
          event.preventDefault();
          setActiveIndex((prev) => (prev > 0 ? prev - 1 : -1));
          break;
        case 'Enter':
          event.preventDefault();
          if (activeIndex >= 0) {
            if (showResults && value && results[activeIndex]) {
              handleSelectResult(results[activeIndex]);
            } else if (!value && recentSearches[activeIndex]) {
              handleSelectRecent(recentSearches[activeIndex]);
            }
          } else {
            onSearch(value);
            setIsDropdownOpen(false);
          }
          break;
        case 'Escape':
          setIsDropdownOpen(false);
          setActiveIndex(-1);
          break;
      }
    },
    [
      activeIndex,
      handleSelectRecent,
      handleSelectResult,
      onSearch,
      popularSearches,
      recentSearches,
      results,
      showPopular,
      showRecent,
      showResults,
      value,
    ]
  );

  // Close dropdown when clicking outside
  const handleClickAway = useCallback(() => {
    setIsDropdownOpen(false);
    setActiveIndex(-1);
  }, []);

  // Focus input on mount if autoFocus
  useEffect(() => {
    if (autoFocus) {
      inputRef.current?.focus();
    }
  }, [autoFocus]);

  // Cleanup debounce timer
  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  const hasDropdownContent =
    (showRecent && recentSearches.length > 0 && !value) ||
    (showPopular && popularSearches.length > 0 && !value) ||
    (showResults && (results.length > 0 || (value && !loading)));

  return (
    <ClickAwayListener onClickAway={handleClickAway}>
      <Box className={className} sx={{ position: 'relative', width: fullWidth ? '100%' : 'auto' }}>
        <TextField
          inputRef={inputRef}
          value={value}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onFocus={() => setIsDropdownOpen(true)}
          placeholder={placeholder}
          disabled={disabled}
          fullWidth={fullWidth}
          size={size}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                {loading ? (
                  <CircularProgress size={20} sx={{ color: theme.palette.primary.main }} />
                ) : (
                  <Search sx={{ color: theme.palette.text.secondary }} />
                )}
              </InputAdornment>
            ),
            endAdornment: value && (
              <InputAdornment position="end">
                <IconButton
                  size="small"
                  onClick={handleClear}
                  edge="end"
                  aria-label="Clear search"
                  sx={{
                    color: theme.palette.text.secondary,
                    '&:hover': {
                      color: theme.palette.text.primary,
                    },
                  }}
                >
                  <Clear fontSize="small" />
                </IconButton>
              </InputAdornment>
            ),
          }}
          sx={{
            '& .MuiOutlinedInput-root': {
              '& fieldset': {
                borderColor: theme.palette.divider,
              },
              '&:hover fieldset': {
                borderColor: theme.palette.primary.main,
              },
              '&.Mui-focused fieldset': {
                borderColor: theme.palette.primary.main,
              },
            },
          }}
        />

        {/* Dropdown */}
        <Fade in={Boolean(isDropdownOpen && hasDropdownContent)}>
          <Paper
            elevation={4}
            sx={{
              position: 'absolute',
              top: '100%',
              left: 0,
              right: 0,
              mt: 0.5,
              maxHeight: 400,
              overflow: 'auto',
              zIndex: 1300,
              display: isDropdownOpen && hasDropdownContent ? 'block' : 'none',
            }}
          >
            {/* Recent Searches */}
            {showRecent && !value && recentSearches.length > 0 && (
              <>
                <Box sx={{ px: 2, py: 1, backgroundColor: theme.palette.background.default }}>
                  <Typography
                    variant="caption"
                    sx={{
                      color: theme.palette.text.secondary,
                      fontWeight: 500,
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.5,
                    }}
                  >
                    <History fontSize="small" />
                    Recent Searches
                  </Typography>
                </Box>
                <List dense disablePadding>
                  {recentSearches.map((search, index) => (
                    <ListItem key={search} disablePadding>
                      <ListItemButton
                        selected={activeIndex === index}
                        onClick={() => handleSelectRecent(search)}
                        sx={{
                          '&:hover': {
                            backgroundColor: alpha(theme.palette.primary.main, 0.08),
                          },
                          '&.Mui-selected': {
                            backgroundColor: alpha(theme.palette.primary.main, 0.12),
                          },
                        }}
                      >
                        <ListItemText
                          primary={search}
                          primaryTypographyProps={{
                            variant: 'body2',
                            color: theme.palette.text.primary,
                          }}
                        />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </List>
              </>
            )}

            {/* Popular Searches */}
            {showPopular && !value && popularSearches.length > 0 && (
              <>
                <Box sx={{ px: 2, py: 1, backgroundColor: theme.palette.background.default }}>
                  <Typography
                    variant="caption"
                    sx={{
                      color: theme.palette.text.secondary,
                      fontWeight: 500,
                      display: 'flex',
                      alignItems: 'center',
                      gap: 0.5,
                    }}
                  >
                    <TrendingUp fontSize="small" />
                    Popular Searches
                  </Typography>
                </Box>
                <List dense disablePadding>
                  {popularSearches.map((search, index) => (
                    <ListItem key={search} disablePadding>
                      <ListItemButton
                        selected={
                          activeIndex ===
                          index + (showRecent ? recentSearches.length : 0)
                        }
                        onClick={() => handleSelectRecent(search)}
                        sx={{
                          '&:hover': {
                            backgroundColor: alpha(theme.palette.primary.main, 0.08),
                          },
                          '&.Mui-selected': {
                            backgroundColor: alpha(theme.palette.primary.main, 0.12),
                          },
                        }}
                      >
                        <ListItemText
                          primary={search}
                          primaryTypographyProps={{
                            variant: 'body2',
                            color: theme.palette.text.primary,
                          }}
                        />
                      </ListItemButton>
                    </ListItem>
                  ))}
                </List>
              </>
            )}

            {/* Search Results */}
            {showResults && value && (
              <>
                {results.length > 0 ? (
                  <List dense disablePadding>
                    {results.map((result, index) => (
                      <ListItem key={result.id} disablePadding>
                        <ListItemButton
                          selected={activeIndex === index}
                          onClick={() => handleSelectResult(result)}
                          sx={{
                            '&:hover': {
                              backgroundColor: alpha(theme.palette.primary.main, 0.08),
                            },
                            '&.Mui-selected': {
                              backgroundColor: alpha(theme.palette.primary.main, 0.12),
                            },
                          }}
                        >
                          {result.icon && (
                            <Box sx={{ mr: 1.5, color: theme.palette.primary.main }}>
                              {result.icon}
                            </Box>
                          )}
                          <ListItemText
                            primary={result.title}
                            secondary={result.subtitle}
                            primaryTypographyProps={{
                              variant: 'body2',
                              color: theme.palette.text.primary,
                            }}
                            secondaryTypographyProps={{
                              variant: 'caption',
                              color: theme.palette.text.secondary,
                            }}
                          />
                          {result.category && (
                            <Typography
                              variant="caption"
                              sx={{
                                ml: 1,
                                px: 1,
                                py: 0.25,
                                backgroundColor: theme.palette.background.default,
                                borderRadius: 1,
                                color: theme.palette.text.secondary,
                              }}
                            >
                              {result.category}
                            </Typography>
                          )}
                        </ListItemButton>
                      </ListItem>
                    ))}
                  </List>
                ) : !loading ? (
                  <Box sx={{ p: 3, textAlign: 'center' }}>
                    <Typography variant="body2" color={theme.palette.text.secondary}>
                      {noResultsMessage}
                    </Typography>
                  </Box>
                ) : null}
              </>
            )}
          </Paper>
        </Fade>
      </Box>
    </ClickAwayListener>
  );
}

export default SearchInput;

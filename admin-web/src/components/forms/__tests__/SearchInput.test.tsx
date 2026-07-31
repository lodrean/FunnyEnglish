import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { SearchInput, SearchResult } from '../SearchInput';
import { ThemeProvider, createTheme } from '@mui/material';

const theme = createTheme();

const renderWithTheme = (component: React.ReactNode) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('SearchInput', () => {
  const mockOnSearch = vi.fn();
  const mockOnSelectResult = vi.fn();

  it('renders with default placeholder', () => {
    renderWithTheme(<SearchInput onSearch={mockOnSearch} />);
    
    expect(screen.getByPlaceholderText('Search...')).toBeInTheDocument();
  });

  it('renders with custom placeholder', () => {
    renderWithTheme(
      <SearchInput onSearch={mockOnSearch} placeholder="Custom placeholder" />
    );
    
    expect(screen.getByPlaceholderText('Custom placeholder')).toBeInTheDocument();
  });

  it('shows loading state', () => {
    renderWithTheme(<SearchInput onSearch={mockOnSearch} loading={true} />);
    
    expect(screen.getByRole('progressbar')).toBeInTheDocument();
  });

  it('displays search results', () => {
    const results: SearchResult[] = [
      { id: 1, title: 'Result 1', subtitle: 'Description 1' },
      { id: 2, title: 'Result 2', subtitle: 'Description 2' },
    ];
    
    renderWithTheme(
      <SearchInput 
        onSearch={mockOnSearch} 
        results={results}
        onSelectResult={mockOnSelectResult}
      />
    );
    
    const input = screen.getByPlaceholderText('Search...');
    fireEvent.change(input, { target: { value: 'test' } });
    fireEvent.focus(input);
    
    expect(screen.getByText('Result 1')).toBeInTheDocument();
    expect(screen.getByText('Result 2')).toBeInTheDocument();
  });

  it('calls onSelectResult when result clicked', () => {
    const results: SearchResult[] = [
      { id: 1, title: 'Result 1', subtitle: 'Description 1' },
    ];
    
    renderWithTheme(
      <SearchInput 
        onSearch={mockOnSearch} 
        results={results}
        onSelectResult={mockOnSelectResult}
      />
    );
    
    const input = screen.getByPlaceholderText('Search...');
    fireEvent.change(input, { target: { value: 'test' } });
    fireEvent.focus(input);
    
    fireEvent.click(screen.getByText('Result 1'));
    
    expect(mockOnSelectResult).toHaveBeenCalledWith(results[0]);
  });

  it('displays recent searches', () => {
    const recentSearches = ['Recent 1', 'Recent 2'];
    
    renderWithTheme(
      <SearchInput 
        onSearch={mockOnSearch}
        recentSearches={recentSearches}
      />
    );
    
    const input = screen.getByPlaceholderText('Search...');
    fireEvent.focus(input);
    
    expect(screen.getByText('Recent Searches')).toBeInTheDocument();
    expect(screen.getByText('Recent 1')).toBeInTheDocument();
    expect(screen.getByText('Recent 2')).toBeInTheDocument();
  });

  it('displays popular searches', () => {
    const popularSearches = ['Popular 1', 'Popular 2'];
    
    renderWithTheme(
      <SearchInput 
        onSearch={mockOnSearch}
        popularSearches={popularSearches}
      />
    );
    
    const input = screen.getByPlaceholderText('Search...');
    fireEvent.focus(input);
    
    expect(screen.getByText('Popular Searches')).toBeInTheDocument();
    expect(screen.getByText('Popular 1')).toBeInTheDocument();
  });

  it('shows no results message', () => {
    renderWithTheme(
      <SearchInput 
        onSearch={mockOnSearch}
        results={[]}
        noResultsMessage="Custom no results"
      />
    );
    
    const input = screen.getByPlaceholderText('Search...');
    fireEvent.change(input, { target: { value: 'test' } });
    fireEvent.focus(input);
    
    expect(screen.getByText('Custom no results')).toBeInTheDocument();
  });

  it('handles disabled state', () => {
    renderWithTheme(<SearchInput onSearch={mockOnSearch} disabled={true} />);
    
    expect(screen.getByPlaceholderText('Search...')).toBeDisabled();
  });

  it('handles autoFocus', () => {
    renderWithTheme(<SearchInput onSearch={mockOnSearch} autoFocus={true} />);
    
    expect(screen.getByPlaceholderText('Search...')).toHaveFocus();
  });
});

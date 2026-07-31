import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { EmptyState, SearchEmptyState, FolderEmptyState } from '../EmptyState';
import { ThemeProvider, createTheme } from '@mui/material';

const theme = createTheme();

const renderWithTheme = (component: React.ReactNode) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('EmptyState', () => {
  it('renders with default variant', () => {
    renderWithTheme(<EmptyState />);
    
    expect(screen.getByText('No data available')).toBeInTheDocument();
    expect(screen.getByText("There are no records to display at this time.")).toBeInTheDocument();
  });

  it('renders with search variant', () => {
    renderWithTheme(<EmptyState variant="search" />);
    
    expect(screen.getByText('No results found')).toBeInTheDocument();
    expect(screen.getByText("Try adjusting your search or filters to find what you're looking for.")).toBeInTheDocument();
  });

  it('renders with folder variant', () => {
    renderWithTheme(<EmptyState variant="folder" />);
    
    expect(screen.getByText('Folder is empty')).toBeInTheDocument();
    expect(screen.getByText("This folder doesn't contain any items yet.")).toBeInTheDocument();
  });

  it('renders with custom title and message', () => {
    renderWithTheme(<EmptyState title="Custom Title" message="Custom message text" />);
    
    expect(screen.getByText('Custom Title')).toBeInTheDocument();
    expect(screen.getByText('Custom message text')).toBeInTheDocument();
  });

  it('renders with action button', () => {
    const handleClick = vi.fn();
    renderWithTheme(
      <EmptyState 
        action={{ label: 'Create New', onClick: handleClick }} 
      />
    );
    
    const button = screen.getByRole('button', { name: /create new/i });
    expect(button).toBeInTheDocument();
    
    fireEvent.click(button);
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('renders with secondary action', () => {
    const handlePrimary = vi.fn();
    const handleSecondary = vi.fn();
    renderWithTheme(
      <EmptyState 
        action={{ label: 'Primary', onClick: handlePrimary }}
        secondaryAction={{ label: 'Secondary', onClick: handleSecondary }}
      />
    );
    
    const primaryButton = screen.getByRole('button', { name: /primary/i });
    const secondaryButton = screen.getByRole('button', { name: /secondary/i });
    
    expect(primaryButton).toBeInTheDocument();
    expect(secondaryButton).toBeInTheDocument();
    
    fireEvent.click(secondaryButton);
    expect(handleSecondary).toHaveBeenCalledTimes(1);
  });

  it('renders with different sizes', () => {
    const { rerender } = renderWithTheme(<EmptyState size="small" />);
    expect(screen.getByText('No data available')).toBeInTheDocument();
    
    rerender(<ThemeProvider theme={theme}><EmptyState size="medium" /></ThemeProvider>);
    expect(screen.getByText('No data available')).toBeInTheDocument();
    
    rerender(<ThemeProvider theme={theme}><EmptyState size="large" /></ThemeProvider>);
    expect(screen.getByText('No data available')).toBeInTheDocument();
  });
});

describe('SearchEmptyState', () => {
  it('renders with search variant by default', () => {
    renderWithTheme(<SearchEmptyState />);
    
    expect(screen.getByText('No results found')).toBeInTheDocument();
  });

  it('accepts and overrides custom props', () => {
    renderWithTheme(<SearchEmptyState title="Custom Search Title" />);
    
    expect(screen.getByText('Custom Search Title')).toBeInTheDocument();
  });
});

describe('FolderEmptyState', () => {
  it('renders with folder variant by default', () => {
    renderWithTheme(<FolderEmptyState />);
    
    expect(screen.getByText('Folder is empty')).toBeInTheDocument();
  });
});

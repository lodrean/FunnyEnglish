import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StatusBadge, PublishedBadge, DraftBadge, ActiveBadge, InactiveBadge } from '../StatusBadge';
import { ThemeProvider, createTheme } from '@mui/material';

const theme = createTheme();

const renderWithTheme = (component: React.ReactNode) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('StatusBadge', () => {
  it('renders with success status', () => {
    renderWithTheme(<StatusBadge status="success" />);
    expect(screen.getByText('Success')).toBeInTheDocument();
  });

  it('renders with error status', () => {
    renderWithTheme(<StatusBadge status="error" />);
    expect(screen.getByText('Error')).toBeInTheDocument();
  });

  it('renders with warning status', () => {
    renderWithTheme(<StatusBadge status="warning" />);
    expect(screen.getByText('Warning')).toBeInTheDocument();
  });

  it('renders with info status', () => {
    renderWithTheme(<StatusBadge status="info" />);
    expect(screen.getByText('Info')).toBeInTheDocument();
  });

  it('renders with draft status', () => {
    renderWithTheme(<StatusBadge status="draft" />);
    expect(screen.getByText('Draft')).toBeInTheDocument();
  });

  it('renders with published status', () => {
    renderWithTheme(<StatusBadge status="published" />);
    expect(screen.getByText('Published')).toBeInTheDocument();
  });

  it('renders with archived status', () => {
    renderWithTheme(<StatusBadge status="archived" />);
    expect(screen.getByText('Archived')).toBeInTheDocument();
  });

  it('renders with active status', () => {
    renderWithTheme(<StatusBadge status="active" />);
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('renders with inactive status', () => {
    renderWithTheme(<StatusBadge status="inactive" />);
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });

  it('renders with pending status', () => {
    renderWithTheme(<StatusBadge status="pending" />);
    expect(screen.getByText('Pending')).toBeInTheDocument();
  });

  it('renders with custom label', () => {
    renderWithTheme(<StatusBadge status="success" label="Completed" />);
    expect(screen.getByText('Completed')).toBeInTheDocument();
  });

  it('renders with filled variant', () => {
    renderWithTheme(<StatusBadge status="success" variant="filled" />);
    expect(screen.getByText('Success')).toBeInTheDocument();
  });

  it('renders with outlined variant', () => {
    renderWithTheme(<StatusBadge status="success" variant="outlined" />);
    expect(screen.getByText('Success')).toBeInTheDocument();
  });

  it('renders with light variant (default)', () => {
    renderWithTheme(<StatusBadge status="success" />);
    expect(screen.getByText('Success')).toBeInTheDocument();
  });

  it('renders with small size', () => {
    renderWithTheme(<StatusBadge status="success" size="small" />);
    expect(screen.getByText('Success')).toBeInTheDocument();
  });

  it('renders with medium size', () => {
    renderWithTheme(<StatusBadge status="success" size="medium" />);
    expect(screen.getByText('Success')).toBeInTheDocument();
  });

  it('renders with dot indicator', () => {
    renderWithTheme(<StatusBadge status="success" dot />);
    const badge = screen.getByText('Success');
    expect(badge).toBeInTheDocument();
  });
});

describe('Convenience Exports', () => {
  it('PublishedBadge renders with published status', () => {
    renderWithTheme(<PublishedBadge />);
    expect(screen.getByText('Published')).toBeInTheDocument();
  });

  it('DraftBadge renders with draft status', () => {
    renderWithTheme(<DraftBadge />);
    expect(screen.getByText('Draft')).toBeInTheDocument();
  });

  it('ActiveBadge renders with active status', () => {
    renderWithTheme(<ActiveBadge />);
    expect(screen.getByText('Active')).toBeInTheDocument();
  });

  it('InactiveBadge renders with inactive status', () => {
    renderWithTheme(<InactiveBadge />);
    expect(screen.getByText('Inactive')).toBeInTheDocument();
  });
});

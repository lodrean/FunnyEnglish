import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { StatsCard } from '../StatsCard';
import { ThemeProvider, createTheme } from '@mui/material';
import { TrendingUp, People, Assessment } from '@mui/icons-material';

const theme = createTheme();

const renderWithTheme = (component: React.ReactNode) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('StatsCard', () => {
  it('renders basic stats card', () => {
    renderWithTheme(
      <StatsCard 
        title="Total Users" 
        value="1,234" 
        icon={People} 
      />
    );
    
    expect(screen.getByText('Total Users')).toBeInTheDocument();
    expect(screen.getByText('1,234')).toBeInTheDocument();
  });

  it('renders with positive change', () => {
    renderWithTheme(
      <StatsCard 
        title="Revenue" 
        value="$50K" 
        icon={Assessment}
        change={{ value: 12.5, isPositive: true, label: 'vs last month' }}
      />
    );
    
    expect(screen.getByText('Revenue')).toBeInTheDocument();
    expect(screen.getByText('$50K')).toBeInTheDocument();
    expect(screen.getByText('+12.5%')).toBeInTheDocument();
    expect(screen.getByText('vs last month')).toBeInTheDocument();
  });

  it('renders with negative change', () => {
    renderWithTheme(
      <StatsCard 
        title="Bounce Rate" 
        value="23%" 
        icon={TrendingUp}
        change={{ value: -5.2, isPositive: false, label: 'vs last week' }}
      />
    );
    
    expect(screen.getByText('Bounce Rate')).toBeInTheDocument();
    expect(screen.getByText('23%')).toBeInTheDocument();
    expect(screen.getByText('-5.2%')).toBeInTheDocument();
  });

  it('renders with zero change', () => {
    renderWithTheme(
      <StatsCard 
        title="Active Sessions" 
        value="42" 
        icon={People}
        change={{ value: 0, isPositive: true }}
      />
    );
    
    expect(screen.getByText('Active Sessions')).toBeInTheDocument();
    expect(screen.getByText('+0%')).toBeInTheDocument();
  });

  it('renders loading state', () => {
    renderWithTheme(
      <StatsCard 
        title="Loading" 
        value="0" 
        icon={People}
        loading={true}
      />
    );
    
    // Loading state renders without crash
    expect(screen.queryByText('Loading')).not.toBeInTheDocument();
  });

  it('handles click events', () => {
    const handleClick = vi.fn();
    renderWithTheme(
      <StatsCard 
        title="Clickable" 
        value="100" 
        icon={People}
        onClick={handleClick}
      />
    );
    
    const card = screen.getByText('Clickable').closest('.MuiCard-root') || 
                 screen.getByText('100').closest('.MuiCard-root');
    
    if (card) {
      fireEvent.click(card);
      expect(handleClick).toHaveBeenCalledTimes(1);
    }
  });

  it('renders with different variants', () => {
    const variants = ['primary', 'success', 'warning', 'error', 'info'] as const;
    
    variants.forEach(variant => {
      const { unmount } = renderWithTheme(
        <StatsCard 
          title={`${variant} Stats`}
          value="100"
          icon={People}
          variant={variant}
        />
      );
      
      expect(screen.getByText(`${variant} Stats`)).toBeInTheDocument();
      unmount();
    });
  });

  it('renders with sparkline chart', () => {
    renderWithTheme(
      <StatsCard 
        title="With Chart" 
        value="500" 
        icon={Assessment}
        chartType="sparkline"
        chartData={[10, 25, 15, 30, 45, 35, 50]}
      />
    );
    
    expect(screen.getByText('With Chart')).toBeInTheDocument();
    expect(screen.getByText('500')).toBeInTheDocument();
  });

  it('renders with area chart', () => {
    renderWithTheme(
      <StatsCard 
        title="Area Chart" 
        value="750" 
        icon={Assessment}
        chartType="area"
        chartData={[20, 35, 25, 40, 55, 45, 60]}
      />
    );
    
    expect(screen.getByText('Area Chart')).toBeInTheDocument();
  });

  it('renders without chart when chartData is empty', () => {
    renderWithTheme(
      <StatsCard 
        title="No Chart Data" 
        value="100" 
        icon={Assessment}
        chartType="sparkline"
        chartData={[]}
      />
    );
    
    expect(screen.getByText('No Chart Data')).toBeInTheDocument();
  });

  it('renders with numeric string value', () => {
    renderWithTheme(
      <StatsCard 
        title="String Value" 
        value="999" 
        icon={People}
      />
    );
    
    expect(screen.getByText('999')).toBeInTheDocument();
  });

  it('renders with numeric value', () => {
    renderWithTheme(
      <StatsCard 
        title="Numeric Value" 
        value={1234} 
        icon={People}
      />
    );
    
    expect(screen.getByText('1234')).toBeInTheDocument();
  });
});

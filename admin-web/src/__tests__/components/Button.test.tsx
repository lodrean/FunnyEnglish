import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from '@mui/material';

describe('Button Component', () => {
  it('should render button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('should handle click events', () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click me</Button>);
    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('should be disabled when disabled prop is true', () => {
    render(<Button disabled>Disabled</Button>);
    expect(screen.getByText('Disabled')).toBeDisabled();
  });

  it('should render with different variants', () => {
    const { rerender } = render(<Button variant="contained">Contained</Button>);
    expect(screen.getByText('Contained')).toHaveClass('MuiButton-contained');

    rerender(<Button variant="outlined">Outlined</Button>);
    expect(screen.getByText('Outlined')).toHaveClass('MuiButton-outlined');

    rerender(<Button variant="text">Text</Button>);
    expect(screen.getByText('Text')).toHaveClass('MuiButton-text');
  });
});

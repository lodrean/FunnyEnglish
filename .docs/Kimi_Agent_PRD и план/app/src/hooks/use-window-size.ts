import { useState, useEffect } from 'react';

interface WindowSize {
  width: number;
  height: number;
}

export type WindowSizeClass = 'compact' | 'medium' | 'expanded';

export function useWindowSize(): WindowSize {
  const [windowSize, setWindowSize] = useState<WindowSize>({
    width: window.innerWidth,
    height: window.innerHeight,
  });

  useEffect(() => {
    function handleResize() {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight,
      });
    }

    window.addEventListener('resize', handleResize);
    handleResize();

    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return windowSize;
}

export function useWindowSizeClass(): WindowSizeClass {
  const { width } = useWindowSize();
  
  if (width < 600) return 'compact';
  if (width < 840) return 'medium';
  return 'expanded';
}

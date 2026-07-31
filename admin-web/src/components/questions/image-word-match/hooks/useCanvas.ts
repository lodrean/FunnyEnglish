import { useRef, useState, useEffect, useCallback } from 'react';
import { Hotspot, DrawingTool, HotspotShape } from '../../../../types/questions';

interface CanvasState {
  scale: number;
  offsetX: number;
  offsetY: number;
}

interface DrawingState {
  isDrawing: boolean;
  startX: number;
  startY: number;
  currentX: number;
  currentY: number;
  tool: DrawingTool;
}

interface UseCanvasProps {
  imageUrl: string;
  hotspots: Hotspot[];
  onAddHotspot: (hotspot: Hotspot) => void;
  onUpdateHotspot?: (id: string, updates: Partial<Hotspot>) => void;
}

export const useCanvas = ({ imageUrl, hotspots, onAddHotspot, onUpdateHotspot: _onUpdateHotspot }: UseCanvasProps) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<HTMLImageElement | null>(null);
  
  const [canvasState, setCanvasState] = useState<CanvasState>({
    scale: 1,
    offsetX: 0,
    offsetY: 0
  });
  
  const [drawingState, setDrawingState] = useState<DrawingState>({
    isDrawing: false,
    startX: 0,
    startY: 0,
    currentX: 0,
    currentY: 0,
    tool: DrawingTool.SELECT
  });
  
  const [selectedHotspotId, setSelectedHotspotId] = useState<string | null>(null);

  // Load and draw image
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.onload = () => {
      imageRef.current = img;
      canvas.width = img.naturalWidth;
      canvas.height = img.naturalHeight;
      draw();
    };
    img.src = imageUrl;
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [imageUrl]);

  // Redraw when hotspots or selection changes
  useEffect(() => {
    draw();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [hotspots, selectedHotspotId, drawingState, canvasState]);

  const draw = () => {
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    const img = imageRef.current;
    if (!canvas || !ctx || !img) return;

    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    // Save context state
    ctx.save();
    
    // Apply transformations
    ctx.translate(canvasState.offsetX, canvasState.offsetY);
    ctx.scale(canvasState.scale, canvasState.scale);
    
    // Draw image
    ctx.drawImage(img, 0, 0);
    
    // Draw hotspots
    hotspots.forEach(hotspot => {
      const isSelected = hotspot.id === selectedHotspotId;
      const x = hotspot.x * img.naturalWidth;
      const y = hotspot.y * img.naturalHeight;
      const width = hotspot.width * img.naturalWidth;
      const height = hotspot.height * img.naturalHeight;
      
      ctx.strokeStyle = isSelected ? '#3b82f6' : '#22c55e';
      ctx.lineWidth = isSelected ? 3 : 2;
      ctx.fillStyle = isSelected ? 'rgba(59, 130, 246, 0.2)' : 'rgba(34, 197, 94, 0.15)';
      
      if (hotspot.shape === 'CIRCLE') {
        const radius = Math.min(width, height) / 2;
        const centerX = x + width / 2;
        const centerY = y + height / 2;
        
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();
      } else {
        ctx.fillRect(x, y, width, height);
        ctx.strokeRect(x, y, width, height);
      }
      
      // Draw word label if linked
      if (hotspot.wordId) {
        ctx.fillStyle = '#1e293b';
        ctx.font = 'bold 14px sans-serif';
        ctx.fillText(hotspot.wordId.slice(0, 8) + '...', x + 4, y + 18);
      }
    });
    
    // Draw selection rectangle during drawing
    if (drawingState.isDrawing) {
      const startX = drawingState.startX * img.naturalWidth;
      const startY = drawingState.startY * img.naturalHeight;
      const currentX = drawingState.currentX * img.naturalWidth;
      const currentY = drawingState.currentY * img.naturalHeight;
      
      const x = Math.min(startX, currentX);
      const y = Math.min(startY, currentY);
      const width = Math.abs(currentX - startX);
      const height = Math.abs(currentY - startY);
      
      ctx.strokeStyle = '#3b82f6';
      ctx.lineWidth = 2;
      ctx.setLineDash([5, 5]);
      ctx.fillStyle = 'rgba(59, 130, 246, 0.1)';
      
      if (drawingState.tool === 'CIRCLE') {
        const radius = Math.min(width, height) / 2;
        const centerX = x + width / 2;
        const centerY = y + height / 2;
        
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();
      } else {
        ctx.fillRect(x, y, width, height);
        ctx.strokeRect(x, y, width, height);
      }
      
      ctx.setLineDash([]);
    }
    
    ctx.restore();
  };

  const getRelativeCoordinates = (clientX: number, clientY: number) => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const x = ((clientX - rect.left) * scaleX - canvasState.offsetX) / canvasState.scale;
    const y = ((clientY - rect.top) * scaleY - canvasState.offsetY) / canvasState.scale;
    const imgWidth = imageRef.current?.naturalWidth || 1;
    const imgHeight = imageRef.current?.naturalHeight || 1;
    return {
      x: Math.max(0, Math.min(1, x / imgWidth)),
      y: Math.max(0, Math.min(1, y / imgHeight))
    };
  };

  const handleMouseDown = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    const coords = getRelativeCoordinates(e.clientX, e.clientY);

    if (drawingState.tool === DrawingTool.SELECT) {
      const clickedHotspot = hotspots.find(h => 
        coords.x >= h.x && coords.x <= h.x + h.width &&
        coords.y >= h.y && coords.y <= h.y + h.height
      );
      setSelectedHotspotId(clickedHotspot?.id || null);
    } else {
      setDrawingState(prev => ({
        ...prev,
        isDrawing: true,
        startX: coords.x,
        startY: coords.y,
        currentX: coords.x,
        currentY: coords.y
      }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [drawingState.tool, hotspots]);

  const handleMouseMove = useCallback((e: React.MouseEvent) => {
    if (!drawingState.isDrawing) return;
    const coords = getRelativeCoordinates(e.clientX, e.clientY);
    setDrawingState(prev => ({
      ...prev,
      currentX: coords.x,
      currentY: coords.y
    }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [drawingState.isDrawing]);

  const handleMouseUp = useCallback(() => {
    if (drawingState.isDrawing) {
      const width = Math.abs(drawingState.currentX - drawingState.startX);
      const height = Math.abs(drawingState.currentY - drawingState.startY);

      if (width >= 0.05 && height >= 0.05) {
        const newHotspot: Hotspot = {
          id: `hotspot_${Date.now()}`,
          x: Math.min(drawingState.startX, drawingState.currentX),
          y: Math.min(drawingState.startY, drawingState.currentY),
          width,
          height,
          shape: drawingState.tool === DrawingTool.RECTANGLE ? HotspotShape.RECTANGLE : HotspotShape.CIRCLE,
          wordId: ''
        };
        onAddHotspot(newHotspot);
        setSelectedHotspotId(newHotspot.id);
      }
      setDrawingState(prev => ({ ...prev, isDrawing: false }));
    }
  }, [drawingState, onAddHotspot]);

  const handleWheel = useCallback((e: React.WheelEvent) => {
    e.preventDefault();
    const delta = e.deltaY > 0 ? 0.9 : 1.1;
    setCanvasState(prev => ({
      ...prev,
      scale: Math.max(0.5, Math.min(3, prev.scale * delta))
    }));
  }, []);

  const zoomIn = () => {
    setCanvasState(prev => ({
      ...prev,
      scale: Math.min(3, prev.scale * 1.2)
    }));
  };

  const zoomOut = () => {
    setCanvasState(prev => ({
      ...prev,
      scale: Math.max(0.5, prev.scale / 1.2)
    }));
  };

  const resetZoom = () => {
    setCanvasState({
      scale: 1,
      offsetX: 0,
      offsetY: 0
    });
  };

  return {
    canvasRef,
    containerRef,
    canvasState,
    drawingState,
    selectedHotspotId,
    setSelectedHotspotId,
    setTool: (tool: DrawingTool) => setDrawingState(prev => ({ ...prev, tool })),
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    handleWheel,
    zoomIn,
    zoomOut,
    resetZoom
  };
};

export type { CanvasState, DrawingState };

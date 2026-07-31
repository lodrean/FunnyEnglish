import React from 'react';
import { Hotspot, Word, DrawingTool } from '../../../types/questions';
import { useCanvas } from './hooks/useCanvas';
import './HotspotCanvas.css';

interface HotspotCanvasProps {
  imageUrl: string;
  hotspots: Hotspot[];
  words: Word[];
  onAddHotspot: (hotspot: Hotspot) => void;
  onUpdateHotspot: (id: string, updates: Partial<Hotspot>) => void;
  onDeleteHotspot: (id: string) => void;
  onLinkHotspotToWord: (hotspotId: string, wordId: string) => void;
}

export const HotspotCanvas: React.FC<HotspotCanvasProps> = ({
  imageUrl,
  hotspots,
  words,
  onAddHotspot,
  onUpdateHotspot,
  onDeleteHotspot,
  onLinkHotspotToWord
}) => {
  const {
    canvasRef,
    containerRef,
    canvasState,
    drawingState,
    selectedHotspotId,
    setSelectedHotspotId,
    setTool,
    handleMouseDown,
    handleMouseMove,
    handleMouseUp,
    handleWheel,
    zoomIn,
    zoomOut,
    resetZoom
  } = useCanvas({ imageUrl, hotspots, onAddHotspot, onUpdateHotspot });

  const selectedHotspot = hotspots.find(h => h.id === selectedHotspotId);
  const linkedWord = selectedHotspot ? words.find(w => w.id === selectedHotspot.wordId) : null;
  const unlinkedWords = words.filter(word => !hotspots.some(h => h.wordId === word.id));

  return (
    <div className="hotspot-canvas-container">
      {/* Toolbar */}
      <div className="hotspot-toolbar">
        <div className="tool-group">
          <button
            className={`tool-button ${drawingState.tool === DrawingTool.SELECT ? 'active' : ''}`}
            onClick={() => setTool(DrawingTool.SELECT)}
            title="Select/Move"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 3l7.07 16.97 2.51-7.39 7.39-2.51L3 3z" />
            </svg>
          </button>
          <button
            className={`tool-button ${drawingState.tool === DrawingTool.RECTANGLE ? 'active' : ''}`}
            onClick={() => setTool(DrawingTool.RECTANGLE)}
            title="Draw Rectangle"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
            </svg>
          </button>
          <button
            className={`tool-button ${drawingState.tool === DrawingTool.CIRCLE ? 'active' : ''}`}
            onClick={() => setTool(DrawingTool.CIRCLE)}
            title="Draw Circle"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
            </svg>
          </button>
        </div>

        <div className="tool-group">
          <button className="tool-button" onClick={zoomOut} title="Zoom Out">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
              <path d="M8 11h6" />
            </svg>
          </button>
          <span className="zoom-level">{Math.round(canvasState.scale * 100)}%</span>
          <button className="tool-button" onClick={zoomIn} title="Zoom In">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
              <path d="M11 8v6M8 11h6" />
            </svg>
          </button>
          <button className="tool-button" onClick={resetZoom} title="Reset Zoom">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8" />
              <path d="M3 3v5h5" />
            </svg>
          </button>
        </div>

        {selectedHotspotId && (
          <button
            className="tool-button delete-button"
            onClick={() => {
              onDeleteHotspot(selectedHotspotId);
              setSelectedHotspotId(null);
            }}
            title="Delete Hotspot"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>
        )}
      </div>

      {/* Canvas */}
      <div
        ref={containerRef}
        className="canvas-wrapper"
        onWheel={handleWheel}
      >
        <canvas
          ref={canvasRef}
          className={`hotspot-canvas ${drawingState.tool !== DrawingTool.SELECT ? 'drawing' : ''}`}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
        />
      </div>

      {/* Properties Panel */}
      {selectedHotspot && (
        <div className="properties-panel">
          <h4>Hotspot Properties</h4>
          <div className="property-row">
            <label>Linked Word:</label>
            <select
              value={selectedHotspot.wordId}
              onChange={(e) => onLinkHotspotToWord(selectedHotspot.id, e.target.value)}
            >
              <option value="">-- Select Word --</option>
              {linkedWord && (
                <option value={linkedWord.id}>{linkedWord.text}</option>
              )}
              {unlinkedWords.map(word => (
                <option key={word.id} value={word.id}>{word.text}</option>
              ))}
            </select>
          </div>
          {linkedWord && (
            <div className="word-preview">
              <span className="word-text">{linkedWord.text}</span>
              {linkedWord.translation && (
                <span className="word-translation">{linkedWord.translation}</span>
              )}
            </div>
          )}
          <div className="property-row">
            <label>Position:</label>
            <span className="coordinates">
              X: {(selectedHotspot.x * 100).toFixed(1)}% | Y: {(selectedHotspot.y * 100).toFixed(1)}%
            </span>
          </div>
          <div className="property-row">
            <label>Size:</label>
            <span className="coordinates">
              {(selectedHotspot.width * 100).toFixed(1)}% × {(selectedHotspot.height * 100).toFixed(1)}%
            </span>
          </div>
          <div className="property-row">
            <label>Shape:</label>
            <span className="coordinates">{selectedHotspot.shape}</span>
          </div>
        </div>
      )}

      {!selectedHotspot && (
        <div className="properties-panel info">
          <p>💡 <strong>Tip:</strong> Select a tool and draw on the image to create hotspots.</p>
          <p>Each hotspot must be linked to a word.</p>
        </div>
      )}
    </div>
  );
};

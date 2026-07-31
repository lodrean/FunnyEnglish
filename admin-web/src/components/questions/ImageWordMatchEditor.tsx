import React, { useState, useCallback, useEffect } from 'react';
import { 
  ImageWordMatchContent, 
  Word, 
  Hotspot, 
  CreateImageWordMatchRequest 
} from '../../types/questions';
import { uploadMedia } from '../../api/client';
import { HotspotCanvas } from './image-word-match/HotspotCanvas';
import { Alert, CircularProgress, Chip, Box } from '@mui/material';
import { Save as SaveIcon } from '@mui/icons-material';
import './ImageWordMatchEditor.css';

interface ImageWordMatchEditorProps {
  testId: string;
  initialContent?: ImageWordMatchContent;
  onSave: (data: CreateImageWordMatchRequest) => void;
  onCancel: () => void;
}

type EditorStep = 'image' | 'words' | 'hotspots' | 'preview';

export const ImageWordMatchEditor: React.FC<ImageWordMatchEditorProps> = ({
  testId,
  initialContent,
  onSave,
  onCancel
}) => {
  const [currentStep, setCurrentStep] = useState<EditorStep>('image');
  const [instruction, setInstruction] = useState(initialContent?.instruction || '');
  const [imageUrl, setImageUrl] = useState(initialContent?.imageUrl || '');
  const [words, setWords] = useState<Word[]>(initialContent?.words || []);
  const [hotspots, setHotspots] = useState<Hotspot[]>(initialContent?.hotspots || []);
  const [newWordText, setNewWordText] = useState('');
  const [newWordTranslation, setNewWordTranslation] = useState('');
  const [isUploading, setIsUploading] = useState(false);

  const addWord = () => {
    if (!newWordText.trim() || words.length >= 8) return;
    const newWord: Word = {
      id: `word_${Date.now()}`,
      text: newWordText.trim(),
      translation: newWordTranslation.trim() || undefined
    };
    setWords([...words, newWord]);
    setNewWordText('');
    setNewWordTranslation('');
  };

  const removeWord = (wordId: string) => {
    setWords(words.filter(w => w.id !== wordId));
    setHotspots(hotspots.filter(h => h.wordId !== wordId));
  };

  const handleAddHotspot = useCallback((hotspot: Hotspot) => {
    setHotspots(prev => [...prev, hotspot]);
  }, []);

  const handleUpdateHotspot = useCallback((id: string, updates: Partial<Hotspot>) => {
    setHotspots(prev => prev.map(h => h.id === id ? { ...h, ...updates } : h));
  }, []);

  const handleDeleteHotspot = useCallback((id: string) => {
    setHotspots(prev => prev.filter(h => h.id !== id));
  }, []);

  const handleLinkHotspotToWord = useCallback((hotspotId: string, wordId: string) => {
    setHotspots(prev => prev.map(h => h.id === hotspotId ? { ...h, wordId } : h));
  }, []);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    
    if (file.size > 5 * 1024 * 1024) {
      alert('Image size must be less than 5MB');
      return;
    }
    
    if (!file.type.startsWith('image/')) {
      alert('Please upload an image file');
      return;
    }
    
    setIsUploading(true);
    try {
      // Upload image using the media API
      const url = await uploadMedia(file, 'questions');
      setImageUrl(url);
      setCurrentStep('words');
    } catch (error) {
      console.error('Upload error:', error);
      alert('Failed to upload image. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  const canProceedToHotspots = words.length >= 2;
  const canSave = hotspots.length === words.length && 
    hotspots.every(h => h.wordId && words.some(w => w.id === h.wordId));
  const linkedCount = hotspots.filter(h => h.wordId).length;

  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [isSaved, setIsSaved] = useState(false);

  useEffect(() => {
    // Reset saved state when content changes
    setIsSaved(false);
  }, [imageUrl, words, hotspots, instruction]);

  const handleSave = async () => {
    if (!canSave) return;
    
    setIsSaving(true);
    setSaveError(null);
    
    try {
      await onSave({
        testId,
        instruction: instruction || 'Match the words to the objects in the image',
        imageUrl,
        words,
        hotspots,
        points: words.length * 5
      });
      setIsSaved(true);
    } catch (error: any) {
      setSaveError(error.message || 'Failed to save question');
    } finally {
      setIsSaving(false);
    }
  };

  // Step renderers
  const renderImageStep = () => (
    <div className="editor-step-content" data-testid="image-step">
      <h3>Step 1: Upload Image</h3>
      <p className="step-description">
        Upload an image with distinct objects that students can identify.
        <br />
        Supported formats: PNG, JPG. Max size: 5MB.
      </p>
      <div className="upload-area">
        {imageUrl ? (
          <div className="preview-container">
            <img src={imageUrl} alt="Preview" className="image-preview" />
            <button className="change-image-button" onClick={() => setImageUrl('')}>
              Change Image
            </button>
          </div>
        ) : (
          <label className="upload-label" data-testid="image-upload-label">
            <input
              type="file"
              accept="image/*"
              onChange={handleImageUpload}
              disabled={isUploading}
              className="file-input"
              data-testid="image-upload-input"
            />
            <div className="upload-placeholder">
              {isUploading ? (
                <span>Uploading...</span>
              ) : (
                <>
                  <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                    <polyline points="17 8 12 3 7 8" />
                    <line x1="12" y1="3" x2="12" y2="15" />
                  </svg>
                  <span>Click to upload image</span>
                  <span className="upload-hint">PNG, JPG up to 5MB</span>
                </>
              )}
            </div>
          </label>
        )}
      </div>
      {imageUrl && (
        <button className="next-button" onClick={() => setCurrentStep('words')}>
          Continue to Words →
        </button>
      )}
    </div>
  );

  const renderWordsStep = () => (
    <div className="editor-step-content" data-testid="words-step">
      <h3>Step 2: Add Words ({words.length}/8)</h3>
      <p className="step-description">
        Add 2-8 words that students will match to objects in the image.
      </p>
      
      <div className="instruction-input">
        <label>Instruction (optional):</label>
        <input
          type="text"
          placeholder="Match the words to the objects"
          value={instruction}
          onChange={(e) => setInstruction(e.target.value)}
        />
      </div>

      <div className="word-input-section">
        <div className="input-row">
          <input
            type="text"
            placeholder="Word (e.g., 'refrigerator')"
            value={newWordText}
            onChange={(e) => setNewWordText(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && addWord()}
            className="word-input"
            data-testid="word-input"
          />
          <input
            type="text"
            placeholder="Translation (optional)"
            value={newWordTranslation}
            onChange={(e) => setNewWordTranslation(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && addWord()}
            className="translation-input"
            data-testid="translation-input"
          />
          <button 
            onClick={addWord} 
            disabled={!newWordText.trim() || words.length >= 8}
            className="add-button"
            data-testid="add-word-button"
          >
            Add
          </button>
        </div>
      </div>

      {words.length > 0 && (
        <div className="word-list">
          {words.map((word, index) => (
            <div key={word.id} className="word-card">
              <span className="word-number">{index + 1}</span>
              <div className="word-info">
                <span className="word-text">{word.text}</span>
                {word.translation && (
                  <span className="word-translation">{word.translation}</span>
                )}
              </div>
              <button onClick={() => removeWord(word.id)} className="remove-button" data-testid="remove-word-button">
                ×
              </button>
            </div>
          ))}
        </div>
      )}

      <div className="step-actions">
        <button className="back-button" onClick={() => setCurrentStep('image')}>
          ← Back
        </button>
        <button 
          className="next-button" 
          onClick={() => setCurrentStep('hotspots')}
          disabled={!canProceedToHotspots}
          data-testid="continue-to-hotspots"
        >
          Continue →
        </button>
      </div>
    </div>
  );

  const renderHotspotsStep = () => (
    <div className="editor-step-content" data-testid="hotspots-step">
      <h3>Step 3: Draw Hotspots</h3>
      <p className="step-description">
        Draw shapes around objects in the image and link them to words.
      </p>
      
      <HotspotCanvas
        imageUrl={imageUrl}
        hotspots={hotspots}
        words={words}
        onAddHotspot={handleAddHotspot}
        onUpdateHotspot={handleUpdateHotspot}
        onDeleteHotspot={handleDeleteHotspot}
        onLinkHotspotToWord={handleLinkHotspotToWord}
      />

      <div className="progress-indicator">
        <div className="progress-bar">
          <div 
            className="progress-fill" 
            style={{ width: `${(linkedCount / words.length) * 100}%` }}
          />
        </div>
        <span className="progress-text">
          {linkedCount} of {words.length} words linked
        </span>
      </div>

      <div className="step-actions">
        <button className="back-button" onClick={() => setCurrentStep('words')}>
          ← Back
        </button>
        <button 
          className="preview-button" 
          onClick={() => setCurrentStep('preview')}
          disabled={!canSave}
          data-testid="preview-button"
        >
          Preview →
        </button>
      </div>
    </div>
  );

  const renderPreviewStep = () => (
    <div className="editor-step-content" data-testid="preview-step">
      <h3>Preview</h3>
      
      {saveError && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {saveError}
        </Alert>
      )}
      
      {isSaved && (
        <Alert severity="success" sx={{ mb: 2 }}>
          Question saved successfully!
        </Alert>
      )}
      <p className="step-description">
        This is how the question will appear to students.
      </p>
      
      <div className="preview-container">
        <div className="preview-image-wrapper">
          <img src={imageUrl} alt="Question" className="preview-image" />
          {hotspots.map(hotspot => {
            const linkedWord = words.find(w => w.id === hotspot.wordId);
            return (
              <div
                key={hotspot.id}
                className="preview-hotspot"
                style={{
                  left: `${hotspot.x * 100}%`,
                  top: `${hotspot.y * 100}%`,
                  width: `${hotspot.width * 100}%`,
                  height: `${hotspot.height * 100}%`,
                  borderRadius: hotspot.shape === 'CIRCLE' ? '50%' : '4px'
                }}
              >
                {linkedWord && (
                  <span className="preview-hotspot-label">{linkedWord.text}</span>
                )}
              </div>
            );
          })}
        </div>
        
        <div className="preview-words">
          <p className="preview-instruction">
            {instruction || 'Match the words to the objects:'}
          </p>
          <div className="preview-word-list">
            {words.map(word => (
              <div key={word.id} className="preview-word-chip">
                {word.text}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="step-actions">
        <button className="back-button" onClick={() => setCurrentStep('hotspots')}>
          ← Edit Hotspots
        </button>
        <button 
          className="save-button" 
          onClick={handleSave}
          disabled={isSaving || !canSave}
          data-testid="save-question-button"
        >
          {isSaving ? (
            <>
              <CircularProgress size={16} sx={{ mr: 1 }} />
              Saving...
            </>
          ) : isSaved ? (
            <>
              <SaveIcon sx={{ mr: 1 }} />
              Saved!
            </>
          ) : (
            'Save Question'
          )}
        </button>
      </div>
    </div>
  );

  const stepNames: Record<EditorStep, string> = {
    image: 'Image',
    words: 'Words',
    hotspots: 'Hotspots',
    preview: 'Preview'
  };

  return (
    <div className="image-word-match-editor" data-testid="image-word-match-editor">
      <div className="editor-header">
        <h2>Create Image-Word Match Question</h2>
        <Box display="flex" alignItems="center" gap={1}>
          {isSaved && (
            <Chip 
              label="Saved" 
              color="success" 
              size="small" 
              icon={<SaveIcon />}
            />
          )}
          <button onClick={onCancel} className="cancel-button">Cancel</button>
        </Box>
      </div>

      <div className="step-indicator">
        {(Object.keys(stepNames) as EditorStep[]).map((step, index) => (
          <React.Fragment key={step}>
            <div
              className={`step ${currentStep === step ? 'active' : ''} ${
                (Object.keys(stepNames) as EditorStep[]).indexOf(currentStep) > index ? 'completed' : ''
              }`}
              onClick={() => setCurrentStep(step)}
              role="button"
              tabIndex={0}
              onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') setCurrentStep(step); }}
              style={{ cursor: 'pointer' }}
            >
              <span className="step-number">{index + 1}</span>
              <span className="step-name">{stepNames[step]}</span>
            </div>
            {index < 3 && <div className="step-connector" />}
          </React.Fragment>
        ))}
      </div>

      <div className="editor-content">
        {currentStep === 'image' && renderImageStep()}
        {currentStep === 'words' && renderWordsStep()}
        {currentStep === 'hotspots' && renderHotspotsStep()}
        {currentStep === 'preview' && renderPreviewStep()}
      </div>
    </div>
  );
};

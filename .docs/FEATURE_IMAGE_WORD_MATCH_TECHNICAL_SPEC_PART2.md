# Image-Word Match Feature - Technical Specification (Part 2)
## Admin Panel Components, Mobile Implementation, Testing & Animations

---

## 3. Admin Panel Implementation (continued)

### 3.4 HotspotCanvas Component

#### HotspotCanvas.tsx
```typescript
// admin-web/src/components/questions/image-word-match/HotspotCanvas.tsx

import React from 'react';
import { Hotspot, Word, DrawingTool } from '../../../types/questions';
import { useCanvas } from './hooks/useCanvas';
import styles from './HotspotCanvas.module.css';

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
  imageUrl, hotspots, words, onAddHotspot, onUpdateHotspot, onDeleteHotspot, onLinkHotspotToWord
}) => {
  const {
    canvasRef, containerRef, canvasState, drawingState, selectedHotspotId,
    setSelectedHotspotId, setTool, handleMouseDown, handleMouseMove, handleMouseUp,
    handleWheel, zoomIn, zoomOut, resetZoom
  } = useCanvas({ imageUrl, hotspots, onAddHotspot, onUpdateHotspot });

  const selectedHotspot = hotspots.find(h => h.id === selectedHotspotId);
  const linkedWord = selectedHotspot ? words.find(w => w.id === selectedHotspot.wordId) : null;
  const unlinkedWords = words.filter(word => !hotspots.some(h => h.wordId === word.id));

  return (
    <div className={styles.container}>
      {/* Toolbar */}
      <div className={styles.toolbar}>
        <div className={styles.toolGroup}>
          <button className={`${styles.toolButton} ${drawingState.tool === DrawingTool.SELECT ? styles.active : ''}`}
            onClick={() => setTool(DrawingTool.SELECT)} title="Select/Move">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 3l7.07 16.97 2.51-7.39 7.39-2.51L3 3z" />
            </svg>
          </button>
          <button className={`${styles.toolButton} ${drawingState.tool === DrawingTool.RECTANGLE ? styles.active : ''}`}
            onClick={() => setTool(DrawingTool.RECTANGLE)} title="Draw Rectangle">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
            </svg>
          </button>
          <button className={`${styles.toolButton} ${drawingState.tool === DrawingTool.CIRCLE ? styles.active : ''}`}
            onClick={() => setTool(DrawingTool.CIRCLE)} title="Draw Circle">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
            </svg>
          </button>
        </div>

        <div className={styles.toolGroup}>
          <button className={styles.toolButton} onClick={zoomOut} title="Zoom Out">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
              <path d="M8 11h6" />
            </svg>
          </button>
          <span className={styles.zoomLevel}>{Math.round(canvasState.scale * 100)}%</span>
          <button className={styles.toolButton} onClick={zoomIn} title="Zoom In">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
              <path d="M11 8v6M8 11h6" />
            </svg>
          </button>
        </div>

        {selectedHotspotId && (
          <button className={`${styles.toolButton} ${styles.deleteButton}`}
            onClick={() => onDeleteHotspot(selectedHotspotId)} title="Delete">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
          </button>
        )}
      </div>

      {/* Canvas */}
      <div ref={containerRef} className={styles.canvasContainer} onWheel={handleWheel}>
        <canvas ref={canvasRef} className={`${styles.canvas} ${drawingState.tool !== DrawingTool.SELECT ? styles.drawing : ''}`}
          onMouseDown={handleMouseDown} onMouseMove={handleMouseMove} onMouseUp={handleMouseUp} onMouseLeave={handleMouseUp} />
      </div>

      {/* Properties Panel */}
      {selectedHotspot && (
        <div className={styles.propertiesPanel}>
          <h4>Hotspot Properties</h4>
          <div className={styles.propertyRow}>
            <label>Linked Word:</label>
            <select value={selectedHotspot.wordId}
              onChange={(e) => onLinkHotspotToWord(selectedHotspot.id, e.target.value)}>
              <option value="">-- Select Word --</option>
              {linkedWord && <option value={linkedWord.id}>{linkedWord.text}</option>}
              {unlinkedWords.map(word => <option key={word.id} value={word.id}>{word.text}</option>)}
            </select>
          </div>
          {linkedWord && (
            <div className={styles.wordPreview}>
              <span className={styles.wordText}>{linkedWord.text}</span>
              {linkedWord.translation && <span className={styles.wordTranslation}>{linkedWord.translation}</span>}
            </div>
          )}
          <div className={styles.propertyRow}>
            <label>Position:</label>
            <span className={styles.coordinates}>
              X: {(selectedHotspot.x * 100).toFixed(1)}% | Y: {(selectedHotspot.y * 100).toFixed(1)}%
            </span>
          </div>
        </div>
      )}
    </div>
  );
};
```

#### HotspotCanvas.module.css
```css
.container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
  flex-wrap: wrap;
}

.toolGroup {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-right: 12px;
  border-right: 1px solid #e2e8f0;
}

.toolGroup:last-child { border-right: none; padding-right: 0; }

.toolButton {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toolButton:hover { background: #f1f5f9; border-color: #cbd5e1; color: #334155; }
.toolButton.active { background: #3b82f6; border-color: #3b82f6; color: white; }
.deleteButton { color: #ef4444; }
.deleteButton:hover { background: #fef2f2; border-color: #ef4444; }

.zoomLevel {
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  min-width: 45px;
  text-align: center;
}

.canvasContainer {
  position: relative;
  width: 100%;
  height: 500px;
  background: #f1f5f9;
  border-radius: 8px;
  overflow: auto;
  display: flex;
  align-items: center;
  justify-content: center;
}

.canvas {
  max-width: 100%;
  max-height: 100%;
  cursor: default;
  user-select: none;
}

.canvas.drawing { cursor: crosshair; }

.propertiesPanel {
  background: #f8fafc;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e2e8f0;
}

.propertiesPanel h4 { margin: 0 0 12px 0; font-size: 14px; font-weight: 600; color: #1e293b; }

.propertyRow {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.propertyRow:last-child { margin-bottom: 0; }
.propertyRow label { font-size: 13px; font-weight: 500; color: #64748b; min-width: 90px; }
.propertyRow select { flex: 1; padding: 8px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; background: white; }
.coordinates { font-family: monospace; font-size: 13px; color: #334155; }

.wordPreview {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
  margin-bottom: 12px;
}

.wordText { font-size: 16px; font-weight: 600; color: #1e293b; }
.wordTranslation { font-size: 13px; color: #64748b; }

@media (max-width: 768px) {
  .container { padding: 12px; }
  .toolbar { gap: 8px; padding: 6px 8px; }
  .toolButton { width: 32px; height: 32px; }
  .canvasContainer { height: 350px; }
}
```

### 3.5 Main Editor Component

#### ImageWordMatchEditor.tsx
```typescript
// admin-web/src/components/questions/ImageWordMatchEditor.tsx

import React, { useState, useCallback } from 'react';
import { ImageWordMatchContent, Word, Hotspot, CreateImageWordMatchRequest } from '../types/questions';
import { HotspotCanvas } from './image-word-match/HotspotCanvas';
import styles from './ImageWordMatchEditor.module.css';

interface ImageWordMatchEditorProps {
  testId: string;
  initialContent?: ImageWordMatchContent;
  onSave: (data: CreateImageWordMatchRequest) => void;
  onCancel: () => void;
}

type EditorStep = 'image' | 'words' | 'hotspots' | 'preview';

export const ImageWordMatchEditor: React.FC<ImageWordMatchEditorProps> = ({ testId, initialContent, onSave, onCancel }) => {
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
    const newWord: Word = { id: `word_${Date.now()}`, text: newWordText.trim(), translation: newWordTranslation.trim() || undefined };
    setWords([...words, newWord]);
    setNewWordText('');
    setNewWordTranslation('');
  };

  const removeWord = (wordId: string) => {
    setWords(words.filter(w => w.id !== wordId));
    setHotspots(hotspots.filter(h => h.wordId !== wordId));
  };

  const handleAddHotspot = useCallback((hotspot: Hotspot) => setHotspots(prev => [...prev, hotspot]), []);
  const handleUpdateHotspot = useCallback((id: string, updates: Partial<Hotspot>) => 
    setHotspots(prev => prev.map(h => h.id === id ? { ...h, ...updates } : h)), []);
  const handleDeleteHotspot = useCallback((id: string) => setHotspots(prev => prev.filter(h => h.id !== id)), []);
  const handleLinkHotspotToWord = useCallback((hotspotId: string, wordId: string) => 
    setHotspots(prev => prev.map(h => h.id === hotspotId ? { ...h, wordId } : h)), []);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || file.size > 5 * 1024 * 1024 || !file.type.startsWith('image/')) return;
    
    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const response = await fetch('/api/upload/image', { method: 'POST', body: formData });
      if (!response.ok) throw new Error('Upload failed');
      const data = await response.json();
      setImageUrl(data.url);
      setCurrentStep('words');
    } catch (error) {
      alert('Failed to upload image. Please try again.');
    } finally {
      setIsUploading(false);
    }
  };

  const canProceedToHotspots = words.length >= 2;
  const canSave = hotspots.length === words.length && hotspots.every(h => h.wordId && words.some(w => w.id === h.wordId));

  const handleSave = () => {
    if (!canSave) return;
    onSave({ testId, instruction: instruction || 'Match the words to the objects', imageUrl, words, hotspots, points: words.length * 5 });
  };

  // Step renderers
  const renderImageStep = () => (
    <div className={styles.stepContent}>
      <h3>Step 1: Upload Image</h3>
      <p className={styles.stepDescription}>Upload an image with distinct objects (max 5MB)</p>
      <div className={styles.uploadArea}>
        {imageUrl ? (
          <div className={styles.previewContainer}>
            <img src={imageUrl} alt="Preview" className={styles.imagePreview} />
            <button className={styles.changeImageButton} onClick={() => setImageUrl('')}>Change Image</button>
          </div>
        ) : (
          <label className={styles.uploadLabel}>
            <input type="file" accept="image/*" onChange={handleImageUpload} disabled={isUploading} className={styles.fileInput} />
            <div className={styles.uploadPlaceholder}>
              {isUploading ? <span>Uploading...</span> : <><svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="17 8 12 3 7 8" /><line x1="12" y1="3" x2="12" y2="15" />
              </svg><span>Click to upload</span><span className={styles.uploadHint}>PNG, JPG up to 5MB</span></>}
            </div>
          </label>
        )}
      </div>
      {imageUrl && <button className={styles.nextButton} onClick={() => setCurrentStep('words')}>Continue to Words →</button>}
    </div>
  );

  const renderWordsStep = () => (
    <div className={styles.stepContent}>
      <h3>Step 2: Add Words ({words.length}/8)</h3>
      <div className={styles.wordInputSection}>
        <div className={styles.inputRow}>
          <input type="text" placeholder="Word" value={newWordText} onChange={(e) => setNewWordText(e.target.value)} onKeyPress={(e) => e.key === 'Enter' && addWord()} className={styles.wordInput} />
          <input type="text" placeholder="Translation (optional)" value={newWordTranslation} onChange={(e) => setNewWordTranslation(e.target.value)} onKeyPress={(e) => e.key === 'Enter' && addWord()} className={styles.translationInput} />
          <button onClick={addWord} disabled={!newWordText.trim() || words.length >= 8} className={styles.addButton}>Add</button>
        </div>
      </div>
      <div className={styles.wordList}>
        {words.map((word, index) => (
          <div key={word.id} className={styles.wordCard}>
            <span className={styles.wordNumber}>{index + 1}</span>
            <div className={styles.wordInfo}>
              <span className={styles.wordText}>{word.text}</span>
              {word.translation && <span className={styles.wordTranslation}>{word.translation}</span>}
            </div>
            <button onClick={() => removeWord(word.id)} className={styles.removeButton}>×</button>
          </div>
        ))}
      </div>
      <div className={styles.stepActions}>
        <button className={styles.backButton} onClick={() => setCurrentStep('image')}>← Back</button>
        <button className={styles.nextButton} onClick={() => setCurrentStep('hotspots')} disabled={!canProceedToHotspots}>Continue →</button>
      </div>
    </div>
  );

  const renderHotspotsStep = () => (
    <div className={styles.stepContent}>
      <h3>Step 3: Draw Hotspots</h3>
      <HotspotCanvas imageUrl={imageUrl} hotspots={hotspots} words={words}
        onAddHotspot={handleAddHotspot} onUpdateHotspot={handleUpdateHotspot}
        onDeleteHotspot={handleDeleteHotspot} onLinkHotspotToWord={handleLinkHotspotToWord} />
      <div className={styles.progressIndicator}>
        <div className={styles.progressBar}>
          <div className={styles.progressFill} style={{ width: `${(hotspots.filter(h => h.wordId).length / words.length) * 100}%` }} />
        </div>
        <span className={styles.progressText}>{hotspots.filter(h => h.wordId).length} of {words.length} words linked</span>
      </div>
      <div className={styles.stepActions}>
        <button className={styles.backButton} onClick={() => setCurrentStep('words')}>← Back</button>
        <button className={styles.previewButton} onClick={() => setCurrentStep('preview')} disabled={!canSave}>Preview →</button>
      </div>
    </div>
  );

  return (
    <div className={styles.editor}>
      <div className={styles.header}>
        <h2>Create Image-Word Match Question</h2>
        <button onClick={onCancel} className={styles.cancelButton}>Cancel</button>
      </div>
      <div className={styles.stepIndicator}>
        {(['image', 'words', 'hotspots', 'preview'] as EditorStep[]).map((step, index) => (
          <React.Fragment key={step}>
            <div className={`${styles.step} ${currentStep === step ? styles.active : ''} ${['image', 'words', 'hotspots', 'preview'].indexOf(currentStep) > index ? styles.completed : ''}`}>
              <span className={styles.stepNumber}>{index + 1}</span>
              <span className={styles.stepName}>{step.charAt(0).toUpperCase() + step.slice(1)}</span>
            </div>
            {index < 3 && <div className={styles.stepConnector} />}
          </React.Fragment>
        ))}
      </div>
      {currentStep === 'image' && renderImageStep()}
      {currentStep === 'words' && renderWordsStep()}
      {currentStep === 'hotspots' && renderHotspotsStep()}
    </div>
  );
};
```

---

## 4. Mobile/Desktop Implementation (Compose Multiplatform)

### 4.1 Project Structure

```
composeApp/src/commonMain/kotlin/com/funnyenglish/app/
├── components/questions/
│   ├── ImageWordMatchQuestion.kt
│   ├── DraggableWordBank.kt
│   ├── HotspotOverlay.kt
│   └── animations/
│       ├── PulseAnimation.kt
│       ├── SnapAnimation.kt
│       └── ShakeAnimation.kt
├── screens/TestPlayScreen.kt
└── utils/AdaptiveLayout.kt
```

### 4.2 Shared Models

```kotlin
// shared/src/commonMain/kotlin/com/funnyenglish/shared/model/Test.kt

@Serializable
enum class QuestionType {
    MULTIPLE_CHOICE, FILL_IN_BLANK, DRAG_DROP_MATCH,
    LISTENING_COMPREHENSION, IMAGE_WORD_MATCH, TRUE_FALSE, ORDERING
}

@Serializable
data class ImageWordMatchContent(
    val imageUrl: String,
    val instruction: String,
    val hotspots: List<HotspotData>,
    val words: List<WordData>
)

@Serializable
data class HotspotData(
    val id: String,
    val x: Float, val y: Float,
    val width: Float, val height: Float,
    val shape: HotspotShape = HotspotShape.RECTANGLE,
    val wordId: String
) {
    fun contains(rx: Float, ry: Float): Boolean = when (shape) {
        HotspotShape.RECTANGLE -> rx >= x && rx <= x + width && ry >= y && ry <= y + height
        HotspotShape.CIRCLE -> {
            val centerX = x + width / 2
            val centerY = y + height / 2
            val radius = minOf(width, height) / 2
            val dx = rx - centerX
            val dy = ry - centerY
            (dx * dx + dy * dy) <= (radius * radius)
        }
    }
}

@Serializable
enum class HotspotShape { RECTANGLE, CIRCLE }

@Serializable
data class WordData(val id: String, val text: String, val translation: String? = null, val audioUrl: String? = null)
```

### 4.3 Animation Components

#### PulseAnimation.kt
```kotlin
@Composable
fun PulseAnimation(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(modifier = Modifier.scale(scale).alpha(alpha)) { content() }
}
```

#### SnapAnimation.kt
```kotlin
@Composable
fun SnapAnimation(targetScale: Float = 1f, content: @Composable () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "snapScale"
    )
    Box(modifier = Modifier.scale(scale)) { content() }
}

@Composable
fun ShakeAnimation(trigger: Boolean, content: @Composable () -> Unit) {
    val shake by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = keyframes { durationMillis = 300; -10f at 50; 10f at 100; -10f at 150; 10f at 200; 0f at 300 },
        label = "shake"
    )
    Box(modifier = Modifier.graphicsLayer { translationX = shake * 5f }) { content() }
}
```

### 4.4 Main Question Component

#### ImageWordMatchQuestion.kt
```kotlin
@Composable
fun ImageWordMatchQuestion(
    content: ImageWordMatchContent,
    onAnswer: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var matchedWords by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var draggedWord by remember { mutableStateOf<WordData?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var imageBounds by remember { mutableStateOf(Rect.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    
    val progress = matchedWords.size.toFloat() / content.words.size
    
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(text = content.instruction, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            AsyncImage(
                model = content.imageUrl,
                contentDescription = "Question image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().onGloballyPositioned { coordinates ->
                    val position = coordinates.positionInRoot()
                    val size = coordinates.size.toSize()
                    imageBounds = Rect(position, size)
                    imageSize = size
                }
            )
            
            if (imageSize.width > 0 && imageSize.height > 0) {
                content.hotspots.forEach { hotspot ->
                    val isMatched = matchedWords.containsValue(hotspot.id)
                    val isTarget = isDragging && draggedWord != null && !matchedWords.containsValue(hotspot.id)
                    HotspotOverlay(
                        hotspot = hotspot, imageSize = imageSize,
                        isMatched = isMatched, isTarget = isTarget,
                        matchedWord = content.words.find { it.id == matchedWords.entries.find { it.value == hotspot.id }?.key },
                        onClick = { if (isMatched) { matchedWords = matchedWords.filter { it.value != hotspot.id }; onAnswer(matchedWords) } }
                    )
                }
            }
            
            if (isDragging && draggedWord != null) {
                DraggedWordOverlay(word = draggedWord!!, offset = dragOffset)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        WordBank(
            words = content.words, matchedWords = matchedWords,
            onDragStart = { word -> draggedWord = word; isDragging = true },
            onDrag = { offset, _ -> dragOffset = offset },
            onDragEnd = { finalPosition ->
                isDragging = false
                val relativeX = (finalPosition.x - imageBounds.left) / imageBounds.width
                val relativeY = (finalPosition.y - imageBounds.top) / imageBounds.height
                
                if (relativeX in 0f..1f && relativeY in 0f..1f) {
                    val targetHotspot = content.hotspots.find { 
                        it.contains(relativeX, relativeY) && !matchedWords.containsValue(it.id) 
                    }
                    if (targetHotspot != null && draggedWord != null) {
                        if (targetHotspot.wordId == draggedWord!!.id) {
                            matchedWords = matchedWords + (draggedWord!!.id to targetHotspot.id)
                            onAnswer(matchedWords)
                        }
                    }
                }
                draggedWord = null
                dragOffset = Offset.Zero
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { }, enabled = matchedWords.size == content.words.size, modifier = Modifier.fillMaxWidth()) {
            Text("Check Answers")
        }
    }
}

@Composable
private fun HotspotOverlay(
    hotspot: HotspotData, imageSize: Size,
    isMatched: Boolean, isTarget: Boolean,
    matchedWord: WordData?, onClick: () -> Unit
) {
    val x = hotspot.x * imageSize.width
    val y = hotspot.y * imageSize.height
    val width = hotspot.width * imageSize.width
    val height = hotspot.height * imageSize.height
    val shape = if (hotspot.shape == HotspotShape.CIRCLE) CircleShape else RoundedCornerShape(8.dp)
    
    Box(
        modifier = Modifier.offset(x.dp, y.dp).size(width.dp, height.dp)
            .clip(shape)
            .border(
                width = if (isMatched) 3.dp else 2.dp,
                color = when { isMatched -> Color(0xFF22C55E); isTarget -> Color(0xFF3B82F6); else -> Color.Transparent },
                shape = shape
            )
            .background(when { isMatched -> Color(0xFF22C55E).copy(alpha = 0.2f); isTarget -> Color(0xFF3B82F6).copy(alpha = 0.15f); else -> Color.Transparent })
            .then(if (isMatched) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        if (isTarget) PulseAnimation { Box(modifier = Modifier.fillMaxSize().border(2.dp, Color(0xFF3B82F6), shape)) }
        if (isMatched && matchedWord != null) MatchedWordLabel(word = matchedWord)
    }
}

@Composable
private fun MatchedWordLabel(word: WordData) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "labelScale")
    Box(modifier = Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
        Surface(color = Color(0xFF22C55E), shape = RoundedCornerShape(4.dp), modifier = Modifier.scale(scale)) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text(text = word.text, color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun WordBank(
    words: List<WordData>, matchedWords: Map<String, String>,
    onDragStart: (WordData) -> Unit, onDrag: (Offset, Offset) -> Unit, onDragEnd: (Offset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Drag words to the image:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            words.forEach { word ->
                DraggableWordCard(word = word, isMatched = matchedWords.containsKey(word.id), onDragStart = { onDragStart(word) }, onDrag = onDrag, onDragEnd = onDragEnd)
            }
        }
    }
}

@Composable
private fun DraggableWordCard(
    word: WordData, isMatched: Boolean,
    onDragStart: () -> Unit, onDrag: (Offset, Offset) -> Unit, onDragEnd: (Offset) -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    
    val scale by animateFloatAsState(targetValue = if (isDragging) 1.1f else 1f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "cardScale")
    val alpha by animateFloatAsState(targetValue = if (isMatched) 0.5f else 1f, label = "cardAlpha")
    
    Surface(
        modifier = Modifier.scale(scale).alpha(alpha)
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .onGloballyPositioned { cardPosition = it.positionInRoot() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { if (!isMatched) { isDragging = true; onDragStart() } },
                    onDrag = { change, dragAmount -> change.consume(); offset += dragAmount; onDrag(offset, cardPosition + offset) },
                    onDragEnd = { isDragging = false; onDragEnd(cardPosition + offset); offset = Offset.Zero },
                    onDragCancel = { isDragging = false; offset = Offset.Zero }
                )
            },
        shape = RoundedCornerShape(12.dp),
        color = when { isMatched -> Color(0xFF22C55E).copy(alpha = 0.1f); isDragging -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surface },
        border = BorderStroke(2.dp, when { isMatched -> Color(0xFF22C55E); isDragging -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) }),
        shadowElevation = if (isDragging) 8.dp else 2.dp
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(text = word.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = if (isMatched) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DraggedWordOverlay(word: WordData, offset: Offset) {
    Box(modifier = Modifier.offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }.graphicsLayer { scaleX = 1.1f; scaleY = 1.1f; shadowElevation = 20f }) {
        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer, border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary), shadowElevation = 8.dp) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(text = word.text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
```

### 4.5 Adaptive Layout

```kotlin
enum class ScreenSize { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenWidthDp.dp < 600.dp -> ScreenSize.COMPACT
        configuration.screenWidthDp.dp < 840.dp -> ScreenSize.MEDIUM
        else -> ScreenSize.EXPANDED
    }
}

@Composable
fun adaptivePadding(): Dp = when (rememberScreenSize()) {
    ScreenSize.COMPACT -> 16.dp
    ScreenSize.MEDIUM -> 24.dp
    ScreenSize.EXPANDED -> 32.dp
}
```

---

## 5. Integration Testing

### 5.1 Backend Integration Test

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class ImageWordMatchIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @Test
    fun `full flow - create question and validate answers`() {
        val createRequest = CreateImageWordMatchRequest(
            testId = UUID.randomUUID().toString(),
            instruction = "Match kitchen items",
            imageUrl = "https://cdn.funnyenglish.com/images/kitchen.jpg",
            words = listOf(WordRequest("w1", "fridge"), WordRequest("w2", "stove"), WordRequest("w3", "sink")),
            hotspots = listOf(
                HotspotRequest("h1", 0.05f, 0.1f, 0.25f, 0.4f, HotspotShape.RECTANGLE, "w1"),
                HotspotRequest("h2", 0.35f, 0.5f, 0.25f, 0.25f, HotspotShape.RECTANGLE, "w2"),
                HotspotRequest("h3", 0.7f, 0.55f, 0.2f, 0.15f, HotspotShape.RECTANGLE, "w3")
            ),
            points = 15
        )

        val createResult = mockMvc.perform(post("/api/questions/image-word-match")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.type").value("IMAGE_WORD_MATCH"))
            .andReturn()

        val questionId = objectMapper.readTree(createResult.response.contentAsString).get("id").asText()

        // Validate correct answers
        val correctAnswers = SubmitImageWordMatchAnswerRequest(questionId = questionId,
            matches = listOf(WordHotspotMatch("w1", "h1"), WordHotspotMatch("w2", "h2"), WordHotspotMatch("w3", "h3")))

        mockMvc.perform(post("/api/questions/image-word-match/$questionId/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(correctAnswers)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.percentage").value(100.0))
    }
}
```

### 5.2 Maestro E2E Test

```yaml
# .maestro/flows/image-word-match-test.yaml
appId: com.funnyenglish.app
---
- launchApp
- tapOn: "Sign In"
- inputText: { text: "test@example.com" }
- tapOn: "Password"
- inputText: { text: "password123" }
- tapOn: "Sign In"
- waitForAnimationToEnd
- tapOn: "Tests"
- tapOn: "Kitchen Vocabulary"
- tapOn: "Start Test"
- assertVisible: "Match the words to the objects"
- dragAndDrop: { from: "refrigerator", to: { id: "hotspot_1" } }
- assertVisible: "refrigerator"
- tapOn: "Check Answers"
- assertVisible: "Correct!"
```

### 5.3 Admin Panel Cypress Test

```typescript
describe('Image-Word Match Editor', () => {
  beforeEach(() => {
    cy.login('admin@example.com', 'password');
    cy.visit('/questions/new');
  });

  it('creates a complete question', () => {
    cy.contains('Image-Word Match').click();
    cy.get('input[type="file"]').selectFile('cypress/fixtures/kitchen.jpg');
    cy.contains('Continue to Words').click();
    
    cy.get('input[placeholder="Word"]').type('refrigerator');
    cy.contains('Add').click();
    cy.get('input[placeholder="Word"]').type('stove');
    cy.contains('Add').click();
    
    cy.contains('Continue to Hotspots').click();
    cy.get('[title="Draw Rectangle"]').click();
    cy.get('canvas').trigger('mousedown', { clientX: 100, clientY: 100 })
      .trigger('mousemove', { clientX: 200, clientY: 250 })
      .trigger('mouseup');
    cy.get('select').select('refrigerator');
    
    cy.contains('Preview').click();
    cy.contains('Save Question').click();
    cy.contains('Question saved successfully');
  });
});
```

---

## 6. Animation Specifications

| Animation | Trigger | Duration | Easing | Properties |
|-----------|---------|----------|--------|------------|
| Pulse | Drag start | 800ms infinite | EaseInOutQuad | scale 1.0→1.15, alpha 0.6→1.0 |
| Snap | Drop on hotspot | 300ms | Spring (bouncy) | scale with bounce |
| Success | Correct match | 400ms | EaseOutBack | scale 1.0→1.2→1.0 |
| Error | Wrong hotspot | 300ms | Linear | translateX ±10px shake |
| Return | Drop outside | 500ms | EaseOutCubic | position reset |
| Word Lift | Drag start | 150ms | EaseOut | scale 1.0→1.1 |

---

## 7. File Checklist

### Backend
- [ ] `entity/Question.kt` - Add IMAGE_WORD_MATCH
- [ ] `entity/ImageWordMatchContent.kt` - NEW
- [ ] `dto/ImageWordMatchDtos.kt` - NEW
- [ ] `service/AnswerValidationService.kt` - NEW
- [ ] `service/QuestionService.kt` - Add methods
- [ ] `controller/QuestionController.kt` - Add endpoints
- [ ] `test/*` - Add tests

### Admin Panel
- [ ] `types/questions.ts` - Add types
- [ ] `image-word-match/HotspotCanvas.tsx` - NEW
- [ ] `image-word-match/hooks/useCanvas.ts` - NEW
- [ ] `ImageWordMatchEditor.tsx` - NEW
- [ ] `QuestionTypeSelector.tsx` - Add type

### Mobile
- [ ] `shared/model/Test.kt` - Add models
- [ ] `questions/ImageWordMatchQuestion.kt` - NEW
- [ ] `questions/animations/*.kt` - NEW
- [ ] `screens/TestPlayScreen.kt` - Add case

---

**End of Technical Specification**

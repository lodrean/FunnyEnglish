# Image-Word Match Feature - Technical Specification (Part 1)
## Полная техническая документация для агентов-разработчиков

**Feature ID:** IMGWORD-001  
**Version:** 1.0  
**Date:** 2024  
**Estimated Effort:** 5-7 days

---

## 📑 Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Backend Implementation](#2-backend-implementation)
3. [Admin Panel Implementation](#3-admin-panel-implementation)
4. [Mobile/Desktop Implementation](#4-mobiledesktop-implementation)
5. [Integration Testing](#5-integration-testing)
6. [Responsive Design Guidelines](#6-responsive-design-guidelines)
7. [Animation Specifications](#7-animation-specifications)

---

## 1. Architecture Overview

### 1.1 System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Image-Word Match Feature                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐                │
│  │   Backend    │◄────│  Admin Panel │     │    Mobile    │                │
│  │ Spring Boot  │     │   React/TS   │     │   Compose    │                │
│  │   Kotlin     │     │              │     │Multiplatform │                │
│  └──────┬───────┘     └──────┬───────┘     └──────┬───────┘                │
│         │                    │                    │                         │
│         └────────────────────┼────────────────────┘                         │
│                              │                                              │
│                    ┌─────────▼─────────┐                                   │
│                    │  PostgreSQL + S3  │                                   │
│                    │   (MinIO/AWS)     │                                   │
│                    └───────────────────┘                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Data Flow

```
Teacher Flow:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Upload Image│───►│  Add Words  │───►│Draw Hotspots│───►│   Save to   │
│   to S3     │    │  (2-8 pcs)  │    │(rect/circle)│    │  Database   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘

Student Flow:
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  Load Test  │───►│ Drag Words  │───►│  Snap to    │───►│   Submit    │
│ with Image  │    │  to Image   │    │  Hotspots   │    │   Answers   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

---

## 2. Backend Implementation

### 2.1 Project Structure

```
backend/src/main/kotlin/com/funnyenglish/
├── entity/
│   ├── Question.kt                    # Add IMAGE_WORD_MATCH to enum
│   └── ImageWordMatchContent.kt       # NEW: Data classes
├── dto/
│   ├── QuestionDtos.kt                # Add DTOs for new type
│   └── ImageWordMatchDtos.kt          # NEW: Request/Response DTOs
├── service/
│   ├── QuestionService.kt             # Add validation logic
│   └── AnswerValidationService.kt     # NEW: Answer scoring
├── controller/
│   └── QuestionController.kt          # Existing, no changes needed
└── repository/
    └── QuestionRepository.kt          # Existing, no changes needed
```

### 2.2 Entity Layer

#### Question.kt - Updated Enum
```kotlin
// backend/src/main/kotlin/com/funnyenglish/entity/Question.kt

package com.funnyenglish.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "questions")
data class Question(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    
    @Column(nullable = false)
    val testId: UUID,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: QuestionType,
    
    @Column(nullable = false)
    val instruction: String,
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val content: Any,
    
    @Column(nullable = false)
    val points: Int = 10,
    
    @Column(nullable = false)
    val orderIndex: Int = 0,
    
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
    
    @Column(nullable = false)
    val updatedAt: Instant = Instant.now()
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    FILL_IN_BLANK,
    DRAG_DROP_MATCH,
    LISTENING_COMPREHENSION,
    IMAGE_WORD_MATCH,  // NEW
    TRUE_FALSE,
    ORDERING
}
```

#### ImageWordMatchContent.kt - NEW FILE
```kotlin
// backend/src/main/kotlin/com/funnyenglish/entity/ImageWordMatchContent.kt

package com.funnyenglish.entity

import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeName("IMAGE_WORD_MATCH")
data class ImageWordMatchContent(
    val imageUrl: String,
    val instruction: String,
    val hotspots: List<HotspotData>,
    val words: List<WordData>
) {
    init {
        require(hotspots.size == words.size) {
            "Number of hotspots must match number of words"
        }
        require(hotspots.all { it.wordId in words.map { w -> w.id } }) {
            "All hotspots must be linked to valid words"
        }
        require(words.size in 2..8) {
            "Word count must be between 2 and 8"
        }
    }
}

data class HotspotData(
    val id: String,
    val x: Float,           // 0.0 - 1.0 (relative)
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape = HotspotShape.RECTANGLE,
    val wordId: String
) {
    init {
        require(x in 0.0..1.0) { "Hotspot x must be in range [0.0, 1.0]" }
        require(y in 0.0..1.0) { "Hotspot y must be in range [0.0, 1.0]" }
        require(width in 0.0..1.0) { "Hotspot width must be in range [0.0, 1.0]" }
        require(height in 0.0..1.0) { "Hotspot height must be in range [0.0, 1.0]" }
        require(width > 0.05f) { "Hotspot width must be at least 5% of image" }
        require(height > 0.05f) { "Hotspot height must be at least 5% of image" }
    }
    
    fun contains(rx: Float, ry: Float): Boolean {
        return when (shape) {
            HotspotShape.RECTANGLE -> {
                rx >= x && rx <= x + width && ry >= y && ry <= y + height
            }
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
}

enum class HotspotShape {
    RECTANGLE,
    CIRCLE
}

data class WordData(
    val id: String,
    val text: String,
    val translation: String? = null,
    val audioUrl: String? = null
) {
    init {
        require(text.isNotBlank()) { "Word text cannot be blank" }
        require(text.length <= 50) { "Word text too long (max 50 chars)" }
    }
}
```

### 2.3 DTO Layer

#### ImageWordMatchDtos.kt - NEW FILE
```kotlin
// backend/src/main/kotlin/com/funnyenglish/dto/ImageWordMatchDtos.kt

package com.funnyenglish.dto

import com.funnyenglish.entity.*

data class CreateImageWordMatchRequest(
    val testId: String,
    val instruction: String,
    val imageUrl: String,
    val words: List<WordRequest>,
    val hotspots: List<HotspotRequest>,
    val points: Int = 10
)

data class WordRequest(
    val id: String,
    val text: String,
    val translation: String? = null,
    val audioUrl: String? = null
)

data class HotspotRequest(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape = HotspotShape.RECTANGLE,
    val wordId: String
)

data class SubmitImageWordMatchAnswerRequest(
    val questionId: String,
    val matches: List<WordHotspotMatch>
)

data class WordHotspotMatch(
    val wordId: String,
    val hotspotId: String
)

data class ImageWordMatchQuestionResponse(
    val id: String,
    val type: QuestionType,
    val instruction: String,
    val points: Int,
    val imageUrl: String,
    val words: List<WordResponse>,
    val hotspots: List<HotspotResponse>
)

data class WordResponse(
    val id: String,
    val text: String,
    val translation: String?,
    val audioUrl: String?
)

data class HotspotResponse(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape,
    val wordId: String
)

data class ImageWordMatchPublicResponse(
    val id: String,
    val type: QuestionType,
    val instruction: String,
    val points: Int,
    val imageUrl: String,
    val words: List<WordResponse>,
    val hotspots: List<HotspotWithoutWordResponse>
)

data class HotspotWithoutWordResponse(
    val id: String,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val shape: HotspotShape
)

data class ImageWordMatchResultResponse(
    val questionId: String,
    val earnedPoints: Int,
    val totalPoints: Int,
    val percentage: Float,
    val details: List<MatchResultDetail>
)

data class MatchResultDetail(
    val wordId: String,
    val wordText: String,
    val selectedHotspotId: String,
    val isCorrect: Boolean,
    val correctHotspotId: String? = null
)

// Mappers
fun ImageWordMatchContent.toAdminResponse(questionId: String, type: QuestionType, points: Int, instruction: String) =
    ImageWordMatchQuestionResponse(
        id = questionId,
        type = type,
        instruction = instruction,
        points = points,
        imageUrl = imageUrl,
        words = words.map { WordResponse(it.id, it.text, it.translation, it.audioUrl) },
        hotspots = hotspots.map { HotspotResponse(it.id, it.x, it.y, it.width, it.height, it.shape, it.wordId) }
    )

fun ImageWordMatchContent.toPublicResponse(questionId: String, type: QuestionType, points: Int, instruction: String) =
    ImageWordMatchPublicResponse(
        id = questionId,
        type = type,
        instruction = instruction,
        points = points,
        imageUrl = imageUrl,
        words = words.map { WordResponse(it.id, it.text, it.translation, it.audioUrl) },
        hotspots = hotspots.map { HotspotWithoutWordResponse(it.id, it.x, it.y, it.width, it.height, it.shape) }
    )
```

### 2.4 Service Layer

#### AnswerValidationService.kt - NEW FILE
```kotlin
// backend/src/main/kotlin/com/funnyenglish/service/AnswerValidationService.kt

package com.funnyenglish.service

import com.funnyenglish.dto.*
import com.funnyenglish.entity.*
import org.springframework.stereotype.Service

@Service
class AnswerValidationService {

    fun validateImageWordMatch(
        content: ImageWordMatchContent,
        submittedMatches: List<WordHotspotMatch>
    ): ImageWordMatchResultResponse {
        val correctMapping = content.hotspots.associate { it.wordId to it.id }
        
        val details = content.words.map { word ->
            val submittedMatch = submittedMatches.find { it.wordId == word.id }
            val correctHotspotId = correctMapping[word.id]
            
            if (submittedMatch != null) {
                val isCorrect = submittedMatch.hotspotId == correctHotspotId
                MatchResultDetail(
                    wordId = word.id,
                    wordText = word.text,
                    selectedHotspotId = submittedMatch.hotspotId,
                    isCorrect = isCorrect,
                    correctHotspotId = if (isCorrect) null else correctHotspotId
                )
            } else {
                MatchResultDetail(
                    wordId = word.id,
                    wordText = word.text,
                    selectedHotspotId = "",
                    isCorrect = false,
                    correctHotspotId = correctHotspotId
                )
            }
        }
        
        val correctCount = details.count { it.isCorrect }
        val totalWords = content.words.size
        val percentage = if (totalWords > 0) correctCount.toFloat() / totalWords else 0f
        
        return ImageWordMatchResultResponse(
            questionId = "",
            earnedPoints = (percentage * content.words.size * 10).toInt(),
            totalPoints = content.words.size * 10,
            percentage = percentage * 100,
            details = details
        )
    }
}
```

#### QuestionService.kt - Key Methods
```kotlin
// backend/src/main/kotlin/com/funnyenglish/service/QuestionService.kt

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val answerValidationService: AnswerValidationService,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun createImageWordMatchQuestion(request: CreateImageWordMatchRequest): Question {
        validateCreateRequest(request)
        
        val content = ImageWordMatchContent(
            imageUrl = request.imageUrl,
            instruction = request.instruction,
            words = request.words.map { WordData(it.id, it.text, it.translation, it.audioUrl) },
            hotspots = request.hotspots.map { 
                HotspotData(it.id, it.x, it.y, it.width, it.height, it.shape, it.wordId) 
            }
        )
        
        val question = Question(
            testId = UUID.fromString(request.testId),
            type = QuestionType.IMAGE_WORD_MATCH,
            instruction = request.instruction,
            content = content,
            points = request.points
        )
        
        return questionRepository.save(question)
    }
    
    private fun validateCreateRequest(request: CreateImageWordMatchRequest) {
        require(request.words.size in 2..8) { "Word count must be between 2 and 8" }
        require(request.hotspots.size == request.words.size) { "Each word must have exactly one hotspot" }
        
        val wordIds = request.words.map { it.id }.toSet()
        require(request.hotspots.all { it.wordId in wordIds }) { "All hotspots must reference valid words" }
        
        val hotspotWordIds = request.hotspots.map { it.wordId }
        require(hotspotWordIds.size == hotspotWordIds.toSet().size) { "Each word can only have one hotspot" }
    }
    
    fun validateAnswer(questionId: UUID, matches: List<WordHotspotMatch>): ImageWordMatchResultResponse {
        val question = questionRepository.findById(questionId)
            .orElseThrow { IllegalArgumentException("Question not found: $questionId") }
        
        val content = objectMapper.convertValue(question.content, ImageWordMatchContent::class.java)
        
        val result = answerValidationService.validateImageWordMatch(content, matches)
        return result.copy(questionId = questionId.toString())
    }
}
```

### 2.5 Controller Layer

#### QuestionController.kt - NEW Endpoints
```kotlin
// backend/src/main/kotlin/com/funnyenglish/controller/QuestionController.kt

@RestController
@RequestMapping("/api/questions")
class QuestionController(private val questionService: QuestionService) {

    @PostMapping("/image-word-match")
    fun createImageWordMatchQuestion(
        @RequestBody request: CreateImageWordMatchRequest
    ): ResponseEntity<ImageWordMatchQuestionResponse> {
        val question = questionService.createImageWordMatchQuestion(request)
        val response = questionService.getQuestionForAdmin(question.id!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    @PutMapping("/image-word-match/{id}")
    fun updateImageWordMatchQuestion(
        @PathVariable id: String,
        @RequestBody request: CreateImageWordMatchRequest
    ): ResponseEntity<ImageWordMatchQuestionResponse> {
        val question = questionService.updateImageWordMatchQuestion(UUID.fromString(id), request)
        val response = questionService.getQuestionForAdmin(question.id!!)
        return ResponseEntity.ok(response)
    }
    
    @GetMapping("/image-word-match/{id}")
    fun getImageWordMatchQuestion(@PathVariable id: String): ResponseEntity<ImageWordMatchQuestionResponse> {
        val response = questionService.getQuestionForAdmin(UUID.fromString(id))
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/image-word-match/{id}/validate")
    fun validateImageWordMatchAnswer(
        @PathVariable id: String,
        @RequestBody request: SubmitImageWordMatchAnswerRequest
    ): ResponseEntity<ImageWordMatchResultResponse> {
        val result = questionService.validateAnswer(UUID.fromString(id), request.matches)
        return ResponseEntity.ok(result)
    }
}
```

### 2.6 Test Layer

#### QuestionServiceTest.kt - Key Tests
```kotlin
// backend/src/test/kotlin/com/funnyenglish/service/QuestionServiceTest.kt

package com.funnyenglish.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class QuestionServiceTest {

    private lateinit var questionRepository: QuestionRepository
    private lateinit var answerValidationService: AnswerValidationService
    private lateinit var objectMapper: ObjectMapper
    private lateinit var questionService: QuestionService

    @BeforeEach
    fun setUp() {
        questionRepository = mockk()
        answerValidationService = AnswerValidationService()
        objectMapper = ObjectMapper()
        questionService = QuestionService(questionRepository, answerValidationService, objectMapper)
    }

    @Test
    fun `createImageWordMatchQuestion should create question with valid data`() {
        val request = CreateImageWordMatchRequest(
            testId = UUID.randomUUID().toString(),
            instruction = "Match words to objects",
            imageUrl = "https://cdn.funnyenglish.com/images/kitchen.jpg",
            words = listOf(
                WordRequest("w1", "fridge", "холодильник"),
                WordRequest("w2", "table", "стол"),
                WordRequest("w3", "knife", "нож")
            ),
            hotspots = listOf(
                HotspotRequest("h1", 0.1f, 0.2f, 0.15f, 0.2f, HotspotShape.RECTANGLE, "w1"),
                HotspotRequest("h2", 0.4f, 0.5f, 0.2f, 0.15f, HotspotShape.RECTANGLE, "w2"),
                HotspotRequest("h3", 0.7f, 0.3f, 0.1f, 0.1f, HotspotShape.CIRCLE, "w3")
            ),
            points = 15
        )

        every { questionRepository.save(any()) } answers { 
            firstArg<Question>().copy(id = UUID.randomUUID()) 
        }

        val result = questionService.createImageWordMatchQuestion(request)

        assertNotNull(result.id)
        assertEquals(QuestionType.IMAGE_WORD_MATCH, result.type)
        assertEquals(15, result.points)
        
        val content = result.content as ImageWordMatchContent
        assertEquals(3, content.words.size)
        assertEquals(3, content.hotspots.size)
        
        verify { questionRepository.save(any()) }
    }

    @Test
    fun `validateAnswer should return correct score for all correct matches`() {
        val questionId = UUID.randomUUID()
        val content = ImageWordMatchContent(
            imageUrl = "kitchen.jpg",
            instruction = "Match words",
            words = listOf(WordData("w1", "fridge"), WordData("w2", "table")),
            hotspots = listOf(
                HotspotData("h1", 0.1f, 0.1f, 0.2f, 0.2f, HotspotShape.RECTANGLE, "w1"),
                HotspotData("h2", 0.5f, 0.5f, 0.2f, 0.2f, HotspotShape.RECTANGLE, "w2")
            )
        )
        
        val question = Question(
            id = questionId,
            testId = UUID.randomUUID(),
            type = QuestionType.IMAGE_WORD_MATCH,
            instruction = "Match",
            content = content,
            points = 10
        )
        
        every { questionRepository.findById(questionId) } returns Optional.of(question)

        val matches = listOf(WordHotspotMatch("w1", "h1"), WordHotspotMatch("w2", "h2"))
        val result = questionService.validateAnswer(questionId, matches)

        assertEquals(100f, result.percentage)
        assertEquals(2, result.details.count { it.isCorrect })
    }

    @Test
    fun `HotspotData contains should work for rectangle`() {
        val hotspot = HotspotData(
            id = "h1", x = 0.2f, y = 0.3f, width = 0.3f, height = 0.2f,
            shape = HotspotShape.RECTANGLE, wordId = "w1"
        )
        
        assertTrue(hotspot.contains(0.3f, 0.4f))
        assertTrue(hotspot.contains(0.5f, 0.5f))
        assertFalse(hotspot.contains(0.1f, 0.1f))
    }
}
```

---

## 3. Admin Panel Implementation

### 3.1 Project Structure

```
admin-web/src/
├── components/
│   └── questions/
│       ├── image-word-match/
│       │   ├── HotspotCanvas.tsx
│       │   ├── HotspotCanvas.module.css
│       │   └── hooks/useCanvas.ts
│       ├── ImageWordMatchEditor.tsx
│       └── QuestionTypeSelector.tsx
├── types/questions.ts
└── api/questions.ts
```

### 3.2 Type Definitions

#### types/questions.ts
```typescript
export enum QuestionType {
  MULTIPLE_CHOICE = 'MULTIPLE_CHOICE',
  FILL_IN_BLANK = 'FILL_IN_BLANK',
  DRAG_DROP_MATCH = 'DRAG_DROP_MATCH',
  LISTENING_COMPREHENSION = 'LISTENING_COMPREHENSION',
  IMAGE_WORD_MATCH = 'IMAGE_WORD_MATCH',
  TRUE_FALSE = 'TRUE_FALSE',
  ORDERING = 'ORDERING'
}

export interface ImageWordMatchContent {
  imageUrl: string;
  instruction: string;
  hotspots: Hotspot[];
  words: Word[];
}

export interface Word {
  id: string;
  text: string;
  translation?: string;
  audioUrl?: string;
}

export interface Hotspot {
  id: string;
  x: number;
  y: number;
  width: number;
  height: number;
  shape: HotspotShape;
  wordId: string;
}

export enum HotspotShape {
  RECTANGLE = 'RECTANGLE',
  CIRCLE = 'CIRCLE'
}

export enum DrawingTool {
  SELECT = 'SELECT',
  RECTANGLE = 'RECTANGLE',
  CIRCLE = 'CIRCLE'
}
```

### 3.3 Canvas Hook - useCanvas.ts
```typescript
export const useCanvas = ({ imageUrl, hotspots, onAddHotspot, onUpdateHotspot }: UseCanvasProps) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const imageRef = useRef<HTMLImageElement | null>(null);
  
  const [canvasState, setCanvasState] = useState<CanvasState>({
    scale: 1, offsetX: 0, offsetY: 0
  });
  
  const [drawingState, setDrawingState] = useState<DrawingState>({
    isDrawing: false, startX: 0, startY: 0, currentX: 0, currentY: 0, tool: DrawingTool.SELECT
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
  }, [imageUrl]);

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
        ...prev, isDrawing: true, startX: coords.x, startY: coords.y, currentX: coords.x, currentY: coords.y
      }));
    }
  }, [drawingState.tool, hotspots]);

  const handleMouseUp = useCallback(() => {
    if (drawingState.isDrawing) {
      const width = Math.abs(drawingState.currentX - drawingState.startX);
      const height = Math.abs(drawingState.currentY - drawingState.startY);

      if (width >= 0.05 && height >= 0.05) {
        const newHotspot: Hotspot = {
          id: `hotspot_${Date.now()}`,
          x: Math.min(drawingState.startX, drawingState.currentX),
          y: Math.min(drawingState.startY, drawingState.currentY),
          width, height,
          shape: drawingState.tool === DrawingTool.RECTANGLE ? 'RECTANGLE' : 'CIRCLE',
          wordId: ''
        };
        onAddHotspot(newHotspot);
        setSelectedHotspotId(newHotspot.id);
      }
      setDrawingState(prev => ({ ...prev, isDrawing: false }));
    }
  }, [drawingState, onAddHotspot]);

  return {
    canvasRef, containerRef, canvasState, drawingState, selectedHotspotId,
    setSelectedHotspotId, setTool: (tool: DrawingTool) => setDrawingState(prev => ({ ...prev, tool })),
    handleMouseDown, handleMouseMove, handleMouseUp, handleWheel,
    zoomIn, zoomOut, resetZoom
  };
};
```

---

*Continue to Part 2 for Admin Panel Components, Mobile Implementation, Testing, and Animation Specifications*

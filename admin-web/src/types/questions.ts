// =====================
// Question Types
// =====================

export type QuestionTypeV2 =
  | 'TEXT_SELECT'
  | 'IMAGE_SELECT'
  | 'AUDIO_SELECT'
  | 'DRAG_DROP_MATCH'
  | 'DRAG_DROP_SORT'
  | 'FILL_BLANK'
  | 'IMAGE_WORD_MATCH';

// =====================
// Content Types (for API)
// =====================

export interface TextSelectContent {
  text: string;
  answers: AnswerOption[];
}

export interface ImageSelectContent {
  text?: string;
  answers: ImageAnswerOption[];
}

export interface AudioSelectContent {
  audioUrl: string;
  transcript?: string;
  text?: string;
  answers: AnswerOption[];
}

export interface DragDropMatchContent {
  text: string;
  items: DragItem[];
  targets: DropTarget[];
}

export interface DragDropSortContent {
  text: string;
  items: SortItem[];
}

export interface FillBlankContent {
  textBefore: string;
  textAfter: string;
  answers: AnswerOption[];
}

export type QuestionContent =
  | TextSelectContent
  | ImageSelectContent
  | AudioSelectContent
  | DragDropMatchContent
  | DragDropSortContent
  | FillBlankContent
  | ImageWordMatchContent;

// =====================
// Image-Word Match Types
// =====================

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
  x: number;        // 0.0 - 1.0 (relative)
  y: number;        // 0.0 - 1.0 (relative)
  width: number;    // 0.0 - 1.0 (relative)
  height: number;   // 0.0 - 1.0 (relative)
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

// API Request/Response types for Image-Word Match
export interface CreateImageWordMatchRequest {
  testId: string;
  instruction: string;
  imageUrl: string;
  words: Word[];
  hotspots: Hotspot[];
  points: number;
}

export interface ImageWordMatchQuestionResponse {
  id: string;
  type: QuestionTypeV2;
  instruction: string;
  points: number;
  imageUrl: string;
  words: Word[];
  hotspots: Hotspot[];
}

// =====================
// Sub-types
// =====================

export interface AnswerOption {
  id: string;
  text: string;
  isCorrect: boolean;
}

export interface ImageAnswerOption {
  id: string;
  imageUrl?: string;
  emoji?: string;
  text?: string;
  isCorrect: boolean;
}

export interface DragItem {
  id: string;
  text: string;
  targetId: string;
}

export interface DropTarget {
  id: string;
  imageUrl?: string;
  emoji?: string;
  text?: string;
}

export interface SortItem {
  id: string;
  text: string;
  correctOrder: number;
}

// =====================
// API Types
// =====================

export interface QuestionV2 {
  id: string;
  testId?: string;
  type: QuestionTypeV2;
  title: string;
  content: QuestionContent;
  mediaUrl?: string;
  displayOrder: number;
  points: number;
  timeLimitSeconds?: number;
  explanation?: string;
  hint?: string;
  isPublished: boolean;
  createdAt: string;
  updatedAt: string;
  // Дополнительные данные для IMAGE_WORD_MATCH (из /details endpoint)
  imageWordMatchData?: {
    instruction: string;
    imageUrl: string;
    words: WordResponse[];
    hotspots: HotspotResponse[];
  };
}

export interface CreateQuestionRequest {
  testId?: string;
  type: QuestionTypeV2;
  title: string;
  content: QuestionContent;
  mediaUrl?: string;
  displayOrder?: number;
  points: number;
  timeLimitSeconds?: number;
  explanation?: string;
  hint?: string;
}

export type UpdateQuestionRequest = Partial<Omit<CreateQuestionRequest, 'testId' | 'type'>>;

export interface ReorderQuestionsRequest {
  questionIds: string[];
}

// Question list item (summary view)
export interface QuestionListItem {
  id: string;
  type: QuestionTypeV2;
  title: string;
  displayOrder: number;
  points: number;
  isPublished: boolean;
  preview?: string;
  updatedAt: string;
}

// Question content request (for validation)
export type QuestionContentRequest = QuestionContent;

// =====================
// Audio Test Types
// =====================

export interface AudioTest {
  id: string;
  categoryId: string;
  title: string;
  description?: string;
  thumbnailUrl?: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  pointsReward: number;
  audioFile: AudioFile;
  questions: AudioTestQuestion[];
  maxPlays: number;
  allowPause: boolean;
  timeLimitSeconds?: number;
  isPublished: boolean;
}

export interface AudioFile {
  id: string;
  url: string;
  durationSeconds: number;
  transcript?: string;
  title?: string;
}

export interface AudioTestQuestion {
  id: string;
  text: string;
  timestampStart?: number;
  timestampEnd?: number;
  points: number;
  options: AudioTestOption[];
  explanation?: string;
}

export interface AudioTestOption {
  id: string;
  text: string;
  isCorrect: boolean;
}

export interface AudioTestListItem {
  id: string;
  categoryId: string;
  title: string;
  description?: string;
  thumbnailUrl?: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  pointsReward: number;
  audioDurationSeconds: number;
  questionsCount: number;
  maxPlays: number;
}

export interface CreateAudioTestRequest {
  categoryId: string;
  title: string;
  description?: string;
  thumbnailUrl?: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  pointsReward: number;
  maxPlays: number;
  allowPause: boolean;
  timeLimitSeconds?: number;
  audioFileId: string;
  questions: CreateAudioTestQuestionRequest[];
}

export interface CreateAudioTestQuestionRequest {
  text: string;
  timestampStart?: number;
  timestampEnd?: number;
  points: number;
  options: CreateAudioTestOptionRequest[];
  explanation?: string;
}

export interface CreateAudioTestOptionRequest {
  text: string;
  isCorrect: boolean;
}

export interface UpdateAudioTestRequest {
  title?: string;
  description?: string;
  thumbnailUrl?: string;
  difficulty?: 'EASY' | 'MEDIUM' | 'HARD';
  pointsReward?: number;
  maxPlays?: number;
  allowPause?: boolean;
  timeLimitSeconds?: number;
  isPublished?: boolean;
  questions?: CreateAudioTestQuestionRequest[];
}

// =====================
// Legacy Types (for backward compatibility)
// =====================

export type QuestionType =
  | 'DRAG_DROP_IMAGE'
  | 'AUDIO_SELECT'
  | 'IMAGE_SELECT'
  | 'TEXT_SELECT'
  | 'FILL_BLANK';

export interface Answer {
  id: string;
  text: string;
  imageUrl?: string;
  isCorrect: boolean;
  displayOrder: number;
}

export interface Question {
  id: string;
  type: QuestionType;
  text?: string;
  audioUrl?: string;
  imageUrl?: string;
  displayOrder: number;
  points: number;
  answers: Answer[];
}

export interface CreateAnswerRequest {
  text: string;
  imageUrl?: string;
  isCorrect: boolean;
}

export interface CreateQuestionRequestLegacy {
  type: string;
  text?: string;
  audioUrl?: string;
  imageUrl?: string;
  displayOrder: number;
  points: number;
  answers: CreateAnswerRequest[];
}

import { useState } from 'react';

type QuestionType = 'TEXT_SELECT' | 'IMAGE_SELECT' | 'AUDIO_SELECT' | 'DRAG_DROP_MATCH' | 'FILL_BLANK';

interface Question {
  id: number;
  type: QuestionType;
  text: string;
  options: { id: string; text: string; isCorrect: boolean }[];
}

const questionTypes: { type: QuestionType; label: string; icon: string }[] = [
  { type: 'TEXT_SELECT', label: 'Multiple Choice (Text)', icon: 'T' },
  { type: 'IMAGE_SELECT', label: 'Multiple Choice (Image)', icon: 'I' },
  { type: 'AUDIO_SELECT', label: 'Listening', icon: 'A' },
  { type: 'DRAG_DROP_MATCH', label: 'Matching', icon: 'M' },
  { type: 'FILL_BLANK', label: 'Fill in Blank', icon: 'F' },
];

export function TestEditorLayout() {
  const [activeTab, setActiveTab] = useState<'edit' | 'preview'>('edit');
  const [questions, setQuestions] = useState<Question[]>([
    {
      id: 1,
      type: 'TEXT_SELECT',
      text: 'What is the correct form of the verb?',
      options: [
        { id: 'a', text: 'go', isCorrect: false },
        { id: 'b', text: 'goes', isCorrect: true },
        { id: 'c', text: 'going', isCorrect: false },
        { id: 'd', text: 'went', isCorrect: false },
      ],
    },
    {
      id: 2,
      type: 'FILL_BLANK',
      text: 'I ___ to the store yesterday.',
      options: [
        { id: 'a', text: 'go', isCorrect: false },
        { id: 'b', text: 'went', isCorrect: true },
      ],
    },
  ]);
  const [selectedQuestion, setSelectedQuestion] = useState<number | null>(1);

  const addQuestion = (type: QuestionType) => {
    const newQuestion: Question = {
      id: Date.now(),
      type,
      text: '',
      options: [
        { id: 'a', text: '', isCorrect: false },
        { id: 'b', text: '', isCorrect: false },
      ],
    };
    setQuestions([...questions, newQuestion]);
    setSelectedQuestion(newQuestion.id);
  };

  return (
    <section className="page-section">
      <div className="section-header">
        <h2 className="section-title">Test Editor</h2>
        <p className="section-description">
          Create and edit tests with multiple question types.
        </p>
      </div>

      <div className="test-editor-layout">
        {/* Left Panel - Settings */}
        <div className="editor-left-panel">
          <div className="panel-section">
            <h4 className="panel-title">Test Settings</h4>
            <div className="form-fields">
              <div className="form-field">
                <label className="form-label">Test Name *</label>
                <input type="text" className="form-input" placeholder="Enter test name..." defaultValue="Basic Grammar Test" />
              </div>
              <div className="form-field">
                <label className="form-label">Category</label>
                <div className="form-select-wrapper">
                  <select className="form-select">
                    <option>Grammar</option>
                    <option>Vocabulary</option>
                    <option>Listening</option>
                    <option>Reading</option>
                    <option>Writing</option>
                  </select>
                  <svg className="form-select-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M6 9l6 6 6-6" />
                  </svg>
                </div>
              </div>
              <div className="form-field">
                <label className="form-label">Description</label>
                <textarea className="form-textarea" rows={3} placeholder="Enter test description..."></textarea>
              </div>
              <div className="form-row-two">
                <div className="form-field">
                  <label className="form-label">Time Limit (min)</label>
                  <input type="number" className="form-input" defaultValue={30} />
                </div>
                <div className="form-field">
                  <label className="form-label">Passing Score (%)</label>
                  <input type="number" className="form-input" defaultValue={70} />
                </div>
              </div>
              <div className="form-field">
                <label className="form-label">Status</label>
                <div className="form-toggle-group">
                  <label className="form-toggle">
                    <input type="checkbox" defaultChecked />
                    <span className="form-toggle-slider">
                      <span className="form-toggle-thumb" />
                    </span>
                    <span className="form-toggle-label">Active</span>
                  </label>
                </div>
              </div>
            </div>
          </div>

          <div className="panel-section">
            <h4 className="panel-title">Questions ({questions.length})</h4>
            <div className="question-list">
              {questions.map((q, index) => (
                <div 
                  key={q.id} 
                  className={`question-list-item ${selectedQuestion === q.id ? 'active' : ''}`}
                  onClick={() => setSelectedQuestion(q.id)}
                >
                  <span className="question-number">{index + 1}</span>
                  <span className="question-type-icon">{questionTypes.find(t => t.type === q.type)?.icon}</span>
                  <span className="question-preview">{q.text || 'Untitled Question'}</span>
                  <button className="question-delete" onClick={(e) => { e.stopPropagation(); }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M18 6L6 18M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>

            <div className="add-question-dropdown">
              <button className="btn btn-secondary btn-full">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M12 5v14M5 12h14" />
                </svg>
                Add Question
              </button>
              <div className="dropdown-menu">
                {questionTypes.map((type) => (
                  <button 
                    key={type.type} 
                    className="dropdown-item"
                    onClick={() => addQuestion(type.type)}
                  >
                    <span className="dropdown-item-icon">{type.icon}</span>
                    {type.label}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Right Panel - Question Editor */}
        <div className="editor-right-panel">
          <div className="editor-tabs">
            <button 
              className={`editor-tab ${activeTab === 'edit' ? 'active' : ''}`}
              onClick={() => setActiveTab('edit')}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
              Edit
            </button>
            <button 
              className={`editor-tab ${activeTab === 'preview' ? 'active' : ''}`}
              onClick={() => setActiveTab('preview')}
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              Preview
            </button>
          </div>

          <div className="editor-content">
            {activeTab === 'edit' ? (
              <div className="question-editor">
                <div className="form-field">
                  <label className="form-label">Question Text *</label>
                  <textarea 
                    className="form-textarea form-textarea-lg" 
                    rows={3}
                    placeholder="Enter your question..."
                    defaultValue="What is the correct form of the verb?"
                  />
                </div>

                <div className="form-field">
                  <label className="form-label">Media (Optional)</label>
                  <div className="media-uploader">
                    <div className="media-uploader-zone">
                      <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
                        <circle cx="8.5" cy="8.5" r="1.5" />
                        <polyline points="21 15 16 10 5 21" />
                      </svg>
                      <p>Drag & drop an image or audio file</p>
                      <span>or click to browse</span>
                    </div>
                  </div>
                </div>

                <div className="form-field">
                  <div className="options-header">
                    <label className="form-label">Answer Options</label>
                    <span className="options-hint">Mark the correct answer(s)</span>
                  </div>
                  <div className="options-list">
                    {[
                      { id: 'a', text: 'go', isCorrect: false },
                      { id: 'b', text: 'goes', isCorrect: true },
                      { id: 'c', text: 'going', isCorrect: false },
                      { id: 'd', text: 'went', isCorrect: false },
                    ].map((option) => (
                      <div key={option.id} className="option-item">
                        <label className="form-radio">
                          <input type="radio" name="correct-answer" defaultChecked={option.isCorrect} />
                          <span className="form-radio-checkmark" />
                        </label>
                        <span className="option-label">{option.id.toUpperCase()}</span>
                        <input 
                          type="text" 
                          className="form-input option-input"
                          defaultValue={option.text}
                          placeholder={`Option ${option.id.toUpperCase()}`}
                        />
                        <button className="btn btn-icon btn-ghost btn-sm">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                            <path d="M18 6L6 18M6 6l12 12" />
                          </svg>
                        </button>
                      </div>
                    ))}
                  </div>
                  <button className="btn btn-ghost btn-sm add-option-btn">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M12 5v14M5 12h14" />
                    </svg>
                    Add Option
                  </button>
                </div>

                <div className="form-field">
                  <label className="form-label">Explanation (Optional)</label>
                  <textarea 
                    className="form-textarea" 
                    rows={2}
                    placeholder="Explain the correct answer..."
                  />
                </div>
              </div>
            ) : (
              <div className="question-preview-panel">
                <div className="preview-card">
                  <div className="preview-question">
                    <span className="preview-question-number">1</span>
                    <p className="preview-question-text">What is the correct form of the verb?</p>
                  </div>
                  <div className="preview-options">
                    {['go', 'goes', 'going', 'went'].map((opt, idx) => (
                      <label key={idx} className="preview-option">
                        <span className="preview-option-letter">{String.fromCharCode(65 + idx)}</span>
                        <span className="preview-option-text">{opt}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="editor-footer">
            <button className="btn btn-ghost">Discard Changes</button>
            <div className="editor-footer-actions">
              <button className="btn btn-secondary">Save as Draft</button>
              <button className="btn btn-primary">Save Test</button>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

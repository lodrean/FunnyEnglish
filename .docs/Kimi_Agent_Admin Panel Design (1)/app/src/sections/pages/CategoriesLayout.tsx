import { useState } from 'react';

interface Category {
  id: number;
  name: string;
  description: string;
  testCount: number;
  color: string;
  icon: string;
}

const mockCategories: Category[] = [
  { id: 1, name: 'Grammar', description: 'English grammar rules and exercises', testCount: 24, color: '#4A90D9', icon: 'G' },
  { id: 2, name: 'Vocabulary', description: 'Word lists and vocabulary building', testCount: 18, color: '#43A047', icon: 'V' },
  { id: 3, name: 'Listening', description: 'Audio comprehension exercises', testCount: 12, color: '#FB8C00', icon: 'L' },
  { id: 4, name: 'Reading', description: 'Reading comprehension passages', testCount: 15, color: '#E53935', icon: 'R' },
  { id: 5, name: 'Writing', description: 'Essay and creative writing', testCount: 8, color: '#9C27B0', icon: 'W' },
  { id: 6, name: 'Speaking', description: 'Pronunciation and conversation', testCount: 6, color: '#00BCD4', icon: 'S' },
];

export function CategoriesLayout() {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);

  return (
    <section className="page-section">
      <div className="section-header">
        <h2 className="section-title">Categories</h2>
        <p className="section-description">
          Organize tests by categories and manage content structure.
        </p>
      </div>

      <div className="categories-layout">
        {/* Header Actions */}
        <div className="categories-header">
          <div className="search-input-wrapper">
            <svg className="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <input 
              type="text" 
              className="form-input search-input"
              placeholder="Search categories..."
            />
          </div>
          <div className="categories-actions">
            <div className="view-toggle">
              <button 
                className={`view-toggle-btn ${viewMode === 'grid' ? 'active' : ''}`}
                onClick={() => setViewMode('grid')}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="3" y="3" width="7" height="7" />
                  <rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" />
                  <rect x="3" y="14" width="7" height="7" />
                </svg>
              </button>
              <button 
                className={`view-toggle-btn ${viewMode === 'list' ? 'active' : ''}`}
                onClick={() => setViewMode('list')}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <line x1="8" y1="6" x2="21" y2="6" />
                  <line x1="8" y1="12" x2="21" y2="12" />
                  <line x1="8" y1="18" x2="21" y2="18" />
                  <line x1="3" y1="6" x2="3.01" y2="6" />
                  <line x1="3" y1="12" x2="3.01" y2="12" />
                  <line x1="3" y1="18" x2="3.01" y2="18" />
                </svg>
              </button>
            </div>
            <button className="btn btn-primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 5v14M5 12h14" />
              </svg>
              Add Category
            </button>
          </div>
        </div>

        {/* Categories Grid */}
        {viewMode === 'grid' ? (
          <div className="categories-grid">
            {mockCategories.map((category) => (
              <div 
                key={category.id} 
                className="category-card"
                onClick={() => setSelectedCategory(category)}
              >
                <div className="category-card-header">
                  <div 
                    className="category-icon"
                    style={{ backgroundColor: category.color }}
                  >
                    {category.icon}
                  </div>
                  <div className="category-actions">
                    <button className="btn btn-icon btn-ghost btn-sm">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                    <button className="btn btn-icon btn-ghost btn-sm">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                      </svg>
                    </button>
                  </div>
                </div>
                <div className="category-card-body">
                  <h4 className="category-name">{category.name}</h4>
                  <p className="category-description">{category.description}</p>
                </div>
                <div className="category-card-footer">
                  <span className="category-test-count">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                      <polyline points="14 2 14 8 20 8" />
                    </svg>
                    {category.testCount} tests
                  </span>
                  <span className="category-arrow">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M9 18l6-6-6-6" />
                    </svg>
                  </span>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="categories-list">
            {mockCategories.map((category) => (
              <div 
                key={category.id} 
                className="category-list-item"
                onClick={() => setSelectedCategory(category)}
              >
                <div 
                  className="category-icon category-icon-sm"
                  style={{ backgroundColor: category.color }}
                >
                  {category.icon}
                </div>
                <div className="category-list-content">
                  <h4 className="category-name">{category.name}</h4>
                  <p className="category-description">{category.description}</p>
                </div>
                <span className="category-test-count">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                    <polyline points="14 2 14 8 20 8" />
                  </svg>
                  {category.testCount} tests
                </span>
                <div className="category-list-actions">
                  <button className="btn btn-icon btn-ghost btn-sm">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                    </svg>
                  </button>
                  <button className="btn btn-icon btn-ghost btn-sm">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                    </svg>
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Category Detail Sidebar */}
        {selectedCategory && (
          <div className="category-sidebar-overlay" onClick={() => setSelectedCategory(null)}>
            <div className="category-sidebar" onClick={e => e.stopPropagation()}>
              <div className="category-sidebar-header">
                <button className="category-sidebar-close" onClick={() => setSelectedCategory(null)}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </div>
              <div className="category-sidebar-content">
                <div 
                  className="category-icon category-icon-lg"
                  style={{ backgroundColor: selectedCategory.color }}
                >
                  {selectedCategory.icon}
                </div>
                <h3 className="category-sidebar-title">{selectedCategory.name}</h3>
                <p className="category-sidebar-description">{selectedCategory.description}</p>
                
                <div className="category-stats">
                  <div className="category-stat">
                    <span className="category-stat-value">{selectedCategory.testCount}</span>
                    <span className="category-stat-label">Tests</span>
                  </div>
                  <div className="category-stat">
                    <span className="category-stat-value">1,234</span>
                    <span className="category-stat-label">Completions</span>
                  </div>
                  <div className="category-stat">
                    <span className="category-stat-value">78%</span>
                    <span className="category-stat-label">Avg. Score</span>
                  </div>
                </div>

                <div className="category-sidebar-actions">
                  <button className="btn btn-primary btn-full">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                    </svg>
                    Edit Category
                  </button>
                  <button className="btn btn-ghost btn-full">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                    View Tests
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

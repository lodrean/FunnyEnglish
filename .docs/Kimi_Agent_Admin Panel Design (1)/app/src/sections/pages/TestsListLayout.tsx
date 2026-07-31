import { useState } from 'react';

interface Test {
  id: number;
  name: string;
  category: string;
  questions: number;
  completions: number;
  avgScore: number;
  status: 'active' | 'draft' | 'archived';
  lastModified: string;
}

const mockTests: Test[] = [
  { id: 1, name: 'Basic Grammar', category: 'Grammar', questions: 25, completions: 1250, avgScore: 78, status: 'active', lastModified: '2024-01-15' },
  { id: 2, name: 'Vocabulary 101', category: 'Vocabulary', questions: 40, completions: 890, avgScore: 82, status: 'active', lastModified: '2024-01-14' },
  { id: 3, name: 'Listening Test A1', category: 'Listening', questions: 15, completions: 567, avgScore: 71, status: 'active', lastModified: '2024-01-13' },
  { id: 4, name: 'Reading Comprehension', category: 'Reading', questions: 30, completions: 432, avgScore: 65, status: 'draft', lastModified: '2024-01-12' },
  { id: 5, name: 'Writing Exercise', category: 'Writing', questions: 10, completions: 234, avgScore: 88, status: 'archived', lastModified: '2024-01-10' },
  { id: 6, name: 'Phrasal Verbs', category: 'Vocabulary', questions: 50, completions: 678, avgScore: 74, status: 'active', lastModified: '2024-01-09' },
  { id: 7, name: 'Advanced Grammar', category: 'Grammar', questions: 35, completions: 345, avgScore: 69, status: 'active', lastModified: '2024-01-08' },
];

export function TestsListLayout() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [selectedTests, setSelectedTests] = useState<number[]>([]);

  const filteredTests = mockTests.filter(test => {
    const matchesSearch = test.name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesCategory = !selectedCategory || test.category === selectedCategory;
    const matchesStatus = !selectedStatus || test.status === selectedStatus;
    return matchesSearch && matchesCategory && matchesStatus;
  });

  const toggleSelection = (id: number) => {
    setSelectedTests(prev => 
      prev.includes(id) ? prev.filter(i => i !== id) : [...prev, id]
    );
  };

  const selectAll = () => {
    setSelectedTests(selectedTests.length === filteredTests.length ? [] : filteredTests.map(t => t.id));
  };

  return (
    <section className="page-section">
      <div className="section-header">
        <h2 className="section-title">Tests</h2>
        <p className="section-description">
          Manage tests, view statistics, and organize content.
        </p>
      </div>

      <div className="tests-layout">
        {/* Header Actions */}
        <div className="tests-header">
          <div className="tests-search-filters">
            <div className="search-input-wrapper">
              <svg className="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <path d="M21 21l-4.35-4.35" />
              </svg>
              <input 
                type="text" 
                className="form-input search-input"
                placeholder="Search tests..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="form-select-wrapper">
              <select 
                className="form-select"
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
              >
                <option value="">All Categories</option>
                <option value="Grammar">Grammar</option>
                <option value="Vocabulary">Vocabulary</option>
                <option value="Listening">Listening</option>
                <option value="Reading">Reading</option>
                <option value="Writing">Writing</option>
              </select>
              <svg className="form-select-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M6 9l6 6 6-6" />
              </svg>
            </div>
            <div className="form-select-wrapper">
              <select 
                className="form-select"
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value)}
              >
                <option value="">All Status</option>
                <option value="active">Active</option>
                <option value="draft">Draft</option>
                <option value="archived">Archived</option>
              </select>
              <svg className="form-select-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M6 9l6 6 6-6" />
              </svg>
            </div>
          </div>
          <button className="btn btn-primary">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 5v14M5 12h14" />
            </svg>
            Create Test
          </button>
        </div>

        {/* Bulk Actions */}
        {selectedTests.length > 0 && (
          <div className="bulk-actions-bar">
            <span className="bulk-actions-count">{selectedTests.length} selected</span>
            <div className="bulk-actions-buttons">
              <button className="btn btn-ghost btn-sm">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                </svg>
                Edit
              </button>
              <button className="btn btn-ghost btn-sm">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2" />
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                </svg>
                Duplicate
              </button>
              <button className="btn btn-danger-outlined btn-sm">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                </svg>
                Delete
              </button>
            </div>
          </div>
        )}

        {/* Tests Table */}
        <div className="data-table-wrapper">
          <table className="data-table data-table-hover">
            <thead>
              <tr>
                <th className="checkbox-cell">
                  <label className="form-checkbox">
                    <input 
                      type="checkbox" 
                      checked={selectedTests.length === filteredTests.length && filteredTests.length > 0}
                      onChange={selectAll}
                    />
                    <span className="form-checkbox-checkmark">
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                        <path d="M20 6L9 17l-5-5" />
                      </svg>
                    </span>
                  </label>
                </th>
                <th>Test Name</th>
                <th>Category</th>
                <th>Questions</th>
                <th>Completions</th>
                <th>Avg. Score</th>
                <th>Status</th>
                <th>Last Modified</th>
                <th className="actions-cell">Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredTests.map((test) => (
                <tr key={test.id} className={selectedTests.includes(test.id) ? 'selected' : ''}>
                  <td className="checkbox-cell">
                    <label className="form-checkbox">
                      <input 
                        type="checkbox" 
                        checked={selectedTests.includes(test.id)}
                        onChange={() => toggleSelection(test.id)}
                      />
                      <span className="form-checkbox-checkmark">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                          <path d="M20 6L9 17l-5-5" />
                        </svg>
                      </span>
                    </label>
                  </td>
                  <td>
                    <div className="test-name-cell">
                      <span className="test-name">{test.name}</span>
                    </div>
                  </td>
                  <td>
                    <span className="badge badge-soft badge-primary">{test.category}</span>
                  </td>
                  <td>{test.questions}</td>
                  <td>{test.completions.toLocaleString()}</td>
                  <td>
                    <div className="score-cell">
                      <span className={`score-value ${test.avgScore >= 80 ? 'score-high' : test.avgScore >= 60 ? 'score-medium' : 'score-low'}`}>
                        {test.avgScore}%
                      </span>
                      <div className="score-bar">
                        <div 
                          className={`score-bar-fill ${test.avgScore >= 80 ? 'score-high' : test.avgScore >= 60 ? 'score-medium' : 'score-low'}`}
                          style={{ width: `${test.avgScore}%` }}
                        />
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className={`badge badge-${test.status === 'active' ? 'success' : test.status === 'draft' ? 'warning' : 'default'}`}>
                      {test.status}
                    </span>
                  </td>
                  <td>{test.lastModified}</td>
                  <td className="actions-cell">
                    <div className="table-actions">
                      <button className="btn btn-icon btn-ghost btn-sm" title="Edit">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                        </svg>
                      </button>
                      <button className="btn btn-icon btn-ghost btn-sm" title="Preview">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                          <circle cx="12" cy="12" r="3" />
                        </svg>
                      </button>
                      <button className="btn btn-icon btn-ghost btn-sm" title="More">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <circle cx="12" cy="12" r="1" />
                          <circle cx="19" cy="12" r="1" />
                          <circle cx="5" cy="12" r="1" />
                        </svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="pagination">
          <div className="pagination-info">
            Showing <strong>1</strong> to <strong>{filteredTests.length}</strong> of <strong>{filteredTests.length}</strong> results
          </div>
          <div className="pagination-controls">
            <button className="btn btn-ghost btn-sm" disabled>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M15 18l-6-6 6-6" />
              </svg>
              Previous
            </button>
            <div className="pagination-pages">
              <button className="pagination-page active">1</button>
              <button className="pagination-page">2</button>
              <button className="pagination-page">3</button>
              <span className="pagination-ellipsis">...</span>
              <button className="pagination-page">10</button>
            </div>
            <button className="btn btn-ghost btn-sm">
              Next
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M9 18l6-6-6-6" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </section>
  );
}

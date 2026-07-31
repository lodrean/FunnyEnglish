import { useState } from 'react';

export function FormShowcase() {
  const [checked, setChecked] = useState(false);
  const [radioValue, setRadioValue] = useState('option1');
  const [toggleOn, setToggleOn] = useState(true);

  return (
    <section className="component-section">
      <div className="section-header">
        <h2 className="section-title">Form Components</h2>
        <p className="section-description">
          Input fields, selects, checkboxes, and other form elements.
        </p>
      </div>

      <div className="component-grid">
        {/* Text Inputs */}
        <div className="component-group">
          <h4 className="component-group-title">Text Inputs</h4>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Default Input</label>
              <input type="text" className="form-input" placeholder="Enter text..." />
            </div>
          </div>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">With Value</label>
              <input type="text" className="form-input" value="Hello World" readOnly />
            </div>
          </div>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Disabled</label>
              <input type="text" className="form-input" placeholder="Disabled input" disabled />
            </div>
          </div>
          <div className="form-row">
            <div className="form-field form-field-error">
              <label className="form-label">With Error</label>
              <input type="text" className="form-input form-input-error" placeholder="Invalid input" />
              <span className="form-error">This field is required</span>
            </div>
          </div>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">With Helper</label>
              <input type="text" className="form-input" placeholder="Enter email..." />
              <span className="form-helper">We'll never share your email</span>
            </div>
          </div>
        </div>

        {/* Select */}
        <div className="component-group">
          <h4 className="component-group-title">Select</h4>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Default Select</label>
              <div className="form-select-wrapper">
                <select className="form-select">
                  <option value="">Choose an option...</option>
                  <option value="1">Option 1</option>
                  <option value="2">Option 2</option>
                  <option value="3">Option 3</option>
                </select>
                <svg className="form-select-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M6 9l6 6 6-6" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* Textarea */}
        <div className="component-group">
          <h4 className="component-group-title">Textarea</h4>
          <div className="form-row">
            <div className="form-field">
              <label className="form-label">Description</label>
              <textarea className="form-textarea" rows={4} placeholder="Enter description..."></textarea>
            </div>
          </div>
        </div>

        {/* Checkbox */}
        <div className="component-group">
          <h4 className="component-group-title">Checkbox</h4>
          <div className="form-row">
            <label className="form-checkbox">
              <input 
                type="checkbox" 
                checked={checked} 
                onChange={(e) => setChecked(e.target.checked)}
              />
              <span className="form-checkbox-checkmark">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                  <path d="M20 6L9 17l-5-5" />
                </svg>
              </span>
              <span className="form-checkbox-label">Default checkbox</span>
            </label>
          </div>
          <div className="form-row">
            <label className="form-checkbox">
              <input type="checkbox" checked disabled />
              <span className="form-checkbox-checkmark">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                  <path d="M20 6L9 17l-5-5" />
                </svg>
              </span>
              <span className="form-checkbox-label">Checked disabled</span>
            </label>
          </div>
          <div className="form-row">
            <label className="form-checkbox">
              <input type="checkbox" disabled />
              <span className="form-checkbox-checkmark">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
                  <path d="M20 6L9 17l-5-5" />
                </svg>
              </span>
              <span className="form-checkbox-label">Unchecked disabled</span>
            </label>
          </div>
        </div>

        {/* Radio */}
        <div className="component-group">
          <h4 className="component-group-title">Radio Button</h4>
          <div className="form-row">
            <label className="form-radio">
              <input 
                type="radio" 
                name="radio-group" 
                value="option1"
                checked={radioValue === 'option1'}
                onChange={(e) => setRadioValue(e.target.value)}
              />
              <span className="form-radio-checkmark" />
              <span className="form-radio-label">Option 1</span>
            </label>
          </div>
          <div className="form-row">
            <label className="form-radio">
              <input 
                type="radio" 
                name="radio-group" 
                value="option2"
                checked={radioValue === 'option2'}
                onChange={(e) => setRadioValue(e.target.value)}
              />
              <span className="form-radio-checkmark" />
              <span className="form-radio-label">Option 2</span>
            </label>
          </div>
          <div className="form-row">
            <label className="form-radio">
              <input type="radio" name="radio-group2" disabled />
              <span className="form-radio-checkmark" />
              <span className="form-radio-label">Disabled option</span>
            </label>
          </div>
        </div>

        {/* Toggle/Switch */}
        <div className="component-group">
          <h4 className="component-group-title">Toggle Switch</h4>
          <div className="form-row">
            <label className="form-toggle">
              <input 
                type="checkbox" 
                checked={toggleOn}
                onChange={(e) => setToggleOn(e.target.checked)}
              />
              <span className="form-toggle-slider">
                <span className="form-toggle-thumb" />
              </span>
              <span className="form-toggle-label">{toggleOn ? 'On' : 'Off'}</span>
            </label>
          </div>
          <div className="form-row">
            <label className="form-toggle">
              <input type="checkbox" disabled />
              <span className="form-toggle-slider">
                <span className="form-toggle-thumb" />
              </span>
              <span className="form-toggle-label">Disabled</span>
            </label>
          </div>
        </div>
      </div>
    </section>
  );
}

import { useState } from 'react';
import { ThemeProvider, ThemeToggle, useTheme } from './design-system/components/ThemeProvider';
import { ColorPalette } from './sections/colors/ColorPalette';
import { TypographyScale } from './sections/typography/TypographyScale';
import { ButtonShowcase } from './sections/components/ButtonShowcase';
import { FormShowcase } from './sections/components/FormShowcase';
import { DataDisplayShowcase } from './sections/components/DataDisplayShowcase';
import { FeedbackShowcase } from './sections/components/FeedbackShowcase';
import { DashboardLayout } from './sections/pages/DashboardLayout';
import { TestsListLayout } from './sections/pages/TestsListLayout';
import { TestEditorLayout } from './sections/pages/TestEditorLayout';
import { CategoriesLayout } from './sections/pages/CategoriesLayout';
import { UsersLayout } from './sections/pages/UsersLayout';
import './App.css';

type Section = 
  | 'colors' 
  | 'typography' 
  | 'buttons' 
  | 'forms' 
  | 'data-display' 
  | 'feedback'
  | 'dashboard'
  | 'tests'
  | 'test-editor'
  | 'categories'
  | 'users';

const sections: { id: Section; label: string; category: string }[] = [
  { id: 'colors', label: 'Colors', category: 'Design Tokens' },
  { id: 'typography', label: 'Typography', category: 'Design Tokens' },
  { id: 'buttons', label: 'Buttons', category: 'Components' },
  { id: 'forms', label: 'Forms', category: 'Components' },
  { id: 'data-display', label: 'Data Display', category: 'Components' },
  { id: 'feedback', label: 'Feedback', category: 'Components' },
  { id: 'dashboard', label: 'Dashboard', category: 'Page Layouts' },
  { id: 'tests', label: 'Tests List', category: 'Page Layouts' },
  { id: 'test-editor', label: 'Test Editor', category: 'Page Layouts' },
  { id: 'categories', label: 'Categories', category: 'Page Layouts' },
  { id: 'users', label: 'Users', category: 'Page Layouts' },
];

function AppContent() {
  const [activeSection, setActiveSection] = useState<Section>('dashboard');
  const { theme } = useTheme();

  const renderSection = () => {
    switch (activeSection) {
      case 'colors':
        return <ColorPalette />;
      case 'typography':
        return <TypographyScale />;
      case 'buttons':
        return <ButtonShowcase />;
      case 'forms':
        return <FormShowcase />;
      case 'data-display':
        return <DataDisplayShowcase />;
      case 'feedback':
        return <FeedbackShowcase />;
      case 'dashboard':
        return <DashboardLayout />;
      case 'tests':
        return <TestsListLayout />;
      case 'test-editor':
        return <TestEditorLayout />;
      case 'categories':
        return <CategoriesLayout />;
      case 'users':
        return <UsersLayout />;
      default:
        return <DashboardLayout />;
    }
  };

  const groupedSections = sections.reduce((acc, section) => {
    if (!acc[section.category]) {
      acc[section.category] = [];
    }
    acc[section.category].push(section);
    return acc;
  }, {} as Record<string, typeof sections>);

  return (
    <div className="app">
      <header className="app-header">
        <div className="app-header-left">
          <div className="app-logo">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />
            </svg>
            FunnyEnglish Design System
          </div>
          <nav className="app-nav">
            {Object.entries(groupedSections).map(([category, items]) => (
              <div key={category} className="nav-group">
                {items.map((section) => (
                  <button
                    key={section.id}
                    className={`nav-link ${activeSection === section.id ? 'active' : ''}`}
                    onClick={() => setActiveSection(section.id)}
                  >
                    {section.label}
                  </button>
                ))}
              </div>
            ))}
          </nav>
        </div>
        <div className="app-header-right">
          <span className="theme-indicator">
            {theme === 'light' ? 'Light' : 'Dark'} Theme
          </span>
          <ThemeToggle />
        </div>
      </header>

      <main className="app-main">
        {renderSection()}
      </main>
    </div>
  );
}

function App() {
  return (
    <ThemeProvider>
      <AppContent />
    </ThemeProvider>
  );
}

export default App;

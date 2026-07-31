# Storybook + Chromatic Setup Guide

## Installation

```bash
cd admin-web

# Install Storybook
npx storybook@latest init

# Install Chromatic
npm install --save-dev chromatic
```

## Running Storybook

```bash
# Development mode
npm run storybook

# Build static version
npm run build-storybook
```

## Chromatic Setup

1. Get project token from [chromatic.com](https://www.chromatic.com/)
2. Add token to GitHub Secrets: `CHROMATIC_TOKEN`

## GitHub Actions Integration

Add to `.github/workflows/visual-tests.yml`:

```yaml
name: Visual Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  chromatic:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Required for git history

      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: admin-web/package-lock.json

      - name: Install dependencies
        run: |
          cd admin-web
          npm ci

      - name: Publish to Chromatic
        uses: chromaui/action@latest
        with:
          projectToken: ${{ secrets.CHROMATIC_TOKEN }}
          workingDir: admin-web
```

## Creating Stories

```tsx
// Button.stories.tsx
import type { Meta, StoryObj } from '@storybook/react';
import { Button } from '@mui/material';

const meta: Meta<typeof Button> = {
  title: 'Components/Button',
  component: Button,
};

export default meta;

export const Primary: Story = {
  args: {
    variant: 'contained',
    children: 'Click me',
  },
};
```

## Visual Regression Workflow

1. Developer creates PR
2. Chromatic builds Storybook
3. Visual changes detected → Review required
4. Designer/QA approves changes
5. PR can be merged

## Best Practices

- Write stories for all UI components
- Use args for component variants
- Add interaction tests with `@storybook/addon-interactions`
- Document component props with JSDoc

# AIDD Hooks

This directory contains hooks and scripts for the AI-Driven Development workflow.

## Available Hooks

### pre-commit.sh
Git pre-commit hook that runs basic checks before allowing a commit:
- Validates branch name (warns on main/develop)
- Checks commit message format (conventional commits)
- Warns about TODO/FIXME in staged files
- Runs syntax checks for Kotlin and TypeScript
- Warns about large file additions

**Installation:**
```bash
# Copy to git hooks
cp .claude/hooks/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### validate-gates.sh
Validates all quality gates for a specific ticket:
- Checks existence of required documents
- Validates document statuses
- Reports pass/fail for each gate

**Usage:**
```bash
.claude/hooks/validate-gates.sh <ticket-id>

# Example:
.claude/hooks/validate-gates.sh FEAT-123
```

## CI/CD Integration

These hooks can be integrated into CI/CD pipelines:

### GitHub Actions Example
```yaml
name: AIDD Quality Gates
on: [pull_request]

jobs:
  validate-gates:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Extract ticket ID from branch
        id: ticket
        run: |
          TICKET=$(echo ${{ github.head_ref }} | grep -oE '[A-Z]+-[0-9]+' || echo "")
          echo "ticket=$TICKET" >> $GITHUB_OUTPUT
      
      - name: Validate Quality Gates
        if: steps.ticket.outputs.ticket != ''
        run: |
          chmod +x .claude/hooks/validate-gates.sh
          .claude/hooks/validate-gates.sh ${{ steps.ticket.outputs.ticket }}
```

### Pre-commit Hook Installation

To install the pre-commit hook for all developers:

```bash
# One-time setup
chmod +x .claude/hooks/pre-commit.sh
ln -s ../../.claude/hooks/pre-commit.sh .git/hooks/pre-commit
```

Or add to your setup script:
```bash
#!/bin/bash
# setup-hooks.sh

if [ -d .git ]; then
    cp .claude/hooks/pre-commit.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    echo "✅ Pre-commit hook installed"
else
    echo "❌ Not a git repository"
    exit 1
fi
```

## Custom Hooks

You can create custom hooks by following the naming convention:
- `pre-<action>.sh` - Runs before an action
- `post-<action>.sh` - Runs after an action
- `validate-<gate>.sh` - Validates a specific gate

### Hook Template
```bash
#!/bin/bash
set -e

echo "🔍 Running custom hook..."

# Your logic here

if [ "$SUCCESS" = true ]; then
    echo "✅ Hook passed"
    exit 0
else
    echo "❌ Hook failed"
    exit 1
fi
```

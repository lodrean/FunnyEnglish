#!/bin/bash
# Pre-commit hook for AIDD quality gates
# Place in .claude/hooks/ or .git/hooks/

set -e

echo "🔍 Running AIDD pre-commit checks..."

# Check if we're in a feature branch
BRANCH=$(git branch --show-current)
if [[ "$BRANCH" == "main" || "$BRANCH" == "develop" ]]; then
    echo "⚠️  Warning: Committing directly to $BRANCH branch"
    echo "Consider creating a feature branch: git checkout -b feature/your-feature"
fi

# Check for tasklist reference in commit message (if not a merge)
if [ -f .git/MERGE_MSG ]; then
    echo "✅ Merge commit - skipping tasklist check"
else
    COMMIT_MSG=$(cat .git/COMMIT_EDITMSG 2>/dev/null || echo "")
    if [[ ! "$COMMIT_MSG" =~ ^(feat|fix|refactor|docs|test|chore) ]]; then
        echo "⚠️  Warning: Commit message doesn't follow conventional format"
        echo "   Format: type(scope): description"
        echo "   Types: feat, fix, refactor, docs, test, chore"
    fi
fi

# Check for TODO/FIXME in staged files
echo "🔍 Checking for TODO/FIXME in staged files..."
if git diff --cached --name-only | xargs grep -l "TODO\|FIXME" 2>/dev/null; then
    echo "⚠️  Warning: Found TODO/FIXME in staged files"
    echo "   Consider completing or tracking these before commit"
fi

# Run quick syntax checks for Kotlin files
echo "🔍 Running Kotlin syntax check..."
KOTLIN_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep '\.kt$' || true)
if [ -n "$KOTLIN_FILES" ]; then
    echo "Found Kotlin files: $KOTLIN_FILES"
    # Optional: Run detekt or ktlint
    # ./gradlew detektCheck --parallel
fi

# Run TypeScript checks for admin-web
echo "🔍 Running TypeScript check..."
TS_FILES=$(git diff --cached --name-only --diff-filter=ACM | grep 'admin-web.*\.tsx\?$' || true)
if [ -n "$TS_FILES" ]; then
    echo "Found TypeScript files in admin-web"
    # Optional: cd admin-web && npm run lint
fi

# Check for large files
echo "🔍 Checking for large files..."
git diff --cached --numstat | while read added removed file; do
    if [ "$added" -gt 1000 ]; then
        echo "⚠️  Warning: Large addition in $file ($added lines)"
    fi
done

echo "✅ Pre-commit checks complete"

#!/bin/bash

# Install git hooks
# This script copies hooks from .githooks/ to .git/hooks/

set -e

GITHOOKS_DIR="$(cd "$(dirname "$0")" && pwd)"
GIT_HOOKS_DIR="$GITHOOKS_DIR/../.git/hooks"

if [ ! -d "$GIT_HOOKS_DIR" ]; then
    echo "Error: .git/hooks directory not found. Are you in a git repository?"
    exit 1
fi

echo "Installing git hooks..."

# Copy pre-push hook
if [ -f "$GITHOOKS_DIR/pre-push" ]; then
    cp "$GITHOOKS_DIR/pre-push" "$GIT_HOOKS_DIR/pre-push"
    chmod +x "$GIT_HOOKS_DIR/pre-push"
    echo "✓ Installed pre-push hook"
else
    echo "✗ pre-push hook not found"
fi

echo "Git hooks installed successfully!"


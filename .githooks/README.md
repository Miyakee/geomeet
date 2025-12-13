# Git Hooks

This directory contains git hooks for code quality checks.

## Pre-push Hook

The `pre-push` hook automatically runs code quality checks before pushing code to the remote repository.

### What it does:

- **API changes**: If any files in the `api/` directory are modified, it runs:
  - Checkstyle
  - PMD
  - SpotBugs
  - Tests

- **UI changes**: If any files in the `ui/` directory are modified, it runs:
  - ESLint
  - Tests (if test script exists)

- If no relevant changes are detected, checks are skipped.

### Installation

Run the install script:

```bash
./.githooks/install.sh
```

Or manually copy the hook:

```bash
cp .githooks/pre-push .git/hooks/pre-push
chmod +x .git/hooks/pre-push
```

### Usage

The hook runs automatically when you push:

```bash
git push origin main
```

If any check fails, the push will be blocked. Fix the issues and try again.

### Bypassing the hook (not recommended)

If you need to bypass the hook in an emergency:

```bash
git push --no-verify
```

**Warning**: Only use this in exceptional circumstances. The checks are there to maintain code quality.

### Troubleshooting

- **Hook not running**: Make sure it's installed and executable:
  ```bash
  ls -la .git/hooks/pre-push
  ```

- **Checks taking too long**: The checks run on all changed files. For large changes, consider breaking them into smaller commits.

- **False positives**: If a check is incorrectly flagging valid code, update the tool's configuration file in the respective directory.


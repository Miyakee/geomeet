# Code Quality Tools Configuration

This directory contains configuration files for static code analysis tools.

## Tools

### Checkstyle
- **Config**: `checkstyle/checkstyle.xml`
- **Version**: 10.12.5
- **Purpose**: Enforces coding standards and style guidelines
- **Reports**: `build/reports/checkstyle/`

### PMD
- **Config**: `pmd/pmd-rules.xml`
- **Version**: 7.0.0
- **Purpose**: Detects common programming flaws and code smells
- **Reports**: `build/reports/pmd/`

### SpotBugs
- **Config**: `spotbugs/exclude.xml`
- **Version**: 4.8.3
- **Purpose**: Finds bugs in Java code
- **Reports**: `build/reports/spotbugs/`

## Usage

Run all quality checks:
```bash
./gradlew check
```

Run individual tools:
```bash
# Checkstyle
./gradlew checkstyleMain checkstyleTest

# PMD
./gradlew pmdMain pmdTest

# SpotBugs
./gradlew spotbugsMain spotbugsTest
```

View reports:
- Checkstyle: `build/reports/checkstyle/main.html`
- PMD: `build/reports/pmd/main.html`
- SpotBugs: `build/reports/spotbugs/main.html`

## Configuration Notes

- All tools are configured to fail the build on violations (`ignoreFailures = false`)
- Reports are generated in HTML format
- Test classes have relaxed rules where appropriate
- Generated classes and build artifacts are excluded


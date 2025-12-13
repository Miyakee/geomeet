# JaCoCo Test Coverage Configuration

## Overview

This project uses JaCoCo (Java Code Coverage) to enforce a minimum test coverage of **90%**.

## Configuration

### Coverage Threshold

- **Minimum Coverage**: 90%
- **Applies to**: Classes, Methods, Branches, Lines
- **Enforcement**: Build fails if coverage is below 90%

### Excluded Classes

The following classes are excluded from coverage requirements (configuration classes, DTOs, etc.):

- `com.geomeet.api.ApiApplication` - Main application class
- `com.geomeet.api.controller.*` - Legacy controllers
- `com.geomeet.api.adapter.web.health.HealthController` - Simple health check
- `com.geomeet.api.infrastructure.config.*` - Configuration classes
- `com.geomeet.api.domain.exception.DomainException` - Base exception class
- `com.geomeet.api.application.command.*` - Command DTOs
- `com.geomeet.api.application.result.*` - Result DTOs
- `com.geomeet.api.adapter.web.auth.dto.*` - DTOs

## Usage

### Generate Coverage Report

```bash
# Run tests and generate coverage report
./gradlew test jacocoTestReport

# View HTML report
open build/reports/jacoco/test/html/index.html
```

### Verify Coverage

```bash
# Run tests and verify coverage meets 90% threshold
./gradlew test jacocoTestCoverageVerification

# This will fail the build if coverage is below 90%
```

### Full Quality Check

```bash
# Run all quality checks including coverage
./gradlew check

# This runs:
# - Checkstyle
# - PMD
# - SpotBugs
# - Tests
# - JaCoCo Coverage Verification
```

## Coverage Reports

Reports are generated in the following locations:

- **HTML Report**: `build/reports/jacoco/test/html/index.html`
- **XML Report**: `build/reports/jacoco/test/jacocoTestReport.xml`
- **CSV Report**: Not generated (disabled)

## Coverage Metrics

JaCoCo tracks the following metrics:

1. **Line Coverage**: Percentage of lines executed
2. **Branch Coverage**: Percentage of branches (if/else, switch) executed
3. **Method Coverage**: Percentage of methods executed
4. **Class Coverage**: Percentage of classes executed

## CI/CD Integration

The coverage verification is automatically run as part of the `check` task, which is typically executed in CI/CD pipelines.

### Example CI Configuration

```yaml
# GitHub Actions example
- name: Run tests with coverage
  run: ./gradlew test jacocoTestReport

- name: Verify coverage
  run: ./gradlew jacocoTestCoverageVerification

- name: Upload coverage report
  uses: codecov/codecov-action@v3
  with:
    files: build/reports/jacoco/test/jacocoTestReport.xml
```

## Troubleshooting

### Coverage Below 90%

If coverage is below 90%, the build will fail with an error message showing which classes don't meet the threshold.

**Solutions**:
1. Add more unit tests for uncovered code
2. Review excluded classes list - add legitimate exclusions if needed
3. Check if new classes were added that need tests

### Viewing Coverage Details

1. Generate the HTML report: `./gradlew jacocoTestReport`
2. Open `build/reports/jacoco/test/html/index.html` in a browser
3. Navigate to specific packages/classes to see line-by-line coverage

### Excluding Classes

To exclude additional classes from coverage requirements, update the `excludes` list in `build.gradle`:

```gradle
excludes = [
    'com.geomeet.api.YourClass',
    'com.geomeet.api.package.*'
]
```

## Best Practices

1. **Write tests first** (TDD) to ensure high coverage from the start
2. **Review coverage reports regularly** to identify gaps
3. **Focus on business logic** - don't just aim for 100% coverage
4. **Exclude legitimate cases** - configuration classes, DTOs, etc.
5. **Use branch coverage** to ensure all code paths are tested

## Current Coverage Status

To check current coverage:

```bash
./gradlew test jacocoTestReport
# Then open build/reports/jacoco/test/html/index.html
```

The report will show:
- Overall coverage percentage
- Coverage by package
- Coverage by class
- Line-by-line coverage highlighting


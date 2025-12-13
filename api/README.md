# GeoMeet API

Spring Boot REST API built with Clean Architecture.

## Architecture

The project follows Clean Architecture principles with the following layers:

- **Domain Layer** (`domain/`): Entities, Repository interfaces
- **Application Layer** (`application/`): Use Case implementations, Exceptions
- **Infrastructure Layer** (`infrastructure/`): JPA repositories, Security, External services, Configuration
- **Adapter Layer** (`adapter/`): REST controllers, DTOs

## Prerequisites

- Java 17+
- PostgreSQL 12+
- Gradle 7+

## Setup

### 1. Database Setup

Start PostgreSQL using Docker Compose:

```bash
docker-compose up -d
```

Or create a PostgreSQL database manually:

```sql
CREATE DATABASE geomeet_local;
```

### 2. Configuration

Update `application-local.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geomeet_local
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## Default Users

When running in `local` profile, the following test users are automatically created:

- **Admin User**:
  - Username/Email: `admin` / `admin@geomeet.com`
  - Password: `admin123`

- **Test User**:
  - Username/Email: `testuser` / `test@geomeet.com`
  - Password: `test123`

## API Endpoints

### Authentication

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "admin",
  "password": "admin123"
}
```

**Success Response (200)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@geomeet.com",
  "message": "Login successful"
}
```

**Error Response (401)**:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

### Health Check

```http
GET /health
```

## Security

- Passwords are hashed using BCrypt (12 rounds)
- JWT tokens are used for authentication
- Tokens expire after 24 hours (configurable)
- CSRF is disabled for stateless API (JWT-based)

## Development

### Running Tests

```bash
# Run tests
./gradlew test

# Run tests and generate coverage report
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Code Quality Checks

```bash
./gradlew check
```

This runs:
- Checkstyle
- PMD
- SpotBugs
- Tests
- JaCoCo Coverage Verification (90% minimum required)

### Test Coverage

The project requires **90% minimum test coverage**. Coverage verification is automatically run as part of the `check` task.

```bash
# Generate coverage report
./gradlew jacocoTestReport

# Verify coverage meets 90% threshold
./gradlew jacocoTestCoverageVerification

# View HTML report
open build/reports/jacoco/test/html/index.html
```

If coverage is below 90%, the build will fail. See `JACOCO_CONFIG.md` for detailed configuration.

## Project Structure

```
src/main/java/com/geomeet/api/
├── domain/              # Domain layer
│   ├── entity/          # Domain entities
│   └── repository/      # Repository interfaces
├── application/         # Application layer
│   ├── exception/       # Application exceptions
│   └── usecase/         # Use case implementations
├── infrastructure/      # Infrastructure layer
│   ├── config/          # Configuration classes
│   ├── persistence/     # JPA repositories
│   └── security/        # Security services
└── adapter/            # Adapter layer
    └── web/             # Web adapters
        └── auth/        # Authentication adapters
            ├── AuthController.java
            └── dto/      # Data Transfer Objects
```


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

## Health Check

```http
GET /health
```

## WebSocket

For detailed WebSocket documentation including connection setup, subscription topics, message formats, and examples, see [WebSocket Documentation](../README.md#-websocket) in the main README.

## Default Users

When running in `local` profile, the following test users are automatically created:

| Username | Email | Password |
|----------|-------|----------|
| admin | admin@geomeet.com | admin123 |
| testuser | test@geomeet.com | test123 |
| tty | tty@geomeet.com | tty123 |


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

**Response (200)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@geomeet.com",
  "message": "Login successful"
}
```

### Session Management

#### Create Session
```http
POST /api/sessions
Authorization: Bearer <token>
```

#### Join Session
```http
POST /api/sessions/join
Authorization: Bearer <token>
Content-Type: application/json

{
  "sessionId": "session-id-123"
}
```

#### Get Session Details
```http
GET /api/sessions/{sessionId}
Authorization: Bearer <token>
```

#### Generate Invite Link
```http
GET /api/sessions/{sessionId}/invite
Authorization: Bearer <token>
```

**Response (200)**:
```json
{
  "inviteLink": "https://example.com/join?sessionId=abc123",
  "inviteCode": "ABC123"
}
```

#### Update Location
```http
POST /api/sessions/{sessionId}/location
Authorization: Bearer <token>
Content-Type: application/json

{
  "latitude": 1.3521,
  "longitude": 103.8198,
  "accuracy": 10.5
}
```

#### Calculate Optimal Location
```http
POST /api/sessions/{sessionId}/optimal-location
Authorization: Bearer <token>
```

**Response (200)**:
```json
{
  "latitude": 1.3212,
  "longitude": 103.8359
}
```

#### Update Meeting Location (Initiator Only)
```http
PUT /api/sessions/{sessionId}/meeting-location
Authorization: Bearer <token>
Content-Type: application/json

{
  "latitude": 1.3521,
  "longitude": 103.8198
}
```

#### End Session (Initiator Only)
```http
POST /api/sessions/{sessionId}/end
Authorization: Bearer <token>
```


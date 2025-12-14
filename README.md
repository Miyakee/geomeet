# GeoMeet

A real-time collaborative meeting platform built with Spring Boot and React, following DDD (Domain-Driven Design) and Clean Architecture principles.

## 🚀 Features

- **User Authentication**: JWT-based authentication with secure password hashing
- **Session Management**: Create and join collaborative sessions
- **Real-time Updates**: WebSocket-based real-time synchronization for session participants
- **Invite System**: Generate invite links and codes for session sharing
- **Clean Architecture**: DDD + Clean Architecture for maintainable and testable code
- **Code Quality**: Automated code quality checks with pre-push hooks

## 📋 Table of Contents

- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Quick Start](#-quick-start)
- [Architecture](#-architecture)
- [API Documentation](#-api-documentation)
- [Development](#-development)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## 🛠 Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3** - Application framework
- **PostgreSQL** - Database
- **Gradle** - Build tool
- **JWT** - Authentication
- **WebSocket/STOMP** - Real-time communication
- **Flyway** - Database migrations

### Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Material UI (MUI)** - Component library
- **Vite** - Build tool
- **React Router** - Routing
- **Axios** - HTTP client
- **STOMP.js** - WebSocket client

### DevOps & Tools
- **Docker Compose** - Database containerization
- **Git Hooks** - Pre-push code quality checks
- **Checkstyle, PMD, SpotBugs** - Code quality tools
- **JaCoCo** - Code coverage
- **ESLint** - JavaScript linting

## 📁 Project Structure

```
geomeet/
├── api/                    # Backend Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/geomeet/api/
│   │   │   │       ├── domain/          # Domain layer (DDD)
│   │   │   │       ├── application/     # Application layer (Use Cases)
│   │   │   │       ├── infrastructure/  # Infrastructure layer
│   │   │   │       └── adapter/         # Adapter layer (Controllers)
│   │   │   └── resources/
│   │   │       ├── application*.properties
│   │   │       └── db/migration/         # Flyway migrations
│   │   └── test/                         # Test files
│   ├── build.gradle
│   └── README.md
├── ui/                      # Frontend React application
│   ├── src/
│   │   ├── components/     # Reusable components
│   │   ├── contexts/        # React contexts
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   └── App.tsx
│   ├── package.json
│   └── README.md
├── .githooks/               # Git hooks for code quality
├── docker-compose.yml       # PostgreSQL database setup
├── DOCKER.md                # Docker documentation
├── WEBSOCKET_FLOW.md        # WebSocket flow documentation
└── README.md                # This file
```

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 12+** (or use Docker Compose)
- **Docker & Docker Compose** (optional, for database)

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/geomeet.git
cd geomeet
```

### 2. Start Database

Using Docker Compose (recommended):

```bash
docker-compose up -d
```

Or use a local PostgreSQL instance:

```bash
createdb geomeet_local
```

### 3. Install Git Hooks

```bash
./.githooks/install.sh
```

This installs pre-push hooks for code quality checks.

### 4. Start Backend

```bash
cd api
./gradlew bootRun
```

Backend will start on `http://localhost:8080`

### 5. Start Frontend

In a new terminal:

```bash
cd ui
npm install
npm run dev
```

Frontend will start on `http://localhost:5173`

### 6. Access the Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/health

## 👥 Default Users

When running in `local` profile, the following test users are automatically created:

| Username | Email | Password |
|----------|-------|----------|
| admin | admin@geomeet.com | admin123 |
| testuser | test@geomeet.com | test123 |
| tty | tty@geomeet.com | tty123 |

## 🏗 Architecture

This project follows **DDD (Domain-Driven Design) + Clean Architecture** principles.

### Architecture Layers

1. **Domain Layer** (最内层)
   - Pure business logic
   - No framework dependencies
   - Entities, Value Objects, Domain Services

2. **Application Layer**
   - Use Cases orchestration
   - Repository interfaces (ports)
   - Commands and Results

3. **Infrastructure Layer**
   - JPA repositories (adapters)
   - Security services
   - External system integrations

4. **Adapter Layer**
   - REST controllers
   - DTOs
   - HTTP request/response handling

### Dependency Flow

```
Adapter → Application → Domain ← Infrastructure
```

For detailed architecture documentation, see [api/ARCHITECTURE.md](api/ARCHITECTURE.md).

## 📡 API Documentation

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

### WebSocket

The application uses WebSocket for real-time session updates. Clients subscribe to:

```
/topic/session/{sessionId}
```

For detailed WebSocket flow, see [WEBSOCKET_FLOW.md](WEBSOCKET_FLOW.md).

## 💻 Development

### Backend Development

```bash
cd api

# Run tests
./gradlew test

# Run code quality checks
./gradlew check

# Generate test coverage report
./gradlew jacocoTestReport

# View coverage report
open build/reports/jacoco/test/html/index.html
```

### Frontend Development

```bash
cd ui

# Install dependencies
npm install

# Start dev server
npm run dev

# Run linting
npm run lint

# Run tests
npm test
```

### Code Quality

The project includes automated code quality checks:

- **Backend**: Checkstyle, PMD, SpotBugs, Tests (90% coverage required)
- **Frontend**: ESLint, Tests

Pre-push hooks automatically run these checks before pushing code. See [.githooks/README.md](.githooks/README.md) for details.

## 🧪 Testing

### Backend Tests

```bash
cd api

# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport

# Verify coverage (must be ≥90%)
./gradlew jacocoTestCoverageVerification
```

**Coverage Requirement**: 90% minimum code coverage is enforced.

### Frontend Tests

```bash
cd ui

# Run tests
npm test

# Run with coverage
npm run test:coverage

# Run with UI
npm run test:ui
```

## 🚢 Deployment

### Backend Deployment

1. Build the application:
```bash
cd api
./gradlew build
```

2. Run the JAR:
```bash
java -jar build/libs/api-*.jar --spring.profiles.active=prod
```

### Frontend Deployment

1. Build for production:
```bash
cd ui
npm run build
```

2. Serve the `dist/` directory with a web server (nginx, Apache, etc.)

### Docker Deployment

See [DOCKER.md](DOCKER.md) for Docker Compose setup and database management.

## 🔒 Security

- **Password Hashing**: BCrypt with 12 rounds
- **JWT Authentication**: Token-based stateless authentication
- **Token Expiration**: 24 hours (configurable)
- **CORS**: Configured for development and production
- **Input Validation**: Jakarta Validation on all endpoints

## 📚 Additional Documentation

- [Backend API README](api/README.md) - Detailed backend documentation
- [Frontend UI README](ui/README.md) - Frontend documentation
- [Architecture Documentation](api/ARCHITECTURE.md) - DDD + Clean Architecture details
- [Quick Start Guide](api/QUICKSTART.md) - Quick setup guide
- [Docker Guide](DOCKER.md) - Docker Compose usage
- [WebSocket Flow](WEBSOCKET_FLOW.md) - WebSocket communication flow
- [WebSocket Internal Mechanism](WEBSOCKET_INTERNAL_MECHANISM.md) - How Spring WebSocket works
- [Message Queue Recommendations](MESSAGE_QUEUE_RECOMMENDATIONS.md) - External message queue options

## 🤝 Contributing

1. **Install Git Hooks**: Run `./.githooks/install.sh` to install pre-push hooks
2. **Create a Branch**: Create a feature branch from `main`
3. **Make Changes**: Follow the existing code style and architecture
4. **Run Tests**: Ensure all tests pass and coverage is ≥90%
5. **Commit**: Write clear commit messages
6. **Push**: Pre-push hooks will run code quality checks
7. **Create PR**: Submit a pull request with a clear description

### Code Style

- **Backend**: Follow Java conventions, Checkstyle rules enforced
- **Frontend**: ESLint rules enforced, TypeScript strict mode
- **Architecture**: Follow DDD + Clean Architecture principles

## 📝 License

[Add your license here]

## 👨‍💻 Authors

- Chen Tingyu - tingyu.chen@thoughtworks.com

## 🙏 Acknowledgments

- Spring Boot team
- React team
- Material UI team
- All contributors

---

**Note**: This is a demonstration project showcasing DDD + Clean Architecture principles with real-time collaboration features.


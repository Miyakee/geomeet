# GeoMeet

A real-time collaborative meeting platform built with Spring Boot and React, following DDD (Domain-Driven Design) and Clean Architecture principles.

## 📋 Table of Contents

- [Key Design Decisions & Trade off](#-key-design-decisions--trade-off)
- [Quick Start](#-quick-start)
- [Deployment](#-deployment)
- [Improvement](#-improvement)
- [Development](#-development)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [WebSocket](#-websocket)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Security](#-security)
- [Additional Documentation](#-additional-documentation)

## 🚀 Key Design Decisions & Trade off

### WebSocket without Message Queue (SQS)
The system uses WebSocket only to establish a real-time, full-duplex communication channel.

Reasoning:

WebSocket provides low-latency and efficient real-time communication

Current user volume is relatively small

Occasional message loss is acceptable

No requirement for historical message tracing or persistence

By not introducing SQS, we reduce system complexity and minimize latency.

Trade-off:
Lower latency and simpler architecture at the cost of message durability and guaranteed delivery.

###  User Location Is Not Requested on Page Load

The application does not automatically request location permission when the page loads.

Reasoning:

Gives users more autonomy and transparency

Improves perceived security and trust

Ensures explicit user consent before location tracking

Trade-off:
Better privacy and user control, with a slight delay in obtaining location data.

###  Manual Location Input as a Fallback

In some scenarios:

Location permission is denied
Location retrieval fails
Location accuracy is insufficient
Location is privacy  
 
These cases may lead to incorrect meeting point calculations.
To address this, the system supports manual location input. After user input. we should stop tracking user's current position

Trade-off:
Improved reliability and correctness at the cost of additional user interaction.

### Optimal Position vs. Meeting Position

The system calculates two different positions:

Optimal Position and Meeting Position

This design comes from a clarified requirement:
the initiator is allowed to modify the meeting address, which means the meeting position is not necessarily the optimal position calculated by the system.

To clearly distinguish these two concepts:

Different map icons are used for the optimal position and the meeting position

Users can visually understand whether the meeting point is system-recommended or manually adjusted

To prevent unreasonable meeting point selections:

Any participant can trigger the recalculation of the optimal position

The optimal position can be used as a reference for comparison when adjusting or reviewing the meeting position

Trade-off:
This approach increases UI and logic complexity, but improves flexibility, transparency, and decision quality for all participants.

### Authentication, Verification, and API Cost Control

Since the map APIs are charged based on usage, and to prevent malicious access or abuse, the system includes a login and user registration flow.

User Authentication

Users must log in to access core functionality

This limits unnecessary or automated API calls and helps control cost

User Registration and Verification

New users are required to register an account

A verification code is required during registration

Currently, a fixed code (2025) is used for verification

This can be considered a placeholder for an email verification code

It is designed to be replaced with a real email-based verification mechanism in the future

Uniqueness Constraints

Username must be unique

Email must be unique

Duplicate usernames or emails are not allowed

Trade-off:
Using a fixed verification code simplifies development and testing, but provides limited security.
This approach reduces implementation complexity in the early stage while leaving room for future enhancement.

### Invite Code for Secure Session Joining

An invite code mechanism was added to the session invitation flow to prevent brute-force guessing of session IDs.

Design Details:

Each session is associated with a 6-digit invite code

Users can join a session only by:

1. Using a valid invite link

2. Providing both session ID + invite code

This ensures that knowing a session ID alone is not sufficient to join a session.

Trade-off:
The invite code adds an extra step for users, but significantly improves security by limiting unauthorized or malicious session access.

###  Multiple Map API Switching

The project uses free map APIs to control cost.

To improve availability:

Multiple map providers are supported

APIs can be switched dynamically

Service downtime from a single provider can be mitigated quickly

Trade-off:
Increased implementation complexity in exchange for better availability and cost control.

###  HTTPS Requirement

The application uses HTTPS instead of HTTP.

Reasoning:

Browsers do not allow geolocation access over HTTP

HTTPS is required for secure browser APIs

Trade-off:
Slightly more setup effort for full browser feature support and security.

###  EC2-Based Deployment

The application is deployed on AWS EC2.

Reasoning:

Simple and flexible deployment

Lower infrastructure cost

Suitable for early-stage development and iteration

Trade-off:
Less managed infrastructure compared to serverless solutions, but greater control and lower cost.

###  Learning-Oriented Technology Choices

React and parts of AWS were not my strongest areas initially.
They were chosen intentionally to align the project more closely with real-world production environments.

Trade-off:
Slower development at the beginning, but significant long-term skill growth and practical experience.

### Swagger for API Documentation

Swagger (OpenAPI) is integrated into the project to provide interactive and self-documented APIs.

Reasoning:

Makes API behavior clear and discoverable

Allows frontend and backend to align on API contracts quickly

Enables easy testing and validation of APIs through the UI

Reduces communication and onboarding cost

Trade-off:
Introducing Swagger adds additional configuration and maintenance overhead, but significantly improves API transparency, development efficiency, and long-term maintainability.


## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Node.js 18+**
- **PostgreSQL 12+** (or use Docker Compose)
- **Docker & Docker Compose** (optional, for database)

### 1. Clone the Repository

```bash
git clone https://github.com/Miyakee/geomeet.git
cd geomeet
```

### 2. Configure Environment Variables

#### Backend Configuration

Create or update `api/src/main/resources/application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geomeet_local
spring.datasource.username=postgres
spring.datasource.password=postgres
```

#### Frontend Configuration (Optional)

Create `ui/.env` for geocoding API keys (optional, will use free Nominatim if not provided):

```env
# Geocoding Service Configuration
VITE_GEOCODING_PROVIDER=auto
VITE_OPENCAGE_API_KEY=your-opencage-api-key  # Optional: Get at https://opencagedata.com/api
VITE_POSITIONSTACK_API_KEY=your-positionstack-api-key  # Optional: Get at https://positionstack.com/
```

**Note**: Geocoding API keys are optional. The app will use free Nominatim service if no keys are provided, but it has CORS limitations in production.

### 3. Start Database

Using Docker Compose (recommended):

```bash
docker-compose up -d
```
after container created:
````
url=jdbc:postgresql://localhost:5432/geomeet_local
username=postgres
password=postgres
````


### 4. Install Git Hooks

Husky will automatically install Git hooks when you install dependencies:

```bash
cd ui
npm install
```

Or manually install hooks:

```bash
cd ui
npm run prepare
```

### 5. Start Backend

```bash
cd api
./gradlew bootRun
```

Backend will start on `http://localhost:8080`

### 6. Start Frontend

In a new terminal:

```bash
cd ui
npm install
npm run dev
```

Frontend will start on `http://localhost:3000`

**Note**: The Vite dev server automatically proxies `/api/*` requests to `http://localhost:8080`, eliminating CORS issues during development.


### 7. Access the Application

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Health Check**: http://localhost:8080/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- 
## 👥 Default Users (can register new one )

When running in `local` profile, the following test users are automatically created:

| Username | Email | Password |
|---|-------|----------|
| admin | admin@geomeet.com | admin123 |
| testuser | test@geomeet.com | test123 |

Note : Also can register user in create account now button

## 🚢 Deployment

### Deploy env Overview
0. **Production env**: https://ttyuuuuuuuuuuuu.us.ci/login

1. **AWS Cloud**
    - Terraform-based infrastructure as code
    - EC2, RDS, ECR, VPC, and security groups
    - See [infrastructure/README.md](infrastructure/README.md) for details

2. **Docker Compose**
    - Full containerization with Nginx reverse proxy
    - Automatic HTTPS/SSL with Let's Encrypt
    - Easy to deploy on any server with Docker


### Production Deployment with Docker Compose

The project includes a complete Docker Compose setup for production deployment with HTTPS support.

#### 1. Build Docker Images (LOCAL)

```bash
# Build API image
aws sso login --profile your-aws-profile-name
cd infrastructure/scripts/deployment
sh ./build-and-push-to-ecr.sh
```

#### 2. Configure Environment Variables (EC2)
```bash
cd geomeet
vi .env
set -a
source .env
set +a
```
```env
# Database Configuration
DB_ENDPOINT=your-rds-endpoint.region.rds.amazonaws.com
DB_NAME=geomeet
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# Geocoding API Keys (Optional)
OPENCAGE_API_KEY=your-opencage-api-key
POSITIONSTACK_API_KEY=your-positionstack-api-key
```

#### 3. Initialize SSL Certificate (EC2)(just set up once)

```bash
cd geommet
chmod +x init-ssl.sh
cp nginx.conf nginx.conf.bak
cp nginx.conf.http-only nginx.conf
sh deploy-from-ecr.sh
./init-ssl.sh
cp nginx.conf nginx.conf.http-only
cp nginx.conf.bak nginx.conf 
sh deploy-from-ecr.sh
```

#### 4. Start Services

```bash
cd 
sh deploy-from-ecr.sh
```

#### 6. Verify Deployment

- **HTTPS**: https://ttyuuuuuuuuuuuu.us.ci

### AWS Cloud Deployment

For AWS deployment using Terraform (Infrastructure as Code):

**Prerequisites:**
- Terraform >= 1.5.0
- AWS CLI configured
- AWS account with appropriate permissions

**Quick Start:**
```bash
cd infrastructure/terraform
terraform init
terraform plan
terraform apply
```

**What gets deployed:**
- VPC with public/private subnets
- EC2 instance for application hosting
- RDS PostgreSQL database
- ECR repositories for Docker images
- Security groups and IAM roles
- Internet gateway and route tables

For detailed AWS deployment instructions, architecture diagrams, and troubleshooting, see [infrastructure/README.md](infrastructure/README.md).

## 💻 Improvement
1. audit log. (had add audit fields in table. but better to put audit log to trace data change in DB)
2. websocket -> websocket + sqs (decoupling, scalability, and reliability) depends on requirements. if no so much user. maybe do it later.mentm

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
#### 📡 API Documentation

For complete API documentation including all endpoints and request/response examples, see [api/README.md](api/README.md).

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

### Docker

```shell
### 1. Start Database

docker-compose up -d

### 2. View Logs

docker-compose logs -f postgres

### 3. Stop Database

docker-compose down

### 4. Stop and Remove Volumes (Clean Data)
docker-compose down -v


### Access Database Container

docker-compose exec postgres psql -U postgres -d geomeet_local

### Execute SQL Commands

docker-compose exec postgres psql -U postgres -d geomeet_local -c "SELECT * FROM users;"

### Backup Database

docker-compose exec postgres pg_dump -U postgres geomeet_local > backup.sql

### Restore Database

docker-compose exec -T postgres psql -U postgres -d geomeet_local < backup.sql

```

### Code Quality

The project includes automated code quality checks:

- **Backend**: Checkstyle, PMD, SpotBugs, Tests (90% coverage required)
- **Frontend**: ESLint, Tests

Pre-push hooks automatically run these checks before pushing code. The hooks are smart and only check directories that have changes:

- **Backend checks** (when `api/` files change):
    - Checkstyle (code style - main code only)
    - PMD (static analysis - main code only)
    - SpotBugs (bug detection - main code only)
    - All tests

- **Frontend checks** (when `ui/` files change):
    - ESLint (linting)
    - Unit tests

If no relevant changes are detected, checks are skipped. Hooks are managed by Husky and located in `.husky/` directory.


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
- **STOMP.js** - WebSocket client

### DevOps & Tools
- **Docker & Docker Compose** - Full application containerization
- **Nginx** - Reverse proxy and static file serving with HTTPS support
- **Certbot** - Automatic SSL certificate management (Let's Encrypt)
- **Terraform** - Infrastructure as Code (IaC) for AWS cloud deployment
- **AWS** - Cloud infrastructure (EC2, RDS, ECR, VPC, IAM)
- **Git Hooks** - Pre-push code quality checks
- **Checkstyle, PMD, SpotBugs** - Code quality tools
- **JaCoCo** - Code coverage
- **ESLint** - JavaScript linting
- **Vitest** - Fast unit testing framework for frontend

### Mapping & Geocoding
- **Leaflet** - Interactive map library
- **OpenCage Geocoding API** - Address to coordinates conversion (recommended)
- **PositionStack API** - Alternative geocoding service
- **Nominatim** - Free fallback geocoding service (OpenStreetMap)

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
│   │   │   ├── auth/       # Authentication components
│   │   │   └── session/    # Session-related components
│   │   ├── contexts/        # React contexts (Auth, etc.)
│   │   ├── hooks/           # Custom React hooks
│   │   ├── pages/           # Page components
│   │   ├── services/        # API services
│   │   ├── types/           # TypeScript type definitions
│   │   └── utils/           # Utility functions
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
├── infrastructure/          # Infrastructure and deployment
│   ├── config/              # Docker Compose and Nginx configs
│   │   ├── docker-compose.yml
│   │   ├── nginx.conf
│   │   └── init-ssl.sh      # SSL certificate initialization
│   ├── terraform/           # AWS infrastructure as code
│   └── docs/                # Infrastructure documentation
├── .husky/                  # Git hooks managed by Husky
├── docker-compose.yml        # Local PostgreSQL database setup
├── DOCKER.md                 # Docker documentation
└── README.md                 # This file
```

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
Domain (no dependencies)
  ↑
Application (depends on Domain)
  ↑
Adapter & Infrastructure (depend on Application & Domain)
```

**Detailed flow:**
- **Domain**: Pure business logic, no dependencies
- **Application**: Depends on Domain (uses entities, value objects, domain services)
- **Adapter**: Depends on Application (calls use cases) and Domain (uses domain types)
- **Infrastructure**: Depends on Application (implements repository interfaces) and Domain (implements domain services)

For detailed architecture documentation, see [api/ARCHITECTURE.md](api/ARCHITECTURE.md).

## 🔌 WebSocket

The application uses WebSocket with STOMP protocol for real-time session updates. This enables instant synchronization of session data, participant locations, and status changes across all connected clients.

### Connection

**WebSocket Endpoint**: `/ws`

The endpoint supports SockJS fallback for better browser compatibility:

```javascript
// JavaScript example using SockJS and STOMP.js
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    // Subscribe to topics...
});
```

**Note**: WebSocket connections are currently allowed without authentication (`/ws/**` is permitted in security config). In production, consider implementing WebSocket authentication.

### Message Broker Configuration

- **Server-to-Client Prefix**: `/topic` - Messages sent from server to clients
- **Client-to-Server Prefix**: `/app` - Messages sent from clients to server (currently not used)

### Subscription Topics

Clients can subscribe to the following topics to receive real-time updates:

#### 1. Session Updates
**Topic**: `/topic/session/{sessionId}`

Broadcasts when session details change (participants join/leave, status changes, etc.).

**Message Format**:
```json
{
  "id": 1,
  "sessionId": "abc123",
  "initiatorId": 1,
  "initiatorUsername": "admin",
  "status": "Active",
  "createdAt": "2024-01-01T12:00:00",
  "participants": [
    {
      "participantId": 1,
      "userId": 1,
      "username": "admin",
      "email": "admin@geomeet.com",
      "joinedAt": "2024-01-01T12:00:00"
    }
  ],
  "participantCount": 1,
  "meetingLocationLatitude": null,
  "meetingLocationLongitude": null,
  "participantLocations": [...]
}
```

**Triggered by**:
- Participant joins session
- Participant leaves session
- Session status changes

#### 2. Location Updates
**Topic**: `/topic/session/{sessionId}/locations`

Broadcasts when a participant updates their location.

**Message Format**:
```json
{
  "sessionId": 1,
  "sessionIdString": "abc123",
  "userId": 1,
  "latitude": 1.3521,
  "longitude": 103.8198,
  "accuracy": 10.5,
  "updatedAt": "2024-01-01T12:00:00"
}
```

**Triggered by**:
- Participant updates location via `POST /api/sessions/{sessionId}/location`

#### 3. Optimal Location Updates
**Topic**: `/topic/session/{sessionId}/optimal-location`

Broadcasts when the optimal meeting location is recalculated.

**Message Format**:
```json
{
  "sessionId": 1,
  "sessionIdString": "abc123",
  "latitude": 1.3212,
  "longitude": 103.8359
}
```

**Triggered by**:
- Optimal location calculation via `POST /api/sessions/{sessionId}/optimal-location`
- Automatically recalculated when participant locations change

#### 4. Meeting Location Updates
**Topic**: `/topic/session/{sessionId}/meeting-location`

Broadcasts when the initiator sets or updates the meeting location.

**Message Format**:
```json
{
  "sessionId": 1,
  "sessionIdString": "abc123",
  "latitude": 1.3521,
  "longitude": 103.8198,
  "message": "Meeting location updated successfully"
}
```

**Triggered by**:
- Initiator updates meeting location via `PUT /api/sessions/{sessionId}/meeting-location`

#### 5. Session End Notification
**Topic**: `/topic/session/{sessionId}/end`

Broadcasts when the session is ended by the initiator.

**Message Format**:
```json
{
  "sessionId": 1,
  "sessionIdString": "abc123",
  "status": "Ended",
  "endedAt": "2024-01-01T13:00:00",
  "message": "Session ended successfully",
  "hasMeetingLocation": true,
  "meetingLocationLatitude": 1.3521,
  "meetingLocationLongitude": 103.8198
}
```

**Triggered by**:
- Initiator ends session via `POST /api/sessions/{sessionId}/end`

### Architecture Notes

- **In-Memory Broker**: Uses Spring's simple in-memory message broker. For production with multiple server instances, consider using an external message broker (RabbitMQ, ActiveMQ, etc.).
- **No Client-to-Server Messages**: Currently, clients only receive messages from the server. No client-to-server WebSocket messaging is implemented.
- **Security**: WebSocket connections are currently permitted without authentication. For production, implement WebSocket authentication using JWT tokens or session-based authentication.

## 🚀 Features

### Core Features
- **User Authentication**: JWT-based authentication with secure password hashing
- **Session Management**: Create and join collaborative sessions with real-time participant tracking
- **Real-time Updates**: WebSocket-based real-time synchronization for session participants, locations, and status
- **Invite System**: Generate invite links and codes for session sharing with automatic session join on registration

### Location Features
- **GPS Location Tracking**: Real-time GPS location tracking with automatic updates (10-meter threshold to reduce server load)
- **Manual Location Input**: Search and set location manually using geocoding services
- **Optimal Location Calculation**: Automatically calculate the optimal meeting point based on all participants' locations using geographic center
- **Meeting Location Setting**: Set and update meeting location (initiator only)
- **Interactive Map**: Real-time map visualization showing all participants, optimal location, and meeting location
- **Distance Calculation**: Display distance from user's location to meeting location
- **Location Persistence**: Location data persists across page refreshes

### Technical Features
- **Clean Architecture**: DDD + Clean Architecture for maintainable and testable code
- **Code Quality**: Automated code quality checks with pre-push hooks (Checkstyle, PMD, SpotBugs)
- **High Test Coverage**: 90%+ code coverage requirement with comprehensive unit and integration tests
- **HTTPS/SSL Support**: Automatic SSL certificate management with Let's Encrypt and Nginx
- **Docker Deployment**: Full containerization with Docker Compose for easy deployment

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

## 🔒 Security

- **Password Hashing**: BCrypt with 12 rounds
- **JWT Authentication**: Token-based stateless authentication
- **Token Expiration**: 24 hours (configurable)
- **HTTPS/SSL**: Automatic SSL certificate management with Let's Encrypt
- **CORS**: Configured for development and production
- **Input Validation**: Jakarta Validation on all endpoints
- **Location Privacy**: Location data is only shared within active sessions
- **Session Authorization**: Only session initiator can end session or update meeting location

## 📚 Additional Documentation

- [Backend API README](api/README.md) - Detailed backend documentation
- [Frontend UI README](ui/README.md) - Frontend documentation
- [Infrastructure Guide](infrastructure/README.md) - AWS deployment and infrastructure setup

### Code Style

- **Backend**: Follow Java conventions, Checkstyle rules enforced
- **Frontend**: ESLint rules enforced, TypeScript strict mode
- **Architecture**: Follow DDD + Clean Architecture principles


---

**Note**: This is a demonstration project showcasing DDD + Clean Architecture principles with real-time collaboration and location-based features.


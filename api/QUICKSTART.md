# Quick Start Guide - User Login API

## Quick Start

### 1. Setup Database

Start the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d
```

Or if you prefer to use a local PostgreSQL installation:

```bash
psql -U postgres
CREATE DATABASE geomeet_local;
\q
```

### 2. Configure Database Connection

Edit `src/main/resources/application-local.properties` and update the database connection information:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geomeet_local
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Run Application

```bash
cd api
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

### 4. Test Login

Test the login endpoint using curl or Postman:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "admin123"
  }'
```

**Success Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@geomeet.com",
  "message": "Login successful"
}
```

**Error Response**:
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 401,
  "error": "Authentication Failed",
  "message": "Invalid credentials",
  "path": "/api/auth/login"
}
```

## Default Test Users

The following test users are automatically created when running in `local` profile:

| Username | Email | Password |
|----------|-------|----------|
| admin | admin@geomeet.com | admin123 |
| testuser | test@geomeet.com | test123 |

## API Endpoints

### POST /api/auth/login

User login endpoint.

**Request Body**:
```json
{
  "usernameOrEmail": "admin",
  "password": "admin123"
}
```

**Response**:
- 200 OK: Login successful, returns JWT token
- 401 Unauthorized: Invalid credentials

## Architecture

This project uses Clean Architecture:

- **Domain Layer**: Business entities and repository interfaces
- **Application Layer**: Use case implementations
- **Infrastructure Layer**: Data persistence, security services
- **Adapter Layer**: REST controllers and DTOs

## Security Features

- ✅ Passwords are hashed using BCrypt (12 rounds)
- ✅ JWT token authentication
- ✅ Token expires after 24 hours (configurable)
- ✅ Passwords are never transmitted or stored in plain text

## Next Steps

- [ ] Implement JWT token validation filter
- [ ] Add user registration functionality
- [ ] Implement refresh token mechanism
- [ ] Add password reset functionality


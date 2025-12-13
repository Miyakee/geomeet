# Architecture Documentation

This project follows **DDD (Domain-Driven Design) + Clean Architecture** principles.

## Architecture Layers

### 1. Domain Layer (最内层，最纯净)

**Location**: `domain/`

**Purpose**: Contains business logic and domain rules. No framework dependencies.

**Components**:
- **Entities** (`entity/`): Aggregate roots (e.g., `User`)
- **Value Objects** (`valueobject/`): Immutable objects with validation (e.g., `Email`, `Username`, `PasswordHash`)
- **Domain Services** (`service/`): Port interfaces for domain needs (e.g., `PasswordEncoder`)
- **Domain Exceptions** (`exception/`): Business exceptions (e.g., `InvalidCredentialsException`)

**Key Principles**:
- ✅ No framework dependencies
- ✅ Pure Java classes
- ✅ Business logic encapsulated in entities and services
- ✅ Value Objects ensure data integrity

### 2. Application Layer

**Location**: `application/`

**Purpose**: Orchestrates domain services to fulfill use cases.

**Components**:
- **Use Cases** (`usecase/`): Application services that orchestrate domain logic
- **Repository Interfaces** (`usecase/`): Ports defining what use cases need (e.g., `UserRepository`)
- **Commands** (`command/`): Input DTOs for use cases (e.g., `LoginCommand`)
- **Results** (`result/`): Output DTOs from use cases (e.g., `LoginResult`)

**Key Principles**:
- ✅ Depends on Domain layer
- ✅ Contains use case orchestration logic
- ✅ Uses Domain entities and exceptions
- ✅ Defines Repository interfaces (ports) for infrastructure

### 3. Infrastructure Layer

**Location**: `infrastructure/`

**Purpose**: Implements technical concerns and adapts external systems.

**Components**:
- **Persistence** (`persistence/`):
  - JPA Entities (`entity/UserEntity.java`)
  - Repository Implementations (`UserRepositoryImpl.java`)
  - Mappers (`mapper/UserMapper.java`)
- **Security** (`security/`): Password encoding, JWT tokens
- **Configuration** (`config/`): Spring configuration

**Key Principles**:
- ✅ Implements domain interfaces (ports)
- ✅ Handles all technical details
- ✅ Can be swapped without affecting domain

### 4. Adapter Layer (Presentation)

**Location**: `adapter/`

**Purpose**: Entry points from outside world (HTTP, messaging, etc.).

**Components**:
- **Controllers** (`web/auth/AuthController.java`): REST endpoints
- **DTOs** (`dto/`): Data transfer objects for API

**Key Principles**:
- ✅ Translates between external format and application commands/results
- ✅ Handles HTTP concerns
- ✅ Validates input

## DDD Concepts Used

### Aggregate Root
- `User` is the aggregate root for the User aggregate
- All access to User data goes through the aggregate root

### Value Objects
- `Email`: Encapsulates email validation
- `Username`: Encapsulates username validation
- `PasswordHash`: Represents encrypted password

### Domain Services (Ports)
- `PasswordEncoder`: Port interface for password encoding (implemented in infrastructure)

### Repository Pattern
- `UserRepository`: Port defined in application layer (use case layer)
- `UserRepositoryImpl`: Adapter implementation in infrastructure

### Factory Methods
- `User.create()`: Factory method for creating new users
- `User.reconstruct()`: Factory method for reconstructing from persistence

## Dependency Flow

```
Adapter → Application → Domain ← Infrastructure
```

- **Adapter** depends on Application
- **Application** depends on Domain
- **Infrastructure** implements Domain interfaces (depends on Domain)
- **Domain** has no dependencies (pure)

## Example Flow: User Login

1. **Adapter** (`AuthController`):
   - Receives HTTP request
   - Creates `LoginCommand` from DTO
   - Calls `LoginUseCase.execute()`

2. **Application** (`LoginUseCase`):
   - Receives `LoginCommand`
   - Uses `UserRepository` to find user
   - Uses `PasswordEncoder` to verify password
   - Checks user state using domain methods
   - Throws domain exceptions if invalid
   - Returns `LoginResult`

3. **Infrastructure**:
   - `UserRepositoryImpl` converts between Domain `User` and JPA `UserEntity`
   - `BcryptPasswordEncoder` implements `PasswordEncoder` port

## Benefits

1. **Testability**: Domain layer can be tested without any framework
2. **Maintainability**: Clear separation of concerns
3. **Flexibility**: Easy to swap implementations (e.g., change database)
4. **Business Focus**: Domain layer focuses purely on business logic
5. **Type Safety**: Value Objects ensure data integrity at compile time

## Project Structure

```
api/src/main/java/com/geomeet/api/
├── domain/                    # Domain Layer (DDD)
│   ├── entity/               # Aggregate Roots
│   ├── valueobject/          # Value Objects
│   ├── service/               # Domain Service Ports
│   └── exception/             # Domain Exceptions
├── application/               # Application Layer
│   ├── usecase/               # Use Cases & Repository Ports
│   ├── command/               # Input Commands
│   └── result/                # Output Results
├── infrastructure/            # Infrastructure Layer
│   ├── persistence/           # Data Access
│   ├── security/              # Security Services
│   ├── service/               # Service Adapters
│   └── config/                # Configuration
└── adapter/                   # Adapter Layer
    └── web/                   # Web Adapters
        └── auth/              # Auth Controllers & DTOs
```


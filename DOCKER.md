# Docker Compose Guide

This project uses Docker Compose to manage the PostgreSQL database.

## Prerequisites

- Docker Desktop installed and running
- Docker Compose installed (usually included with Docker Desktop)

## Quick Start

### 1. Start Database

```bash
docker-compose up -d
```

This will start the PostgreSQL database container.

### 2. View Logs

```bash
docker-compose logs -f postgres
```

### 3. Stop Database

```bash
docker-compose down
```

### 4. Stop and Remove Volumes (Clean Data)

```bash
docker-compose down -v
```

## Common Commands

### Check Status

```bash
docker-compose ps
```

### Access Database Container

```bash
docker-compose exec postgres psql -U postgres -d geomeet_local
```

### Execute SQL Commands

```bash
docker-compose exec postgres psql -U postgres -d geomeet_local -c "SELECT * FROM users;"
```

### Backup Database

```bash
docker-compose exec postgres pg_dump -U postgres geomeet_local > backup.sql
```

### Restore Database

```bash
docker-compose exec -T postgres psql -U postgres -d geomeet_local < backup.sql
```

## Environment Variables

You can customize the configuration via `.env` file (optional):

```bash
cp .env.example .env
```

Then edit the `.env` file:

```env
POSTGRES_DB=geomeet_local
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432
```

## Database Connection Information

Default configuration (matches `application-local.properties`):

- **Host**: localhost
- **Port**: 5432
- **Database**: geomeet_local
- **Username**: postgres
- **Password**: postgres

## Data Persistence

Database data is stored in Docker volume `postgres_data`, so data persists even when the container is stopped.

To completely clean the data:

```bash
docker-compose down -v
```

## Health Check

Docker Compose is configured with health checks to ensure the database is fully started before marking it as healthy.

Check health status:

```bash
docker-compose ps
```

## Troubleshooting

### Port Already in Use

If port 5432 is already in use, you can modify `POSTGRES_PORT` in the `.env` file or directly edit `docker-compose.yml`.

### Container Won't Start

1. View logs: `docker-compose logs postgres`
2. Check if port is in use: `lsof -i :5432`
3. Check if Docker is running: `docker ps`

### Connection Refused

Make sure the container is running:

```bash
docker-compose ps
```

If the container is not running, start it:

```bash
docker-compose up -d
```

## Integration with Spring Boot Application

Ensure the database configuration in `application-local.properties` matches the Docker Compose configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/geomeet_local
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Then start the Spring Boot application:

```bash
cd api
./gradlew bootRun
```


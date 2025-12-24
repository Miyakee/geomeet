# Swagger API Documentation

This project uses Swagger (OpenAPI 3) to provide interactive API documentation.

## Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml

## Authentication

Most endpoints require JWT authentication. To use authenticated endpoints in Swagger UI:

1. First, use the `/api/auth/login` or `/api/auth/register` endpoint to get a JWT token
2. Click the "Authorize" button at the top of the Swagger UI
3. Enter your JWT token in the format: `Bearer <your-token>`
4. Click "Authorize" and then "Close"
5. Now you can test authenticated endpoints

## API Groups

The APIs are organized into the following groups:

- **Authentication**: User login and registration
- **Sessions**: Session management (create, join, get details, end)
- **Locations**: Location tracking and optimal location calculation

## Example Usage

1. Start the application
2. Open http://localhost:8080/swagger-ui.html in your browser
3. Try the `/api/auth/login` endpoint with:
   ```json
   {
     "usernameOrEmail": "admin",
     "password": "admin123"
   }
   ```
4. Copy the `token` from the response
5. Click "Authorize" and enter: `Bearer <your-token>`
6. Now you can test other endpoints like creating a session


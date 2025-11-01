# Docker Setup Instructions

This document provides instructions for setting up and running the Autocare360 backend application using Docker Compose.

## Prerequisites

- Docker installed on your system
- Docker Compose installed on your system

## Setup

1. **Clone the repository** (if not already done):
   ```bash
   git clone <repository-url>
   cd dev-autocare360-backend
   ```

2. **Create environment file**:
   Copy the example environment file and update the values as needed:
   ```bash
   cp .env.example .env
   ```
   Edit `.env` with your preferred configuration values.

3. **Build and run the application**:
   ```bash
   docker-compose --env-file .env up --build
   ```

   This command will:
   - Build the application image
   - Start the MySQL database
   - Start the Spring Boot application
   - Run Flyway database migrations automatically

4. **Access the application**:
   - API will be available at: `http://localhost:8080`
   - Database will be available at: `localhost:3306`

## Environment Variables

The following environment variables need to be configured in your `.env` file:

### Database Configuration
- `MYSQL_ROOT_PASSWORD`: Root password for MySQL
- `MYSQL_DATABASE`: Database name
- `MYSQL_USER`: Database user
- `MYSQL_PASSWORD`: Database user password

### Application Configuration
- `DB_URL`: JDBC URL for database connection
- `JWT_SECRET`: Secret key for JWT token generation

## Stopping the Application

To stop the running containers:
```bash
docker-compose down
```

To stop and remove volumes (this will delete database data):
```bash
docker-compose down -v
```

## Troubleshooting

- If the application fails to start, check the container logs:
  ```bash
  docker-compose logs app
  docker-compose logs mysql
  ```

- Ensure port 8080 and 3306 are not in use by other applications.

- For database connection issues, verify the `.env` file values match the Docker Compose configuration.
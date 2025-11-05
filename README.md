# AutoCare360 Backend

A comprehensive backend service for automotive care management, built with Spring Boot. This application provides RESTful APIs for user management, appointment scheduling, notifications, and real-time messaging.

## Features

- **User Management**: Authentication and authorization with JWT
- **Appointment Scheduling**: Manage service appointments and time logs
- **Notifications**: Real-time notifications system
- **Messaging**: WebSocket-based real-time messaging
- **Database Migrations**: Flyway-managed schema evolution
- **Containerized**: Docker and Kubernetes ready
- **CI/CD**: Automated pipelines with GitHub Actions

## Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: MySQL 8.0
- **Security**: Spring Security with JWT
- **Documentation**: OpenAPI/Swagger
- **Containerization**: Docker
- **Orchestration**: Kubernetes
- **CI/CD**: GitHub Actions

## Quick Start

### Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose
- Git

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/ColabDevelopers/dev-autocare360-backend.git
   cd dev-autocare360-backend
   ```

2. Set up environment variables:
   - Copy `.env.example` to `.env` and configure

3. Start the application:
   ```bash
   docker-compose --env-file .env up --build -d
   ```

4. Access the API at `http://localhost:8080`

## API Documentation

Visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## Project Structure

```
dev-autocare360-backend/
├── .github/                 # CI/CD workflows
├── deployment/              # Docker and Kubernetes configs
├── docs/                    # Documentation
├── scripts/                 # Build and deployment scripts
├── src/
│   ├── main/java/com/autocare360/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions
│   │   ├── repository/      # Data repositories
│   │   ├── security/        # Security configuration
│   │   ├── service/         # Business logic
│   │   └── util/            # Utility classes
│   └── main/resources/
│       ├── db/migration/    # Flyway migrations
│       └── application.properties
├── target/                  # Build output
├── docker-compose.yml       # Local development setup
├── Dockerfile               # Container build
├── pom.xml                  # Maven configuration
└── README.md
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
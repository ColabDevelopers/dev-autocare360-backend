# AutoCare360 Backend

## Overview

AutoCare360 is a backend service for an automotive care management system, built with Spring Boot. It provides APIs for user management, appointments, notifications, and messaging, using MySQL as the database and Flyway for migrations. The project follows DevOps best practices with Docker containerization, Kubernetes deployment, and CI/CD via GitHub Actions.

This setup is designed for a university-level software engineering project, emphasizing clean architecture, automation, and production readiness.

## Tech Stack

- **Backend**: Spring Boot 3.5.6 (Java 21)
- **Database**: MySQL 8.0
- **Migrations**: Flyway
- **Security**: Spring Security with JWT
- **Containerization**: Docker
- **Orchestration**: Kubernetes (k3s/minikube/microk8s)
- **CI/CD**: GitHub Actions
- **Other**: Maven for build management

## Repository Structure

```
dev-autocare360-backend/
├── .github/
│   └── workflows/          # CI/CD pipelines (GitHub Actions)
├── deployment/
│   ├── kubernetes/         # Kubernetes manifests (deployments, services, ingress, secrets)
│   └── docker/             # Dockerfiles
├── src/
│   └── main/
│       ├── java/           # Spring Boot application code
│       └── resources/
│           ├── db/migration/  # Flyway migration scripts
│           └── application.properties
├── scripts/                # Build and deployment scripts
├── docs/                   # Documentation and diagrams
├── .env.example            # Sample environment variables
├── docker-compose.yml      # Local development setup
├── Dockerfile              # Multi-stage build for the app
├── pom.xml                 # Maven configuration
├── Makefile                # Automation commands
├── README.md               # This file
└── LICENSE
```

## Setup Instructions

### Prerequisites

- Java 21
- Maven 3.9+
- Docker and Docker Compose
- Kubernetes cluster (e.g., minikube or k3s)
- Git

### Local Development

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/dev-autocare360-backend.git
   cd dev-autocare360-backend
   ```

2. Set up environment variables:
   - Copy `.env.example` to `.env` and fill in the values (e.g., DB credentials, JWT secret).

3. Run with Docker Compose for local testing:
   ```bash
   docker-compose up --build
   ```
   - This starts MySQL and the Spring Boot app.

4. Access the app at `http://localhost:8080`.

### Building and Running

- Build the JAR:
  ```bash
  mvn clean package
  ```

- Run locally:
  ```bash
  java -jar target/autocare360-0.0.1-SNAPSHOT.jar
  ```

## Deployment

### Docker

- Build the image:
  ```bash
  docker build -t autocare360-backend .
  ```

- Run the container:
  ```bash
  docker run -p 8080:8080 --env-file .env autocare360-backend
  ```

### Kubernetes

1. Apply manifests:
   ```bash
   kubectl apply -f deployment/kubernetes/
   ```

2. Ensure secrets and configmaps are set up for environment variables.

3. Access via ingress (configure DNS as needed).

### CI/CD

- GitHub Actions workflows in `.github/workflows/` handle:
  - Linting and testing on PRs.
  - Building and pushing Docker images to GHCR on merge to `main`.
  - Deploying to Kubernetes.

## Best Practices

| Category       | Best Practice                          | Example                     |
|----------------|----------------------------------------|-----------------------------|
| **Branching**  | GitHub Flow (`main`, `dev`, `feature/*`) | `feature/add-notifications` |
| **Commits**    | Conventional Commits                   | `feat: add JWT authentication` |
| **Security**   | Use `.env` and never commit secrets    | Add `.env` to `.gitignore`  |
| **Automation** | Use Makefile for tasks                 | `make test`, `make deploy`  |
| **Documentation** | Maintain clear docs and diagrams     | `docs/api-diagram.png`      |

## Example Commands (via Makefile)

| Task              | Command       | Description                      |
|-------------------|---------------|----------------------------------|
| Setup local dev   | `make setup`  | Build and start containers       |
| Deploy to cluster | `make deploy` | Apply Kubernetes manifests       |
| Run tests         | `make test`   | Execute unit tests               |
| Lint code         | `make lint`   | Lint Java code                   |
| Cleanup           | `make clean`  | Remove build artifacts           |

## API Endpoints

- **Authentication**: `/api/auth/login`, `/api/auth/register`
- **Users**: `/api/users`
- **Appointments**: `/api/appointments`
- **Notifications**: `/api/notifications`
- **Messages**: `/api/messages`

Refer to the source code in `src/main/java/` for detailed implementation.

## Contributing

1. Follow branching and commit conventions.
2. Run tests and linting before PRs.
3. Update documentation as needed.

## License

This project is licensed under the MIT License.
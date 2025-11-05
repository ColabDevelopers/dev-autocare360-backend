# Contributing to AutoCare360 Backend

Thank you for your interest in contributing to AutoCare360! This document provides guidelines and information for contributors.

## Code of Conduct

This project follows a code of conduct to ensure a welcoming environment for all contributors. By participating, you agree to:

- Be respectful and inclusive
- Focus on constructive feedback
- Accept responsibility for mistakes
- Show empathy towards other contributors
- Help create a positive community

## Getting Started

1. **Fork the repository** on GitHub
2. **Clone your fork** locally
3. **Set up development environment** (see [DEVELOPMENT.md](DEVELOPMENT.md))
4. **Create a feature branch** from `main`
5. **Make your changes**
6. **Test your changes**
7. **Submit a pull request**

## Development Workflow

### Branch Naming

Use descriptive branch names following this pattern:

```
feature/add-user-authentication
bugfix/fix-login-validation
hotfix/critical-security-patch
docs/update-api-documentation
refactor/cleanup-controllers
```

### Commits

Follow conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Testing
- `chore`: Maintenance

Examples:
```
feat(auth): add JWT token refresh endpoint
fix(api): resolve null pointer in user service
docs(readme): update installation instructions
```

### Pull Requests

1. **Create a PR** from your feature branch to `main`
2. **Provide a clear title** and description
3. **Reference related issues** using `#issue-number`
4. **Ensure CI passes** all checks
5. **Request review** from maintainers

PR Template:
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Documentation updated
- [ ] Tests pass
- [ ] No breaking changes
```

## Code Standards

### Java Code Style

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods small and focused
- Use dependency injection

### Code Quality

```bash
# Run code quality checks
mvn spotless:check
mvn spotless:apply  # Auto-fix formatting

# Run tests
mvn test

# Check test coverage
mvn jacoco:report
```

### Security

- Never commit secrets or credentials
- Use environment variables for configuration
- Validate all inputs
- Implement proper authentication/authorization
- Follow OWASP guidelines

## Testing

### Unit Tests

```java
@SpringBootTest
class UserServiceTest {

    @Test
    void shouldCreateUserSuccessfully() {
        // Given
        UserDTO userDTO = createValidUserDTO();

        // When
        User user = userService.createUser(userDTO);

        // Then
        assertThat(user.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(user.getId()).isNotNull();
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnUserWhenValidId() {
        // Test implementation
    }
}
```

### Test Coverage

Maintain minimum 80% code coverage for:
- Unit tests
- Integration tests
- API endpoints

## API Design

### RESTful Principles

- Use appropriate HTTP methods (GET, POST, PUT, DELETE)
- Use plural nouns for resource names
- Use HTTP status codes correctly
- Implement proper error handling

### API Documentation

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    @PostMapping
    @Operation(summary = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        // Implementation
    }
}
```

## Database Changes

### Migrations

- Use Flyway for schema changes
- Follow naming convention: `V{version}__{description}.sql`
- Make migrations backward compatible when possible
- Test migrations on clean database

### Entity Design

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Getters and setters
}
```

## Documentation

### Code Documentation

- Add JavaDoc for classes and public methods
- Document complex business logic
- Explain design decisions

### API Documentation

- Use OpenAPI/Swagger annotations
- Provide examples for request/response
- Document error responses

## Review Process

### Code Review Checklist

- [ ] Code follows project conventions
- [ ] Tests are included and passing
- [ ] Documentation is updated
- [ ] Security considerations addressed
- [ ] Performance impact assessed
- [ ] No hardcoded values
- [ ] Proper error handling

### Review Comments

Be constructive and specific:

```markdown
❌ Bad: "This looks wrong"
✅ Good: "Consider using Optional here to avoid null checks"
```

## Release Process

### Versioning

Follow [Semantic Versioning](https://semver.org/):

- `MAJOR.MINOR.PATCH`
- Breaking changes increment MAJOR
- New features increment MINOR
- Bug fixes increment PATCH

### Release Checklist

- [ ] All tests pass
- [ ] Code reviewed and approved
- [ ] Documentation updated
- [ ] Migration scripts tested
- [ ] Performance benchmarks run
- [ ] Security scan completed
- [ ] Tag created with version
- [ ] Release notes written

## Getting Help

### Communication Channels

- **Issues**: Bug reports and feature requests
- **Discussions**: General questions and ideas
- **Pull Request comments**: Code review discussions

### Support

For questions:
1. Check existing documentation
2. Search existing issues
3. Create a new issue with detailed information
4. Join community discussions

## Recognition

Contributors are recognized through:
- GitHub contributor statistics
- Mention in release notes
- Attribution in documentation

Thank you for contributing to AutoCare360! 🚗✨
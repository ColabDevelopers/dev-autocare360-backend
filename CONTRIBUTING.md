# Contributing to AutoCare360 Backend

Thank you for your interest in contributing to AutoCare360 Backend! This document provides guidelines and instructions for contributing.

## 🤝 Code of Conduct

We are committed to providing a welcoming and inspiring community for all. Please read and follow our Code of Conduct.

## 🚀 Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- Git
- Your favorite IDE (IntelliJ IDEA, Eclipse, VS Code)

### Setting Up Development Environment

1. **Fork the repository**
   ```bash
   # Click 'Fork' on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/dev-autocare360-backend.git
   cd dev-autocare360-backend
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/ColabDevelopers/dev-autocare360-backend.git
   ```

4. **Set up environment**
   ```bash
   copy .env.example .env
   # Configure your local environment variables
   ```

5. **Start development environment**
   ```bash
   docker-compose up -d
   ```

## 🔄 Development Workflow

### 1. Create a Feature Branch

```bash
git checkout DevOps
git pull upstream DevOps
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/` - New features
- `bugfix/` - Bug fixes
- `hotfix/` - Critical fixes
- `refactor/` - Code refactoring
- `docs/` - Documentation updates

### 2. Make Your Changes

- Write clean, maintainable code
- Follow Java coding conventions
- Add/update tests for your changes
- Update documentation as needed

### 3. Test Your Changes

```bash
# Run all tests
mvn test

# Run specific tests
mvn test -Dtest=YourTestClass

# Check code style
mvn checkstyle:check

# Build the project
mvn clean package
```

### 4. Commit Your Changes

Follow conventional commit messages:

```bash
git add .
git commit -m "type(scope): description"
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes
- `perf`: Performance improvements

Examples:
```bash
git commit -m "feat(auth): add JWT refresh token endpoint"
git commit -m "fix(appointment): resolve date validation bug"
git commit -m "docs(readme): update installation instructions"
```

### 5. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 6. Create a Pull Request

1. Go to your fork on GitHub
2. Click "Pull Request"
3. Select base: `DevOps` and compare: `feature/your-feature-name`
4. Fill in the PR template
5. Submit the pull request

## 📝 Pull Request Guidelines

### PR Title

Use conventional commit format:
```
type(scope): Brief description
```

### PR Description

Include:
- **What**: What changes were made?
- **Why**: Why were these changes necessary?
- **How**: How were they implemented?
- **Testing**: How were the changes tested?
- **Screenshots**: If applicable

### PR Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Changes Made
- List of changes

## Testing Done
- Describe testing performed

## Screenshots (if applicable)

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests passing
- [ ] No new warnings
```

## 🎨 Code Style Guidelines

### Java Code Style

1. **Naming Conventions**
   - Classes: `PascalCase`
   - Methods/Variables: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
   - Packages: `lowercase`

2. **Formatting**
   - Indentation: 4 spaces (no tabs)
   - Line length: 120 characters max
   - Use meaningful variable names

3. **Best Practices**
   - Use `@Slf4j` for logging
   - Use `@Valid` for validation
   - Handle exceptions appropriately
   - Write javadoc for public methods
   - Keep methods small and focused

### Example

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    
    /**
     * Creates a new appointment.
     *
     * @param request the appointment creation request
     * @return the created appointment
     * @throws AppointmentException if validation fails
     */
    @Transactional
    public AppointmentDTO createAppointment(@Valid CreateAppointmentRequest request) {
        log.info("Creating appointment for user: {}", request.getUserId());
        
        // Implementation
        
        return appointmentDTO;
    }
}
```

## 🧪 Testing Guidelines

### Unit Tests

- Use JUnit 5
- Use Mockito for mocking
- Test edge cases
- Aim for >80% code coverage

```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @InjectMocks
    private AppointmentService appointmentService;
    
    @Test
    @DisplayName("Should create appointment successfully")
    void shouldCreateAppointmentSuccessfully() {
        // Given
        CreateAppointmentRequest request = new CreateAppointmentRequest();
        
        // When
        AppointmentDTO result = appointmentService.createAppointment(request);
        
        // Then
        assertNotNull(result);
        verify(appointmentRepository).save(any(Appointment.class));
    }
}
```

### Integration Tests

- Test complete flows
- Use `@SpringBootTest`
- Use test database

## 📚 Documentation Guidelines

### Code Documentation

- Add Javadoc for public classes and methods
- Explain complex logic with inline comments
- Keep comments up-to-date

### API Documentation

- Update OpenAPI/Swagger annotations
- Document request/response examples
- Include error responses

### README Updates

- Update README.md for new features
- Add usage examples
- Update configuration instructions

## 🔍 Review Process

### What We Look For

1. **Functionality**: Does it work as intended?
2. **Code Quality**: Is it clean and maintainable?
3. **Tests**: Are there adequate tests?
4. **Documentation**: Is it well documented?
5. **Performance**: Are there performance concerns?
6. **Security**: Are there security issues?

### Response Time

- Initial review: Within 2-3 business days
- Follow-up reviews: Within 1-2 business days

## 🐛 Reporting Bugs

### Before Reporting

1. Check existing issues
2. Try to reproduce the bug
3. Gather relevant information

### Bug Report Template

```markdown
**Description**
Clear description of the bug

**Steps to Reproduce**
1. Step one
2. Step two
3. ...

**Expected Behavior**
What should happen

**Actual Behavior**
What actually happens

**Environment**
- OS: [e.g., Windows 11]
- Java Version: [e.g., 21]
- Spring Boot Version: [e.g., 3.5.6]

**Screenshots**
If applicable

**Additional Context**
Any other relevant information
```

## 💡 Suggesting Features

### Feature Request Template

```markdown
**Feature Description**
Clear description of the feature

**Problem It Solves**
What problem does this solve?

**Proposed Solution**
How should it work?

**Alternatives Considered**
Other solutions you've thought about

**Additional Context**
Any other relevant information
```

## 🆘 Getting Help

- 💬 Ask questions in GitHub Discussions
- 📧 Email: dev-team@autocare360.com
- 📖 Check existing documentation
- 🐛 Search existing issues

## 📜 License

By contributing, you agree that your contributions will be licensed under the same license as the project.

## 🙏 Thank You

Thank you for contributing to AutoCare360 Backend! Your contributions help make this project better for everyone.

---

Happy Coding! 🚀

# AutoCare360 Backend - Test Suite

## Overview

This directory contains comprehensive JUnit 5 test cases for the AutoCare360 backend application. The tests cover all layers of the application including services, controllers, repositories, security components, and utilities.

## Test Statistics

- **Total Test Classes**: 17
- **Total Test Cases**: 231+
- **Coverage**: Services, Controllers, Repositories, Security, Utilities

## Project Structure

```
src/test/java/com/autocare360/
├── controller/               # Controller layer tests (MockMvc)
│   ├── AuthControllerTest.java
│   ├── AppointmentControllerTest.java
│   └── VehicleControllerTest.java
├── service/                  # Service layer tests (Mockito)
│   ├── UserServiceTest.java
│   ├── AuthServiceTest.java
│   ├── AppointmentServiceTest.java
│   ├── VehicleServiceTest.java
│   ├── EmployeeServiceTest.java
│   ├── MessageServiceTest.java
│   ├── NotificationServiceTest.java
│   ├── ServiceRecordServiceTest.java
│   └── CustomerServiceTest.java
├── repo/                     # Repository layer tests (@DataJpaTest)
│   ├── UserRepositoryTest.java
│   ├── AppointmentRepositoryTest.java
│   └── VehicleRepositoryTest.java
├── security/                 # Security component tests
│   └── JwtServiceTest.java
├── util/                     # Utility class tests
│   └── AuthUtilTest.java
└── TEST_COVERAGE_REPORT.md   # Detailed coverage report

```

## Running Tests

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=UserServiceTest
```

### Run tests in a specific package
```bash
mvn test -Dtest="com.autocare360.service.*Test"
```

### Run tests with coverage report
```bash
mvn test jacoco:report
```
The coverage report will be generated in `target/site/jacoco/index.html`

### Run only unit tests (service layer)
```bash
mvn test -Dtest="*ServiceTest"
```

### Run only integration tests (repository layer)
```bash
mvn test -Dtest="*RepositoryTest"
```

### Run only controller tests
```bash
mvn test -Dtest="*ControllerTest"
```

### Skip tests during build
```bash
mvn clean install -DskipTests
```

## Test Configuration

### Test Properties
Location: `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
app.security.jwt.secret=test-jwt-secret-key-for-testing-purposes-only
```

### Key Dependencies
- **JUnit 5 (Jupiter)**: Testing framework
- **Mockito**: Mocking framework
- **Spring Boot Test**: Spring testing support
- **Spring Security Test**: Security testing support
- **H2 Database**: In-memory database for tests
- **AssertJ**: Fluent assertions (optional)

## Test Categories

### 1. Service Layer Tests
**Technology**: JUnit 5 + Mockito  
**Purpose**: Unit testing business logic in isolation  
**Example**: `UserServiceTest.java`

These tests mock all dependencies and focus on testing business logic.

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private UserService userService;
    // Test methods...
}
```

### 2. Controller Layer Tests
**Technology**: JUnit 5 + MockMvc  
**Purpose**: Integration testing of REST endpoints  
**Example**: `AuthControllerTest.java`

These tests use MockMvc to test HTTP endpoints with mocked services.

```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private AuthService authService;
    // Test methods...
}
```

### 3. Repository Layer Tests
**Technology**: JUnit 5 + @DataJpaTest  
**Purpose**: Integration testing of database operations  
**Example**: `UserRepositoryTest.java`

These tests use an in-memory H2 database.

```java
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryTest {
    @Autowired private TestEntityManager entityManager;
    @Autowired private UserRepository userRepository;
    // Test methods...
}
```

### 4. Security Tests
**Technology**: JUnit 5  
**Purpose**: Testing JWT token generation and validation  
**Example**: `JwtServiceTest.java`

### 5. Utility Tests
**Technology**: JUnit 5 + Mockito  
**Purpose**: Testing utility classes  
**Example**: `AuthUtilTest.java`

## Test Patterns

### AAA Pattern
All tests follow the Arrange-Act-Assert pattern:

```java
@Test
void testExample() {
    // Arrange - Set up test data
    User user = new User();
    when(repository.findById(1L)).thenReturn(Optional.of(user));
    
    // Act - Execute the method under test
    UserResponse result = service.getUser(1L);
    
    // Assert - Verify the results
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(repository, times(1)).findById(1L);
}
```

### DisplayName Annotations
All tests have descriptive names using `@DisplayName`:

```java
@Test
@DisplayName("Should register new user successfully")
void testRegister_Success() {
    // Test implementation
}
```

## Common Test Scenarios

### 1. Happy Path Tests
- Successful operations
- Valid data scenarios
- Expected workflows

### 2. Error Handling Tests
- Invalid input validation
- Not found scenarios
- Permission denied cases
- Constraint violations

### 3. Edge Cases
- Null handling
- Empty collections
- Boundary values
- Race conditions

## Mocking Strategy

### Service Tests
- Mock all repositories and external dependencies
- Focus on business logic validation

### Controller Tests
- Mock service layer
- Test HTTP request/response handling
- Validate status codes and JSON responses

### Repository Tests
- Use real database (H2)
- Test custom queries
- Validate constraints and relationships

## Code Coverage Goals

- **Service Layer**: 90%+ coverage
- **Controller Layer**: 85%+ coverage
- **Repository Layer**: 80%+ coverage
- **Utility Classes**: 95%+ coverage

## Best Practices

1. **Test Independence**: Each test should be independent and not rely on other tests
2. **Clear Naming**: Use descriptive test method names
3. **Single Responsibility**: Each test should verify one specific behavior
4. **Fast Execution**: Keep tests fast by minimizing external dependencies
5. **Maintainability**: Keep tests simple and easy to understand
6. **Documentation**: Use @DisplayName for clear test descriptions

## Troubleshooting

### Tests fail with "Cannot find symbol" errors
```bash
mvn clean compile test-compile
```

### H2 database issues
Check that Flyway is disabled in test properties:
```properties
spring.flyway.enabled=false
```

### Mockito errors
Ensure you're using `@ExtendWith(MockitoExtension.class)` instead of deprecated runners.

### WebMvc tests fail
Verify you're using `@WebMvcTest` with the correct controller class and have `@MockBean` for dependencies.

## Continuous Integration

These tests are designed to run in CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Tests
  run: mvn test
  
- name: Generate Coverage Report
  run: mvn jacoco:report
```

## Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)

## Contact

For questions or issues with tests, please contact the development team.

---

**Last Updated**: 2025-01-18  
**Test Framework**: JUnit 5 (Jupiter)  
**Build Tool**: Maven 3.x  
**Java Version**: 21


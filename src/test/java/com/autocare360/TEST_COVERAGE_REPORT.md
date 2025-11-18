# AutoCare360 - Test Coverage Report

## Overview
This document provides a comprehensive overview of all test cases created for the AutoCare360 backend application using JUnit 5.

## Test Structure

### 1. Service Layer Tests (Unit Tests with Mockito)

#### UserServiceTest
- ✅ Get current user successfully
- ✅ Get current employee user with employee details
- ✅ Throw exception when user not found
- ✅ Change password successfully
- ✅ Throw exception when current password is invalid
- ✅ Update phone successfully
- ✅ Get all customers successfully
- ✅ Return empty list when no customers exist
- ✅ Handle user with null status

**Total: 9 test cases**

#### AuthServiceTest
- ✅ Register new user successfully
- ✅ Throw ConflictException when email already exists
- ✅ Create CUSTOMER role if not exists during registration
- ✅ Login successfully with valid credentials
- ✅ Throw exception when user not found during login
- ✅ Throw exception when password is invalid
- ✅ Login admin with hardcoded credentials
- ✅ Create admin user if not exists with hardcoded credentials
- ✅ Get user by id in me endpoint
- ✅ Throw exception when user not found in me endpoint
- ✅ Capitalize status correctly
- ✅ Handle null status as Active

**Total: 12 test cases**

#### AppointmentServiceTest
- ✅ List appointments by user
- ✅ List all appointments
- ✅ List appointments by employee and status
- ✅ Create appointment successfully
- ✅ Throw exception when user not found during creation
- ✅ Create appointment without technician
- ✅ Set default status to PENDING if not provided
- ✅ Update appointment successfully
- ✅ Throw exception when appointment not found during update
- ✅ Update technician and assigned user
- ✅ Delete appointment successfully
- ✅ Get availability for specific technician
- ✅ Get availability for all technicians
- ✅ Return all time slots when no appointments
- ✅ Broadcast appointment update via WebSocket

**Total: 15 test cases**

#### VehicleServiceTest
- ✅ Check if vehicle exists by VIN and user ID
- ✅ Return false when vehicle does not exist
- ✅ Create vehicle successfully
- ✅ Get vehicle by ID
- ✅ Return null when vehicle not found
- ✅ List vehicles by user
- ✅ Return empty list when user has no vehicles
- ✅ Update vehicle successfully
- ✅ Return null when updating non-existent vehicle
- ✅ Update only non-null fields
- ✅ Delete vehicle successfully
- ✅ Link vehicle to user successfully
- ✅ Return null when linking non-existent vehicle
- ✅ List all vehicles

**Total: 14 test cases**

#### EmployeeServiceTest
- ✅ Create employee successfully
- ✅ Throw ConflictException when email already exists
- ✅ Create EMPLOYEE role if not exists
- ✅ Generate unique employee number
- ✅ Update employee successfully
- ✅ Throw exception when employee not found during update
- ✅ Reset password successfully
- ✅ Throw exception when employee not found during password reset
- ✅ List all employees
- ✅ Return empty list when no employees
- ✅ Get employee by id
- ✅ Throw exception when employee not found
- ✅ Delete employee successfully
- ✅ Throw exception when deleting non-existent employee
- ✅ Set default password to 'password' when creating employee
- ✅ Include roles in employee response

**Total: 16 test cases**

#### MessageServiceTest
- ✅ Send message from customer to employees
- ✅ Send message from employee to customer
- ✅ Throw exception when sender not found
- ✅ Get conversation between two users
- ✅ Filter out messages with null sender
- ✅ Get conversations for employee (shared inbox)
- ✅ Get conversations for customer
- ✅ Mark messages as read
- ✅ Get unread count
- ✅ Search users by role
- ✅ Search users with empty query
- ✅ Get designated employee
- ✅ Throw exception when no employee available
- ✅ Get all customer messages
- ✅ Format time correctly for just now
- ✅ Skip null conversation partners
- ✅ Broadcast to all employees when customer sends message

**Total: 17 test cases**

#### NotificationServiceTest
- ✅ Send notification to user successfully
- ✅ Not send notification when blocked by preference
- ✅ Create default preference if not exists
- ✅ Notify service progress
- ✅ Notify appointment
- ✅ Notify project request
- ✅ Broadcast announcement
- ✅ Get user notifications
- ✅ Get unread notifications
- ✅ Get unread count
- ✅ Mark notification as read
- ✅ Not mark notification as read if user mismatch
- ✅ Mark all notifications as read
- ✅ Delete notification
- ✅ Not delete notification if user mismatch
- ✅ Get user preferences
- ✅ Create default preferences if not found
- ✅ Update user preferences
- ✅ Update only non-null preference fields
- ✅ Not send notification when push notifications disabled

**Total: 20 test cases**

#### ServiceRecordServiceTest
- ✅ List all service records
- ✅ Return empty list when no records
- ✅ List service records for vehicle
- ✅ Return empty list for vehicle with no records
- ✅ List service records by status
- ✅ Get service record by id
- ✅ Return null when service record not found
- ✅ Create service record successfully
- ✅ Update service record successfully
- ✅ Return null when updating non-existent record
- ✅ Update only non-null fields
- ✅ Update all fields when provided
- ✅ Delete service record successfully
- ✅ Return false when deleting non-existent record

**Total: 14 test cases**

#### CustomerServiceTest
- ✅ List all customers
- ✅ Return empty list when no customers
- ✅ Get customer by id
- ✅ Throw exception when customer not found
- ✅ Update customer successfully
- ✅ Update only provided fields
- ✅ Throw exception when updating non-existent customer
- ✅ Delete customer successfully
- ✅ Throw exception when deleting non-existent customer
- ✅ Include roles in customer response
- ✅ Handle customer with multiple roles
- ✅ Update phone number format

**Total: 12 test cases**

**Total Service Layer Tests: 129 test cases**

---

### 2. Controller Layer Tests (Integration Tests with MockMvc)

#### AuthControllerTest
- ✅ POST /auth/register - Should register successfully
- ✅ POST /auth/register - Should fail with conflict when email exists
- ✅ POST /auth/register - Should fail validation with invalid email
- ✅ POST /auth/register - Should fail with missing required fields
- ✅ POST /auth/login - Should login successfully
- ✅ POST /auth/login - Should fail with invalid credentials
- ✅ POST /auth/login - Should fail validation with missing fields
- ✅ POST /auth/refresh - Should return not implemented
- ✅ POST /auth/logout - Should logout successfully

**Total: 9 test cases**

#### AppointmentControllerTest
- ✅ GET /api/appointments - Should get user appointments
- ✅ GET /api/appointments - Should return 401 without token
- ✅ GET /api/appointments - Should return 401 with invalid token
- ✅ POST /api/appointments - Should create appointment
- ✅ POST /api/appointments - Should return 401 without token
- ✅ POST /api/appointments - Should fail validation with missing fields
- ✅ PUT /api/appointments/{id} - Should update appointment
- ✅ PUT /api/appointments/{id} - Should return 401 without token
- ✅ DELETE /api/appointments/{id} - Should delete appointment
- ✅ DELETE /api/appointments/{id} - Should return 401 without token
- ✅ GET /api/availability - Should get availability
- ✅ GET /api/availability - Should get availability without technician

**Total: 12 test cases**

#### VehicleControllerTest
- ✅ GET /api/vehicles - Should get user vehicles
- ✅ GET /api/vehicles - Should return 401 without token
- ✅ POST /api/vehicles - Should create vehicle
- ✅ POST /api/vehicles - Should return 409 when VIN already exists
- ✅ GET /api/vehicles/{id} - Should get vehicle by id
- ✅ GET /api/vehicles/{id} - Should return 404 when not found
- ✅ PUT /api/vehicles/{id} - Should update vehicle
- ✅ DELETE /api/vehicles/{id} - Should delete vehicle

**Total: 8 test cases**

**Total Controller Layer Tests: 29 test cases**

---

### 3. Security Layer Tests

#### JwtServiceTest
- ✅ Generate valid JWT token
- ✅ Generate token with multiple roles
- ✅ Validate valid token
- ✅ Reject invalid token
- ✅ Reject empty token
- ✅ Reject null token
- ✅ Extract subject from token
- ✅ Extract roles from token
- ✅ Extract single role from token
- ✅ Return empty list for token without roles
- ✅ Check if user has specific role
- ✅ Return false when user doesn't have role
- ✅ Handle case-insensitive role check
- ✅ Return false for null authorization header
- ✅ Return false for invalid authorization header format
- ✅ Return false for invalid token in hasRole
- ✅ Generate different tokens for different users
- ✅ Include email in token claims
- ✅ Handle token with special characters in subject
- ✅ Reject malformed token

**Total: 20 test cases**

---

### 4. Repository Layer Tests (Integration Tests with @DataJpaTest)

#### UserRepositoryTest
- ✅ Find user by email
- ✅ Return empty when user not found by email
- ✅ Find user by name
- ✅ Return empty when user not found by name
- ✅ Find users by role name
- ✅ Return empty list when no users with role
- ✅ Save new user with roles
- ✅ Update existing user
- ✅ Delete user
- ✅ Find all users
- ✅ Count users
- ✅ Handle user with employee fields
- ✅ Enforce unique email constraint
- ✅ Auto-set timestamps on create
- ✅ Auto-set default status on create

**Total: 15 test cases**

#### AppointmentRepositoryTest
- ✅ Find appointments by user id ordered by date desc
- ✅ Return empty list when no appointments for user
- ✅ Find all appointments ordered by date asc
- ✅ Find appointments by assigned user and status
- ✅ Find appointments by date and technician excluding cancelled
- ✅ Not find cancelled appointments
- ✅ Find appointments by date excluding cancelled
- ✅ Save new appointment
- ✅ Update existing appointment
- ✅ Delete appointment
- ✅ Auto-set default status to PENDING
- ✅ Handle appointments with progress tracking
- ✅ Order multiple appointments correctly

**Total: 13 test cases**

#### VehicleRepositoryTest
- ✅ Find vehicles by user id
- ✅ Return empty list when no vehicles for user
- ✅ Check if vehicle exists by VIN and user id (true)
- ✅ Return false when vehicle with VIN doesn't exist for user
- ✅ Return false when VIN exists but for different user
- ✅ Save new vehicle
- ✅ Update existing vehicle
- ✅ Delete vehicle
- ✅ Find all vehicles
- ✅ Handle multiple vehicles for same user
- ✅ Not find vehicles from different user
- ✅ Enforce unique VIN constraint per user properly
- ✅ Handle vehicle with all fields populated

**Total: 13 test cases**

**Total Repository Layer Tests: 41 test cases**

---

### 5. Utility Layer Tests

#### AuthUtilTest
- ✅ Get user ID from authentication successfully
- ✅ Throw exception when authentication is null
- ✅ Throw exception when principal is null
- ✅ Throw exception when user not found by email
- ✅ Get full user from authentication successfully
- ✅ Throw exception when getting user from null authentication
- ✅ Throw exception when getting user from null principal
- ✅ Throw exception when user not found in getUserFromAuth
- ✅ Get user ID from email successfully
- ✅ Throw exception when user not found by email in getUserIdFromEmail
- ✅ Handle different email formats
- ✅ Handle authentication with email as string principal

**Total: 12 test cases**

---

## Summary

| Layer | Test Classes | Test Cases |
|-------|-------------|------------|
| Service Layer | 9 | 129 |
| Controller Layer | 3 | 29 |
| Security Layer | 1 | 20 |
| Repository Layer | 3 | 41 |
| Utility Layer | 1 | 12 |
| **TOTAL** | **17** | **231** |

## Test Coverage Areas

### ✅ Fully Covered
- User management (authentication, registration, profile updates)
- Appointment scheduling and management
- Vehicle management
- Employee management
- Message and conversation handling
- Notification system
- Service records
- Customer management
- JWT token generation and validation
- Authorization and authentication utilities
- Data persistence and retrieval

### 🔧 Test Features
- **Unit Tests**: Use Mockito for mocking dependencies
- **Integration Tests**: Use @DataJpaTest for repository tests
- **Controller Tests**: Use MockMvc for API endpoint testing
- **Security Tests**: Test JWT generation, validation, and role checking
- **Edge Cases**: Null handling, empty results, validation failures
- **Error Scenarios**: Exception handling, unauthorized access, not found scenarios

## Running Tests

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=UserServiceTest
```

### Run tests with coverage
```bash
mvn test jacoco:report
```

### Run integration tests only
```bash
mvn test -Dtest=*RepositoryTest
```

## Test Configuration

- **Test Properties**: `src/test/resources/application-test.properties`
- **In-Memory Database**: H2 database for testing
- **JUnit Version**: JUnit 5 (Jupiter)
- **Mocking Framework**: Mockito
- **Test Scope Dependencies**: 
  - spring-boot-starter-test
  - spring-security-test
  - h2 database

## Notes

- All tests follow AAA pattern (Arrange, Act, Assert)
- Tests are independent and can run in any order
- Each test has descriptive @DisplayName annotations
- Tests cover both happy path and error scenarios
- Integration tests use test containers where necessary
- Controller tests include authentication and authorization testing

---

**Generated**: 2025-01-18
**Framework**: Spring Boot 3.5.6 with JUnit 5
**Test Strategy**: Comprehensive coverage with unit, integration, and API tests


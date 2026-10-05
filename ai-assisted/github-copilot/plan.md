# 📋 Plan: AI-Generated Implementation Strategy

## Overview

This is the **comprehensive implementation plan** generated collaboratively with AI to address all requirements,
organized in 7 phases by increasing complexity.

---

## 🎯 Problem Statement

**Current State**:

- Spring Boot application with basic user management
- No input validation, no authentication, incomplete search feature
- No centralized exception handling, no tests, no documentation
- Multiple security vulnerabilities (SQL injection risk, no access control)

**Goal**:
Refactor, secure, and extend the application while maintaining clean architecture and demonstrating best practices in
Java/Spring development.

---

## 📊 Functional Requirements Analysis

### Functional Requirements (Business Logic)

1. **User Management**: Create users with validation
2. **User Search**: Find users with pagination, sorting, case-insensitive filtering
3. **Authentication**: JWT-based token generation and validation

### Technical-Functional Requirements (Quality & Security)

1. **Validation**: Email (RFC format) + Italian phone numbers (standard format)
2. **Security**: SQL injection prevention, JWT with signature/issuer/expiration/policy validation
3. **Error Handling**: Centralized, all responses via StatusDTO with HTTP 200
4. **Code Quality**: Refactoring, exception handling, comprehensive Javadoc
5. **Testing**: Unit tests with >80% coverage
6. **Bug Fixes**: Identify and fix logical errors

---

## 🚀 7-Phase Implementation Plan

### PHASE 1: Foundations & Setup

**Complexity**: LOW | **Estimated Effort**: 2-3 hours

#### Task 1.1: Deep Codebase Analysis

- **Objective**: Understand current architecture, identify all vulnerabilities
- **Deliverable**: Annotated list of issues
- **Steps**:
    1. Review all Java files for security issues
    2. Trace data flow through layers
    3. Document existing patterns and anti-patterns
    4. Create checklist of vulnerabilities

#### Task 1.2: Maven Dependencies Setup

- **Objective**: Add required libraries
- **Deliverable**: Updated pom.xml with tested versions
- **Dependencies to Add**:
  ```xml
  <!-- JWT -->
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
  </dependency>

  <!-- Validation -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
  </dependency>

  <!-- Spring Security (for JWT integration) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
  </dependency>
  ```

---

### PHASE 2: Data Validation & Bug Fixing

**Complexity**: LOW-MEDIUM | **Estimated Effort**: 3-4 hours

#### Task 2.1: Email & PhoneNumber Validation

- **Objective**: Implement robust validators with custom annotations
- **Deliverable**:
    - `@ValidEmail` annotation
    - `@ValidItalianPhoneNumber` annotation
    - Validator implementations with test cases
- **Implementation Strategy**:
  ```java
  // Phone number: +39xxxxxxxxxxxxx (13 digits) or 3xxxxxxxxxx (10 digits)
  // Email: standard RFC 5322 simplified regex
  ```
- **Integration Points**:
    - Apply to `AddUserRequest` fields
    - Raise `ValidationException` on failure
    - Leverage `@Validated` on controller

#### Task 2.2: SQL Injection Prevention

- **Objective**: Identify and fix injection vulnerability in PUT endpoint
- **Deliverable**: Secured PUT endpoint with input sanitization
- **Analysis**:
    1. Trace: Controller → Service → Repository
    2. Identify: Where user input is used unsanitized
    3. Fix: Input validation before any DB operation (FakeDatabase in this case)
    4. Add: Security test case attempting injection
- **Prevention Technique**:
    - Input validation + whitelisting
    - No string concatenation for queries
    - Use parametrized operations

#### Task 2.3: Bug Identification & Fixing

- **Objective**: Find and fix logical bugs
- **Deliverable**: List of bugs found + fixes applied
- **Common Areas to Check**:
    - Null pointer exceptions in getters/setters
    - GUID uniqueness in FakeDatabase
    - State mutations in concurrent scenarios
    - Boundary conditions in list operations
    - Optional handling

---

### PHASE 3: Centralized Exception Handling

**Complexity**: MEDIUM | **Estimated Effort**: 2-3 hours

#### Task 3.1: Global Exception Handler

- **Objective**: Implement `@ControllerAdvice` for centralized error handling
- **Deliverable**: `GlobalExceptionHandler.java`
- **Implementation**:
  ```java
  @ControllerAdvice
  public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<GenericResponse> handleValidation(...) {
      // Return GenericResponse with error status code
      // HTTP 200 but status.code = 400 (or custom error code)
    }
    
    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericResponse> handleGeneric(...) { }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGeneric(...) { }
  }
  ```
- **Key Requirement**: All responses return HTTP 200, with error info in StatusDTO.code

#### Task 3.2: Response Consistency

- **Objective**: Ensure all responses (success/error) follow same format
- **Deliverable**: Updated `GenericResponse` with error static method
- **Changes**:
  ```java
  public static GenericResponse error(int code, String message) {
    GenericResponse resp = new GenericResponse();
    resp.setStatus(new StatusDTO());
    resp.getStatus().setCode(code);
    resp.getStatus().setMessage(message);
    resp.getStatus().setTraceId(UUID.randomUUID().toString());
    return resp;
  }
  ```

---

### PHASE 4: Pagination, Sorting & Search

**Complexity**: MEDIUM | **Estimated Effort**: 4-5 hours

#### Task 4.1: Extend GetUsersRequest

- **Objective**: Add search/pagination/sorting parameters
- **Deliverable**: Complete `GetUsersRequest` DTO
- **Fields to Add**:
  ```java
  @NotBlank(message = "Search term cannot be blank")
  private String searchTerm;
  
  @Min(0)
  private int pageNumber = 0;
  
  @Min(1)
  @Max(100)
  private int pageSize = 10;
  
  @NotNull
  private SortDirection sortDirection; // ASC, DESC
  
  @NotNull
  private SortField sortField; // FIRST_NAME, LAST_NAME, EMAIL, CREATED_AT
  ```

#### Task 4.2: Repository Search Implementation

- **Objective**: Implement `UserRepository.search(SearchCriteria)`
- **Deliverable**: Search method with filtering, sorting, pagination
- **Algorithm**:
    1. Filter: case-insensitive match on name/email
    2. Sort: apply sort direction and field
    3. Paginate: calculate start/end indices
    4. Return: `List<User>` and total count
- **Edge Cases**:
    - Empty results
    - Page out of bounds
    - Null search term
    - Invalid sort field

#### Task 4.3: UserService Search Logic

- **Objective**: Wire repository to service with criteria pattern
- **Deliverable**: `UserService.searchUsers(CriteriaSearchUser)`
- **Implementation**:
    1. Create `CriteriaSearchUser` class
    2. Call `repository.search(criteria)`
    3. Wrap in `GenericPagedResult`
    4. Handle exceptions

#### Task 4.4: Controller Implementation

- **Objective**: Complete POST `/v1/user` endpoint
- **Deliverable**: Functional search endpoint
- **Implementation**:
  ```java
  @RequestMapping(value = "/v1/user", method = RequestMethod.POST)
  public ResponseEntity<GetUsersResponse> getUsers(
      @Valid @RequestBody GetUsersRequest request) {
    GenericPagedResult result = userService.searchUsers(...);
    GetUsersResponse response = assembler.toResponse(result);
    return ResponseEntity.ok(response);
  }
  ```

---

### PHASE 5: JWT Authentication

**Complexity**: MEDIUM-HIGH | **Estimated Effort**: 5-6 hours

#### Task 5.1: JWT Token Provider

- **Objective**: Create `JwtTokenProvider` for token operations
- **Deliverable**: Class with 4 core methods
- **Methods**:
  ```java
  public String generateToken(String username, String role)
  public boolean validateToken(String token)
  public String extractUsername(String token)
  public String extractRole(String token)
  ```
- **Validations**:
    - **Signature**: Verify HMAC signature with secret key
    - **Issuer**: Check issuer matches configured value (e.g., "high-card-app")
    - **Expiration**: Verify not expired (e.g., 1 hour TTL)
    - **Policy**: Validate role/claims against allowed set (USER, ADMIN)
- **Configuration**:
  ```properties
  jwt.secret=your-secret-key-change-in-production
  jwt.expiration=3600000
  jwt.issuer=high-card-app
  ```

#### Task 5.2: Login Endpoint

- **Objective**: Create `POST /auth/login` endpoint
- **Deliverable**: Functional login with fake credentials
- **Implementation**:
  ```java
  @PostMapping("/auth/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
    // Validate credentials (fake: username=admin, password=admin123)
    // Generate token
    // Return token in response
  }
  ```
- **Response Format**:
  ```json
  {
    "status": {"code": 200, "message": "Login successful"},
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600
  }
  ```

#### Task 5.3: JWT Filter

- **Objective**: Create `JwtAuthenticationFilter` to validate requests
- **Deliverable**: Filter extending `OncePerRequestFilter`
- **Flow**:
    1. Extract `Authorization: Bearer <token>` header
    2. Validate token via `JwtTokenProvider`
    3. Set `SecurityContext` with authenticated user
    4. Allow or reject request
- **Error Handling**:
    - Invalid token → 401
    - Missing token → allow (depends on endpoint)
    - Expired token → 401

#### Task 5.4: Endpoint Protection

- **Objective**: Secure endpoints requiring authentication
- **Deliverable**: Configuration and annotations
- **Strategy**:
    - Use `@PreAuthorize("hasRole('USER')")` on protected endpoints
    - `/auth/login` → public
    - `/user/v1/user` → requires ROLE_USER or ROLE_ADMIN
    - `/v1/user` (GET) → requires ROLE_USER or ROLE_ADMIN
- **Configuration**:
  ```java
  @Configuration
  @EnableWebSecurity
  public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.addFilterBefore(jwtAuthenticationFilter, 
        UsernamePasswordAuthenticationFilter.class);
      return http.build();
    }
  }
  ```

---

### PHASE 6: Testing & Documentation

**Complexity**: MEDIUM | **Estimated Effort**: 4-5 hours

#### Task 6.1: Validation Tests

- **Objective**: Unit tests for email and phone validators
- **Deliverable**: `EmailValidatorTest.java`, `PhoneNumberValidatorTest.java`
- **Test Cases**:
    - ✅ Valid emails: standard, with subdomain, numeric
    - ❌ Invalid emails: missing @, domain, dots
    - ✅ Valid phone: +39 format, length check
    - ❌ Invalid phone: wrong prefix, length, characters
    - Edge cases: empty, null, special chars

#### Task 6.2: Repository Search Tests

- **Objective**: Test pagination, sorting, filtering
- **Deliverable**: `UserRepositorySearchTest.java`
- **Test Cases**:
    - Pagination: multiple pages, boundary conditions
    - Sorting: ASC/DESC, multiple fields
    - Search: case-insensitive, partial matches, no results
    - Edge: empty database, invalid parameters

#### Task 6.3: JWT Integration Tests

- **Objective**: Test login, token validation, endpoint protection
- **Deliverable**: `JwtAuthenticationIntegrationTest.java`
- **Test Cases**:
    - Login valid credentials → token returned
    - Login invalid credentials → 401
    - Valid token → access granted
    - Expired token → 401
    - Tampered token → 401
    - Missing token → 401

#### Task 6.4: Javadoc Documentation

- **Objective**: Document all public classes and methods
- **Deliverable**: Comprehensive Javadoc coverage
- **Classes to Document**:
    - Controllers: method purpose, parameters, responses
    - Services: business logic, error conditions
    - Validators: validation rules, format requirements
    - DTOs: field meanings, constraints
    - Utilities: function purpose, usage
- **Template**:
  ```java
  /**
   * Brief description of class/method.
   * 
   * @param name description of parameter
   * @return description of return value
   * @throws ExceptionType when exception occurs
   * @since 1.0
   */
  ```

---

### PHASE 7: AI-Assisted Documentation (BONUS)

**Complexity**: LOW | **Effort**: 1-2 hours

#### Task 7.1: Create `/ai-assisted/` Folder

- **Objective**: Document AI collaboration process
- **Deliverable**: Three markdown files:
    1. `pre-analysis.md` - Context and initial understanding
    2. `plan.md` - This file (implementation strategy)
    3. `report.md` - Prompts, difficulties, assessment

---

## 🔗 Task Dependencies

```
1.1 Codebase Analysis
  ↓
1.2 Maven Setup
  ↓ (parallel execution possible)
  ├→ 2.1 Email/Phone Validation
  │   ├→ 2.2 SQL Injection Fix
  │   └→ 2.3 Bug Fixing
  │       ↓
  │     3.1 Exception Handler
  │       ├→ 3.2 Response Consistency
  │           ↓
  │         4.1 GetUsersRequest Completion
  │           ├→ 4.2 Repository Search
  │           ├→ 4.3 Service Logic
  │           └→ 4.4 Controller Implementation
  │               ↓
  │             5.1 JWT Provider
  │               ├→ 5.2 Login Endpoint
  │               ├→ 5.3 JWT Filter
  │               └→ 5.4 Endpoint Protection
  │                   ↓
  │                 6.1 Validation Tests
  │                 6.2 Repository Tests
  │                 6.3 JWT Tests
  │                 6.4 Javadoc
  │                   ↓
  │                 7.1 AI Documentation (this file)
```

**Critical Path**: 1.1 → 1.2 → 3.1 → 5.1 → 5.4
**Parallelizable**: All 6.x tasks after their respective phase completes

---

## 📌 Follow-up Question Answer

> **Why is it discouraged to use web-exposed objects (GenericRequest/GenericResponse) within the Service layer?**

### Answer

**Separation of Concerns** — The Service layer should encapsulate business logic independent of how data is presented or
received:

1. **Independence**: Service logic doesn't change when API format changes (REST → GraphQL → gRPC)
2. **Reusability**: Same service serves multiple interfaces without duplication
3. **Testability**: Test business logic with domain objects, not web objects
4. **Maintainability**: Modifications to API DTOs don't cascade to service code
5. **Single Responsibility**: Service focuses on "what" not "how to represent"

**Best Practice**: Use `Criteria` and domain `Model` objects in Service, transform via `Assembler` at layer boundaries.

---

## ✅ Success Criteria

- [ ] All 8 requirements from README addressed
- [ ] Code compiles without warnings
- [ ] All tests passing (target >85% coverage)
- [ ] Security vulnerabilities fixed
- [ ] Javadoc coverage complete
- [ ] Architecture layers preserved
- [ ] Email/phone validation working
- [ ] JWT authentication operational
- [ ] Pagination/sorting/search complete
- [ ] Exception handling centralized

---

## 📚 Key Technologies & Versions

- **Spring Boot**: 3.5.0 (Spring Security, Validation)
- **Java**: 17
- **JWT Library**: io.jsonwebtoken:jjwt 0.12.3
- **Validation**: jakarta.validation + hibernate-validator
- **Testing**: JUnit 5 (included in boot-starter-test)
- **Build**: Maven

---

## 🎯 Architecture Preserved

```
┌─────────────────────────────────────┐
│      REST Controller Layer          │ (GenericRequest/GenericResponse)
│  UserController, AuthController     │
└──────────────────┬──────────────────┘
                   │
        (Assembler: DTO ↔ Criteria)
                   ↓
┌─────────────────────────────────────┐
│      Service Business Logic         │ (Criteria, Domain Objects)
│  UserService, AuthService           │
└──────────────────┬──────────────────┘
                   │
      (Assembler: Domain ↔ Entity)
                   ↓
┌─────────────────────────────────────┐
│      Repository Data Access         │ (Domain Models)
│  UserRepository                     │
└──────────────────┬──────────────────┘
                   │
                   ↓
        ┌──────────────────┐
        │  FakeDatabase    │ (In-memory list)
        └──────────────────┘
```

This plan ensures progressive complexity, maintains clean architecture, and addresses all requirements systematically.


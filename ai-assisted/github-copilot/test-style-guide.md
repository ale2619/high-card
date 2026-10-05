# 🏗️ Test Style Guide: Given-When-Then + Mockito Standards

## Overview

This document defines the **standardized testing style** for the High-Card project using:

- **BDD Pattern**: Given-When-Then for clear test structure
- **Mockito Framework**: For unit test isolation
- **JUnit 5**: As testing framework
- **Naming Convention**: Clear, intent-driven test names

---

## 1. Test Class Structure & Setup

### Standard Test Class Template

```java
/**
 * Tests for {@link EmailValidator}.
 *
 * Uses Given-When-Then pattern for clarity:
 * - Given: setup preconditions
 * - When: execute behavior
 * - Then: assert outcomes
 */
@DisplayName("EmailValidator")
class EmailValidatorTest {

    // ========== FIELDS ==========
    private EmailValidator emailValidator;

    // ========== SETUP/TEARDOWN ==========

    @BeforeEach
    void setUp() {
        // Initialize components without mocks (pure unit)
        emailValidator = new EmailValidator();
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
        // Usually not needed with mocks/fakes
    }

    // ========== TEST METHODS ==========

    // Tests organized by positive, negative, edge cases
}
```

### Key Principles

- ✅ **One behavior per test**
- ✅ **Descriptive @DisplayName** annotations
- ✅ **AAA pattern** (Arrange, Act, Assert) = (Given, When, Then)
- ✅ **Clear intent** in test names
- ✅ **No test interdependencies** (each test independent)
- ✅ **Fast execution** (<1 second per test)

---

## 2. Given-When-Then Pattern in Practice

### Structure Template

```java

@Test
@DisplayName("should validate email with valid format when given 'user@example.com'")
void shouldValidateEmailWithValidFormat() {
    // ========== GIVEN ==========
    // Setup: preconditions before executing behavior
    String validEmail = "user@example.com";
    EmailValidator validator = new EmailValidator();

    // ========== WHEN ==========
    // Execute: the actual behavior being tested
    boolean result = validator.isValid(validEmail);

    // ========== THEN ==========
    // Assert: verify outcomes match expectations
    assertTrue(result, "Email should be valid");
}
```

### Why Given-When-Then?

1. **Clarity**: Instantly see setup, action, assertion
2. **Traceability**: Each test tells a story
3. **Maintainability**: Easy to modify setup or assertions
4. **Documentation**: Test acts as specification

### Alternative: Inline Comments

```java

@Test
@DisplayName("should reject email when missing @symbol")
void shouldRejectEmailMissingAtSymbol() {
    // Given: an invalid email without @ symbol
    String invalidEmail = "userexample.com";

    // When: validation is performed
    boolean result = validator.isValid(invalidEmail);

    // Then: validation should fail
    assertFalse(result, "Email without @ should be invalid");
}
```

---

## 3. Naming Convention: Test Method Names

### Pattern: `should[ExpectedBehavior]When[Condition]`

**Structure**:

```
shouldX_WhenY()
```

**Examples**:

```java
// ✅ GOOD: Clear intent
@Test
void shouldValidateEmail_WhenFormatIsCorrect() {
}

@Test
void shouldRejectEmail_WhenMissingAtSymbol() {
}

@Test
void shouldThrowException_WhenEmailIsNull() {
}

@Test
void shouldReturnEmpty_WhenNoResultsMatch() {
}

// ❌ BAD: Unclear intent
@Test
void testEmail() {
}  // too vague

@Test
void emailTest1() {
}  // not descriptive

@Test
void validEmailTest() {
}  // doesn't say what should happen

@Test
void test_email_validation() {
}  // generic
```

### With @DisplayName Annotation

```java

@Test
@DisplayName("Email validator should accept valid format: user@example.com")
void shouldValidateEmail_WhenFormatIsCorrect() {
    // Implementation
}
```

**Benefits**:

- Test explorer shows human-readable names
- Reports look professional
- Serves as documentation

---

## 4. Mockito for Unit Test Isolation

### When to Use Mocks

| Scenario                                | Mock?             | Why                     |
|-----------------------------------------|-------------------|-------------------------|
| Testing a service that calls repository | ✅ Mock repository | Isolate service logic   |
| Testing a validator (no dependencies)   | ❌ No mock         | Use real object         |
| Testing a controller that calls service | ✅ Mock service    | Test HTTP layer only    |
| Testing JWT provider                    | ❌ No mock         | Use real implementation |
| Testing repository with FakeDatabase    | ❌ No mock         | Test real data access   |

### Mock Setup Patterns

#### Pattern 1: Constructor Injection with Mockito

```java

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;  // Mock dependency

    @InjectMocks
    private UserService userService;  // Service with mocks injected

    @BeforeEach
    void setUp() {
        // Mockito initializes mocks and injects into service
        // No need to manually call MockitoAnnotations.openMocks()
    }

    @Test
    @DisplayName("should add user when validation passes")
    void shouldAddUser_WhenValidationPasses() {
        // Given
        User user = new User("Alice", "Smith", "alice@example.com", "+393123456789");
        given(userRepository.save(user)).willReturn(true);

        // When
        userService.addUser(user);

        // Then
        verify(userRepository, times(1)).save(user);
    }
}
```

#### Pattern 2: Method-Level Mocking (ArgumentCaptor)

```java

@Test
@DisplayName("should pass correct parameters to repository when saving user")
void shouldPassCorrectParametersToRepository_WhenSavingUser() {
    // Given
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    User user = new User("Alice", "Smith", "alice@example.com", "+393123456789");
    given(userRepository.save(any())).willReturn(true);

    // When
    userService.addUser(user);

    // Then
    verify(userRepository).save(userCaptor.capture());
    User capturedUser = userCaptor.getValue();
    assertEquals("Alice", capturedUser.getFirstName());
    assertEquals("alice@example.com", capturedUser.getEmail());
}
```

#### Pattern 3: Stubbing with Different Responses

```java

@Test
@DisplayName("should return empty when search term matches no users")
void shouldReturnEmpty_WhenSearchTermMatchesNoUsers() {
    // Given
    String searchTerm = "zzz";
    given(userRepository.search(searchTerm, 0, 10, "firstName", "ASC"))
            .willReturn(new GenericPagedResult(Collections.emptyList(), 0, 0));

    // When
    GenericPagedResult result = userService.searchUsers(searchTerm, 0, 10, "firstName", "ASC");

    // Then
    assertTrue(result.getItems().isEmpty());
    assertEquals(0, result.getTotalCount());
}
```

#### Pattern 4: Exception Stubbing

```java

@Test
@DisplayName("should propagate exception when repository fails")
void shouldPropagateException_WhenRepositoryFails() {
    // Given
    given(userRepository.save(any()))
            .willThrow(new RuntimeException("Database connection failed"));
    User user = new User("Alice", "Smith", "alice@example.com", "+393123456789");

    // When & Then
    assertThrows(RuntimeException.class, () -> userService.addUser(user));
}
```

#### Pattern 5: Verification with Mockito

```java

@Test
@DisplayName("should call repository exactly once when adding user")
void shouldCallRepositoryExactlyOnce_WhenAddingUser() {
    // Given
    User user = new User("Alice", "Smith", "alice@example.com", "+393123456789");
    given(userRepository.save(user)).willReturn(true);

    // When
    userService.addUser(user);

    // Then
    verify(userRepository, times(1)).save(user);  // Exactly 1 call
    verify(userRepository, never()).delete(any());  // Never called
    verifyNoMoreInteractions(userRepository);  // No other calls
}
```

---

## 5. Integration Tests (With Spring Context)

### Integration Test Template

```java
/**
 * Integration tests for {@link UserController}.
 *
 * Tests the full Spring context with MockMvc.
 * Uses Mockito to mock service layer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;  // Mock the service layer

    @BeforeEach
    void setUp() {
        // Setup test data if needed
    }

    @Test
    @DisplayName("should return 200 OK when adding valid user via PUT endpoint")
    void shouldReturn200_WhenAddingValidUserViaPutEndpoint() throws Exception {
        // Given
        AddUserRequest request = new AddUserRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@example.com");
        request.setPhoneNumber("+393123456789");

        given(userService.addUser(any())).willReturn(true);

        // When
        mockMvc.perform(put("/user/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectToJson(request)))

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status.code").value(200))
                .andExpect(jsonPath("$.status.message").value("User added."));

        verify(userService, times(1)).addUser(any());
    }

    @Test
    @DisplayName("should return 401 unauthorized when request lacks valid JWT token")
    void shouldReturn401_WhenRequestLacksValidJwtToken() throws Exception {
        // Given: no Authorization header

        // When
        mockMvc.perform(get("/user/v1/user")
                        .contentType(MediaType.APPLICATION_JSON))

                // Then
                .andExpect(status().isUnauthorized());
    }
}
```

### MockMvc Patterns

```java
// Simple GET
mockMvc.perform(get("/endpoint"))
        .

andExpect(status().

isOk());

// With header
        mockMvc.

perform(get("/endpoint")
    .

header("Authorization","Bearer "+token))
        .

andExpect(status().

isOk());

// POST with JSON body
        mockMvc.

perform(post("/endpoint")
    .

contentType(MediaType.APPLICATION_JSON)
    .

content(json))
        .

andExpect(status().

isCreated());

// JSON path assertions
        .

andExpect(jsonPath("$.status.code").

value(200))
        .

andExpect(jsonPath("$.items[0].firstName").

value("Alice"))
        .

andExpect(jsonPath("$.items").

isArray())
        .

andExpect(jsonPath("$.items", hasSize(10)));

// Content type assertions
        .

andExpect(content().

contentType(MediaType.APPLICATION_JSON));
```

---

## 6. Parameterized Tests for Multiple Cases

### Pattern: @ParameterizedTest with @ValueSource

```java

@ParameterizedTest
@ValueSource(strings = {"user@example.com", "alice+test@example.co.uk", "bob.smith@company.org"})
@DisplayName("should validate various valid email formats")
void shouldValidateVariousValidEmails(String validEmail) {
    // Given: multiple valid emails provided by @ValueSource
    EmailValidator validator = new EmailValidator();

    // When
    boolean result = validator.isValid(validEmail);

    // Then
    assertTrue(result, "Email '" + validEmail + "' should be valid");
}

@ParameterizedTest
@ValueSource(strings = {"userexample.com", "@example.com", "user@", "a@@b.com"})
@DisplayName("should reject various invalid email formats")
void shouldRejectVariousInvalidEmails(String invalidEmail) {
    // Given
    EmailValidator validator = new EmailValidator();

    // When
    boolean result = validator.isValid(invalidEmail);

    // Then
    assertFalse(result, "Email '" + invalidEmail + "' should be invalid");
}
```

### Pattern: @ParameterizedTest with @CsvSource

```java

@ParameterizedTest
@CsvSource({
        "user@example.com, true",
        "userexample.com, false",
        "user@, false",
        "@example.com, false"
})
@DisplayName("should validate email according to specifications")
void shouldValidateEmail_MultipleScenarios(String email, boolean expected) {
    // Given
    EmailValidator validator = new EmailValidator();

    // When
    boolean result = validator.isValid(email);

    // Then
    assertEquals(expected, result, "Email '" + email + "' validation mismatch");
}
```

---

## 7. Exception Testing

### Pattern: assertThrows

```java

@Test
@DisplayName("should throw ValidationException when email is null")
void shouldThrowValidationException_WhenEmailIsNull() {
    // Given
    EmailValidator validator = new EmailValidator();

    // When & Then
    ValidationException exception = assertThrows(
            ValidationException.class,
            () -> validator.isValid(null),
            "Should throw ValidationException for null email"
    );

    assertEquals("Email cannot be null", exception.getMessage());
}

@Test
@DisplayName("should throw IllegalArgumentException when phone is empty")
void shouldThrowIllegalArgumentException_WhenPhoneIsEmpty() {
    // Given
    PhoneNumberValidator validator = new PhoneNumberValidator();

    // When & Then
    assertThrows(
            IllegalArgumentException.class,
            () -> validator.isValid("")
    );
}
```

### Pattern: Asserting Exception Properties

```java

@Test
@DisplayName("should include error details in ValidationException")
void shouldIncludeErrorDetails_InValidationException() {
    // Given
    EmailValidator validator = new EmailValidator();

    // When & Then
    ValidationException exception = assertThrows(ValidationException.class,
            () -> validator.isValid("invalid"));

    assertTrue(exception.getMessage().contains("Email"));
    assertEquals("INVALID_EMAIL_FORMAT", exception.getErrorCode());
}
```

---

## 8. Test Organization: Nested Classes for Grouping

### Pattern: @Nested for Logical Grouping

```java

@DisplayName("EmailValidator")
class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    // ========== NESTED: VALID EMAIL TESTS ==========
    @Nested
    @DisplayName("when validating valid email addresses")
    class ValidEmailTests {

        @Test
        void shouldValidateSimpleFormat() {
            assertTrue(validator.isValid("user@example.com"));
        }

        @Test
        void shouldValidateWithSubdomain() {
            assertTrue(validator.isValid("user@mail.example.co.uk"));
        }

        @Test
        void shouldValidateWithNumbers() {
            assertTrue(validator.isValid("user123@example123.com"));
        }
    }

    // ========== NESTED: INVALID EMAIL TESTS ==========
    @Nested
    @DisplayName("when validating invalid email addresses")
    class InvalidEmailTests {

        @Test
        void shouldRejectMissingAtSymbol() {
            assertFalse(validator.isValid("userexample.com"));
        }

        @Test
        void shouldRejectMissingDomain() {
            assertFalse(validator.isValid("user@"));
        }

        @Test
        void shouldRejectMultipleAtSymbols() {
            assertFalse(validator.isValid("user@@example.com"));
        }
    }

    // ========== NESTED: EDGE CASES ==========
    @Nested
    @DisplayName("when validating edge case emails")
    class EdgeCaseEmailTests {

        @Test
        void shouldRejectNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> validator.isValid(null));
        }

        @Test
        void shouldRejectEmpty() {
            assertFalse(validator.isValid(""));
        }

        @Test
        void shouldRejectVeryLongEmail() {
            String longEmail = "a".repeat(300) + "@example.com";
            assertFalse(validator.isValid(longEmail));
        }
    }
}
```

**Benefits of @Nested**:

- ✅ Organizes tests logically
- ✅ Improves readability
- ✅ Test reports show hierarchy
- ✅ Shared setup per group (optional)

---

## 9. Common Assertion Patterns

### Simple Assertions

```java
// Boolean assertions
assertTrue(result);

assertFalse(result);

// Equality assertions
assertEquals(expected, actual);

assertEquals(expected, actual, "Error message if fails");

// Null assertions
assertNull(result);

assertNotNull(result);

// Collection assertions
assertEquals(0,list.size());

assertTrue(list.isEmpty());

assertTrue(list.contains(item));

assertEquals(10,list.size());

// String assertions
assertTrue(message.contains("error"));

assertTrue(message.startsWith("Invalid"));

assertEquals("expected message",actual);

// Exception assertions
assertThrows(Exception .class, () ->

methodCall());

assertDoesNotThrow(() ->

methodCall());
```

### AssertJ Fluent Assertions (Optional, more readable)

```java
// AssertJ offers more readable syntax
// Add: org.assertj:assertj-core:3.24.1

@Test
void shouldValidateEmailsWithAssertJ() {
    EmailValidator validator = new EmailValidator();

    // Given
    String validEmail = "user@example.com";
    String invalidEmail = "userexample.com";

    // Then with AssertJ
    assertThat(validator.isValid(validEmail))
            .as("Valid email should pass validation")
            .isTrue();

    assertThat(validator.isValid(invalidEmail))
            .as("Invalid email should fail validation")
            .isFalse();

    // Collections
    List<String> validEmails = List.of("a@b.com", "c@d.com");
    assertThat(validEmails)
            .hasSize(2)
            .contains("a@b.com")
            .doesNotContain("invalid");
}
```

---

## 10. Example: Complete Test Class with All Standards

### Full Working Example

```java
/**
 * Unit tests for {@link EmailValidator}.
 *
 * Tests email validation logic using Given-When-Then pattern
 * with Mockito for dependencies (none in this case).
 *
 * @author Your Name
 * @since 1.0
 */
@DisplayName("EmailValidator Unit Tests")
class EmailValidatorTest {

    // ========== SETUP ==========

    private EmailValidator emailValidator;

    @BeforeEach
    void setUp() {
        emailValidator = new EmailValidator();
    }

    // ========== VALID EMAILS ==========

    @Nested
    @DisplayName("Valid Email Validation")
    class ValidEmailValidation {

        @Test
        @DisplayName("should validate simple email format")
        void shouldValidateSimpleEmailFormat() {
            // Given
            String validEmail = "user@example.com";

            // When
            boolean result = emailValidator.isValid(validEmail);

            // Then
            assertTrue(result, "user@example.com should be valid");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "user@example.com",
                "alice+tag@example.co.uk",
                "bob.smith@company.org"
        })
        @DisplayName("should validate multiple valid email formats")
        void shouldValidateMultipleValidFormats(String validEmail) {
            // Given: various valid email formats

            // When
            boolean result = emailValidator.isValid(validEmail);

            // Then
            assertTrue(result, "Email '" + validEmail + "' should be valid");
        }
    }

    // ========== INVALID EMAILS ==========

    @Nested
    @DisplayName("Invalid Email Rejection")
    class InvalidEmailRejection {

        @ParameterizedTest
        @CsvSource({
                "userexample.com, Missing @ symbol",
                "user@, Missing domain",
                "@example.com, Missing local part",
                "user@@example.com, Multiple @ symbols"
        })
        @DisplayName("should reject malformed emails")
        void shouldRejectMalformedEmails(String email, String reason) {
            // Given

            // When
            boolean result = emailValidator.isValid(email);

            // Then
            assertFalse(result, reason + ": '" + email + "' should be invalid");
        }
    }

    // ========== EDGE CASES ==========

    @Nested
    @DisplayName("Edge Case Handling")
    class EdgeCaseHandling {

        @Test
        @DisplayName("should throw exception when email is null")
        void shouldThrowException_WhenEmailIsNull() {
            // Given

            // When & Then
            assertThrows(
                    IllegalArgumentException.class,
                    () -> emailValidator.isValid(null),
                    "Null email should throw exception"
            );
        }

        @Test
        @DisplayName("should reject very long email (>254 chars)")
        void shouldRejectVeryLongEmail() {
            // Given
            String longEmail = "a".repeat(300) + "@example.com";

            // When
            boolean result = emailValidator.isValid(longEmail);

            // Then
            assertFalse(result, "Email exceeding 254 chars should be invalid");
        }
    }
}
```

---

## 11. Maven Dependencies for Testing

### pom.xml Configuration

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
    <exclusions>
        <exclusion>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
        </exclusion>
    </exclusions>
</dependency>

        <!-- Additional testing libraries -->
<dependency>
<groupId>org.mockito</groupId>
<artifactId>mockito-inline</artifactId>
<version>5.2.1</version>
<scope>test</scope>
</dependency>

<dependency>
<groupId>org.mockito</groupId>
<artifactId>mockito-junit-jupiter</artifactId>
<version>5.2.1</version>
<scope>test</scope>
</dependency>

        <!-- Code Coverage -->
<dependency>
<groupId>org.jacoco</groupId>
<artifactId>jacoco-maven-plugin</artifactId>
<version>0.8.10</version>
<scope>test</scope>
</dependency>

        <!-- Optional: AssertJ for fluent assertions -->
<dependency>
<groupId>org.assertj</groupId>
<artifactId>assertj-core</artifactId>
<version>3.24.1</version>
<scope>test</scope>
</dependency>
```

---

## 12. Best Practices Summary

| Aspect            | Best Practice                             |
|-------------------|-------------------------------------------|
| **Test Names**    | `shouldX_WhenY()` + @DisplayName          |
| **Structure**     | Given-When-Then (AAA pattern)             |
| **Mocks**         | Use @Mock @InjectMocks only when needed   |
| **Isolation**     | Test one behavior per test                |
| **Speed**         | Each test <1 second                       |
| **Organization**  | Group related tests with @Nested          |
| **Parameters**    | Use @ParameterizedTest for multiple cases |
| **Exceptions**    | Test both success and failure paths       |
| **Coverage**      | JaCoCo: target 80%+                       |
| **Documentation** | Test is the specification                 |
| **No Flakiness**  | Tests must be deterministic               |
| **Independence**  | No test dependencies                      |

---

## 13. Checklist: Code Review for Tests

### Before Committing Tests

- [ ] **Naming**: Test names clearly describe behavior
- [ ] **Structure**: Given-When-Then sections clearly marked
- [ ] **Mocks**: Mockito used appropriately (not overused)
- [ ] **Coverage**: Critical paths tested (happy + error)
- [ ] **Edge Cases**: Null, empty, boundary tested
- [ ] **Speed**: No slow tests (>1 second)
- [ ] **Clarity**: Easy to understand intent
- [ ] **Independence**: Tests run in any order
- [ ] **Documentation**: @DisplayName present
- [ ] **Assertions**: Clear error messages on failure
- [ ] **No Duplication**: DRY principle applied
- [ ] **Passes Locally**: `mvn clean test` passes

---

## Conclusion

This style guide ensures:

- ✅ **Consistency** across all test code
- ✅ **Readability** for team review
- ✅ **Maintainability** for future changes
- ✅ **Professionalism** in code quality

**Everyone writes tests following this standard** → tests become living documentation of expected behavior.


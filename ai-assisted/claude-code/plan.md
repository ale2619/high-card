# Implementation Plan — Claude Code

## Context

Plan co-built with Claude Code (claude-sonnet-4-6) based on direct codebase reading.
References the same 7-phase structure identified with GitHub Copilot, with adjustments
derived from autonomous source code analysis.

---

## Starting State (from actual code)

| Component                        | State                                                    |
|----------------------------------|----------------------------------------------------------|
| `UserController.addUser` (PUT)   | Working but no input validation                          |
| `UserController.getUsers` (POST) | Stub — `ResponseEntity.ok().build()`                     |
| `UserServiceImpl.addUser`        | Bug: catch swallows GenericException, always returns 500 |
| `UserServiceImpl.getUsers`       | Returns `null`                                           |
| `GetUsersRequest`                | Empty — no fields at all                                 |
| `UserRepository`                 | Only `save`, `getByGuid`, `getAll`                       |
| Exception handling               | No `@ControllerAdvice`                                   |
| JWT / Spring Security            | No dependencies present in `pom.xml`                     |
| Tests                            | Only stub `HighCardApplicationTests`                     |
| Javadoc                          | None                                                     |

---

## 7 Implementation Phases

---

### PHASE 1 — Foundations and Dependencies

**Complexity**: LOW | **Priority**: BLOCKING

#### 1.1 Update `pom.xml`

Add missing dependencies:

```xml
<!-- Jakarta Bean Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

        <!-- Spring Security -->
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-security</artifactId>
</dependency>

        <!-- JWT — jjwt 0.12.x (latest stable) -->
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
```

#### 1.2 Configuration (`application.yml`)

`application.properties` was converted to `application.yml` for better readability and
hierarchical structure — YAML grouping makes related properties visually explicit and
avoids repeated prefixes (e.g., `jwt.secret`, `jwt.expiration` → nested under `jwt:`).

```yaml
spring:
  application:
    name: demo

jwt:
  secret: change-in-production-key-minimum-256-bit-a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6
  expiration: 3600000
  issuer: high-card-idp
  allowed-roles: USER,ADMIN
```

**Checkpoint**: `mvn compile` must pass before proceeding.

---

### PHASE 2 — Bug Fixing and Data Validation

**Complexity**: LOW-MEDIUM | **Depends on**: Phase 1

#### ✅ 2.1 Fix Blocking Bug — `UserServiceImpl.java`

**Problem** (line 59): `catch (Exception e)` also catches `GenericException`.

```java
// BEFORE (buggy)
}catch(Exception e){
        log.

error(e.getMessage(),e);
        throw new

GenericException(GenericException.GENERIC_ERROR);
}

// AFTER (fixed)
        }catch(
GenericException e){
        log.

error(e.getMessage(),e);
        throw e;
}catch(
Exception e){
        log.

error(e.getMessage(),e);
        throw new

GenericException(GenericException.GENERIC_ERROR);
}
```

#### ✅ 2.2 Email and Italian Phone Number Validation

**Custom annotation `@ValidEmail`**:

- Regex: `^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$`
- Applied to `AddUserRequest.email`

**Custom annotation `@ValidItalianPhoneNumber`**:

- Accepted formats:
    - Mobile: `3[0-9]{9}` (10 digits, starts with 3)
    - With country code: `\+39[0-9]{9,10}` (12-13 characters total)
- Applied to `AddUserRequest.phoneNumber`

**Files to create**:

```
web/user/validation/
├── ValidEmail.java                   (annotation)
├── ValidItalianPhoneNumber.java      (annotation)
├── EmailValidator.java               (ConstraintValidator impl)
└── ItalianPhoneNumberValidator.java  (ConstraintValidator impl)
```

#### ✅ 2.3 Fix Seed Data — `FakeDatabase.java`

Current seed phone numbers (`"+39" + i`) are too short and invalid.
Replace with:

```java
user.setPhoneNumber("+393331234"+String.format("%03d", i));
// → "+393331234000", "+393331234001", …
```

#### ✅ 2.4 SQL Injection Prevention

The vulnerability is conceptual (no real SQL), but the defense must be implemented:

- Validate all inputs before any repository operation
- No string concatenation to build queries
- `AddUserAssembler.toCriteria()` must only receive already-validated inputs

#### ✅ 2.5 Constructor Injection Refactoring (added during code review)

Replaced `@Autowired` field injection with constructor injection across all eligible Spring beans:

- `UserController` and `UserServiceImpl` — fields made `private final`, `@Autowired` removed, `@RequiredArgsConstructor`
  added
- `AddUserAssembler`, `UserAssembler`, `StringUtil`, `UserRepository` — already stateless with no dependencies; no
  change needed

**Rationale**: constructor injection is the Spring-recommended approach since 4.3 — it makes dependencies explicit,
enforces immutability via `final`, and allows instantiation without a Spring context (easier unit testing).

#### ✅ 2.6 Additional Bugs Fixed During Code Review

- `AddUserAssembler:14` — `setLastName` was calling `getFirstName()` instead of `getLastName()`; every created user had
  `firstName == lastName`
- `UserAssembler:12` — email was being stripped to domain only via `substring(lastIndexOf("@") + 1)`; the full address
  is now returned

#### ✅ 2.7 Replace Custom `StringUtil` with `org.springframework.util.StringUtils`

The project's custom `StringUtil.isNullOrEmpty()` was replaced with Spring's built-in
`StringUtils.hasText()` (inverse logic: `!StringUtils.hasText(x)` ≡ `isNullOrEmpty(x)`).
`StringUtil.java` was deleted. No new dependency needed — `spring-core` is already on the classpath.

#### ✅ 2.8 Builder Pattern — standing rule

`@Builder` (or `@SuperBuilder` for inheritance chains) is preferred over `new Type()` + setters wherever technically
correct.
`@SuperBuilder` requires the annotation on all classes in the hierarchy; avoided on response classes whose base (
`GenericResponse`) carries static factory methods.

`@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor` added to `StatusDTO`, `User`, `UserDTO`, `CriteriaAddUser`.
All call sites refactored to use `Type.builder()...build()` instead of `new Type()` + setters.
`GenericException` static initialiser and `createStatus()` helper replaced with inline `StatusDTO.builder()` calls.

---

### ✅ PHASE 2.9 — Pre-Phase-5 additions (custom exceptions, AOP logging, addUser response)

#### ✅ addUser returns created user

`AddUserResult` and `AddUserResponse` now carry a `UserDTO user` field populated with the
assigned GUID. `AddUserAssembler.toResponse()` builds the response via `@SuperBuilder`.
Controller return type changed to `AddUserResponse`.

#### ✅ AOP logging (`LoggingAspect`)

- **Controllers** (INFO): logs `[HTTP_METHOD URI]` on entry and `OK [Xms]` on exit.
  Uses `HttpServletRequest` from `RequestContextHolder` for accurate path info.
- **Services** (INFO): logs method entry/exit with elapsed time via `@Around`.
- **Inline debug logs** added to `UserServiceImpl`: field validation steps, email uniqueness
  check, GUID after persist, search criteria and result counts.
- **Validators** (`EmailValidator`, `ItalianPhoneNumberValidator`): `@Slf4j` + debug logs
  on entry, blank check, and regex failure (AOP cannot intercept ConstraintValidator calls).
- `spring-boot-starter-aop` added to `pom.xml`.

### ✅ PHASE 3 — Centralized Exception Handling

**Complexity**: MEDIUM | **Depends on**: Phase 2

#### ✅ 3.1 `GlobalExceptionHandler.java`

```java

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GenericException.class)
    public ResponseEntity<GenericResponse> handleGenericException(GenericException e) {
        return ResponseEntity.ok(GenericResponse.error(e.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleValidation(MethodArgumentNotValidException e) {
        // aggregate violation messages from BindingResult
        return ResponseEntity.ok(GenericResponse.error("Validation failed: ..."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleUnexpected(Exception e) {
        return ResponseEntity.ok(GenericResponse.error("Unexpected error"));
    }
}
```

**Original requirement**: all HTTP responses must return status code **200**.
Errors are communicated via `StatusDTO.code` in the response body.

> **⚠️ Revised** — see Phase 6.6: this constraint was later dropped in favour of standard HTTP semantics.

#### ✅ 3.2 `GenericResponse.error()` factory methods

`GenericResponse` already has `success()`. Add:

```java
public static GenericResponse error(String message) { ...}

public static GenericResponse error(StatusDTO status) { ...}
```

---

### ✅ PHASE 4 — Pagination, Sorting and Search

**Complexity**: MEDIUM | **Depends on**: Phase 3

#### 4.1 `GetUsersRequest` — add fields

```java
public class GetUsersRequest extends GenericRequest {

    @NotBlank
    private String searchTerm;

    @Min(0)
    private int pageNumber = 0;

    @Min(1)
    @Max(100)
    private int pageSize = 10;

    @NotNull
    private OrderType orderType;      // enum: FIRST_NAME, LAST_NAME, EMAIL

    @NotNull
    private SortDirection sortDirection;  // ASC, DESC
}
```

#### 4.2 `CriteriaGetUsers` — synchronize with request

Add the same fields to the criteria (no web layer dependency).

#### 4.3 `UserRepository` — implement `search(CriteriaGetUsers)`

```java
public List<User> search(CriteriaGetUsers criteria) {
    return FakeDatabase.TABLE_USER.stream()
            .filter(u -> matchesSearchTerm(u, criteria.getSearchTerm()))
            .sorted(buildComparator(criteria))
            .skip((long) criteria.getPageNumber() * criteria.getPageSize())
            .limit(criteria.getPageSize())
            .collect(Collectors.toList());
}
```

- Filter: case-insensitive `contains` on `firstName`, `lastName`, `email`
- Sorting: by `OrderType` (field) + `SortDirection` (ASC/DESC)
- Pagination: skip + limit

#### 4.4 `UserServiceImpl.getUsers` — implement

Wire controller → service → repository using the criteria pattern.

#### 4.5 `UserController.getUsers` — complete POST endpoint

---

### ✅ PHASE 5 — JWT Authentication

**Complexity**: MEDIUM-HIGH | **Depends on**: Phase 3

#### 5.1 `JwtTokenProvider.java`

```
security/jwt/
├── JwtTokenProvider.java
├── JwtAuthenticationFilter.java
└── SecurityConfig.java
```

**`JwtTokenProvider` methods**:

```java
String generateToken(String username, String role)

Optional<Claims> validateAndExtractClaims(String token)   // single parse: signature + expiration + issuer + policy
// returns empty Optional if invalid
```

**Security refactoring applied post-Phase-5 (round 1)**:

- `validateToken(boolean)` replaced by `validateAndExtractClaims(Optional<Claims>)` — eliminates 3-parse-per-request
  pattern in the filter
- `signingKey()` removed; key cached in `cachedSigningKey` via `@PostConstruct`
- `PasswordEncoder` (BCrypt) bean added to `SecurityConfig`

**Security refactoring applied post-Phase-5 (round 2 — UserDetailsService)**:

- `InMemoryUserDetailsManager` registered in `SecurityConfig` with two accounts: `user/user123` (USER) and
  `admin/admin123` (ADMIN); passwords BCrypt-encoded inline
- `AuthenticationManager` bean exposed via `AuthenticationConfiguration`
- `AuthController` completely rewritten: `@Value` fields, `@PostConstruct`, `PasswordEncoder` and manual
  `passwordEncoder.matches()` removed; credential verification delegated to `authenticationManager.authenticate()`
- Role extracted from `Authentication.getAuthorities()` post-login, `ROLE_` prefix stripped before embedding in JWT
- Login failure returns HTTP `401 Unauthorized` — only endpoint deviating from the project's HTTP-200-for-all-errors
  convention, justified because authentication failure is a transport-level concern
- `auth.username` / `auth.password` properties removed from `application.yml`

**Required validations**:

| Validation              | jjwt method                           |
|-------------------------|---------------------------------------|
| Signature (HMAC-SHA256) | `verifyWith(secretKey)`               |
| Issuer                  | `.requireIssuer("high-card-app")`     |
| Expiration              | `.requireExpiration()` (jjwt default) |
| Policy/role             | check claim `role` ∈ `{USER, ADMIN}`  |

#### 5.2 `AuthController` + `POST /auth/login`

Fake credentials: `admin` / `admin123` → returns token.
Response structure consistent with `GenericResponse` + `token` field.

#### 5.3 `JwtAuthenticationFilter`

Extends `OncePerRequestFilter`:

- Extracts `Authorization: Bearer <token>`
- Validates with `JwtTokenProvider`
- Sets `SecurityContextHolder`

#### 5.4 `SecurityConfig`

```java
http
        .csrf(AbstractHttpConfigurer::disable)
    .

sessionManagement(sm ->sm.

sessionCreationPolicy(STATELESS))
        .

authorizeHttpRequests(auth ->auth
        .

requestMatchers("/auth/login")
        .

permitAll()
        .

anyRequest()
        .

authenticated()
    )
            .

addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter .class);
```

---

### ✅ PHASE 6 — Testing and Documentation

**Complexity**: MEDIUM | **Depends on**: Phases 2–5

#### 6.1 Tests to Create

| File                              | What it tests                                                    |
|-----------------------------------|------------------------------------------------------------------|
| `EmailValidatorTest`              | Valid, invalid, null, edge cases                                 |
| `ItalianPhoneNumberValidatorTest` | +39 format, mobile, invalid                                      |
| `UserServiceImplTest`             | addUser (ok, validation errors, duplicate), catch bug regression |
| `UserRepositorySearchTest`        | Pagination, sorting ASC/DESC, case-insensitive filter            |
| `JwtTokenProviderTest`            | Generate, validate, expired, tampered, wrong issuer              |
| `GlobalExceptionHandlerTest`      | All handlers, HTTP status code matches semantic code in body     |

**Target coverage**: >80%

#### 6.2 Javadoc

Priority classes:

- `UserController` — all public methods with `@param`, `@return`, `@throws`
- `UserServiceImpl` — business logic explanation
- `JwtTokenProvider` — validation logic documented
- Validation annotations — expected regex and format
- `FakeDatabase` — note that it is for demo purposes only

### ✅ PHASE 6.5 — OpenAPI / Swagger UI

**Complexity**: LOW | **Depends on**: Phase 5 (JWT security scheme), Phase 6 (Javadoc context)

> **Prompt**: *"Aggiungi la dipendenza springdoc-openapi al progetto per esporre uno swagger con spec 2.8.9. Arricchisci
i controller e i DTO principali con le annotazioni @Operation, @Schema e @ApiResponse. Configura il security scheme JWT
nella UI in modo che si possa testare gli endpoint direttamente dal browser senza dover usare strumenti esterni."*

#### Changes

| File                                                | Change                                                                                                     |
|-----------------------------------------------------|------------------------------------------------------------------------------------------------------------|
| `pom.xml`                                           | `springdoc-openapi-starter-webmvc-ui:2.8.9`            |
| `application.yml`                                   | `springdoc.api-docs.version: openapi_3_1` + `spring.main.allow-bean-definition-overriding: true`           |
| `SecurityConfig`                                    | `permitAll()` on `/swagger-ui/**` and `/v3/api-docs/**`                                                    |
| `config/OpenApiConfig`                              | New bean — title, version, global `bearerAuth` JWT security scheme                                         |
| `AuthController`                                    | `@Tag`, `@Operation`, `@ApiResponses`, `@SecurityRequirements` (login is public)                           |
| `UserController`                                    | `@Tag`, `@Operation`, `@ApiResponses` on both methods                                                      |
| `StatusDTO`, `UserDTO`                              | `@Schema` on class and all fields with `description` + `example`                                           |
| `LoginRequest`, `AddUserRequest`, `GetUsersRequest` | `@Schema` on class and all fields; `requiredMode` on mandatory fields; `@Schema` on `OrderType` enum values |

#### Notes

- `allow-bean-definition-overriding: true` needed because springdoc 2.8.9 and Spring Boot 3.5.0 both autoconfigure an
  `ErrorMvcAutoConfiguration` bean with the same name.
- `@SecurityRequirements` (empty) on `POST /auth/login` removes the padlock icon so the login endpoint is clearly marked
  as public in the UI.
- Swagger UI: `http://localhost:8080/swagger-ui/index.html` | API docs JSON: `http://localhost:8080/v3/api-docs`

### ✅ PHASE 6.6 — HTTP Status Code Standardisation

**Complexity**: LOW | **Depends on**: Phase 6.5 (Swagger UI made the inconsistency visible)

> **Prompt**: *"Ora ti chiedo di fare una modifica sugli errori: voglio adeguarmi agli standard delle api rest
> e restituire gli http status code corretti, quindi 400, 500 ecc"*

The original spec required HTTP 200 for every response (errors communicated via `StatusDTO.code`).
After seeing the Swagger UI expose this inconsistency clearly, the decision was made to align with
standard REST semantics: HTTP status mirrors the semantic outcome, and `StatusDTO.code` redundantly
carries the same value so clients that already inspect the body do not break.

#### Changes

| File                        | Change                                                                                   |
|-----------------------------|------------------------------------------------------------------------------------------|
| `GlobalExceptionHandler`    | `handleValidation` → `400 Bad Request`; `handleUnexpected` → `500 Internal Server Error`; `handleGenericException` → HTTP status derived from `StatusDTO.code` via `HttpStatus.resolve()`, fallback 500 |
| `AuthController`            | Already returned `401 Unauthorized` on `BadCredentialsException` (aligned from Phase 5) |
| `UserController`            | `@ApiResponses` updated with accurate `responseCode` + `content/schema` per response    |
| `AuthController`            | `@ApiResponse` for 400 (→ `GenericResponse`) and 401 (→ `LoginResponse`) with schema    |
| `GlobalExceptionHandlerTest`| Three tests renamed and assertions updated: `404`/`400`/`500` instead of `200`           |

#### Design rationale

- **Discoverability**: standard HTTP status codes allow reverse proxies, monitoring tools, and API gateways to handle errors without parsing the body.
- **Swagger accuracy**: `@ApiResponse` entries now carry distinct `content/schema` per status code, so the UI shows the correct example shape for each outcome instead of reusing the 200 schema.
- **`StatusDTO.code` kept**: body code is preserved to avoid breaking callers that already deserialise it; the HTTP status and body code always agree.
- **403 anomaly unchanged**: Spring Security raises HTTP 403 before the controller runs; it cannot be intercepted by `GlobalExceptionHandler`. `@ApiResponse(responseCode="403", content=@Content())` marks it as intentionally body-less.

---

### PHASE 7 — AI-Assisted Documentation (Bonus)

**Complexity**: LOW | **Current phase**

- [x] `pre-analysis.md` — context and initial analysis
- [x] `plan.md` — this file
- [ ] `report.md` — to be completed after implementation

---

## Phase Dependencies

```
PHASE 1 (pom.xml, config)
  └─→ PHASE 2 (bug fix, validation)
        └─→ PHASE 3 (exception handling)
              ├─→ PHASE 4 (search/pagination)
              └─→ PHASE 5 (JWT)
                    └─→ PHASE 6 (tests + javadoc)
                              └─→ PHASE 7 (documentation)
```

**Critical path**: 1 → 2.1 (catch bug fix) → 3 → 5 → 6 (JWT tests)

---

## Differences from GitHub Copilot Plan

| Aspect                    | GitHub Copilot      | Claude Code                         |
|---------------------------|---------------------|-------------------------------------|
| `catch (Exception e)` bug | Not identified      | Identified and planned (task 2.1)   |
| Invalid seed data         | Not mentioned       | Identified and planned (task 2.3)   |
| Change execution          | Suggestions only    | Direct file writes                  |
| Architecture verification | Assumed from README | Verified by reading each file       |
| Report                    | Completed upfront   | To be completed post-implementation |

---

## Answer to the Follow-Up Question

> **Why is it discouraged to use web-exposed objects (GenericRequest/GenericResponse) in the Service layer?**

The Service layer must be transport-independent. If the Service depends on `GenericRequest`:

1. **Coupling**: changing the API format (REST → gRPC, adding a field) forces changes in the
   Service, violating the Single Responsibility Principle.

2. **Non-reusability**: the same Service cannot be called from non-web contexts
   (scheduled jobs, JMS messages) without artificial adapters.

3. **Testability**: testing business logic requires constructing web objects,
   introducing irrelevant dependencies.

4. **Security**: web objects may carry unsanitized input that the Service should never receive.

**Solution already adopted in this project**:
Controller → `Assembler.toCriteria()` → Service receives `Criteria` objects (pure POJOs).
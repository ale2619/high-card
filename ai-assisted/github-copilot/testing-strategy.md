# 🧪 Detailed Testing Strategy: High-Card Exercise

## Executive Summary

**Goal**: Achieve >80% code coverage with focus on edge cases, security, and business logic validation.

**Approach**:

- Unit tests for validators, repository, services
- Integration tests for authentication and endpoints
- Contract tests for API responses
- Use JUnit 5 + Mockito for mocking
- Target coverage: 80%+ (verified with JaCoCo)

---

## Test Structure & Organization

### Folder Structure

```
src/test/java/it/sara/demo/
├── validation/
│   ├── EmailValidatorTest.java
│   └── PhoneNumberValidatorTest.java
├── repository/
│   ├── UserRepositorySearchTest.java
│   └── UserRepositoryPersistenceTest.java
├── service/
│   ├── UserServiceTest.java
│   └── AuthServiceTest.java
├── security/
│   ├── JwtTokenProviderTest.java
│   ├── JwtAuthenticationFilterTest.java
│   └── AuthenticationIntegrationTest.java
├── controller/
│   ├── UserControllerTest.java
│   └── AuthControllerTest.java
├── integration/
│   └── EndToEndAuthenticationTest.java
└── util/
    └── TestDataBuilder.java (fixtures)
```

---

## PHASE 2: Validation Tests

### Task 6.1a: EmailValidatorTest

**Target Coverage**: 100% (validator is critical for security)

#### Test Cases Detail

```java
// ✅ VALID EMAIL CASES (Happy Path)
1.Simple valid
email
Input:"user@example.com"
Expected:valid
   
2.
With subdomain
Input:"user@mail.example.co.uk"
Expected:valid
   
3.
With numbers
Input:"user123@example123.com"
Expected:valid
   
4.
With dots
in local
part
Input:"user.name@example.com"
Expected:valid
   
5.
With plus
sign
Input:"user+tag@example.com"
Expected:valid
   
6.
With hyphen
in domain
Input:"user@my-example.com"
Expected:valid

// ❌ INVALID EMAIL CASES (Error Paths)
7.Missing@symbol
Input:"userexample.com"
Expected:invalid,
error message:"Email must contain @"

        8.Multiple@symbols
Input:"user@@example.com"
Expected:invalid
   
9.
Missing domain
Input:"user@"
Expected:invalid
   
10.
Missing local
part
Input:"@example.com"
Expected:invalid
    
11.

Invalid domain(no TLD)

Input:"user@example"
Expected:

invalid(or configurable -discuss!)
    
12.
Space in
email
Input:"user name@example.com"
Expected:invalid
    
13.
Consecutive dots
Input:"user..name@example.com"
Expected:invalid
    
14.Starts/
ends with
dot
Input:".user@example.com"or "user.@example.com"
Expected:invalid

// 🔍 EDGE CASES (Boundary & Special)
15.
Null email
Input:null
Expected:

invalid(throw ValidationException)
    
16.
Empty string
Input:""
Expected:invalid
    
17.
Whitespace only
Input:"   "
Expected:

invalid(after trim)
    
18.Very

long email(254+chars)

Input:"a"*300+"@example.com"
Expected:

invalid(max 254per RFC 5321)
    
19.
Case insensitivity
check
Input:"User@EXAMPLE.COM"
Expected:

valid(should be case-insensitive)
    
20.

International domain(IDN)

Input:"user@münchen.de"(UTF-8)
Expected:?(
depends on
requirements -discuss!)
```

**Test Approach**:

```java

@Test
void validEmail_SimpleFormat() {
    assertTrue(emailValidator.isValid("user@example.com"));
}

@Test
void invalidEmail_MissingAt() {
    assertFalse(emailValidator.isValid("userexample.com"));
}

@Test
void invalidEmail_Null() {
    assertThrows(IllegalArgumentException.class,
            () -> emailValidator.isValid(null));
}

@Test
@ParameterizedTest
@ValueSource(strings = {"user@example.com", "user+tag@example.com", "user.name@example.co.uk"})
void validEmails_AllFormats(String email) {
    assertTrue(emailValidator.isValid(email));
}

@Test
@ParameterizedTest
@ValueSource(strings = {"userexample.com", "@example.com", "user@", "a@@b@c.com"})
void invalidEmails_AllFormats(String email) {
    assertFalse(emailValidator.isValid(email));
}
```

---

### Task 6.1b: PhoneNumberValidatorTest

**Target Coverage**: 100%

**Italian Phone Number Format**:

- ✅ +39 + 10 digits (e.g., +393123456789)
- ✅ 3 + 9 digits (e.g., 3123456789) - mobile only
- ✅ With spaces (optional): +39 312 345 6789
- ❌ 02 + 8 digits (landline) - only mobile required?
- ❌ With dashes: +39-312-345-6789 (optional?)

**Clarification Needed**: Confirm exact Italian phone requirements!

#### Test Cases Detail

```java
// ✅ VALID PHONE NUMBERS
1.+39with 10digits
Input:"+393123456789"
Expected:valid
   
2. 3with 9

digits(mobile)

Input:"3123456789"
Expected:valid
   
3.+39
with spaces
Input:"+39 312 345 6789"
Expected:

valid(after normalization)
   
4. 3
with spaces
Input:"3 312 345 6789"
Expected:

valid(after normalization)

// ❌ INVALID PHONE NUMBERS
5.

Wrong prefix(2instead of 3 for mobile)

Input:"2123456789"
Expected:invalid
   
6.
Wrong international
prefix
Input:"+41123456789"(Switzerland)
Expected:invalid
   
7.
Too few
digits
Input:"+3912345678"(only 9digits)
Expected:invalid,error:"Phone must have exactly 10 digits after +39"

        8.
Too many
digits
Input:"+391234567890"(11digits)
Expected:invalid
   
9.Non-

numeric characters(except +, space, dash)

Input:"+39-ABC-DEFGHI"
Expected:invalid
   
10.
Empty prefix
Input:"123456789"(no 3or +39)
Expected:invalid

// 🔍 EDGE CASES
11.
Null phone
Input:null
Expected:

invalid(throw exception)
    
12.
Empty string
Input:""
Expected:invalid
    
13.
Only spaces
Input:"          "
Expected:

invalid(after trim)
    
14.+39
without digits
Input:"+39"
Expected:invalid
    
15.Multiple +39prefixes
Input:"+39+393123456789"
Expected:invalid
    
16.
Special characters
Input:"+39(312)345-6789"
Expected:?(
depends on
format flexibility -discuss!)

        17.
Leading zeros
in digits
Input:"+390123456789"
Expected:?(
technically valid
but uncommon -discuss!)
```

**Test Approach**:

```java

@Test
void validPhone_PlusFormat() {
    assertTrue(phoneValidator.isValid("+393123456789"));
}

@Test
void validPhone_MobileFormat() {
    assertTrue(phoneValidator.isValid("3123456789"));
}

@Test
void invalidPhone_WrongPrefix() {
    assertFalse(phoneValidator.isValid("2123456789"));
}

@Test
void invalidPhone_TooFewDigits() {
    assertFalse(phoneValidator.isValid("+3912345678"));
}

@Test
@ParameterizedTest
@ValueSource(strings = {"+393123456789", "3123456789", "+39 312 345 6789"})
void validPhones_AllFormats(String phone) {
    assertTrue(phoneValidator.isValid(phone));
}

@Test
void invalidPhone_Null() {
    assertThrows(IllegalArgumentException.class,
            () -> phoneValidator.isValid(null));
}
```

---

## PHASE 4: Repository & Service Tests

### Task 6.2: UserRepositorySearchTest

**Target Coverage**: 90% (critical for functionality)

#### Test Cases Detail

```java
// ========== PAGINATION TESTS ==========

1.First page
with default size
Setup:
Database has 25users
Input:pageNumber=0,pageSize=10
Expected:returns 10

users(indices 0-9)
   
2.
Second page
Setup:
Database has 25users
Input:pageNumber=1,pageSize=10
Expected:returns 10

users(indices 10-19)
   
3.
Last page
with partial
results
Setup:
Database has 25users
Input:pageNumber=2,pageSize=10
Expected:returns 5

users(indices 20-24)
   
4.
Page beyond
available
Setup:
Database has 25users
Input:pageNumber=10,pageSize=10
Expected:
returns empty

list(not error)
   
5.
Page zero
with size
zero
Setup:
Any database
Input:pageNumber=0,pageSize=0
Expected:
returns empty

list(edge case)

6.
Negative page
number
Setup:
Database has 25users
Input:pageNumber=-1,pageSize=10
Expected:

invalid(throw exception or default to 0)

// ========== SORTING TESTS ==========

7.
Sort by
firstName ASC
Setup:Users ["Zoe","Alice","Bob"]
Input:sortBy=FIRST_NAME,sortDirection=ASC
Expected:["Alice","Bob","Zoe"]

        8.
Sort by
firstName DESC
Setup:Users ["Zoe","Alice","Bob"]
Input:sortBy=FIRST_NAME,sortDirection=DESC
Expected:["Zoe","Bob","Alice"]

        9.
Sort by
email ASC
Setup:
Users with
emails ["z@...","a@...","b@..."]
Input:sortBy=EMAIL,sortDirection=ASC
Expected:["a@...","b@...","z@..."]

        10.
Sort by

lastName(case-insensitive)

Setup:Users ["smith","Smith","SMITH"]
Input:sortBy=LAST_NAME,sortDirection=ASC
Expected:
All three
returned,
properly sorted

11.
Invalid sort
field
Setup:
Any database
Input:sortBy=INVALID_FIELD
Expected:throw
exception or
default to firstName

// ========== SEARCH (FILTER) TESTS ==========

12.Search case-
insensitive by
firstName
Setup:Users ["Alice","alice","ALICE"]
Input:searchTerm="alice"
Expected:all 3
users returned
    
13.
Search partial
match in
firstName
Setup:Users ["Alice","Alicia","Bob"]
Input:searchTerm="ali"
Expected:["Alice","Alicia"]

        14.
Search no
results
Setup:Users ["Alice","Bob"]
Input:searchTerm="zzz"
Expected:
empty list
    
15.
Search by
email
Setup:
Users with
emails ["alice@example.com","bob@example.com"]
Input:searchTerm="alice"
Expected:
user with
alice email
    
16.
Search by
lastName
Setup:
Users with
lastNames ["Smith","Jones"]
Input:searchTerm="smith"
Expected:
user with
Smith lastName
    
17.
Search special
characters in
name
Setup:Users ["O'Neil","Smith-Jones"]
Input:searchTerm="o'n"
Expected:"O'Neil"found

18.
Search whitespace
handling
Setup:Users ["Alice Smith"]
Input:searchTerm="  alice  "(
with extra
spaces)
Expected:
found after

trim()

19.
Empty search
term
Setup:
Database has 25users
Input:searchTerm=""
Expected:
all users
returned or throw
validation error?

        20.
Search with
pagination combined
Setup:100
users matching "ali",
database has 250total
Input:searchTerm="ali",pageNumber=0,pageSize=10
Expected:10

results(first page of matched results)

// ========== COMBINED: SEARCH + SORT + PAGINATE ==========

21.
Complex query:search,sort,paginate
Setup:50
users total, 30match "john",
sorted by
lastName
Input:searchTerm="john",pageNumber=1,pageSize=10,
sortBy=LAST_NAME,sortDirection=ASC
Expected:users 10-19
of matched 30,
sorted by
lastName

22.
No results
after search
Setup:50
users total, 0match "zzz"
Input:searchTerm="zzz",pageNumber=0,pageSize=10
Expected:
empty list

// ========== EDGE CASES ==========

23.
Empty database
Setup:
No users
Input:pageNumber=0,pageSize=10
Expected:
empty list
    
24.
Null search
term
Setup:25users
Input:searchTerm=null
Expected:throw
exception or
handle gracefully
    
25.
Very large
page size
Setup:25users
Input:pageSize=1000
Expected:
returns all 25

users(not throw error)
    
26.
Database concurrent
modification
Setup:
        Start search, add
user during
search
Input:
search query
Expected:

consistent result(snapshot or safe iteration)
    
27.
User with null firstName
Setup:
User with
firstName=null,lastName="Smith"
Input:searchTerm="smith"
Expected:
found by
lastName
```

**Mock Strategy**:

```java
// Use real FakeDatabase for integration-level testing
// No mocks needed since it's in-memory

@BeforeEach
void setup() {
    userRepository = new UserRepository();
    // Clear and populate with test data
    FakeDatabase.TABLE_USER.clear();
    populateTestData();
}

@Test
void search_PaginationFirstPage() {
    SearchCriteria criteria = new SearchCriteria(
            searchTerm = null,
            pageNumber = 0,
            pageSize = 10,
            sortField = FIRST_NAME,
            sortDirection = ASC
    );

    GenericPagedResult result = userRepository.search(criteria);

    assertEquals(10, result.getItems().size());
    assertEquals(25, result.getTotalCount());
    assertEquals(0, result.getPageNumber());
}
```

---

## PHASE 5: JWT & Security Tests

### Task 6.3a: JwtTokenProviderTest

**Target Coverage**: 95% (security-critical)

#### Test Cases Detail

```java
// ========== TOKEN GENERATION ==========

1.Generate valid
token
Input:username="alice",role="USER"
Expected:
JWT string
generated,non-null

        2.
Token contains
correct claims
Input:username="alice",role="USER"
Expected:
token decoding
        shows username = "alice", role = "USER"
   
3.
Token has
correct issuer
Input:
Generate token
Expected:issuer ="high-card-app"

        4.
Token has
expiration
Input:
Generate token
Expected:
exp claim
exists and
is ~3600
seconds from
now
   
5.
Token has
issuedAt
Input:
Generate token
Expected:
iat claim
exists and
is current
time

// ========== TOKEN VALIDATION ==========

6.
Validate correct
token
Input:
Valid token
just generated
Expected:
validation returns true

        7.
Validate with
correct signature
Input:
Token signed
with correct
secret
Expected:
signature validation
passes
   
8.
Validate wrong
secret
Input:
Token signed
with different
secret
Expected:
validation returns false,
signature error
   
9.
Validate expired
token
Setup:
Generate token
with 0

expiration(expired immediately)

Input:
Expired token
Expected:
validation returns false,
expiration error
   
10.
Validate nearly

expired token(1second left)

Input:
Token expiring
in 1second
Expected:
validation returns true(
should pass)

        11.
Validate just-

expired token(1second past)

Input:
Token expired 1
second ago
Expected:
validation returns false

        12.
Validate token
with wrong
issuer
Setup:
Create token
with issuer = "wrong-issuer"
Input:
Token with
wrong issuer
Expected:
validation returns false,
issuer mismatch

13.
Validate malformed
token
Input:"not.a.valid.jwt"
Expected:
validation returns false,
parsing error
    
14.
Validate token
missing signature
Input:"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGljZSJ9"(2
parts only)
Expected:
validation returns false

        15.Validate null token
Input:null
Expected:throw exception,
not just false

        16.
Validate empty
string
Input:""
Expected:throw
exception or return false

// ========== CLAIM EXTRACTION ==========

        17.
Extract username
from valid
token
Input:Token for user "alice"
Expected:"alice"

        18.
Extract role
from valid
token
Input:
Token with
role "ADMIN"
Expected:"ADMIN"

        19.
Extract from
invalid token
Input:
Malformed token
Expected:throw
exception or return null

        20.
Extract missing
claim
Setup:
Token without
certain claim
Input:
Extract that
claim
Expected:throw
exception or return default value

// ========== EDGE CASES ==========

21.
Generate token
with special
characters in
username
Input:username="alice.smith+test@example.com"
Expected:
username correctly
encoded in
token
    
22.
Generate token
with very

long username(255+chars)

Input:Very
long username
Expected:
encoded or throw
validation error
    
23.
Token at

expiration boundary(exactly now)

Setup:
Token expiring
exactly at
current time
Input:
Validate token
Expected:?(discuss:
should it
pass or
fail at
exact boundary)

        24.
Multiple tokens
generated in
same millisecond
Input:Generate 10
tokens simultaneously
Expected:
all different

tokens(unique iat/jti)
    
25.
Validate token
after server
secret rotation
Setup:
Generate token
with secret
A,
rotate to
secret B
Input:
Validate with new
secret B
Expected:

validation fails(old token invalid)

// ========== CONFIGURATION TESTS ==========

26.
Test expiration

duration(1hour)

Setup:
        Generate token, wait
until edge case times
Input:
Token at 59minutes 59seconds
Expected:valid
    
27.
Test expiration

duration(1hour +1second)

Input:
Token at 61minutes
Expected:invalid
```

**Test Implementation**:

```java

@RunWith(SpringRunner.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtProvider;

    @Mock
    private JwtProperties jwtProperties;

    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setup() {
        given(jwtProperties.getSecret()).willReturn("test-secret-key-1234567890");
        given(jwtProperties.getExpiration()).willReturn(3600000L);
        given(jwtProperties.getIssuer()).willReturn("high-card-app");

        validToken = jwtProvider.generateToken("alice", "USER");
    }

    @Test
    void generateToken_CreatesValidJwt() {
        String token = jwtProvider.generateToken("alice", "USER");
        assertNotNull(token);
        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
    void validateToken_WithCorrectToken() {
        assertTrue(jwtProvider.validateToken(validToken));
    }

    @Test
    void validateToken_WithExpiredToken() {
        String expiredToken = createExpiredToken();
        assertFalse(jwtProvider.validateToken(expiredToken));
    }

    @Test
    void extractUsername_ReturnsCorrectValue() {
        String token = jwtProvider.generateToken("alice", "USER");
        assertEquals("alice", jwtProvider.extractUsername(token));
    }

    @Test
    void validateToken_WithNullToken() {
        assertThrows(IllegalArgumentException.class,
                () -> jwtProvider.validateToken(null));
    }
}
```

---

### Task 6.3b: AuthenticationIntegrationTest

**Target Coverage**: 80% (integration-focused)

#### Test Cases Detail

```java
// ========== END-TO-END LOGIN FLOW ==========

1.Login with
valid credentials
Input:username="admin",password="admin123"
Expected:HTTP 200,
token in
response,
token valid
   
2.
Login with
invalid username
Input:username="unknown",password="admin123"
Expected:HTTP 401,
no token
   
3.
Login with
invalid password
Input:username="admin",password="wrong"
Expected:HTTP 401,
no token
   
4.
Login with
both invalid
Input:username="unknown",password="wrong"
Expected:HTTP 401,
no token

// ========== TOKEN USAGE IN REQUESTS ==========

5.
Request with
valid token
Setup:
Login and
get token
Input:GET /user/v1/
user with
Authorization:Bearer<token>
Expected:HTTP 200,
request succeeds
   
6.
Request with
invalid token
Input:GET /user/v1/
user with
Authorization:
Bearer invalid-token
Expected:HTTP 401,
access denied
   
7.
Request with
expired token
Setup:
Use token
that's been made to expire
Input:GET /user/v1/
user with
expired token
Expected:HTTP 401,
token expired
   
8.
Request with
missing token
Input:GET /user/v1/
user with
no Authorization
header
Expected:HTTP 401(or 403
depending on
security config)

        9.
Request with
wrong Bearer
format
Input:Authorization:Token <token> (
not Bearer)
Expected:HTTP 401

        10.
Request with
Bearer but
no token
Input:Authorization:

Bearer(space but no actual token)

Expected:HTTP 401

// ========== PROTECTED ENDPOINTS ==========

        11.POST /user/v1/
user without

token(add user)

Input:AddUserRequest,
no auth
Expected:HTTP 401

        12.POST /user/v1/
user with

token(add user)

Input:AddUserRequest,
valid token
Expected:HTTP 200,
user added
    
13.POST /
user with
valid token
but invalid
data
Input:
        Invalid email, valid
token
Expected:HTTP 200(
app standard),status.code=400
in body
    
14.GET /

user(search) without token

Input:GetUsersRequest,
no auth
Expected:HTTP 401

// ========== EDGE CASES ==========

        15.
Multiple authorization
headers
Input:
Two Authorization
headers
Expected:?(
depends on
implementation)

        16.
Token with null
username claim
Setup:
Manually create
token without
username
Input:
Use in
request
Expected:HTTP 401
or handle
gracefully
    
17.
Concurrent requests
with same
token
Setup:Make 10
parallel requests
Input:
All with
same valid
token
Expected:

All succeed(token not consumed)
    
18.
Token reuse

after logout(future feature)

Setup:
If logout
implemented
Input:
Use old
token
Expected:
fail after
logout
```

**Integration Test Setup**:

```java

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtProvider;

    private String validToken;

    @BeforeEach
    void setup() {
        validToken = jwtProvider.generateToken("admin", "ADMIN");
    }

    @Test
    void loginWithValidCredentials() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void requestWithValidToken() throws Exception {
        mockMvc.perform(get("/user/v1/user")
                        .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithoutToken() throws Exception {
        mockMvc.perform(get("/user/v1/user"))
                .andExpect(status().isUnauthorized());
    }
}
```

---

## Coverage Target & Measurement

### JaCoCo Configuration

Add to pom.xml:

```xml

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Run: `mvn clean test jacoco:report`
Report location: `target/site/jacoco/index.html`

---

## Test Data Fixtures

### TestDataBuilder Pattern

```java

@Component
public class TestDataBuilder {

    public static User buildUser(String firstName, String lastName) {
        return User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(firstName + "." + lastName + "@example.com")
                .phoneNumber("+393123456789")
                .build();
    }

    public static AddUserRequest buildAddUserRequest() {
        AddUserRequest request = new AddUserRequest();
        request.setFirstName("Alice");
        request.setLastName("Smith");
        request.setEmail("alice@example.com");
        request.setPhoneNumber("+393123456789");
        return request;
    }

    public static List<User> buildUserList(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> buildUser("User" + i, "Last" + i))
                .collect(Collectors.toList());
    }
}
```

---

## Summary: Test Case Count

| Area                       | Test Cases | Priority |
|----------------------------|------------|----------|
| Email Validation           | 20         | CRITICAL |
| Phone Validation           | 20         | CRITICAL |
| Repository Search          | 27         | HIGH     |
| JWT Provider               | 26         | CRITICAL |
| Authentication Integration | 18         | HIGH     |
| **Total Test Cases**       | **111**    | -        |

**Estimated Effort**: 8-10 hours for all tests
**Expected Coverage**: 80-90%

---

## Key Discussion Points for Refinement

❓ **Questions to Clarify**:

1. **Italian Phone Number Format**
    - Accept only mobile (3xx)? Or also landlines (02, 06)?
    - Allow spaces/dashes for formatting?
    - Allow international +39 and domestic 3 interchangeably?

2. **Email Validation**
    - Minimum domain length (e.g., .co.uk vs just .c)?
    - Allow IDN (international domain names)?
    - Require specific TLD validation?

3. **Pagination Edge Cases**
    - What if page > available pages? Return empty or error?
    - What if pageSize=0? Error or empty?

4. **Search Empty Term**
    - Empty searchTerm="" means return all? Or error?
    - Should it be @NotBlank or @NotNull?

5. **Token Expiration Boundary**
    - Token at expiration instant: pass or fail?
    - Clock skew tolerance? (e.g., 30 seconds)

6. **Concurrent Access**
    - FakeDatabase thread-safe? Should add synchronization?
    - Test concurrent login/search operations?

7. **Test Database Setup**
    - Populate with real-world data? (addresses, phone formats)
    - Or minimal test data?

8. **Mock Strategy**
    - Should we mock FakeDatabase or test with real data?
    - Should we mock JWT provider in controller tests?

---

This detailed strategy replaces the vague test section in plan.md with concrete, actionable test cases covering edge
cases, security, and business logic validation.


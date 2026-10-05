package it.sara.demo.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    // HMAC-SHA256 requires a key >= 256 bits (32 bytes)
    private static final String SECRET = "test-secret-key-minimum-256-bits-for-hmac-sha256-aaaa";
    private static final String ISSUER  = "test-idp";
    private static final long EXPIRATION_MS = 3_600_000L; // 1 hour

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = buildProvider(SECRET, EXPIRATION_MS, ISSUER, List.of("USER", "ADMIN"));
    }

    // --- generate + validate ---

    @Test
    void generateAndValidate_userRole_claimsContainCorrectSubjectAndRole() {
        String token = provider.generateToken("mario", "USER");
        Optional<Claims> result = provider.validateAndExtractClaims(token);

        assertTrue(result.isPresent());
        assertEquals("mario", result.get().getSubject());
        assertEquals("USER", result.get().get("role", String.class));
    }

    @Test
    void generateAndValidate_adminRole_returnsPresent() {
        String token = provider.generateToken("admin", "ADMIN");
        assertTrue(provider.validateAndExtractClaims(token).isPresent());
    }

    // --- invalid role (policy check) ---

    @Test
    void validateToken_roleNotInAllowedList_returnsEmpty() {
        String token = provider.generateToken("mario", "SUPERADMIN");
        assertFalse(provider.validateAndExtractClaims(token).isPresent());
    }

    // --- tampered / malformed token ---

    @Test
    void validateToken_tamperedSignature_returnsEmpty() {
        String token = provider.generateToken("mario", "USER");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertFalse(provider.validateAndExtractClaims(tampered).isPresent());
    }

    @Test
    void validateToken_malformedToken_returnsEmpty() {
        assertFalse(provider.validateAndExtractClaims("not.a.jwt.token").isPresent());
    }

    @Test
    void validateToken_emptyString_returnsEmpty() {
        assertFalse(provider.validateAndExtractClaims("").isPresent());
    }

    // --- wrong issuer ---

    @Test
    void validateToken_wrongIssuer_returnsEmpty() {
        JwtTokenProvider otherIssuerProvider =
                buildProvider(SECRET, EXPIRATION_MS, "other-idp", List.of("USER"));

        String foreignToken = otherIssuerProvider.generateToken("mario", "USER");
        // Our provider expects issuer = test-idp → must reject
        assertFalse(provider.validateAndExtractClaims(foreignToken).isPresent());
    }

    // --- expired token ---

    @Test
    void validateToken_expiredToken_returnsEmpty() {
        JwtTokenProvider expiredProvider =
                buildProvider(SECRET, -1000L, ISSUER, List.of("USER"));

        String expiredToken = expiredProvider.generateToken("mario", "USER");
        assertFalse(provider.validateAndExtractClaims(expiredToken).isPresent());
    }

    // --- getExpiration ---

    @Test
    void getExpiration_returnsConfiguredValue() {
        assertEquals(EXPIRATION_MS, provider.getExpiration());
    }

    // --- helper ---

    private static JwtTokenProvider buildProvider(String secret, long expiration,
                                                   String issuer, List<String> allowedRoles) {
        JwtProperties props = new JwtProperties();
        props.setSecret(secret);
        props.setExpiration(expiration);
        props.setIssuer(issuer);
        props.setAllowedRoles(allowedRoles);

        JwtTokenProvider p = new JwtTokenProvider(props);
        ReflectionTestUtils.invokeMethod(p, "init");
        return p;
    }
}
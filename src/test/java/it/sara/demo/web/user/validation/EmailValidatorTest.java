package it.sara.demo.web.user.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    // --- valid inputs ---

    @Test
    void simpleEmail_returnsTrue() {
        assertTrue(validator.isValid("user@example.com", null));
    }

    @Test
    void emailWithSubdomain_returnsTrue() {
        assertTrue(validator.isValid("user@mail.example.co.uk", null));
    }

    @Test
    void emailWithPlusTag_returnsTrue() {
        assertTrue(validator.isValid("user.name+tag@example.org", null));
    }

    @Test
    void emailWithDashAndUnderscore_returnsTrue() {
        assertTrue(validator.isValid("first_last-name@my-domain.it", null));
    }

    // --- null / blank ---

    @Test
    void nullEmail_returnsFalse() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void emptyEmail_returnsFalse() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    void blankEmail_returnsFalse() {
        assertFalse(validator.isValid("   ", null));
    }

    // --- malformed ---

    @Test
    void missingAtSign_returnsFalse() {
        assertFalse(validator.isValid("userexample.com", null));
    }

    @Test
    void missingDomain_returnsFalse() {
        assertFalse(validator.isValid("user@", null));
    }

    @Test
    void missingTld_returnsFalse() {
        assertFalse(validator.isValid("user@example", null));
    }

    @Test
    void singleCharTld_returnsFalse() {
        assertFalse(validator.isValid("user@example.c", null));
    }

    @Test
    void emailWithSpaceInLocal_returnsFalse() {
        assertFalse(validator.isValid("user name@example.com", null));
    }

    @Test
    void emailWithDoubleAt_returnsFalse() {
        assertFalse(validator.isValid("user@@example.com", null));
    }
}
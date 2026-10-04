package it.sara.demo.web.user.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItalianPhoneNumberValidatorTest {

    private ItalianPhoneNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ItalianPhoneNumberValidator();
    }

    // --- valid inputs ---

    @Test
    void mobileWithoutPrefix_returnsTrue() {
        assertTrue(validator.isValid("3331234567", null));
    }

    @Test
    void mobileWithCountryCode_returnsTrue() {
        assertTrue(validator.isValid("+393331234567", null));
    }

    @Test
    void landlineWithCountryCode_9digits_returnsTrue() {
        assertTrue(validator.isValid("+390212345678", null));
    }

    @Test
    void landlineWithCountryCode_8digits_returnsTrue() {
        assertTrue(validator.isValid("+39021234567", null));
    }

    // --- null / blank ---

    @Test
    void nullPhone_returnsFalse() {
        assertFalse(validator.isValid(null, null));
    }

    @Test
    void emptyPhone_returnsFalse() {
        assertFalse(validator.isValid("", null));
    }

    @Test
    void blankPhone_returnsFalse() {
        assertFalse(validator.isValid("   ", null));
    }

    // --- wrong prefix / format ---

    @Test
    void foreignPrefix_returnsFalse() {
        assertFalse(validator.isValid("+441234567890", null));
    }

    @Test
    void mobileWithoutLeading3_returnsFalse() {
        assertFalse(validator.isValid("6331234567", null));
    }

    @Test
    void tooShort_returnsFalse() {
        assertFalse(validator.isValid("33312", null));
    }

    @Test
    void tooLong_returnsFalse() {
        assertFalse(validator.isValid("333123456789", null));
    }

    @Test
    void alphaChars_returnsFalse() {
        assertFalse(validator.isValid("333123456a", null));
    }

    @Test
    void spaces_returnsFalse() {
        assertFalse(validator.isValid("333 123 4567", null));
    }
}
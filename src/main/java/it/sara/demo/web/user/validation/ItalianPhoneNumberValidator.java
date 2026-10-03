package it.sara.demo.web.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Validates that a string is a valid Italian phone number.
 * <p>
 * Accepted patterns:
 * <ul>
 *   <li>{@code 3[0-9]{9}} — mobile without prefix (10 digits)</li>
 *   <li>{@code \+393[0-9]{9}} — mobile with country code (13 chars)</li>
 *   <li>{@code \+390[0-9]{8,9}} — landline with country code (12–13 chars)</li>
 * </ul>
 */
@Slf4j
public class ItalianPhoneNumberValidator implements ConstraintValidator<ValidItalianPhoneNumber, String> {

    private static final String PHONE_REGEX =
            "^(\\+39)?(3[0-9]{9}|0[0-9]{8,9})$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.debug("Validating Italian phone number for value [{}]", value);
        if (!StringUtils.hasText(value)) {
            log.debug("Phone validation failed — value is blank or null");
            return false;
        }
        return value.matches(PHONE_REGEX);
    }
}
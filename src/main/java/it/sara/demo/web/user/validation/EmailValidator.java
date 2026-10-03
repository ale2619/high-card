package it.sara.demo.web.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * Validates that a string is a well-formed email address.
 * Accepted format: {@code local@domain.tld}
 * <ul>
 *   <li>Local part: alphanumeric characters plus {@code . _ % + -}</li>
 *   <li>Domain: alphanumeric characters plus {@code . -}</li>
 *   <li>TLD: at least 2 alphabetic characters</li>
 * </ul>
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        return value.matches(EMAIL_REGEX);
    }
}
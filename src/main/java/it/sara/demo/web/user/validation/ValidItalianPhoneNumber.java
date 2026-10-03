package it.sara.demo.web.user.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that the annotated field is a valid Italian phone number.
 * <p>
 * Accepted formats:
 * <ul>
 *   <li>Mobile without prefix: {@code 3XXXXXXXXX} (10 digits, starting with 3)</li>
 *   <li>Mobile with country code: {@code +393XXXXXXXXX} (13 chars)</li>
 *   <li>Landline with country code: {@code +390XXXXXXXXX} (12–13 chars)</li>
 * </ul>
 */
@Documented
@Constraint(validatedBy = ItalianPhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidItalianPhoneNumber {

    String message() default "Invalid Italian phone number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
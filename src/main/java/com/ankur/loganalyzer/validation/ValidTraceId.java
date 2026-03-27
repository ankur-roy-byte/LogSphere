package com.ankur.loganalyzer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

/**
 * Validates that a string is a valid UUID or trace ID format.
 *
 * Accepts:
 * - Standard UUID format (8-4-4-4-12 hexadecimal)
 * - 32-character hexadecimal strings
 * - Alphanumeric strings (for custom trace IDs)
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidTraceIdValidator.class)
@Documented
public @interface ValidTraceId {
    String message() default "Invalid trace ID format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class ValidTraceIdValidator implements ConstraintValidator<ValidTraceId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        // Try UUID format
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            // Not a UUID, try other formats
        }

        // Try 32-char hex (UUID without hyphens)
        if (value.length() == 32 && value.matches("[0-9a-fA-F]{32}")) {
            return true;
        }

        // Allow alphanumeric trace IDs (3-128 chars)
        return value.length() >= 3 && value.length() <= 128 && value.matches("[a-zA-Z0-9_-]+");
    }
}

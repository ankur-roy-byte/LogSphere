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

/**
 * Validates that a string is a valid log level.
 *
 * Accepted values: TRACE, DEBUG, INFO, WARN, ERROR, FATAL
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidLogLevelValidator.class)
@Documented
public @interface ValidLogLevel {
    String message() default "Invalid log level. Must be one of: TRACE, DEBUG, INFO, WARN, ERROR, FATAL";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class ValidLogLevelValidator implements ConstraintValidator<ValidLogLevel, String> {
    private static final String[] VALID_LEVELS = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String upperValue = value.toUpperCase();
        for (String level : VALID_LEVELS) {
            if (level.equals(upperValue)) {
                return true;
            }
        }
        return false;
    }
}

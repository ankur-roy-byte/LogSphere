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
 * Validates that a string is a supported log format.
 *
 * Supported formats: json, regex, stacktrace, text
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidLogFormatValidator.class)
@Documented
public @interface ValidLogFormat {
    String message() default "Invalid log format. Supported formats: json, regex, stacktrace, text";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class ValidLogFormatValidator implements ConstraintValidator<ValidLogFormat, String> {
    private static final String[] VALID_FORMATS = {"json", "regex", "stacktrace", "text"};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }

        String lowerValue = value.toLowerCase();
        for (String format : VALID_FORMATS) {
            if (format.equals(lowerValue)) {
                return true;
            }
        }
        return false;
    }
}

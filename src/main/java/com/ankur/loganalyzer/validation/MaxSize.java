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
 * Validates that string content size is within acceptable limits.
 *
 * Default max size: 10MB (10,485,760 bytes)
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MaxSizeValidator.class)
@Documented
public @interface MaxSize {
    long value() default 10485760; // 10MB
    String message() default "Content size exceeds maximum allowed size";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

class MaxSizeValidator implements ConstraintValidator<MaxSize, String> {
    private long maxSize;

    @Override
    public void initialize(MaxSize annotation) {
        maxSize = annotation.value();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        long sizeInBytes = value.getBytes().length;
        if (sizeInBytes > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Content size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            sizeInBytes, maxSize))
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}

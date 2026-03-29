package com.ankur.loganalyzer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting to controller methods.
 *
 * Limits the number of requests from a single IP address within
 * a specified time window.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * Maximum number of requests allowed per time window
     */
    int value() default 100;

    /**
     * Time window in seconds
     */
    int timeWindow() default 60;

    /**
     * Description of the rate limit
     */
    String description() default "";
}

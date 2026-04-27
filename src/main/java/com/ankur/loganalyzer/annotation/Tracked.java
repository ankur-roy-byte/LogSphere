package com.ankur.loganalyzer.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a service method for automatic metrics collection via AOP.
 * The intercepting aspect records timing and success/failure counters
 * into MetricsCollectorService without any changes to the method body.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Tracked {

    MetricCategory category();

    String operation() default "";
}

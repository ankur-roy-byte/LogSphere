package com.ankur.loganalyzer.interceptor;

import com.ankur.loganalyzer.annotation.RateLimit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting interceptor using token bucket algorithm.
 *
 * Prevents API abuse by limiting requests per IP address.
 * Uses a simple token bucket implementation without external dependencies.
 */
@Component
@Slf4j
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                            Object handler) throws Exception {
        // Check if handler has RateLimit annotation
        if (!(handler instanceof org.springframework.web.method.HandlerMethod)) {
            return true;
        }

        org.springframework.web.method.HandlerMethod handlerMethod =
                (org.springframework.web.method.HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true; // No rate limiting
        }

        String clientIp = getClientIp(request);
        String key = clientIp + ":" + request.getRequestURI();

        TokenBucket tokenBucket = clientBuckets.computeIfAbsent(key,
                k -> new TokenBucket(rateLimit.value(), rateLimit.timeWindow()));

        int remainingTokens = tokenBucket.tryConsume();

        if (remainingTokens >= 0) {
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));
            return true;
        }

        // Rate limit exceeded
        long waitTime = tokenBucket.getWaitTime();
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitTime));
        response.getWriter().write("{\"error\":\"Rate limit exceeded. Retry after " +
                waitTime + " seconds\"}");
        response.setContentType("application/json");

        log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, request.getRequestURI());
        return false;
    }

    /**
     * Extract client IP from request, considering proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Simple token bucket implementation for rate limiting
     */
    private static class TokenBucket {
        private final int capacity;
        private final long refillIntervalNanos;
        private final AtomicLong tokensRemaining;
        private final AtomicLong lastRefillTime;

        TokenBucket(int capacity, int timeWindowSeconds) {
            this.capacity = capacity;
            this.refillIntervalNanos = (long) timeWindowSeconds * 1_000_000_000L;
            this.tokensRemaining = new AtomicLong(capacity);
            this.lastRefillTime = new AtomicLong(System.nanoTime());
        }

        synchronized int tryConsume() {
            refill();

            if (tokensRemaining.get() > 0) {
                tokensRemaining.decrementAndGet();
                return (int) tokensRemaining.get();
            }

            return -1; // Rate limit exceeded
        }

        synchronized long getWaitTime() {
            long elapsedNanos = System.nanoTime() - lastRefillTime.get();
            long waitNanos = refillIntervalNanos - elapsedNanos;
            return Math.max(1, (waitNanos + 999_999_999L) / 1_000_000_000L); // Round up to seconds
        }

        private void refill() {
            long now = System.nanoTime();
            long timeSinceLastRefill = now - lastRefillTime.get();

            if (timeSinceLastRefill >= refillIntervalNanos) {
                tokensRemaining.set(capacity);
                lastRefillTime.set(now);
            }
        }
    }
}


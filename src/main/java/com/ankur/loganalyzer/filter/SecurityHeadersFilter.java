package com.ankur.loganalyzer.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security headers filter for HTTP response protection.
 *
 * Adds security headers to prevent common web vulnerabilities:
 * - XSS (Cross-Site Scripting)
 * - Clickjacking
 * - MIME type sniffing
 * - Insecure content loading
 */
@Component
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        // X-Frame-Options: Prevent clickjacking by restricting frame embedding
        response.setHeader("X-Frame-Options", "DENY");

        // X-Content-Type-Options: Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // X-XSS-Protection: Enable XSS filter in browsers
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Content-Security-Policy: Prevent inline scripts and restrict resources
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self' https:; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self'");

        // Referrer-Policy: Control referrer information
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions-Policy: Control browser features
        response.setHeader("Permissions-Policy",
                "accelerometer=(), " +
                        "camera=(), " +
                        "geolocation=(), " +
                        "gyroscope=(), " +
                        "magnetometer=(), " +
                        "microphone=(), " +
                        "payment=(), " +
                        "usb=()");

        // Strict-Transport-Security (HSTS): Force HTTPS
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");

        // Cache-Control: Prevent sensitive data caching
        if (request.getRequestURI().contains("/api/") &&
                (request.getMethod().equals("POST") || request.getMethod().equals("PUT") ||
                        request.getMethod().equals("DELETE"))) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        filterChain.doFilter(request, response);
    }
}

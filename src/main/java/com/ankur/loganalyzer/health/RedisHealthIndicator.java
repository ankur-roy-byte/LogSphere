package com.ankur.loganalyzer.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Health checker for Redis connectivity.
 *
 * Verifies that the application can successfully connect to the Redis cache.
 * Reports Redis server information and connection status.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Check Redis health and return status.
     *
     * @return Map containing health status and details
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        try {
            var connection = redisConnectionFactory.getConnection();
            if (connection != null) {
                var pong = connection.ping();
                connection.close();

                result.put("status", "UP");
                result.put("message", "Redis is available");
                result.put("ping", pong);
            } else {
                result.put("status", "DOWN");
                result.put("message", "Redis connection unavailable");
            }
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            result.put("status", "DOWN");
            result.put("message", "Redis unavailable");
            result.put("error", e.getMessage());
        }
        return result;
    }
}


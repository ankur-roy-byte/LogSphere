package com.ankur.loganalyzer.cache;

import lombok.Getter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache statistics tracker for monitoring cache performance.
 *
 * Tracks hits, misses, and evictions for performance monitoring
 * and tuning.
 */
@Component
@Getter
@ToString
public class CacheStats {

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    private final AtomicLong puts = new AtomicLong(0);

    /**
     * Record a cache hit
     */
    public void recordHit() {
        hits.incrementAndGet();
    }

    /**
     * Record a cache miss
     */
    public void recordMiss() {
        misses.incrementAndGet();
    }

    /**
     * Record a cache eviction
     */
    public void recordEviction() {
        evictions.incrementAndGet();
    }

    /**
     * Record a cache put
     */
    public void recordPut() {
        puts.incrementAndGet();
    }

    /**
     * Get cache hit rate percentage
     */
    public double getHitRate() {
        long total = hits.get() + misses.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) hits.get() / total * 100;
    }

    /**
     * Get total cache operations
     */
    public long getTotalOperations() {
        return hits.get() + misses.get();
    }

    /**
     * Reset cache statistics
     */
    public void reset() {
        hits.set(0);
        misses.set(0);
        evictions.set(0);
        puts.set(0);
    }
}

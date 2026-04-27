package com.ankur.loganalyzer.config;

/**
 * Database Index Optimization Documentation
 *
 * This class documents all database indexes created for performance optimization.
 * Indexes are crucial for query performance, especially on large datasets.
 *
 * Index Strategy:
 * - Single-column indexes on frequently filtered/searched fields
 * - Compound indexes on common query patterns (e.g., user_id + timestamp)
 * - DESC sorting on timestamp columns for pagination queries
 *
 * Index Performance Impact:
 * ✅ Faster SELECT queries (especially with WHERE, ORDER BY, JOIN)
 * ✅ Reduced query execution time
 * ✅ Better pagination performance
 * ❌ Slower INSERT/UPDATE operations (indexes must be updated)
 * ❌ Increased disk space usage
 *
 * Tables and Their Indexes:
 *
 * 1. alert_rules
 *    - idx_alert_rule_enabled: Filter active/inactive rules
 *    - idx_alert_rule_service: Filter by service name
 *    - idx_alert_rule_name: Search by name
 *
 * 2. alert_events
 *    - idx_alert_event_triggered_at: Timeline queries
 *    - idx_alert_event_resolved: Filter resolved/unresolved
 *    - idx_alert_event_rule_id: Foreign key queries
 *    - idx_alert_event_rule_triggered: Compound for rule timeline
 *
 * 3. parsed_log_events
 *    - idx_parsed_log_timestamp: Temporal queries (DESC for newest first)
 *    - idx_parsed_log_service: Service filtering
 *    - idx_parsed_log_level: Log level filtering
 *    - idx_parsed_log_trace_id: Trace ID search
 *    - idx_parsed_log_service_timestamp: Compound for service timelines
 *    - idx_parsed_log_level_timestamp: Compound for level+time queries
 *
 * 4. raw_log_events
 *    - idx_raw_log_timestamp: Temporal queries
 *    - idx_raw_log_source: Source filtering
 *    - idx_raw_log_trace_id: Trace ID search
 *
 * 5. audit_logs
 *    - idx_audit_timestamp: Audit trail timeline
 *    - idx_audit_user_action: User action auditing
 *    - idx_audit_entity_type: Entity tracking
 *    - idx_audit_status: Success/failure tracking
 *    - idx_audit_endpoint: API endpoint monitoring
 *    - idx_audit_user_timestamp: Compound for user activity timeline
 *
 * 6. analysis_results
 *    - idx_analysis_created_at: Result timeline
 *    - idx_analysis_service: Service filtering
 *
 * 7. log_sources
 *    - idx_log_source_name: Source name lookup
 *
 * Best Practices:
 * 1. Monitor index usage: SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';
 * 2. Remove unused indexes to improve write performance
 * 3. Consider query patterns before creating indexes
 * 4. Compound indexes (multi-column) are more effective than many single indexes
 * 5. Keep indexes lean - fewer columns = less disk space and faster writes
 *
 * Query Optimization Notes:
 * - Always include timestamp DESC for pagination (ORDER BY timestamp LIMIT 20)
 * - Compound indexes work best if columns appear in same order in WHERE clause
 * - Filter most selective column first for better index efficiency
 * - Regularly analyze query plans: EXPLAIN ANALYZE SELECT ...;
 */
public final class IndexOptimization {
    private IndexOptimization() {
        // Documentation class - no instantiation
    }
}

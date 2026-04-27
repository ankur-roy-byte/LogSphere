-- Database Index Optimization Migration
-- Created for LogSphere performance optimization (Commit 12)
-- Indexes improve query performance on frequently filtered columns

-- Alert Rules Indexes
CREATE INDEX IF NOT EXISTS idx_alert_rule_enabled ON alert_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_rule_service ON alert_rules(service_name);
CREATE INDEX IF NOT EXISTS idx_alert_rule_name ON alert_rules(name);

-- Alert Events Indexes
CREATE INDEX IF NOT EXISTS idx_alert_event_triggered_at ON alert_events(triggered_at);
CREATE INDEX IF NOT EXISTS idx_alert_event_resolved ON alert_events(resolved);
CREATE INDEX IF NOT EXISTS idx_alert_event_rule_id ON alert_events(rule_id);

-- Parsed Log Events Indexes
CREATE INDEX IF NOT EXISTS idx_parsed_log_timestamp ON parsed_log_events(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_parsed_log_service ON parsed_log_events(service_name);
CREATE INDEX IF NOT EXISTS idx_parsed_log_level ON parsed_log_events(level);
CREATE INDEX IF NOT EXISTS idx_parsed_log_trace_id ON parsed_log_events(trace_id);
CREATE INDEX IF NOT EXISTS idx_parsed_log_timestamp_level ON parsed_log_events(timestamp DESC, level);

-- Raw Log Events Indexes
CREATE INDEX IF NOT EXISTS idx_raw_log_timestamp ON raw_log_events(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_raw_log_source ON raw_log_events(source_id);
CREATE INDEX IF NOT EXISTS idx_raw_log_trace_id ON raw_log_events(trace_id);

-- Audit Logs Indexes (from AuditLog entity)
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_user_action ON audit_logs(user_id, action_type);
CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_status ON audit_logs(status);
CREATE INDEX IF NOT EXISTS idx_audit_endpoint ON audit_logs(endpoint);

-- Analysis Results Indexes (for result lookups and filtering)
CREATE INDEX IF NOT EXISTS idx_analysis_created_at ON analysis_results(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_analysis_service ON analysis_results(service_name);

-- Log Source Indexes
CREATE INDEX IF NOT EXISTS idx_log_source_name ON log_sources(name);

-- Compound Indexes for Common Query Patterns
CREATE INDEX IF NOT EXISTS idx_parsed_log_service_timestamp ON parsed_log_events(service_name, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_parsed_log_level_timestamp ON parsed_log_events(level, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_alert_event_rule_triggered ON alert_events(rule_id, triggered_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_user_timestamp ON audit_logs(user_id, timestamp DESC);

-- Remember: Indexes improve READ performance but slightly decrease WRITE performance
-- Monitor index usage regularly: SELECT * FROM pg_stat_user_indexes WHERE schemaname = 'public';

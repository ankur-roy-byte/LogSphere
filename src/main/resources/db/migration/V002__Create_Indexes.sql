-- Indexes for raw_log_events
CREATE INDEX IF NOT EXISTS idx_raw_logs_source ON raw_log_events(source_name);
CREATE INDEX IF NOT EXISTS idx_raw_logs_format ON raw_log_events(format);
CREATE INDEX IF NOT EXISTS idx_raw_logs_created ON raw_log_events(created_at DESC);

-- Indexes for parsed_log_events
CREATE INDEX IF NOT EXISTS idx_parsed_logs_raw_log_id ON parsed_log_events(raw_log_id);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_service ON parsed_log_events(service_name);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_level ON parsed_log_events(log_level);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_trace_id ON parsed_log_events(trace_id);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_exception ON parsed_log_events(exception_type);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_created ON parsed_log_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_service_level ON parsed_log_events(service_name, log_level);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_service_created ON parsed_log_events(service_name, created_at DESC);

-- Indexes for log_sources
CREATE INDEX IF NOT EXISTS idx_log_sources_type ON log_sources(type);
CREATE INDEX IF NOT EXISTS idx_log_sources_active ON log_sources(active);

-- Indexes for alert_rules
CREATE INDEX IF NOT EXISTS idx_alert_rules_enabled ON alert_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_rules_service ON alert_rules(service_name);
CREATE INDEX IF NOT EXISTS idx_alert_rules_name ON alert_rules(name);

-- Indexes for alert_events
CREATE INDEX IF NOT EXISTS idx_alert_events_rule_id ON alert_events(alert_rule_id);
CREATE INDEX IF NOT EXISTS idx_alert_events_triggered ON alert_events(triggered_at DESC);
CREATE INDEX IF NOT EXISTS idx_alert_events_resolved ON alert_events(resolved);
CREATE INDEX IF NOT EXISTS idx_alert_events_severity ON alert_events(severity);

-- Indexes for analysis_results
CREATE INDEX IF NOT EXISTS idx_analysis_service ON analysis_results(service_name);
CREATE INDEX IF NOT EXISTS idx_analysis_timestamp ON analysis_results(analysis_timestamp DESC);

-- Indexes for audit_logs
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_endpoint ON audit_logs(endpoint);

-- LogSphere Performance Optimization Indexes
-- Version 1.0.0
-- Indexes for query performance on frequently used columns

-- LogEntry Indexes
CREATE INDEX IF NOT EXISTS idx_log_entry_service_timestamp ON log_entry(service_name, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_log_entry_trace_id ON log_entry(trace_id);
CREATE INDEX IF NOT EXISTS idx_log_entry_level ON log_entry(level);
CREATE INDEX IF NOT EXISTS idx_log_entry_timestamp ON log_entry(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_log_entry_created_at ON log_entry(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_log_entry_service_level ON log_entry(service_name, level);

-- AlertRule Indexes
CREATE INDEX IF NOT EXISTS idx_alert_rule_service_name ON alert_rule(service_name);
CREATE INDEX IF NOT EXISTS idx_alert_rule_enabled ON alert_rule(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_rule_created_at ON alert_rule(created_at DESC);

-- AuditLog Indexes
CREATE INDEX IF NOT EXISTS idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_endpoint ON audit_log(endpoint);

-- AnalysisResult Indexes
CREATE INDEX IF NOT EXISTS idx_analysis_result_service ON analysis_result(service_name, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_analysis_result_type ON analysis_result(analysis_type);
CREATE INDEX IF NOT EXISTS idx_analysis_result_created_at ON analysis_result(created_at DESC);

-- ParsedLog Indexes
CREATE INDEX IF NOT EXISTS idx_parsed_log_entry_id ON parsed_log(log_entry_id);

-- Session Indexes
CREATE INDEX IF NOT EXISTS idx_session_user_id ON session(user_id);
CREATE INDEX IF NOT EXISTS idx_session_expires_at ON session(expires_at);

-- ApplicationEvent Indexes
CREATE INDEX IF NOT EXISTS idx_app_event_type ON application_event(event_type);
CREATE INDEX IF NOT EXISTS idx_app_event_created_at ON application_event(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_app_event_severity ON application_event(severity);

-- Constraints
ALTER TABLE parsed_log 
    ADD CONSTRAINT fk_parsed_log_entry 
    FOREIGN KEY (log_entry_id) REFERENCES log_entry(id) ON DELETE CASCADE;

-- Unique Constraints
ALTER TABLE system_config 
    ADD CONSTRAINT uk_config_key UNIQUE (config_key);

COMMIT;

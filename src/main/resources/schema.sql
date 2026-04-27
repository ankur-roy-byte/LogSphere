-- LogSphere Application Schema
-- Complete database schema for log ingestion, analysis, and alert management

-- Raw Log Events Table
-- Stores raw, unparsed log entries from various sources
CREATE TABLE IF NOT EXISTS raw_log_events (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    source_name VARCHAR(255) NOT NULL,
    format VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_raw_logs_source ON raw_log_events(source_name);
CREATE INDEX IF NOT EXISTS idx_raw_logs_format ON raw_log_events(format);
CREATE INDEX IF NOT EXISTS idx_raw_logs_created ON raw_log_events(created_at DESC);

-- Parsed Log Events Table
-- Stores structured, parsed log entries with extracted fields
CREATE TABLE IF NOT EXISTS parsed_log_events (
    id BIGSERIAL PRIMARY KEY,
    raw_log_id BIGINT REFERENCES raw_log_events(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    log_level VARCHAR(50),
    service_name VARCHAR(255),
    trace_id VARCHAR(255),
    exception_type VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_raw_log_id ON parsed_log_events(raw_log_id);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_service ON parsed_log_events(service_name);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_level ON parsed_log_events(log_level);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_trace_id ON parsed_log_events(trace_id);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_exception ON parsed_log_events(exception_type);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_created ON parsed_log_events(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_service_level ON parsed_log_events(service_name, log_level);
CREATE INDEX IF NOT EXISTS idx_parsed_logs_service_created ON parsed_log_events(service_name, created_at DESC);

-- Log Sources Table
-- Defines the sources from which logs are ingested
CREATE TABLE IF NOT EXISTS log_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    config_json TEXT,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_log_sources_type ON log_sources(type);
CREATE INDEX IF NOT EXISTS idx_log_sources_active ON log_sources(active);

-- Alert Rules Table
-- Defines conditions that trigger alerts
CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    condition_type VARCHAR(100) NOT NULL,
    threshold INTEGER NOT NULL,
    service_name VARCHAR(255),
    description TEXT,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_alert_rules_enabled ON alert_rules(enabled);
CREATE INDEX IF NOT EXISTS idx_alert_rules_service ON alert_rules(service_name);
CREATE INDEX IF NOT EXISTS idx_alert_rules_name ON alert_rules(name);

-- Alert Events Table
-- Records alert trigger events and their status
CREATE TABLE IF NOT EXISTS alert_events (
    id BIGSERIAL PRIMARY KEY,
    alert_rule_id BIGINT REFERENCES alert_rules(id) ON DELETE CASCADE,
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN DEFAULT FALSE,
    resolved_at TIMESTAMP,
    message TEXT,
    severity VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_alert_events_rule_id ON alert_events(alert_rule_id);
CREATE INDEX IF NOT EXISTS idx_alert_events_triggered ON alert_events(triggered_at DESC);
CREATE INDEX IF NOT EXISTS idx_alert_events_resolved ON alert_events(resolved);
CREATE INDEX IF NOT EXISTS idx_alert_events_severity ON alert_events(severity);

-- Analysis Results Table
-- Stores results from log analysis operations
CREATE TABLE IF NOT EXISTS analysis_results (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    total_logs BIGINT,
    error_count BIGINT,
    warning_count BIGINT,
    anomaly_count BIGINT,
    analysis_timestamp TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_analysis_service ON analysis_results(service_name);
CREATE INDEX IF NOT EXISTS idx_analysis_timestamp ON analysis_results(analysis_timestamp DESC);

-- Audit Logs Table
-- Maintains audit trail of system operations
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255),
    action_type VARCHAR(100),
    entity_type VARCHAR(100),
    entity_id BIGINT,
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    client_ip VARCHAR(50),
    status_code INTEGER,
    duration_ms BIGINT,
    error_message TEXT,
    details_json TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user ON audit_logs(username);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_endpoint ON audit_logs(endpoint);

-- LogSphere Database Schema Initialization
-- Version 1.0.0
-- Initial table creation for log ingestion and analysis platform

-- Create LogEntry table
CREATE TABLE IF NOT EXISTS log_entry (
    id BIGSERIAL PRIMARY KEY,
    trace_id VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    source VARCHAR(255),
    stack_trace TEXT,
    raw_content TEXT,
    parsed_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create AlertRule table
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    service_name VARCHAR(255) NOT NULL,
    threshold BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create AuditLog table
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255),
    action_type VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id VARCHAR(255),
    http_method VARCHAR(10),
    endpoint VARCHAR(500),
    client_ip VARCHAR(45),
    status VARCHAR(50),
    duration_ms BIGINT,
    error_message TEXT,
    details JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create AnalysisResult table
CREATE TABLE IF NOT EXISTS analysis_result (
    id BIGSERIAL PRIMARY KEY,
    service_name VARCHAR(255) NOT NULL,
    analysis_type VARCHAR(100) NOT NULL,
    result_data JSONB,
    confidence_score DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create ParsedLog table
CREATE TABLE IF NOT EXISTS parsed_log (
    id BIGSERIAL PRIMARY KEY,
    log_entry_id BIGINT NOT NULL,
    format VARCHAR(100) NOT NULL,
    parsed_fields JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (log_entry_id) REFERENCES log_entry(id) ON DELETE CASCADE
);

-- Create SystemConfig table for runtime configuration
CREATE TABLE IF NOT EXISTS system_config (
    id BIGSERIAL PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL UNIQUE,
    config_value VARCHAR(2000),
    description TEXT,
    config_type VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Session table for user sessions
CREATE TABLE IF NOT EXISTS session (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

-- Create ApplicationEvent table for event tracking
CREATE TABLE IF NOT EXISTS application_event (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    event_source VARCHAR(255),
    event_data JSONB,
    severity VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMIT;

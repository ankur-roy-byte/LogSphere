-- LogSphere Sample Data for Development and Testing
-- Version 1.0.0
-- Contains sample records for testing and demonstration

-- Insert sample alert rules
INSERT INTO alert_rule (name, description, service_name, threshold, enabled, created_by)
VALUES 
    ('High Error Rate', 'Alert when error logs exceed 100 per hour', 'auth-service', 100, true, 'system'),
    ('Database Connection Pool', 'Alert on connection pool exhaustion', 'db-service', 50, true, 'system'),
    ('Memory Usage Alert', 'Alert when memory usage exceeds threshold', 'api-gateway', 75, true, 'system'),
    ('API Latency Monitor', 'Alert when API response time exceeds 5 seconds', 'user-service', 5000, true, 'system'),
    ('Cache Miss Rate', 'Alert on high cache miss ratio', 'cache-service', 30, true, 'system')
ON CONFLICT (name) DO NOTHING;

-- Insert sample application events
INSERT INTO application_event (event_type, event_source, severity, event_data)
VALUES 
    ('APPLICATION_START', 'core', 'INFO', '{"version":"1.0.0","timestamp":"2024-01-15T10:00:00Z"}'),
    ('DATABASE_CONNECTED', 'persistence', 'INFO', '{"database":"postgresql","host":"localhost"}'),
    ('REDIS_CONNECTED', 'cache', 'INFO', '{"host":"localhost","port":6379}'),
    ('CONFIG_LOADED', 'core', 'INFO', '{"profiles":["dev"],"properties_count":50}'),
    ('METRICS_AVAILABLE', 'monitoring', 'INFO', '{"endpoint":"/api/metrics"}')
ON CONFLICT DO NOTHING;

-- Insert sample system configuration
INSERT INTO system_config (config_key, config_value, config_type, description)
VALUES 
    ('max.concurrent.parsers', '16', 'INTEGER', 'Maximum concurrent log parsing threads'),
    ('batch.ingestion.size', '1000', 'INTEGER', 'Batch size for bulk ingestion'),
    ('cache.ttl.default', '300', 'INTEGER', 'Default cache TTL in seconds'),
    ('anomaly.detection.enabled', 'true', 'BOOLEAN', 'Enable anomaly detection'),
    ('audit.logging.enabled', 'true', 'BOOLEAN', 'Enable audit logging'),
    ('api.rate.limit.requests', '1000', 'INTEGER', 'Max requests per minute'),
    ('alert.retry.attempts', '3', 'INTEGER', 'Number of alert delivery retries'),
    ('log.retention.days', '90', 'INTEGER', 'Log retention period in days')
ON CONFLICT (config_key) DO NOTHING;

COMMIT;

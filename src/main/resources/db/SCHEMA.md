# LogSphere Database Schema Documentation

## Overview
LogSphere uses PostgreSQL for persistent storage with optimized indexes for high-performance log ingestion and analysis.

## Tables

### 1. log_entry
Core table for storing ingested logs.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY) - Unique log entry identifier
- `trace_id` (VARCHAR 255, NOT NULL) - Distributed trace ID for request correlation
- `service_name` (VARCHAR 255, NOT NULL) - Source service identifier
- `level` (VARCHAR 50, NOT NULL) - Log level (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- `message` (TEXT, NOT NULL) - Main log message
- `timestamp` (TIMESTAMP, NOT NULL) - When the log was created
- `source` (VARCHAR 255) - Log source (file, system, etc.)
- `stack_trace` (TEXT) - Optional stack trace for errors
- `raw_content` (TEXT) - Raw original log content
- `parsed_data` (JSONB) - Parsed key-value pairs
- `created_at` (TIMESTAMP, DEFAULT NOW)
- `updated_at` (TIMESTAMP, DEFAULT NOW)

**Indexes:**
- `idx_log_entry_service_timestamp` - Most common query pattern
- `idx_log_entry_trace_id` - Trace correlation lookups
- `idx_log_entry_level` - Level-based filtering
- `idx_log_entry_timestamp` - Time-range queries
- `idx_log_entry_service_level` - Combined service + level searches

### 2. alert_rule
Alert rules for automated notifications.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY)
- `name` (VARCHAR 255, UNIQUE, NOT NULL) - Rule identifier
- `description` (TEXT) - Rule documentation
- `service_name` (VARCHAR 255, NOT NULL) - Target service
- `threshold` (BIGINT, NOT NULL) - Trigger threshold value
- `enabled` (BOOLEAN, DEFAULT true)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)
- `created_by` (VARCHAR 255)
- `updated_by` (VARCHAR 255)

### 3. audit_log
Compliance and audit trail records.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY)
- `user_id` (VARCHAR 255) - User who performed the action
- `action_type` (VARCHAR 100, NOT NULL) - CREATE, UPDATE, DELETE, etc.
- `entity_type` (VARCHAR 100) - log_entry, alert_rule, config, etc.
- `entity_id` (VARCHAR 255) - ID of affected entity
- `http_method` (VARCHAR 10) - GET, POST, PUT, DELETE
- `endpoint` (VARCHAR 500) - API endpoint accessed
- `client_ip` (VARCHAR 45) - IPv4 or IPv6 address
- `status` (VARCHAR 50) - SUCCESS, FAILURE
- `duration_ms` (BIGINT) - Operation duration
- `error_message` (TEXT) - Error details if failed
- `details` (JSONB) - Additional context
- `timestamp` (TIMESTAMP)

### 4. analysis_result
Results from log analysis operations.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY)
- `service_name` (VARCHAR 255, NOT NULL)
- `analysis_type` (VARCHAR 100, NOT NULL) - ANOMALY, TREND, PATTERN, etc.
- `result_data` (JSONB) - Structured analysis results
- `confidence_score` (DECIMAL 5,2) - 0.00 to 99.99
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

### 5. parsed_log
Structured parsed log formats.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY)
- `log_entry_id` (BIGINT, FOREIGN KEY) - Reference to log_entry
- `format` (VARCHAR 100, NOT NULL) - JSON, REGEX, STACKTRACE, etc.
- `parsed_fields` (JSONB) - Format-specific parsed data
- `created_at` (TIMESTAMP)

### 6. system_config
Runtime configuration management.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY)
- `config_key` (VARCHAR 255, UNIQUE, NOT NULL)
- `config_value` (VARCHAR 2000)
- `description` (TEXT)
- `config_type` (VARCHAR 50) - STRING, INTEGER, BOOLEAN, JSON
- `updated_at` (TIMESTAMP)

### 7. session
User session management.

**Columns:**
- `id` (VARCHAR 255, PRIMARY KEY)
- `user_id` (VARCHAR 255)
- `created_at` (TIMESTAMP)
- `last_accessed` (TIMESTAMP)
- `expires_at` (TIMESTAMP)

### 8. application_event
Application-level event tracking.

**Columns:**
- `id` (BIGSERIAL PRIMARY KEY)
- `event_type` (VARCHAR 100, NOT NULL)
- `event_source` (VARCHAR 255) - Component that raised the event
- `event_data` (JSONB) - Event-specific data
- `severity` (VARCHAR 50) - INFO, WARNING, ERROR
- `created_at` (TIMESTAMP)

## Performance Considerations

### Query Patterns
1. **Service-based queries**: Use `idx_log_entry_service_timestamp`
2. **Trace lookups**: Use `idx_log_entry_trace_id` for request correlation
3. **Level filtering**: Use `idx_log_entry_level` for severity searches
4. **Time-range queries**: Use `idx_log_entry_timestamp`

### Connections
- JSONB columns use GiST indexing for flexible queries
- Foreign key constraints ensure referential integrity
- Cascade deletes prevent orphaned records

### Optimization Tips
- Partition `log_entry` by date/service for very large datasets (>1B rows)
- Archive logs older than 90 days
- Vacuum regularly to reclaim disk space
- Monitor index usage with `pg_stat_user_indexes`

## Migration Strategy

LogSphere uses Flyway for automated database migrations:

1. **V001__Initial_Schema.sql** - Create all tables
2. **V002__Create_Indexes.sql** - Add performance indexes
3. **V003__Sample_Data.sql** - Load sample data
4. Additional migrations as needed

### Running Migrations
Migrations run automatically on application startup via Spring Boot + Flyway integration.

To manually run:
```sql
-- Connect to PostgreSQL
psql -U username -h localhost -d logsphere -f V001__Initial_Schema.sql
psql -U username -h localhost -d logsphere -f V002__Create_Indexes.sql
psql -U username -h localhost -d logsphere -f V003__Sample_Data.sql
```

## Backup and Recovery

### Regular Backups
```bash
pg_dump -U username -h localhost logsphere > backup_$(date +%Y%m%d).sql
```

### Point-in-Time Recovery
Configure PostgreSQL with WAL archiving enabled.

### Disaster Recovery
```bash
# Restore from backup
psql -U username -h localhost -d logsphere < backup_20240115.sql
```

## Monitoring

### Useful Queries

**Table sizes:**
```sql
SELECT schemaname, tablename, 
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables 
WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

**Index usage:**
```sql
SELECT schemaname, tablename, indexname, idx_scan
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

**Slow queries:**
```sql
SELECT mean_exec_time, calls, query 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;
```

## Security

- Use parameterized queries to prevent SQL injection
- Enable row-level security (RLS) for multi-tenant scenarios
- Encrypt sensitive data in JSONB columns
- Audit all data modifications via audit_log table
- Restrict database user permissions by principle of least privilege


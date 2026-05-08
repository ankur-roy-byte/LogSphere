CREATE TABLE IF NOT EXISTS saved_searches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    service_name VARCHAR(100),
    level VARCHAR(20),
    trace_id VARCHAR(128),
    keyword VARCHAR(255),
    host VARCHAR(100),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_saved_search_name ON saved_searches(name);
CREATE INDEX IF NOT EXISTS idx_saved_search_created_by ON saved_searches(created_by);
CREATE INDEX IF NOT EXISTS idx_saved_search_service_level ON saved_searches(service_name, level);

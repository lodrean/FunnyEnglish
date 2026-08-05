-- Клиентские логи WARN/ERROR с устройств пользователей (OpenSpec add-client-logging)
CREATE TABLE IF NOT EXISTS client_logs (
    id UUID PRIMARY KEY,
    anonymous_id UUID,
    level VARCHAR(10) NOT NULL,
    tag VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    stack_trace TEXT,
    platform VARCHAR(20) NOT NULL,
    app_version VARCHAR(50),
    client_timestamp TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_client_logs_created_at ON client_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_client_logs_level ON client_logs(level);
CREATE INDEX IF NOT EXISTS idx_client_logs_anonymous_id ON client_logs(anonymous_id);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE notification (
                              id                  UUID PRIMARY KEY,
                              user_id             VARCHAR(128) NOT NULL,
                              event_type          VARCHAR(128) NOT NULL,
                              locale              VARCHAR(16),
                              brand               VARCHAR(64),
                              channels            TEXT NOT NULL,
                              status              VARCHAR(32) NOT NULL,
                              novu_transaction_id VARCHAR(255),
                              idempotency_key     VARCHAR(255),
                              payload_json        JSONB,
                              created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                              updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX ux_notification_idempotency
    ON notification (idempotency_key);

CREATE INDEX idx_notification_user_created_at
    ON notification (user_id, created_at DESC);

CREATE INDEX idx_notification_status
    ON notification (status);

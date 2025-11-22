CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- ← You don't need uuid-ossp for generating UUIDv7 in Java,
-- but it's harmless to have; you can skip if you want.

CREATE TYPE notification_channel AS ENUM ('EMAIL', 'SMS', 'PUSH', 'CHAT', 'IN_APP');
CREATE TYPE notification_status AS ENUM ('PENDING', 'SENT', 'DELIVERED', 'FAILED');

CREATE TABLE notification (
                              id                  UUID PRIMARY KEY,
                              user_id             VARCHAR(128) NOT NULL,
                              event_type          VARCHAR(128) NOT NULL,
                              locale              VARCHAR(16),
                              brand               VARCHAR(64),
                              channels            TEXT NOT NULL,
                              status              notification_status NOT NULL,
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

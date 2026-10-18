CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE spend_events (
    id              UUID PRIMARY KEY,
    user_id         UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    customer_id     VARCHAR(100) NOT NULL,
    event_type      VARCHAR(50)  NOT NULL,
    category        VARCHAR(100) NOT NULL,
    amount          NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'USD',
    channel         VARCHAR(50)  NOT NULL,
    description     VARCHAR(500),
    occurred_at     TIMESTAMPTZ  NOT NULL,
    metadata_json   TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_amount_non_negative CHECK (amount >= 0),
    CONSTRAINT chk_event_type CHECK (event_type IN ('PURCHASE', 'REFUND', 'INTERACTION', 'SUBSCRIPTION'))
);

CREATE INDEX idx_spend_events_user_id ON spend_events (user_id);
CREATE INDEX idx_spend_events_customer_id ON spend_events (customer_id);
CREATE INDEX idx_spend_events_occurred_at ON spend_events (occurred_at);
CREATE INDEX idx_spend_events_category ON spend_events (category);
CREATE INDEX idx_spend_events_user_occurred ON spend_events (user_id, occurred_at DESC);

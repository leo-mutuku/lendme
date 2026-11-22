ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE otp_codes (
    id            UUID         PRIMARY KEY,
    user_id       UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    purpose       VARCHAR(30)  NOT NULL,
    code_hash     VARCHAR(255) NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    attempts      INTEGER      NOT NULL DEFAULT 0,
    consumed_at   TIMESTAMPTZ,

    -- from BaseEntity
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT ck_otp_codes_purpose CHECK (purpose IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET'))
);

CREATE INDEX ix_otp_codes_user_purpose ON otp_codes (user_id, purpose, created_at DESC);

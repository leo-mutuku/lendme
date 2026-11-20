CREATE TABLE users (
                       id                    UUID         PRIMARY KEY,
                       first_name            VARCHAR(75)  NOT NULL,
                       last_name             VARCHAR(75)  NOT NULL,
                       phone_number          VARCHAR(13)  NOT NULL,
                       email                 VARCHAR(255),
                       password_hash         VARCHAR(255) NOT NULL,
                       role                  VARCHAR(20)  NOT NULL,
                       enabled               BOOLEAN      NOT NULL DEFAULT TRUE,
                       failed_login_attempts INTEGER      NOT NULL DEFAULT 0,
                       locked_until          TIMESTAMPTZ,
                       last_login_at         TIMESTAMPTZ,

    -- from BaseEntity
                       created_at            TIMESTAMPTZ  NOT NULL,
                       updated_at            TIMESTAMPTZ  NOT NULL,
                       version               BIGINT       NOT NULL DEFAULT 0,

                       CONSTRAINT uk_users_phone_number UNIQUE (phone_number),
                       CONSTRAINT uk_users_email        UNIQUE (email),
                       CONSTRAINT ck_users_role         CHECK (role IN ('BORROWER', 'LENDER', 'ADMIN'))
);
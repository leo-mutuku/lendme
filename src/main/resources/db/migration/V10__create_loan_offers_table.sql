-- A lender's standing offer. Borrowers apply to an offer; each approved
-- application takes its amount out of available_amount.
CREATE TABLE loan_offers (
    id                     UUID          PRIMARY KEY,
    lender_id              UUID          NOT NULL REFERENCES users (id),
    title                  VARCHAR(100)  NOT NULL,
    description            VARCHAR(1000),
    min_amount             NUMERIC(19,2) NOT NULL,
    max_amount             NUMERIC(19,2) NOT NULL,
    total_amount           NUMERIC(19,2) NOT NULL,
    available_amount       NUMERIC(19,2) NOT NULL,
    interest_rate_percent  NUMERIC(5,2)  NOT NULL,   -- flat % on principal, e.g. 10.00
    min_term_months        INTEGER       NOT NULL,
    max_term_months        INTEGER       NOT NULL,
    status                 VARCHAR(20)   NOT NULL,

    created_at             TIMESTAMPTZ   NOT NULL,
    updated_at             TIMESTAMPTZ   NOT NULL,
    version                BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT ck_offers_amounts   CHECK (min_amount > 0 AND min_amount <= max_amount AND max_amount <= total_amount),
    CONSTRAINT ck_offers_available CHECK (available_amount >= 0 AND available_amount <= total_amount),
    CONSTRAINT ck_offers_rate      CHECK (interest_rate_percent >= 0 AND interest_rate_percent <= 100),
    CONSTRAINT ck_offers_terms     CHECK (min_term_months > 0 AND min_term_months <= max_term_months),
    CONSTRAINT ck_offers_status    CHECK (status IN ('OPEN','PAUSED','CLOSED','EXHAUSTED'))
);

CREATE INDEX idx_offers_lender ON loan_offers (lender_id);
CREATE INDEX idx_offers_status ON loan_offers (status);

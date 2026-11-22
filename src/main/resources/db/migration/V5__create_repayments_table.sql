CREATE TABLE repayments (
    id          UUID          PRIMARY KEY,
    loan_id     UUID          NOT NULL REFERENCES loans (id),
    amount      NUMERIC(19,2) NOT NULL,
    paid_on     DATE          NOT NULL,

    created_at  TIMESTAMPTZ   NOT NULL,
    updated_at  TIMESTAMPTZ   NOT NULL,
    version     BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT ck_repayments_amount CHECK (amount > 0)
);

CREATE INDEX idx_repayments_loan ON repayments (loan_id);

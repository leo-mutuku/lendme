CREATE TABLE loans (
    id           UUID          PRIMARY KEY,
    borrower_id  UUID          NOT NULL REFERENCES users (id),
    lender_id    UUID          NOT NULL REFERENCES users (id),
    principal    NUMERIC(19,2) NOT NULL,
    balance      NUMERIC(19,2) NOT NULL,
    due_date     DATE          NOT NULL,
    status       VARCHAR(20)   NOT NULL,

    created_at   TIMESTAMPTZ   NOT NULL,
    updated_at   TIMESTAMPTZ   NOT NULL,
    version      BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT ck_loans_not_self    CHECK (borrower_id <> lender_id),
    CONSTRAINT ck_loans_principal   CHECK (principal > 0),
    CONSTRAINT ck_loans_balance     CHECK (balance >= 0 AND balance <= principal),
    CONSTRAINT ck_loans_status      CHECK (status IN ('PENDING','APPROVED','REJECTED','ACTIVE','OVERDUE','COMPLETED','DEFAULTED'))
);

CREATE INDEX idx_loans_borrower ON loans (borrower_id);
CREATE INDEX idx_loans_lender   ON loans (lender_id);

-- A loan request. It becomes a loan (row in loans) only after approval.
CREATE TABLE loan_applications (
    id                UUID          PRIMARY KEY,
    applicant_id      UUID          NOT NULL REFERENCES users (id),
    reviewer_id       UUID          REFERENCES users (id),
    requested_amount  NUMERIC(19,2) NOT NULL,
    term_months       INTEGER       NOT NULL,
    purpose           VARCHAR(255)  NOT NULL,
    submitted_on      DATE          NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    rejection_reason  VARCHAR(255),

    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL,
    version           BIGINT        NOT NULL DEFAULT 0,

    CONSTRAINT ck_applications_amount   CHECK (requested_amount > 0),
    CONSTRAINT ck_applications_term     CHECK (term_months > 0),
    CONSTRAINT ck_applications_not_self CHECK (reviewer_id IS NULL OR reviewer_id <> applicant_id),
    CONSTRAINT ck_applications_status   CHECK (status IN ('SUBMITTED','UNDER_REVIEW','APPROVED','REJECTED'))
);

CREATE INDEX idx_applications_applicant ON loan_applications (applicant_id);
CREATE INDEX idx_applications_status    ON loan_applications (status);

-- Every loan comes from exactly one approved application (loans is empty at this point).
ALTER TABLE loans
    ADD COLUMN application_id UUID NOT NULL
        CONSTRAINT uk_loans_application UNIQUE
        REFERENCES loan_applications (id);

-- Request steps moved to loan_applications; a loan only has these states now.
ALTER TABLE loans DROP CONSTRAINT ck_loans_status;
ALTER TABLE loans ADD CONSTRAINT ck_loans_status
    CHECK (status IN ('ACTIVE','OVERDUE','COMPLETED','DEFAULTED'));

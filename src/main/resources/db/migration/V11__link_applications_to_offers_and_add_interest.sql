-- Applications are now always made to a lender's offer; the offer's lender decides.
-- (loan_applications and loans are empty at this point.)
ALTER TABLE loan_applications
    ADD COLUMN offer_id              UUID         NOT NULL REFERENCES loan_offers (id),
    ADD COLUMN interest_rate_percent NUMERIC(5,2) NOT NULL;   -- copied from the offer when applying

ALTER TABLE loan_applications ALTER COLUMN reviewer_id SET NOT NULL;

ALTER TABLE loan_applications DROP CONSTRAINT ck_applications_status;
ALTER TABLE loan_applications ADD CONSTRAINT ck_applications_status
    CHECK (status IN ('SUBMITTED','APPROVED','REJECTED','WITHDRAWN'));

ALTER TABLE loan_applications ADD CONSTRAINT ck_applications_rate
    CHECK (interest_rate_percent >= 0 AND interest_rate_percent <= 100);

CREATE INDEX idx_applications_offer    ON loan_applications (offer_id);
CREATE INDEX idx_applications_reviewer ON loan_applications (reviewer_id);

-- A borrower can have only one pending application per offer.
CREATE UNIQUE INDEX uk_applications_one_pending
    ON loan_applications (applicant_id, offer_id) WHERE status = 'SUBMITTED';

-- Loans now carry flat interest: balance starts at principal + interest_amount.
ALTER TABLE loans
    ADD COLUMN interest_rate_percent NUMERIC(5,2)  NOT NULL,
    ADD COLUMN interest_amount       NUMERIC(19,2) NOT NULL;

ALTER TABLE loans DROP CONSTRAINT ck_loans_balance;
ALTER TABLE loans ADD CONSTRAINT ck_loans_balance
    CHECK (balance >= 0 AND balance <= principal + interest_amount);
ALTER TABLE loans ADD CONSTRAINT ck_loans_interest CHECK (interest_amount >= 0);

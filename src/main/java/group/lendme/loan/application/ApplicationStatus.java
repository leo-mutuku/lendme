package group.lendme.loan.application;

/** Steps of a loan REQUEST. Only APPROVED produces a Loan. */
public enum ApplicationStatus {
    /** Waiting for the offer's lender to decide. */
    SUBMITTED,
    APPROVED,
    REJECTED,
    /** Cancelled by the borrower before a decision. */
    WITHDRAWN
}

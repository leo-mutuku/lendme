package group.lendme.loan.application;

/** Steps of a loan REQUEST. Only APPROVED produces a Loan. */
public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}

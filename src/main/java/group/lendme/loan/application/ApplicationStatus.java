package group.lendme.loan;

/** Steps of a loan REQUEST. Only APPROVED produces a Loan. */
public enum ApplicationStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}

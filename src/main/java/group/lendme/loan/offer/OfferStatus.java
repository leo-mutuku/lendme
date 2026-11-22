package group.lendme.loan.offer;

public enum OfferStatus {
    /** Borrowers can apply. */
    OPEN,
    /** Temporarily hidden by the lender; can be resumed. */
    PAUSED,
    /** Ended by the lender for good. */
    CLOSED,
    /** Not enough money left for even the minimum loan. */
    EXHAUSTED
}

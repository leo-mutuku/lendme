package group.lendme.loan.state;

/**
 * States of a real loan - this is what is stored in loans.status.
 * Request steps (submitted/approved/rejected) live in ApplicationStatus.
 * Each value holds the LoanState object that defines its behaviour.
 */
public enum LoanStatus {
    ACTIVE(new ActiveState()),
    OVERDUE(new OverdueState()),
    COMPLETED(new CompletedState()),
    DEFAULTED(new DefaultedState());

    private final LoanState state;

    LoanStatus(LoanState state) { this.state = state; }

    public LoanState state() { return state; }

    public boolean isOpen() { return this == ACTIVE || this == OVERDUE; }
}

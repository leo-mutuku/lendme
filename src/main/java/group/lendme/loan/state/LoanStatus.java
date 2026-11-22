package group.lendme.loan;

/** States of a real loan. Request steps (submitted/approved/rejected) live in ApplicationStatus. */
public enum LoanStatus {
    ACTIVE(new ActiveState()),
    OVERDUE(new OverdueState()),
    COMPLETED(new CompletedState()),
    DEFAULTED(new DefaultedState());

    private final LoanState state;

    LoanStatus(LoanState state) { this.state = state; }

    LoanState state() { return state; }

    public boolean isOpen() { return this == ACTIVE || this == OVERDUE; }
}

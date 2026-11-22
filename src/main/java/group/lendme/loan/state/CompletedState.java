package group.lendme.loan;

/** Final state: the loan is fully repaid. No overrides, so every action is rejected. */
final class CompletedState implements LoanState {
    public LoanStatus status() { return LoanStatus.COMPLETED; }
}

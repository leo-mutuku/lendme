package group.lendme.loan;

/** Final state: the borrower failed to repay. No overrides, so every action is rejected. */
final class DefaultedState implements LoanState {
    public LoanStatus status() { return LoanStatus.DEFAULTED; }
}

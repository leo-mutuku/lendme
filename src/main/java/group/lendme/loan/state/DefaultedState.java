package group.lendme.loan.state;

/** Final state: the borrower failed to repay. No overrides, so every action is refused. */
final class DefaultedState implements LoanState {

    @Override public LoanStatus status() { return LoanStatus.DEFAULTED; }
}

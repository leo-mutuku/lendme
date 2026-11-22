package group.lendme.loan.state;

/** Final state: the loan is fully repaid. No overrides, so every action is refused. */
final class CompletedState implements LoanState {

    @Override public LoanStatus status() { return LoanStatus.COMPLETED; }
}

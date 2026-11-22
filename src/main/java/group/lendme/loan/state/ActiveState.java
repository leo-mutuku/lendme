package group.lendme.loan.state;

/** Money is out and repayments are expected. */
final class ActiveState implements LoanState {

    @Override public LoanStatus status() { return LoanStatus.ACTIVE; }

    @Override public LoanStatus onRepayment(boolean fullyPaid) {
        return fullyPaid ? LoanStatus.COMPLETED : LoanStatus.ACTIVE;
    }

    @Override public LoanStatus onOverdue() { return LoanStatus.OVERDUE; }
}

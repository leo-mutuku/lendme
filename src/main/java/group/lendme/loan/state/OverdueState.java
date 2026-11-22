package group.lendme.loan.state;

/** Past the due date but still repayable. */
final class OverdueState implements LoanState {

    @Override public LoanStatus status() { return LoanStatus.OVERDUE; }

    @Override public LoanStatus onRepayment(boolean fullyPaid) {
        return fullyPaid ? LoanStatus.COMPLETED : LoanStatus.OVERDUE;
    }

    @Override public LoanStatus onDefault() { return LoanStatus.DEFAULTED; }
}

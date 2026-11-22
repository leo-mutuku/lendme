package group.lendme.loan;

import group.lendme.repayment.Repayment;

final class OverdueState implements LoanState {
    public LoanStatus status() { return LoanStatus.OVERDUE; }

    @Override public void recordRepayment(Loan loan, Repayment r) {
        loan.reduceBalance(r.getAmount());
        if (loan.isFullyPaid()) loan.setState(LoanStatus.COMPLETED);
    }

    @Override public void markDefaulted(Loan loan) { loan.setState(LoanStatus.DEFAULTED); }
}

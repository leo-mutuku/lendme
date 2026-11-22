package group.lendme.loan;

import group.lendme.repayment.Repayment;

final class ActiveState implements LoanState {
    public LoanStatus status() { return LoanStatus.ACTIVE; }

    @Override public void recordRepayment(Loan loan, Repayment r) {
        loan.reduceBalance(r.getAmount());
        if (loan.isFullyPaid()) loan.setState(LoanStatus.COMPLETED);
    }

    @Override public void markOverdue(Loan loan) { loan.setState(LoanStatus.OVERDUE); }
}
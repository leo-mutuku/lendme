package group.lendme.loan;

import group.lendme.common.exception.BusinessRuleException;
import group.lendme.repayment.Repayment;

public interface LoanState {

    LoanStatus status();

    default void recordRepayment(Loan loan, Repayment r)    { throw illegal("recordRepayment"); }
    default void markOverdue(Loan loan)                     { throw illegal("markOverdue"); }
    default void markDefaulted(Loan loan)                   { throw illegal("markDefaulted"); }

    private BusinessRuleException illegal(String action) {
        return new BusinessRuleException("INVALID_LOAN_STATE",
                "Cannot " + action + " a loan that is " + status());
    }
}
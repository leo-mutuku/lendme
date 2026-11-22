package group.lendme.loan.state;

import group.lendme.common.exception.BusinessRuleException;

/**
 * State pattern: each state answers "which status comes next?" for an action.
 * States never touch Loan's fields - Loan applies the answer itself, so its
 * balance and status stay private.
 *
 * Every action is refused by default; each state overrides only what it allows.
 */
public interface LoanState {

    LoanStatus status();

    /** @param fullyPaid true when this repayment brings the balance to zero */
    default LoanStatus onRepayment(boolean fullyPaid) { throw illegal("record a repayment on"); }

    default LoanStatus onOverdue()                    { throw illegal("mark overdue"); }

    default LoanStatus onDefault()                    { throw illegal("mark defaulted"); }

    private BusinessRuleException illegal(String action) {
        return new BusinessRuleException("INVALID_LOAN_STATE",
                "Cannot " + action + " a loan that is " + status());
    }
}

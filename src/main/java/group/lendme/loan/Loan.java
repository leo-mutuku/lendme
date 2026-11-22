package group.lendme.loan;

import group.lendme.common.entity.BaseEntity;
import group.lendme.common.exception.BusinessRuleException;
import group.lendme.loan.application.ApplicationStatus;
import group.lendme.loan.application.LoanApplication;
import group.lendme.loan.state.LoanState;
import group.lendme.loan.state.LoanStatus;
import group.lendme.repayment.Repayment;
import group.lendme.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A real debt. It only exists once a LoanApplication has been approved:
 * the constructor is private and open() refuses anything not APPROVED.
 *
 * Its balance and status can only change through its own methods; the
 * state objects only say which status comes next.
 *
 * Borrower and lender are positions a User takes on THIS loan.
 * The same User can be the borrower on one loan and the lender on another.
 */
@Getter
@Entity
@Table(name = "loans")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Loan extends BaseEntity {

    /** The request this loan came from. One application produces at most one loan. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private LoanApplication application;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lender_id", nullable = false)
    private User lender;

    @Column(name = "principal", nullable = false, precision = 19, scale = 2)
    private BigDecimal principal;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Stored in the database; the matching LoanState comes from status.state(). */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status;

    /** Private: use open(). Self-lending, canLend and amount were checked by the application. */
    private Loan(LoanApplication application, User lender, LocalDate startDate) {
        this.application = application;
        this.borrower = application.getApplicant();
        this.lender = lender;
        this.principal = application.getRequestedAmount();
        this.balance = principal;          // nothing repaid yet
        this.dueDate = startDate.plusMonths(application.getTermMonths());
        this.status = LoanStatus.ACTIVE;   // a loan starts active - the request steps are done
    }

    /**
     * The only way to create a Loan. Called by LoanApplication.approve().
     * Public because it lives in another package, so it checks the rules itself;
     * the UNIQUE(application_id) constraint stops a second loan per application.
     */
    public static Loan open(LoanApplication application, User lender, LocalDate startDate) {
        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessRuleException("APPLICATION_NOT_APPROVED",
                    "Only an approved application can become a loan");
        }
        if (application.getReviewer() == null
                || !application.getReviewer().getId().equals(lender.getId())) {
            throw new BusinessRuleException("NOT_REVIEWER", "Only the approving lender can fund this loan");
        }
        return new Loan(application, lender, startDate);
    }

    // ---------- actions: the state decides the next status, Loan applies it ----------

    public void recordRepayment(Repayment repayment) {
        BigDecimal amount = repayment.getAmount();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Repayment must be positive");
        }
        BigDecimal newBalance = balance.subtract(amount);
        LoanStatus next = state().onRepayment(newBalance.signum() <= 0);  // refuses if not allowed
        if (newBalance.signum() < 0) {
            throw new BusinessRuleException("OVERPAYMENT", "Repayment exceeds outstanding balance");
        }
        this.balance = newBalance;
        this.status = next;
    }

    public void markOverdue()   { this.status = state().onOverdue(); }

    public void markDefaulted() { this.status = state().onDefault(); }

    public boolean isPastDue(LocalDate today) { return today.isAfter(dueDate); }

    public boolean isFullyPaid() { return balance.signum() == 0; }

    private LoanState state() { return status.state(); }

    // ---------- position checks (replace "roles" for loan actions) ----------

    public boolean isBorrower(UUID userId) { return borrower.getId().equals(userId); }

    public boolean isLender(UUID userId)   { return lender.getId().equals(userId); }

    public boolean involves(UUID userId)   { return isBorrower(userId) || isLender(userId); }
}

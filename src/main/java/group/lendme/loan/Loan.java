package group.lendme.loan;

import group.lendme.common.entity.BaseEntity;
import group.lendme.common.exception.BusinessRuleException;
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
 * A real debt. It only exists once a LoanApplication has been approved
 * (see LoanApplication.approve) - there is no public constructor.
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

    /** Private: the rules (self-lending, canLend, amount) were already checked by the application. */
    private Loan(LoanApplication application, User lender, LocalDate startDate) {
        this.application = application;
        this.borrower = application.getApplicant();
        this.lender = lender;
        this.principal = application.getRequestedAmount();
        this.balance = principal;          // nothing repaid yet
        this.dueDate = startDate.plusMonths(application.getTermMonths());
        this.status = LoanStatus.ACTIVE;   // a loan starts active - the request steps are done
    }

    /** Package-private: only LoanApplication.approve() calls this. */
    static Loan fromApplication(LoanApplication application, User lender, LocalDate startDate) {
        return new Loan(application, lender, startDate);
    }

    // ---------- actions: Loan hands each one to its current state ----------

    public void recordRepayment(Repayment r)  { state().recordRepayment(this, r); }
    public void markOverdue()                 { state().markOverdue(this); }
    public void markDefaulted()               { state().markDefaulted(this); }

    public boolean isPastDue(LocalDate today) { return today.isAfter(dueDate); }

    // ---------- package-private: only state classes use these ----------

    LoanState state() { return status.state(); }

    void setState(LoanStatus newStatus) { this.status = newStatus; }

    void reduceBalance(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Repayment must be positive");
        if (amount.compareTo(balance) > 0)
            throw new BusinessRuleException("OVERPAYMENT", "Repayment exceeds outstanding balance");
        balance = balance.subtract(amount);
    }

    boolean isFullyPaid() { return balance.signum() == 0; }

    // ---------- position checks (replace "roles" for loan actions) ----------

    public boolean isBorrower(UUID userId) { return borrower.getId().equals(userId); }

    public boolean isLender(UUID userId)   { return lender.getId().equals(userId); }

    public boolean involves(UUID userId)   { return isBorrower(userId) || isLender(userId); }
}

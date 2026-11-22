package group.lendme.loan;

import group.lendme.common.entity.BaseEntity;
import group.lendme.common.exception.BusinessRuleException;
import group.lendme.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A request to borrow money. It is NOT a loan: a Loan only exists after a
 * lender approves this application, and approve() is the only way to create one.
 */
@Getter
@Entity
@Table(name = "loan_applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    /** The lender looking at this request; null until a review starts. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "submitted_on", nullable = false)
    private LocalDate submittedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    public LoanApplication(User applicant, BigDecimal requestedAmount, int termMonths,
                           String purpose, LocalDate submittedOn) {
        if (!applicant.canBorrow()) {
            throw new BusinessRuleException("BORROWER_NOT_ALLOWED", "This account cannot borrow");
        }
        if (requestedAmount == null || requestedAmount.signum() <= 0) {
            throw new IllegalArgumentException("Requested amount must be positive");
        }
        if (termMonths <= 0) {
            throw new IllegalArgumentException("Term must be at least one month");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Purpose is required");
        }
        this.applicant = applicant;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.purpose = purpose.trim();
        this.submittedOn = submittedOn;
        this.status = ApplicationStatus.SUBMITTED;
    }

    // ---------- lifecycle ----------

    public void startReview(User lender) {
        requireStatus(ApplicationStatus.SUBMITTED);
        if (applicant.getId().equals(lender.getId())) {
            throw new BusinessRuleException("SELF_LENDING", "A user cannot lend to themselves");
        }
        if (!lender.canLend()) {
            throw new BusinessRuleException("LENDER_NOT_ALLOWED", "This account cannot lend");
        }
        this.reviewer = lender;
        this.status = ApplicationStatus.UNDER_REVIEW;
    }

    /** The request becomes a loan here, and only here. */
    public Loan approve(User lender, LocalDate startDate) {
        requireStatus(ApplicationStatus.UNDER_REVIEW);
        requireReviewer(lender);
        this.status = ApplicationStatus.APPROVED;
        return Loan.fromApplication(this, lender, startDate);
    }

    /** Rejected applications never produce a Loan. */
    public void reject(User lender, String reason) {
        requireStatus(ApplicationStatus.UNDER_REVIEW);
        requireReviewer(lender);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        this.rejectionReason = reason.trim();
        this.status = ApplicationStatus.REJECTED;
    }

    // ---------- helpers ----------

    private void requireStatus(ApplicationStatus expected) {
        if (status != expected) {
            throw new BusinessRuleException("INVALID_APPLICATION_STATE",
                    "Application must be " + expected + " but is " + status);
        }
    }

    private void requireReviewer(User lender) {
        if (reviewer == null || !reviewer.getId().equals(lender.getId())) {
            throw new BusinessRuleException("NOT_REVIEWER", "Only the reviewing lender can decide");
        }
    }
}

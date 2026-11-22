package group.lendme.loan.application;

import group.lendme.common.entity.BaseEntity;
import group.lendme.common.exception.BusinessRuleException;
import group.lendme.loan.Loan;
import group.lendme.loan.offer.LoanOffer;
import group.lendme.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A borrower's request against a lender's offer. It is NOT a loan: a Loan only
 * exists after the offer's lender approves it, and approve() is the only way to create one.
 */
@Getter
@Entity
@Table(name = "loan_applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanApplication extends BaseEntity {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false, updatable = false)
    private LoanOffer offer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false, updatable = false)
    private User applicant;

    /** The lender who decides: always the offer's lender. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false, updatable = false)
    private User reviewer;

    @Column(name = "requested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "term_months", nullable = false)
    private int termMonths;

    /** Copied from the offer when applying, so later offer changes don't affect this request. */
    @Column(name = "interest_rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRatePercent;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "submitted_on", nullable = false)
    private LocalDate submittedOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    public LoanApplication(LoanOffer offer, User applicant, BigDecimal requestedAmount, int termMonths,
                           String purpose, LocalDate submittedOn) {
        if (!applicant.canBorrow()) {
            throw new BusinessRuleException("BORROWER_NOT_ALLOWED", "This account cannot borrow");
        }
        if (offer.isOwnedBy(applicant.getId())) {
            throw new BusinessRuleException("SELF_LENDING", "You cannot apply to your own offer");
        }
        if (requestedAmount == null || requestedAmount.signum() <= 0) {
            throw new IllegalArgumentException("Requested amount must be positive");
        }
        if (purpose == null || purpose.isBlank()) {
            throw new IllegalArgumentException("Purpose is required");
        }
        offer.requireAcceptable(requestedAmount, termMonths);   // open, amount/term in range, funds left

        this.offer = offer;
        this.applicant = applicant;
        this.reviewer = offer.getLender();
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.interestRatePercent = offer.getInterestRatePercent();
        this.purpose = purpose.trim();
        this.submittedOn = submittedOn;
        this.status = ApplicationStatus.SUBMITTED;
    }

    // ---------- lifecycle ----------

    /** The request becomes a loan here, and only here. Takes the money out of the offer's pool. */
    public Loan approve(User lender, LocalDate startDate) {
        requireStatus(ApplicationStatus.SUBMITTED);
        requireReviewer(lender);
        if (!lender.canLend()) {
            throw new BusinessRuleException("LENDER_NOT_ALLOWED", "This account cannot lend");
        }
        offer.allocate(requestedAmount);
        this.status = ApplicationStatus.APPROVED;
        return Loan.open(this, lender, startDate);
    }

    /** Rejected applications never produce a Loan. */
    public void reject(User lender, String reason) {
        requireStatus(ApplicationStatus.SUBMITTED);
        requireReviewer(lender);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A rejection reason is required");
        }
        this.rejectionReason = reason.trim();
        this.status = ApplicationStatus.REJECTED;
    }

    public void withdraw(User borrower) {
        requireStatus(ApplicationStatus.SUBMITTED);
        if (!isApplicant(borrower.getId())) {
            throw new BusinessRuleException("NOT_APPLICANT", "Only the applicant can withdraw");
        }
        this.status = ApplicationStatus.WITHDRAWN;
    }

    // ---------- figures ----------

    /** Flat interest: requestedAmount x rate / 100, rounded to cents. */
    public BigDecimal getInterestAmount() {
        return requestedAmount.multiply(interestRatePercent)
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalRepayable() {
        return requestedAmount.add(getInterestAmount());
    }

    // ---------- position checks ----------

    public boolean isApplicant(UUID userId) { return applicant.getId().equals(userId); }

    public boolean isReviewer(UUID userId)  { return reviewer.getId().equals(userId); }

    // ---------- helpers ----------

    private void requireStatus(ApplicationStatus expected) {
        if (status != expected) {
            throw new BusinessRuleException("INVALID_APPLICATION_STATE",
                    "Application must be " + expected + " but is " + status);
        }
    }

    private void requireReviewer(User lender) {
        if (!isReviewer(lender.getId())) {
            throw new BusinessRuleException("NOT_REVIEWER", "Only the offer's lender can decide");
        }
    }
}

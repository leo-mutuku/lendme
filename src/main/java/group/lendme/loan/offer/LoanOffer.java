package group.lendme.loan.offer;

import group.lendme.common.entity.BaseEntity;
import group.lendme.common.exception.BusinessRuleException;
import group.lendme.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A lender's standing offer: "I'll lend between min and max, up to total in all,
 * at X% flat, for N to M months". Borrowers apply to an offer.
 *
 * availableAmount is what is left of the pool; it only goes down when an
 * application is approved (see allocate()).
 */
@Getter
@Entity
@Table(name = "loan_offers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoanOffer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lender_id", nullable = false, updatable = false)
    private User lender;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "min_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "available_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableAmount;

    /** Flat interest on the principal, e.g. 10.00 means repay principal + 10%. */
    @Column(name = "interest_rate_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRatePercent;

    @Column(name = "min_term_months", nullable = false)
    private int minTermMonths;

    @Column(name = "max_term_months", nullable = false)
    private int maxTermMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OfferStatus status;

    public LoanOffer(User lender, OfferTerms terms) {
        if (!lender.canLend()) {
            throw new BusinessRuleException("LENDER_NOT_ALLOWED",
                    "Only enabled, KYC-verified accounts can create offers");
        }
        this.lender = lender;
        apply(terms);
        this.availableAmount = terms.totalAmount();
        this.status = OfferStatus.OPEN;
    }

    // ---------- lender actions ----------

    /**
     * Changes the terms. The new total can't be less than what has already been lent.
     * Only affects future applications.
     */
    public void update(OfferTerms terms) {
        requireNotClosed();
        BigDecimal lent = totalAmount.subtract(availableAmount);
        if (terms.totalAmount().compareTo(lent) < 0) {
            throw new BusinessRuleException("TOTAL_BELOW_LENT",
                    "totalAmount cannot be less than the " + lent + " already lent from this offer");
        }
        apply(terms);
        this.availableAmount = terms.totalAmount().subtract(lent);
        refreshExhausted();
    }

    public void pause() {
        if (status != OfferStatus.OPEN) {
            throw invalidState("Only an OPEN offer can be paused");
        }
        status = OfferStatus.PAUSED;
    }

    public void resume() {
        if (status != OfferStatus.PAUSED) {
            throw invalidState("Only a PAUSED offer can be resumed");
        }
        status = OfferStatus.OPEN;
        refreshExhausted();
    }

    public void close() {
        requireNotClosed();
        status = OfferStatus.CLOSED;
    }

    // ---------- used when an application is approved ----------

    /** Checks an amount/term fits this offer (for applying). */
    public void requireAcceptable(BigDecimal amount, int termMonths) {
        if (status != OfferStatus.OPEN) {
            throw new BusinessRuleException("OFFER_NOT_OPEN", "This offer is not accepting applications");
        }
        if (amount.compareTo(minAmount) < 0 || amount.compareTo(maxAmount) > 0) {
            throw new BusinessRuleException("AMOUNT_OUT_OF_RANGE",
                    "Amount must be between " + minAmount + " and " + maxAmount);
        }
        if (amount.compareTo(availableAmount) > 0) {
            throw new BusinessRuleException("OFFER_INSUFFICIENT_FUNDS",
                    "Only " + availableAmount + " is left on this offer");
        }
        if (termMonths < minTermMonths || termMonths > maxTermMonths) {
            throw new BusinessRuleException("TERM_OUT_OF_RANGE",
                    "Term must be between " + minTermMonths + " and " + maxTermMonths + " months");
        }
    }

    /** Takes the amount out of the pool. Marks the offer EXHAUSTED when the rest is below minAmount. */
    public void allocate(BigDecimal amount, int termMonths) {
        requireAcceptable(amount, termMonths);
        availableAmount = availableAmount.subtract(amount);
        refreshExhausted();
    }

    public boolean isOwnedBy(UUID userId) {
        return lender.getId().equals(userId);
    }

    // ---------- helpers ----------

    private void apply(OfferTerms terms) {
        this.title = terms.title();
        this.description = terms.description();
        this.minAmount = terms.minAmount();
        this.maxAmount = terms.maxAmount();
        this.totalAmount = terms.totalAmount();
        this.interestRatePercent = terms.interestRatePercent();
        this.minTermMonths = terms.minTermMonths();
        this.maxTermMonths = terms.maxTermMonths();
    }

    /** OPEN <-> EXHAUSTED depending on whether at least one minimum loan still fits. */
    private void refreshExhausted() {
        boolean fundsLeft = availableAmount.compareTo(minAmount) >= 0;
        if (status == OfferStatus.OPEN && !fundsLeft) {
            status = OfferStatus.EXHAUSTED;
        } else if (status == OfferStatus.EXHAUSTED && fundsLeft) {
            status = OfferStatus.OPEN;
        }
    }

    private void requireNotClosed() {
        if (status == OfferStatus.CLOSED) {
            throw invalidState("This offer is closed");
        }
    }

    private static BusinessRuleException invalidState(String message) {
        return new BusinessRuleException("INVALID_OFFER_STATE", message);
    }
}

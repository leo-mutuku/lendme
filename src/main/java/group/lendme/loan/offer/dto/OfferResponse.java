package group.lendme.loan.offer.dto;

import group.lendme.loan.offer.LoanOffer;
import group.lendme.loan.offer.OfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OfferResponse(
        UUID id,
        Lender lender,
        String title,
        String description,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal totalAmount,
        BigDecimal availableAmount,
        BigDecimal interestRatePercent,
        int minTermMonths,
        int maxTermMonths,
        OfferStatus status,
        Instant createdAt
) {
    /** Only public details of the lender. */
    public record Lender(UUID id, String name) {}

    public static OfferResponse from(LoanOffer o) {
        return new OfferResponse(o.getId(),
                new Lender(o.getLender().getId(), o.getLender().getFullName()),
                o.getTitle(), o.getDescription(),
                o.getMinAmount(), o.getMaxAmount(), o.getTotalAmount(), o.getAvailableAmount(),
                o.getInterestRatePercent(), o.getMinTermMonths(), o.getMaxTermMonths(),
                o.getStatus(), o.getCreatedAt());
    }
}

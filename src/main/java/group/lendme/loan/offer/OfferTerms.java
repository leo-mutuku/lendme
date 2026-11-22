package group.lendme.loan.offer;

import java.math.BigDecimal;

/**
 * The lender-chosen numbers of an offer, validated together.
 * Used both when creating and when updating an offer.
 */
public record OfferTerms(
        String title,
        String description,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        BigDecimal totalAmount,
        BigDecimal interestRatePercent,
        int minTermMonths,
        int maxTermMonths
) {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    public OfferTerms {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        title = title.trim();
        description = description == null || description.isBlank() ? null : description.trim();

        requirePositive(minAmount, "minAmount");
        requirePositive(maxAmount, "maxAmount");
        requirePositive(totalAmount, "totalAmount");
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new IllegalArgumentException("minAmount cannot be greater than maxAmount");
        }
        if (maxAmount.compareTo(totalAmount) > 0) {
            throw new IllegalArgumentException("maxAmount cannot be greater than totalAmount");
        }
        if (interestRatePercent == null || interestRatePercent.signum() < 0 || interestRatePercent.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("interestRatePercent must be between 0 and 100");
        }
        if (minTermMonths <= 0 || minTermMonths > maxTermMonths) {
            throw new IllegalArgumentException("Terms must satisfy 0 < minTermMonths <= maxTermMonths");
        }
    }

    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(field + " must be greater than 0");
        }
    }
}

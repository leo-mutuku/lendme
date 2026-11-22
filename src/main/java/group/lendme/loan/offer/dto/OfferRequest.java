package group.lendme.loan.offer.dto;

import group.lendme.loan.offer.OfferTerms;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Body for creating or updating an offer. */
public record OfferRequest(
        @NotBlank @Size(max = 100) String title,
        @Size(max = 1000) String description,

        @Schema(example = "1000.00", description = "Smallest loan a borrower can request")
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal minAmount,

        @Schema(example = "20000.00", description = "Largest single loan")
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal maxAmount,

        @Schema(example = "100000.00", description = "Total the lender is willing to lend from this offer")
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal totalAmount,

        @Schema(example = "10.00", description = "Flat interest on the principal, in percent")
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal interestRatePercent,

        @Min(1) @Max(120) int minTermMonths,
        @Min(1) @Max(120) int maxTermMonths
) {
    public OfferTerms toTerms() {
        return new OfferTerms(title, description, minAmount, maxAmount, totalAmount,
                interestRatePercent, minTermMonths, maxTermMonths);
    }
}

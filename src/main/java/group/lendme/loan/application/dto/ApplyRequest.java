package group.lendme.loan.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ApplyRequest(
        @NotNull UUID offerId,
        @Schema(example = "5000.00")
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Schema(example = "6")
        @Min(1) @Max(120) int termMonths,
        @Schema(example = "Stock for my shop")
        @NotBlank @Size(max = 255) String purpose
) {}

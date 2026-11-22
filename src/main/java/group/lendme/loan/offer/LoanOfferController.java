package group.lendme.loan.offer;

import group.lendme.common.dto.PageResponse;
import group.lendme.loan.offer.dto.OfferRequest;
import group.lendme.loan.offer.dto.OfferResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Served at /api/v1/offers (see WebConfig). */
@RestController
@RequestMapping("/offers")
@RequiredArgsConstructor
@Tag(name = "Loan offers", description = "Lenders publish offers; borrowers browse and apply to them")
public class LoanOfferController {

    private final LoanOfferService offerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an offer", description = "Requires an enabled, KYC-verified account.")
    public OfferResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OfferRequest request) {
        return offerService.create(userId(jwt), request);
    }

    @GetMapping
    @Operation(summary = "Browse open offers",
            description = "Newest first. Filter by the amount and term you want, and the highest rate you accept.")
    public PageResponse<OfferResponse> browse(
            @Parameter(description = "Amount you want to borrow") @RequestParam(required = false) @DecimalMin("0.01") BigDecimal amount,
            @Parameter(description = "Term you want, in months") @RequestParam(required = false) @Min(1) Integer termMonths,
            @Parameter(description = "Highest flat interest % you accept") @RequestParam(required = false) @DecimalMin("0") BigDecimal maxRate,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return offerService.browse(amount, termMonths, maxRate, newestFirst(page, size));
    }

    @GetMapping("/mine")
    @Operation(summary = "My offers (all statuses)")
    public PageResponse<OfferResponse> mine(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam(defaultValue = "0") @Min(0) int page,
                                            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return offerService.mine(userId(jwt), newestFirst(page, size));
    }

    @GetMapping("/{offerId}")
    @Operation(summary = "Get an offer")
    public OfferResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID offerId) {
        return offerService.get(offerId, userId(jwt));
    }

    @PutMapping("/{offerId}")
    @Operation(summary = "Update my offer", description = "Changes apply to future applications only.")
    public OfferResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID offerId,
                                @Valid @RequestBody OfferRequest request) {
        return offerService.update(offerId, userId(jwt), request);
    }

    @PostMapping("/{offerId}/pause")
    @Operation(summary = "Pause my offer (stop new applications)")
    public OfferResponse pause(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID offerId) {
        return offerService.pause(offerId, userId(jwt));
    }

    @PostMapping("/{offerId}/resume")
    @Operation(summary = "Resume my paused offer")
    public OfferResponse resume(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID offerId) {
        return offerService.resume(offerId, userId(jwt));
    }

    @PostMapping("/{offerId}/close")
    @Operation(summary = "Close my offer for good")
    public OfferResponse close(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID offerId) {
        return offerService.close(offerId, userId(jwt));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static PageRequest newestFirst(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}

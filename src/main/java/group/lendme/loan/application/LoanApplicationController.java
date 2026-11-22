package group.lendme.loan.application;

import group.lendme.common.dto.PageResponse;
import group.lendme.loan.application.dto.ApplicationResponse;
import group.lendme.loan.application.dto.ApplyRequest;
import group.lendme.loan.application.dto.RejectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Served at /api/v1/applications (see WebConfig). */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
@Tag(name = "Loan applications", description = "Borrowers apply to offers; the offer's lender approves or rejects")
public class LoanApplicationController {

    private final LoanApplicationService applicationService;

    // ---------- borrower ----------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Apply to an offer",
            description = "Amount and term must fit the offer. The offer's current interest rate is locked in.")
    public ApplicationResponse apply(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ApplyRequest request) {
        return applicationService.apply(userId(jwt), request);
    }

    @GetMapping("/mine")
    @Operation(summary = "My applications (as borrower)")
    public PageResponse<ApplicationResponse> mine(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestParam(required = false) ApplicationStatus status,
                                                  @RequestParam(defaultValue = "0") @Min(0) int page,
                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return applicationService.mine(userId(jwt), status, newestFirst(page, size));
    }

    @PostMapping("/{applicationId}/withdraw")
    @Operation(summary = "Withdraw my pending application")
    public ApplicationResponse withdraw(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId) {
        return applicationService.withdraw(applicationId, userId(jwt));
    }

    // ---------- lender ----------

    @GetMapping("/received")
    @Operation(summary = "Applications to my offers (as lender)")
    public PageResponse<ApplicationResponse> received(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestParam(required = false) ApplicationStatus status,
                                                      @RequestParam(required = false) UUID offerId,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return applicationService.received(userId(jwt), status, offerId, newestFirst(page, size));
    }

    @PostMapping("/{applicationId}/approve")
    @Operation(summary = "Approve (creates the loan)",
            description = "Takes the amount out of the offer's pool. Fails if not enough is left.")
    public ApplicationResponse approve(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId) {
        return applicationService.approve(applicationId, userId(jwt));
    }

    @PostMapping("/{applicationId}/reject")
    @Operation(summary = "Reject with a reason")
    public ApplicationResponse reject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId,
                                      @Valid @RequestBody RejectRequest request) {
        return applicationService.reject(applicationId, userId(jwt), request.reason());
    }

    // ---------- both ----------

    @GetMapping("/{applicationId}")
    @Operation(summary = "Get an application", description = "Visible to the applicant and the offer's lender only.")
    public ApplicationResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID applicationId) {
        return applicationService.get(applicationId, userId(jwt));
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static PageRequest newestFirst(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}

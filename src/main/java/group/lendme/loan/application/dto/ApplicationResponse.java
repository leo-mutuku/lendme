package group.lendme.loan.application.dto;

import group.lendme.loan.application.ApplicationStatus;
import group.lendme.loan.application.LoanApplication;
import group.lendme.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        Offer offer,
        Party applicant,
        Party lender,
        BigDecimal amount,
        int termMonths,
        BigDecimal interestRatePercent,
        BigDecimal interestAmount,
        BigDecimal totalRepayable,
        String purpose,
        ApplicationStatus status,
        String rejectionReason,
        LocalDate submittedOn,
        Instant updatedAt,
        /** Set once approved. */
        UUID loanId
) {
    public record Offer(UUID id, String title) {}

    /** Only public details of a user. */
    public record Party(UUID id, String name) {
        static Party of(User u) { return new Party(u.getId(), u.getFullName()); }
    }

    public static ApplicationResponse from(LoanApplication a, UUID loanId) {
        return new ApplicationResponse(a.getId(),
                new Offer(a.getOffer().getId(), a.getOffer().getTitle()),
                Party.of(a.getApplicant()), Party.of(a.getReviewer()),
                a.getRequestedAmount(), a.getTermMonths(),
                a.getInterestRatePercent(), a.getInterestAmount(), a.getTotalRepayable(),
                a.getPurpose(), a.getStatus(), a.getRejectionReason(),
                a.getSubmittedOn(), a.getUpdatedAt(), loanId);
    }
}

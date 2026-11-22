package group.lendme.loan.application;

import group.lendme.common.dto.PageResponse;
import group.lendme.common.exception.ConflictException;
import group.lendme.common.exception.ResourceNotFoundException;
import group.lendme.loan.Loan;
import group.lendme.loan.LoanRepository;
import group.lendme.loan.application.dto.ApplicationResponse;
import group.lendme.loan.application.dto.ApplyRequest;
import group.lendme.loan.offer.LoanOffer;
import group.lendme.loan.offer.LoanOfferRepository;
import group.lendme.notification.EmailService;
import group.lendme.user.User;
import group.lendme.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository applications;
    private final LoanOfferRepository offers;
    private final LoanRepository loans;
    private final UserRepository users;
    private final EmailService emailService;

    // ---------- borrower ----------

    @Transactional
    public ApplicationResponse apply(UUID borrowerId, ApplyRequest req) {
        User borrower = user(borrowerId);
        LoanOffer offer = offers.findById(req.offerId())
                .orElseThrow(() -> new ResourceNotFoundException("Offer", req.offerId()));

        if (applications.existsByApplicantIdAndOfferIdAndStatus(borrowerId, offer.getId(), ApplicationStatus.SUBMITTED)) {
            throw new ConflictException("You already have a pending application for this offer");
        }

        LoanApplication application = applications.save(new LoanApplication(
                offer, borrower, req.amount(), req.termMonths(), req.purpose(), LocalDate.now()));
        return ApplicationResponse.from(application, null);
    }

    @Transactional
    public ApplicationResponse withdraw(UUID applicationId, UUID borrowerId) {
        LoanApplication application = findVisibleTo(applicationId, borrowerId);
        application.withdraw(user(borrowerId));
        return ApplicationResponse.from(application, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> mine(UUID borrowerId, ApplicationStatus status, Pageable pageable) {
        return PageResponse.of(applications.findByApplicant(borrowerId, status, pageable), this::toResponse);
    }

    // ---------- lender ----------

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> received(UUID lenderId, ApplicationStatus status, UUID offerId,
                                                      Pageable pageable) {
        return PageResponse.of(applications.findByLender(lenderId, status, offerId, pageable), this::toResponse);
    }

    /** Creates the loan and takes the amount out of the offer's pool. */
    @Transactional
    public ApplicationResponse approve(UUID applicationId, UUID lenderId) {
        LoanApplication application = findVisibleTo(applicationId, lenderId);
        Loan loan = loans.save(application.approve(user(lenderId), LocalDate.now()));

        User borrower = application.getApplicant();
        notify(borrower, application, true, """
                Amount: %s
                Interest (%s%% flat): %s
                Total to repay: %s by %s""".formatted(
                loan.getPrincipal(), loan.getInterestRatePercent(), loan.getInterestAmount(),
                loan.getTotalRepayable(), loan.getDueDate()));
        return ApplicationResponse.from(application, loan.getId());
    }

    @Transactional
    public ApplicationResponse reject(UUID applicationId, UUID lenderId, String reason) {
        LoanApplication application = findVisibleTo(applicationId, lenderId);
        application.reject(user(lenderId), reason);
        notify(application.getApplicant(), application, false, "Reason: " + application.getRejectionReason());
        return ApplicationResponse.from(application, null);
    }

    // ---------- both ----------

    @Transactional(readOnly = true)
    public ApplicationResponse get(UUID applicationId, UUID userId) {
        return toResponse(findVisibleTo(applicationId, userId));
    }

    // ---------- helpers ----------

    /** Only the applicant and the offer's lender may see an application; others get 404. */
    private LoanApplication findVisibleTo(UUID applicationId, UUID userId) {
        LoanApplication application = applications.findWithDetails(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));
        if (!application.isApplicant(userId) && !application.isReviewer(userId)) {
            throw new ResourceNotFoundException("Application", applicationId);
        }
        return application;
    }

    private ApplicationResponse toResponse(LoanApplication a) {
        UUID loanId = a.getStatus() == ApplicationStatus.APPROVED
                ? loans.findByApplicationId(a.getId()).map(Loan::getId).orElse(null)
                : null;
        return ApplicationResponse.from(a, loanId);
    }

    private void notify(User borrower, LoanApplication application, boolean approved, String details) {
        if (borrower.getEmail() != null) {
            emailService.sendApplicationDecision(borrower.getEmail().value(), borrower.getFirstName(),
                    application.getOffer().getTitle(), approved, details);
        }
    }

    private User user(UUID id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}

package group.lendme.loan.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    @EntityGraph(attributePaths = {"offer", "applicant", "reviewer"})
    @Query("select a from LoanApplication a where a.id = :id")
    Optional<LoanApplication> findWithDetails(@Param("id") UUID id);

    /** "My requests" (borrower). Null status = all. */
    @EntityGraph(attributePaths = {"offer", "applicant", "reviewer"})
    @Query("""
            select a from LoanApplication a
            where a.applicant.id = :applicantId
              and (:status is null or a.status = :status)
            """)
    Page<LoanApplication> findByApplicant(@Param("applicantId") UUID applicantId,
                                          @Param("status") ApplicationStatus status, Pageable pageable);

    /** Requests made to my offers (lender). Null status/offer = all. */
    @EntityGraph(attributePaths = {"offer", "applicant", "reviewer"})
    @Query("""
            select a from LoanApplication a
            where a.reviewer.id = :lenderId
              and (:status is null or a.status = :status)
              and (:offerId is null or a.offer.id = :offerId)
            """)
    Page<LoanApplication> findByLender(@Param("lenderId") UUID lenderId,
                                       @Param("status") ApplicationStatus status,
                                       @Param("offerId") UUID offerId, Pageable pageable);

    boolean existsByApplicantIdAndOfferIdAndStatus(UUID applicantId, UUID offerId, ApplicationStatus status);
}

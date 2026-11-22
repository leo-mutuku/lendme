package group.lendme.loan.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    List<LoanApplication> findByApplicantId(UUID applicantId);     // "my requests"

    List<LoanApplication> findByStatus(ApplicationStatus status);  // e.g. open requests for lenders
}

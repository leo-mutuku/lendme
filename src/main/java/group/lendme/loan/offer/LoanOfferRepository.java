package group.lendme.loan.offer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LoanOfferRepository extends JpaRepository<LoanOffer, UUID>, JpaSpecificationExecutor<LoanOffer> {

    @EntityGraph(attributePaths = "lender")
    Page<LoanOffer> findByLenderId(UUID lenderId, Pageable pageable);   // "my offers"

    @Override
    @EntityGraph(attributePaths = "lender")
    Page<LoanOffer> findAll(Specification<LoanOffer> spec, Pageable pageable);
}

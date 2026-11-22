
package group.lendme.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, UUID> {

    List<Loan> findByBorrowerId(UUID borrowerId);   // "my borrowings"

    List<Loan> findByLenderId(UUID lenderId);       // "my lendings"

    @Query("select l from Loan l where l.borrower.id = :userId or l.lender.id = :userId")
    List<Loan> findAllInvolving(UUID userId);       // both
}

package group.lendme.repayment;

import group.lendme.common.entity.BaseEntity;
import group.lendme.loan.Loan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "repayments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_on", nullable = false)
    private LocalDate paidOn;

    public Repayment(Loan loan, BigDecimal amount, LocalDate paidOn) {
        if (amount == null || amount.signum() <= 0)
            throw new IllegalArgumentException("Amount must be positive");
        this.loan = loan;
        this.amount = amount;
        this.paidOn = paidOn;
    }
}

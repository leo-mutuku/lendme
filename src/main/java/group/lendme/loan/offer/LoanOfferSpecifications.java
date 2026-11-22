package group.lendme.loan.offer;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/** Filters for browsing offers. Null arguments mean "don't filter". */
final class LoanOfferSpecifications {

    private LoanOfferSpecifications() {}

    static Specification<LoanOffer> isOpen() {
        return (root, q, cb) -> cb.equal(root.get("status"), OfferStatus.OPEN);
    }

    /** The offer allows this amount and still has enough money for it. */
    static Specification<LoanOffer> fitsAmount(BigDecimal amount) {
        if (amount == null) return null;
        return (root, q, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("minAmount"), amount),
                cb.greaterThanOrEqualTo(root.get("maxAmount"), amount),
                cb.greaterThanOrEqualTo(root.get("availableAmount"), amount));
    }

    static Specification<LoanOffer> fitsTerm(Integer termMonths) {
        if (termMonths == null) return null;
        return (root, q, cb) -> cb.and(
                cb.lessThanOrEqualTo(root.get("minTermMonths"), termMonths),
                cb.greaterThanOrEqualTo(root.get("maxTermMonths"), termMonths));
    }

    static Specification<LoanOffer> maxRate(BigDecimal maxRatePercent) {
        if (maxRatePercent == null) return null;
        return (root, q, cb) -> cb.lessThanOrEqualTo(root.get("interestRatePercent"), maxRatePercent);
    }
}

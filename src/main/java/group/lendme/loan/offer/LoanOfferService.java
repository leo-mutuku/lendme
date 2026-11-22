package group.lendme.loan.offer;

import group.lendme.common.dto.PageResponse;
import group.lendme.common.exception.ForbiddenException;
import group.lendme.common.exception.ResourceNotFoundException;
import group.lendme.loan.offer.dto.OfferRequest;
import group.lendme.loan.offer.dto.OfferResponse;
import group.lendme.user.User;
import group.lendme.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LoanOfferService {

    private final LoanOfferRepository offers;
    private final UserRepository users;

    @Transactional
    public OfferResponse create(UUID lenderId, OfferRequest req) {
        User lender = users.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", lenderId));
        return OfferResponse.from(offers.save(new LoanOffer(lender, req.toTerms())));
    }

    /** Open offers any logged-in user can apply to, optionally filtered. */
    @Transactional(readOnly = true)
    public PageResponse<OfferResponse> browse(BigDecimal amount, Integer termMonths, BigDecimal maxRate,
                                              Pageable pageable) {
        // filters that weren't given are null; leave them out
        Specification<LoanOffer> spec = Specification.allOf(Stream.of(
                        LoanOfferSpecifications.isOpen(),
                        LoanOfferSpecifications.fitsAmount(amount),
                        LoanOfferSpecifications.fitsTerm(termMonths),
                        LoanOfferSpecifications.maxRate(maxRate))
                .filter(Objects::nonNull)
                .toList());
        return PageResponse.of(offers.findAll(spec, pageable), OfferResponse::from);
    }

    @Transactional(readOnly = true)
    public PageResponse<OfferResponse> mine(UUID lenderId, Pageable pageable) {
        return PageResponse.of(offers.findByLenderId(lenderId, pageable), OfferResponse::from);
    }

    /** Anyone can see an OPEN offer; other statuses only the owner. */
    @Transactional(readOnly = true)
    public OfferResponse get(UUID offerId, UUID userId) {
        LoanOffer offer = find(offerId);
        if (offer.getStatus() != OfferStatus.OPEN && !offer.isOwnedBy(userId)) {
            throw new ResourceNotFoundException("Offer", offerId);
        }
        return OfferResponse.from(offer);
    }

    @Transactional
    public OfferResponse update(UUID offerId, UUID userId, OfferRequest req) {
        return ownerAction(offerId, userId, o -> o.update(req.toTerms()));
    }

    @Transactional
    public OfferResponse pause(UUID offerId, UUID userId) {
        return ownerAction(offerId, userId, LoanOffer::pause);
    }

    @Transactional
    public OfferResponse resume(UUID offerId, UUID userId) {
        return ownerAction(offerId, userId, LoanOffer::resume);
    }

    @Transactional
    public OfferResponse close(UUID offerId, UUID userId) {
        return ownerAction(offerId, userId, LoanOffer::close);
    }

    // ---------- helpers ----------

    private OfferResponse ownerAction(UUID offerId, UUID userId, Consumer<LoanOffer> action) {
        LoanOffer offer = find(offerId);
        if (!offer.isOwnedBy(userId)) {
            throw new ForbiddenException("NOT_OFFER_OWNER", "Only the lender who created this offer can change it");
        }
        action.accept(offer);
        return OfferResponse.from(offer);
    }

    private LoanOffer find(UUID offerId) {
        return offers.findById(offerId).orElseThrow(() -> new ResourceNotFoundException("Offer", offerId));
    }
}

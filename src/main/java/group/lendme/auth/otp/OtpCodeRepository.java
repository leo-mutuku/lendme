package group.lendme.auth.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {

    /** The newest code for this user and purpose, used or not (for the resend cooldown). */
    Optional<OtpCode> findFirstByUserIdAndPurposeOrderByCreatedAtDesc(UUID userId, OtpPurpose purpose);

    /** Marks every open code as used, so only the newest code ever works. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update OtpCode o set o.consumedAt = :now
            where o.userId = :userId and o.purpose = :purpose and o.consumedAt is null
            """)
    int consumeAllOpen(@Param("userId") UUID userId, @Param("purpose") OtpPurpose purpose, @Param("now") Instant now);
}

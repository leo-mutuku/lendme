package group.lendme.auth.otp;

import group.lendme.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A one-time code sent by email. Only the hash is stored, never the code itself.
 * A code is usable while it is not consumed, not expired and under the attempt limit.
 */
@Getter
@Entity
@Table(name = "otp_codes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OtpCode extends BaseEntity {

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, updatable = false, length = 30)
    private OtpPurpose purpose;

    @Column(name = "code_hash", nullable = false, updatable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    public OtpCode(UUID userId, OtpPurpose purpose, String codeHash, Instant expiresAt) {
        this.userId = Objects.requireNonNull(userId);
        this.purpose = Objects.requireNonNull(purpose);
        this.codeHash = Objects.requireNonNull(codeHash);
        this.expiresAt = Objects.requireNonNull(expiresAt);
    }

    public boolean isUsable(Instant now, int maxAttempts) {
        return consumedAt == null && expiresAt.isAfter(now) && attempts < maxAttempts;
    }

    public void recordFailedAttempt() {
        attempts++;
    }

    public void consume(Instant now) {
        consumedAt = now;
    }
}

package group.lendme.auth.token;

import group.lendme.common.exception.UnauthorizedException;
import group.lendme.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokens;
    private final SecurityProperties props;

    /** The raw token goes to the client once; we keep only its hash. */
    public record IssuedRefreshToken(String value, Instant expiresAt) {}

    @Transactional
    public IssuedRefreshToken issue(UUID userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant expiresAt = Instant.now().plus(props.jwt().refreshTokenTtl());
        refreshTokens.save(new RefreshToken(userId, hash(raw), expiresAt));
        return new IssuedRefreshToken(raw, expiresAt);
    }

    /**
     * Checks the token and revokes it (it can be used once).
     * Using an already-revoked token means it was probably stolen, so every
     * session of that user is ended.
     *
     * @return the id of the user the token belongs to
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public UUID consume(String rawToken) {
        Instant now = Instant.now();
        RefreshToken token = refreshTokens.findByTokenHash(hash(rawToken))
                .orElseThrow(RefreshTokenService::invalid);

        if (token.isRevoked()) {
            log.warn("Revoked refresh token reused for user {}; revoking all sessions", token.getUserId());
            refreshTokens.revokeAllForUser(token.getUserId(), now);
            throw invalid();
        }
        if (token.isExpired(now)) {
            throw invalid();
        }

        token.revoke(now);
        return token.getUserId();
    }

    /** Logout: revoke this one token. Unknown or already revoked tokens are ignored. */
    @Transactional
    public void revoke(String rawToken) {
        refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(t -> t.revoke(Instant.now()));
    }

    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokens.revokeAllForUser(userId, Instant.now());
    }

    private static String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static UnauthorizedException invalid() {
        return new UnauthorizedException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired. Please log in again.");
    }
}

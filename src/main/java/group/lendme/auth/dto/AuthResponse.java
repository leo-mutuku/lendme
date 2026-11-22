package group.lendme.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        UUID userId
) {
    public static AuthResponse bearer(String token, Instant expiresAt, UUID userId) {
        return new AuthResponse(token, "Bearer", expiresAt, userId);
    }
}

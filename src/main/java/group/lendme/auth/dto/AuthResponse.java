package group.lendme.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Instant expiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UUID userId
) {
    public static AuthResponse bearer(String accessToken, Instant expiresAt,
                                      String refreshToken, Instant refreshTokenExpiresAt, UUID userId) {
        return new AuthResponse(accessToken, "Bearer", expiresAt, refreshToken, refreshTokenExpiresAt, userId);
    }
}

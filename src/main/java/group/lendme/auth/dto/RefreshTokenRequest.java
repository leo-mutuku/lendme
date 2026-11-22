package group.lendme.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for /refresh and /logout. */
public record RefreshTokenRequest(@NotBlank @Size(max = 100) String refreshToken) {}

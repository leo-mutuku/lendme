package group.lendme.config;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "lendme.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @Valid @NotNull Login login,
        @Valid @NotNull Otp otp
) {
    public record Jwt(
            @NotBlank String secret,
            @NotBlank String issuer,
            @NotNull Duration accessTokenTtl,
            @NotNull Duration refreshTokenTtl
    ) {}

    public record Login(
            @Min(1) int maxFailedAttempts,
            @NotNull Duration lockDuration
    ) {}

    public record Otp(
            @NotNull Duration ttl,
            @Min(1) int maxAttempts,
            @NotNull Duration resendCooldown
    ) {}
}
package group.lendme.auth.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(description = "Email address or phone number (E.164, e.g. +254712345678)")
        @NotBlank String identifier,
        @NotBlank String password
) {}

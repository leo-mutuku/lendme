package group.lendme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "must be a 6-digit code") String otp,
        @NotBlank @Size(min = 8, max = 72, message = "must be 8 to 72 characters") String newPassword
) {}

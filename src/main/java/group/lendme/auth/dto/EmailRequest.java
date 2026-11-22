package group.lendme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Body for endpoints that only need an email: resend verification, forgot password. */
public record EmailRequest(@NotBlank @Email String email) {}

package group.lendme.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 75) String firstName,
        @NotBlank @Size(max = 75) String lastName,
        @NotBlank @Size(max = 13) String phoneNumber,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 72, message = "must be 8 to 72 characters") String password
) {}

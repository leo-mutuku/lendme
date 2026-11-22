package group.lendme.user;

import group.lendme.auth.AuthService;
import group.lendme.auth.dto.AuthResponse;
import group.lendme.auth.dto.ChangePasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Endpoints for the logged-in user's own account. Served at /api/v1/users (see WebConfig). */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "The logged-in user's account")
public class UserController {

    private final AuthService authService;

    @PutMapping("/me/password")
    @Operation(summary = "Change my password",
            description = "Needs the current password. Signs out all other devices and returns new tokens for this one.")
    public AuthResponse changePassword(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(UUID.fromString(jwt.getSubject()), request);
    }
}

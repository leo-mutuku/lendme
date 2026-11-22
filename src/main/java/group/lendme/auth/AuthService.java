package group.lendme.auth;

import group.lendme.auth.dto.*;
import group.lendme.auth.otp.OtpPurpose;
import group.lendme.auth.otp.OtpService;
import group.lendme.common.exception.BadRequestException;
import group.lendme.common.exception.BusinessRuleException;
import group.lendme.common.exception.ConflictException;
import group.lendme.common.exception.ForbiddenException;
import group.lendme.common.exception.UnauthorizedException;
import group.lendme.config.SecurityProperties;
import group.lendme.user.Email;
import group.lendme.user.PhoneNumber;
import group.lendme.user.User;
import group.lendme.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String INVALID_OTP = "The code is invalid or has expired";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final SecurityProperties props;

    /** Hash of a random password, compared against when the user doesn't exist so timing doesn't reveal it. */
    private String dummyHash;

    // ---------- register ----------

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        PhoneNumber phone = new PhoneNumber(req.phoneNumber());
        Email email = new Email(req.email());

        if (users.existsByPhoneNumber(phone)) {
            throw new ConflictException("Phone number is already registered");
        }
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }

        User user = users.save(new User(req.firstName(), req.lastName(), phone, email,
                passwordEncoder.encode(req.password())));
        otpService.issue(user, OtpPurpose.EMAIL_VERIFICATION);

        return new MessageResponse("Account created. We sent a 6-digit code to " + email + " to verify your email.");
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest req) {
        User user = findByEmail(req.email())
                .orElseThrow(() -> new BadRequestException(INVALID_OTP));
        if (user.isEmailVerified()) {
            throw new BusinessRuleException("EMAIL_ALREADY_VERIFIED", "This email is already verified. Please log in.");
        }
        if (!otpService.verify(user, OtpPurpose.EMAIL_VERIFICATION, req.otp())) {
            throw new BadRequestException(INVALID_OTP);
        }

        user.markEmailVerified();
        user.recordSuccessfulLogin(Instant.now());
        return toAuthResponse(user);
    }

    @Transactional
    public MessageResponse resendVerification(EmailRequest req) {
        findByEmail(req.email())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(user -> otpService.issue(user, OtpPurpose.EMAIL_VERIFICATION)); // cooldown: silently skip
        // same answer whether or not the email exists, so it can't be used to discover accounts
        return new MessageResponse("If that email belongs to an unverified account, a new code has been sent. "
                + "Codes can be requested once every " + otpService.resendCooldown().toSeconds() + " seconds.");
    }

    // ---------- login ----------

    /** Failed-attempt counters must be saved even though we throw, so don't roll back on auth errors. */
    @Transactional(noRollbackFor = {UnauthorizedException.class, ForbiddenException.class})
    public AuthResponse login(LoginRequest req) {
        Instant now = Instant.now();
        Optional<User> found = findByIdentifier(req.identifier());

        if (found.isEmpty()) {
            passwordEncoder.matches(req.password(), dummyHash());
            throw invalidCredentials();
        }
        User user = found.get();

        if (user.isLocked(now)) {
            throw new ForbiddenException("ACCOUNT_LOCKED",
                    "Too many failed attempts. Try again later or reset your password.");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            SecurityProperties.Login cfg = props.login();
            boolean locked = user.recordFailedLogin(cfg.maxFailedAttempts(), cfg.lockDuration(), now);
            if (locked) {
                log.warn("User {} locked after too many failed logins", user.getId());
            }
            throw invalidCredentials();
        }
        if (!user.isEnabled()) {
            throw new ForbiddenException("ACCOUNT_DISABLED", "This account has been disabled.");
        }
        if (!user.isEmailVerified()) {
            throw new ForbiddenException("EMAIL_NOT_VERIFIED",
                    "Please verify your email first. Use /api/v1/auth/resend-verification to get a new code.");
        }

        user.recordSuccessfulLogin(now);
        return toAuthResponse(user);
    }

    // ---------- forgot / reset password ----------

    @Transactional
    public MessageResponse forgotPassword(EmailRequest req) {
        findByEmail(req.email())
                .filter(User::isEnabled)
                .ifPresent(user -> {
                    if (otpService.issue(user, OtpPurpose.PASSWORD_RESET) == OtpService.IssueResult.COOLDOWN) {
                        log.info("Password reset code for user {} not re-sent: cooldown", user.getId());
                    }
                });
        // never reveal whether the email is registered
        return new MessageResponse("If that email is registered, we sent a 6-digit code to reset your password.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest req) {
        User user = findByEmail(req.email())
                .filter(User::isEnabled)
                .orElseThrow(() -> new BadRequestException(INVALID_OTP));

        if (!otpService.verify(user, OtpPurpose.PASSWORD_RESET, req.otp())) {
            throw new BadRequestException(INVALID_OTP);
        }

        user.changePassword(passwordEncoder.encode(req.newPassword()));
        user.unlock();
        user.markEmailVerified(); // receiving the code proves they own the email
        return new MessageResponse("Your password has been reset. You can now log in.");
    }

    // ---------- helpers ----------

    private Optional<User> findByEmail(String raw) {
        try {
            return users.findByEmail(new Email(raw));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /** "+..." is treated as a phone number, anything else as an email. */
    private Optional<User> findByIdentifier(String identifier) {
        String value = identifier.trim();
        try {
            return value.startsWith("+")
                    ? users.findByPhoneNumber(new PhoneNumber(value))
                    : users.findByEmail(new Email(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private AuthResponse toAuthResponse(User user) {
        TokenService.AccessToken token = tokenService.issueAccessToken(user);
        return AuthResponse.bearer(token.value(), token.expiresAt(), user.getId());
    }

    private static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("INVALID_CREDENTIALS", "Invalid email/phone or password");
    }

    private String dummyHash() {
        if (dummyHash == null) {
            dummyHash = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
        }
        return dummyHash;
    }
}
